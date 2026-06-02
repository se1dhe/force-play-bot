package l2p.gameserver.clientpackets;

import l2p.commons.util.GArray;
import l2p.gameserver.Config;
import l2p.gameserver.TradeController;
import l2p.gameserver.TradeController.NpcTradeList;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.TradeItem;
import l2p.gameserver.model.entity.residence.Castle;
import l2p.gameserver.model.instances.*;
import l2p.gameserver.serverpackets.ExBuySellList;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.tables.ItemTable;
import l2p.gameserver.templates.L2Item;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * format:		cddb, b - array of (dd)
 */
public class RequestBuyItem extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(RequestBuyItem.class);

	private int _listId;
	private int _count;
	private int[] _items; // count*2

	@Override
	protected void readImpl()
	{
		_listId = readD();
		_count = readD();
		if(getClient().isITClient())
		{
			if(_count * 8 > _buf.remaining() || _count > Short.MAX_VALUE || _count < 1)
			{
				_items = null;
				return;
			}
			_items = new int[_count * 2];
			for(int i = 0; i < _count; i++)
			{
				_items[i * 2 + 0] = readD();
				_items[i * 2 + 1] = readD();
				if(_items[i * 2 + 1] < 0)
				{
					_items = null;
					break;
				}
			}
		}
		else
		{
			if(_count * 12 > _buf.remaining() || _count > Short.MAX_VALUE || _count < 1)
			{
				_items = null;
				return;
			}
			_items = new int[_count * 2];
			for(int i = 0; i < _count; i++)
			{
				_items[i * 2 + 0] = readD();
				// TODO [V] - long
				_items[i * 2 + 1] = (int) readQ();
				if(_items[i * 2 + 1] < 0)
				{
					_items = null;
					break;
				}
			}
		}
	}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();

		if(activeChar == null)
			return;

		if(_items == null || _count == 0 || activeChar.getBuyListId() != _listId)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInStoreMode())
		{
			activeChar.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}

		if(activeChar.isInTrade())
		{
			activeChar.sendMessage("You can't buy while trade.");
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isFishing())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
			return;
		}

		if(Config.SERVICES_DISABLE_CHECK_FOR_SHOP && activeChar.isTradeKeyBlocked())
		{
			activeChar.sendMessage(activeChar.isLangRus() ? "Вы не можете торговать, отключите Lock." : "You cannot trade, disable Lock.");
			activeChar.sendActionFailed();
			return;
		}

		boolean gmshop = activeChar.getPlayerAccess().UseGMShop;
		L2NpcInstance npc = activeChar.getLastNpc();
		if(!gmshop && (!L2NpcInstance.canBypassCheck(activeChar, npc) || !activeChar.checkLastNpc()))
		{
			activeChar.sendActionFailed();
			return;
		}

		if(!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && !gmshop && activeChar.getKarma() > 0 && !ArrayUtils.contains(Config.ALT_GAME_KARMA_NPC, npc.getNpcId()))
		{
			activeChar.sendActionFailed();
			return;
		}

		if(!gmshop)
		{
			boolean isValidMerchant = npc instanceof L2ClanHallManagerInstance || npc instanceof L2MerchantInstance || npc instanceof L2MercManagerInstance || npc instanceof L2CastleChamberlainInstance || npc instanceof L2NpcFriendInstance;
			if(!isValidMerchant)
			{
				activeChar.sendActionFailed();
				return;
			}
			activeChar.turn(npc, 3000);
		}

		L2NpcInstance merchant = null;
		if(npc != null && (npc instanceof L2MerchantInstance || npc instanceof L2ClanHallManagerInstance))
			merchant = npc;

		int slots = 0;
		long weight = 0;
		long totalPrice = 0;
		long tax = 0;
		double taxRate = 0;

		Castle castle = null;
		if(merchant != null)
		{
			castle = merchant.getCastle();
			if(castle != null)
				taxRate = castle.getTaxRate();
		}

		NpcTradeList list = TradeController.getInstance().getBuyList(_listId);
		if(list == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		GArray<TradeItem> buyList = new GArray<TradeItem>(_count);
		GArray<TradeItem> tradeList = list.getItems();
		activeChar.getInventory().writeInvLock();
		try
		{
			loop: for(int i = 0; i < _count; i++)
			{
				int itemId = _items[i * 2 + 0];
				long count = _items[i * 2 + 1];
				int price = 0;
				if(count <= 0)
				{
					activeChar.sendActionFailed();
					return;
				}

				if(count > Integer.MAX_VALUE)
				{
					sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED));
					activeChar.sendActionFailed();
					return;
				}
				L2Item temp = ItemTable.getInstance().getTemplate(itemId);
				if(temp == null)
				{
					activeChar.sendActionFailed();
					return;
				}
				if(!temp.isStackable() && count != 1)
				{
					activeChar.sendActionFailed();
					return;
				}

				for(TradeItem ti : tradeList)
				{
					if(ti.getItemId() == itemId)
					{
						if(ti.isCountLimited() && ti.getCurrentValue() < count)
							continue loop;

						price = ti.getOwnersPrice();
					}
				}

				if(price == 0 && !gmshop)
				{
					activeChar.sendMessage("You can't buy with zero price!");
					activeChar.sendActionFailed();
					return;
				}
				if(price < 0)
				{
					_log.warn("ERROR, no price found for listId: " + _listId + " itemId: " + itemId);
					activeChar.sendMessage("You can't buy with negative price!");
					activeChar.sendActionFailed();
					return;
				}

				totalPrice += count * price;

				TradeItem ti = new TradeItem();
				ti.setItemId(itemId);
				ti.setCount((int) count);
				ti.setOwnersPrice(price);

				weight += count * ti.getItem().getWeight();
				if(!ti.getItem().isStackable() || activeChar.getInventory().getItemByItemId(itemId) == null)
					slots++;

				buyList.add(ti);
				continue;
			}

			tax = (long)(totalPrice * taxRate);
			totalPrice += tax;
			if(weight < 0 || weight > Integer.MAX_VALUE)
			{
				activeChar.sendMessage("You can't buy with incorrect weight!");
				activeChar.sendActionFailed();
				return;
			}

			if(totalPrice < 0 || totalPrice > Integer.MAX_VALUE)
			{
				activeChar.sendMessage("You can't buy with incorrect total price!");
				activeChar.sendActionFailed();
				return;
			}

			if(!activeChar.getInventory().validateWeight(weight))
			{
				sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT, Msg.ActionFail);
				return;
			}

			if(!activeChar.getInventory().validateCapacity(slots))
			{
				sendPacket(Msg.YOUR_INVENTORY_IS_FULL, Msg.ActionFail);
				return;
			}

			long currentMoney = activeChar.getAdena();
			if(totalPrice > currentMoney)
			{
				sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA, Msg.ActionFail);
				return;
			}
			activeChar.reduceAdena(totalPrice, true);
			for(TradeItem ti : buyList)
				activeChar.getInventory().addItem(ti.getItemId(), ti.getCount(), "<Buy>");

			list.updateItems(buyList);

			// Add tax to castle treasury if not owned by npc clan
			if(castle != null && tax > 0 && castle.getOwnerId() > 0)
				castle.addToTreasury((int) tax, true, false);
		}
		catch (ArithmeticException ae)
		{
			activeChar.sendMessage("Failed to buying!");
			activeChar.sendActionFailed();
			return;
		}
		finally
		{
			activeChar.getInventory().writeInvUnlock();
		}

		if(!activeChar.isITClient())
			sendPacket(new ExBuySellList.SellRefundList(activeChar, true));
		activeChar.sendItemList(true);
		activeChar.sendChanges();
	}
}