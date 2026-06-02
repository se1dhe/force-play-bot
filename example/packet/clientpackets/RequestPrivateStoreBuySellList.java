package l2p.gameserver.clientpackets;

import java.util.concurrent.ConcurrentLinkedQueue;

import l2p.commons.math.SafeMath;
import l2p.gameserver.Config;
import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2TradeList;
import l2p.gameserver.model.TradeItem;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.serverpackets.SystemMessage;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Список продаваемого в приватный магазин покупки
 *
 */
public class RequestPrivateStoreBuySellList extends L2GameClientPacket
{
	private int _buyerId, _count, _itemId;
	private int[] _items; // object id
	private int[] _itemQ; // count
	private int[] _itemP; // price
	private int[] _itemE; // enchant

	@Override
	public void readImpl()
	{
		_buyerId = readD();
		_count = readD();

		L2Player _seller = getClient().getActiveChar();
		if(_seller == null)
			return;

		if(_count * 28 > _buf.remaining() || _count > Short.MAX_VALUE || _count < 1)
		{
			_count = 0;
			return;
		}

		_items = new int[_count];
		_itemQ = new int[_count];
		_itemP = new int[_count];
		_itemE = new int[_count];

		for(int i = 0; i < _count; i++)
		{
			readD(); //itemId
			_itemId = readD();
			_itemE[i] = readH();
			readH();
			_itemQ[i] = (int) readQ();
			_itemP[i] = (int) readQ();
			for(L2ItemInstance itm : _seller.getInventory().getAllItemsById(_itemId))
				if(!ArrayUtils.contains(_items, itm.getObjectId()) && _itemE[i] == itm.getEnchantLevel())
				{
					_items[i] = itm.getObjectId();
					break;
				}

			if(_itemQ[i] < 1 || _itemP[i] < 1 || ArrayUtils.indexOf(_items, _items[i]) < i)
			{
				_count = 0;
				break;
			}
		}
	}

	@Override
	public void runImpl()
	{
		L2Player seller = getClient().getActiveChar();
		if(seller == null || _count == 0)
			return;

		if(seller.isActionsDisabled())
		{
			seller.sendActionFailed();
			return;
		}

		if(!Config.ALLOW_PRIVATE_STORE)
		{
			seller.sendMessage(seller.isLangRus() ? "Приватная торговля отключена." : "Private store disabled.");
			return;
		}

		if(seller.isInStoreMode())
		{
			seller.sendPacket(new SystemMessage(SystemMessage.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM));
			return;
		}

		if(seller.isInTrade())
		{
			seller.sendActionFailed();
			return;
		}

		if(seller.isFishing())
		{
			seller.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_DO_ANYTHING_ELSE_WHILE_FISHING));
			return;
		}

		if(seller.isTradeBannedByGM())
		{
			seller.sendMessage("You can't use private store.");
			return;
		}

		if(!seller.getPlayerAccess().UseTrade)
		{
			seller.sendMessage("You can't use private store.");
			return;
		}

		if(Config.SERVICES_DISABLE_PRIVATE_STORE_SELL && seller.isTradeKeyBlocked())
		{
			seller.sendMessage(seller.isLangRus() ? "Предмет нельзя продать, отключите Lock." : "The item cannot be sold, turn off Lock.");
			seller.sendActionFailed();
			return;
		}

		L2Player buyer = (L2Player) seller.getVisibleObject(_buyerId);
		if(buyer == null || buyer.getPrivateStoreType() != L2Player.STORE_PRIVATE_BUY || !seller.isInActingRange(buyer))
		{
			seller.sendPacket(new SystemMessage(SystemMessage.THE_ATTEMPT_TO_SELL_HAS_FAILED));
			seller.sendActionFailed();
			return;
		}

		ConcurrentLinkedQueue<TradeItem> buyList = buyer.getBuyList();
		if(buyList.isEmpty())
		{
			seller.sendPacket(new SystemMessage(SystemMessage.THE_ATTEMPT_TO_SELL_HAS_FAILED));
			seller.sendActionFailed();
			return;
		}

		ConcurrentLinkedQueue<TradeItem> sellList = new ConcurrentLinkedQueue<TradeItem>();

		int totalCost = 0;
		int slots = 0;
		int weight = 0;

		buyer.getInventory().writeInvLock();
		seller.getInventory().writeInvLock();
		try
		{
			loop: for(int i = 0; i < _count; i++)
			{
				int objectId = _items[i];
				int count = _itemQ[i];
				int price = _itemP[i];
				int enchant = _itemE[i];

				L2ItemInstance item = seller.getInventory().getItemByObjectId(objectId);
				if(item == null || item.getCount() < count || !item.canBeTraded(seller))
					break loop;

				TradeItem si = null;

				for(TradeItem bi : buyList)
					if(bi.getItemId() == item.getItemId() && bi.getEnchantLevel() == item.getEnchantLevel())
						if(bi.getOwnersPrice() == price)
						{
							if(count > bi.getCount())
								break loop;

							totalCost = SafeMath.addAndCheck(totalCost, SafeMath.mulAndCheck(count, price));
							weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(count, item.getItem().getWeight()));
							if(!item.isStackable() || buyer.getInventory().getItemByItemId(item.getItemId()) == null)
								slots++;

							si = new TradeItem();
							si.setObjectId(objectId);
							si.setItemId(item.getItemId());
							si.setCount(count);
							si.setOwnersPrice(price);
							si.setEnchantLevel(item.getEnchantLevel());

							sellList.add(si);
							break;
						}
			}
		}
		catch(ArithmeticException ae)
		{
			//TODO audit
			sellList.clear();
			sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED));
			return;
		}
		finally
		{
			try
			{
				if(sellList.size() != _count)
				{
					seller.sendPacket(new SystemMessage(SystemMessage.THE_ATTEMPT_TO_SELL_HAS_FAILED));
					seller.sendActionFailed();
					return;
				}

				if(!buyer.getInventory().validateWeight(weight))
				{
					buyer.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT));
					seller.sendPacket(new SystemMessage(SystemMessage.THE_ATTEMPT_TO_SELL_HAS_FAILED));
					seller.sendActionFailed();
					return;
				}

				if(!buyer.getInventory().validateCapacity(slots))
				{
					buyer.sendPacket(new SystemMessage(SystemMessage.YOUR_INVENTORY_IS_FULL));
					seller.sendPacket(new SystemMessage(SystemMessage.THE_ATTEMPT_TO_SELL_HAS_FAILED));
					seller.sendActionFailed();
					return;
				}

				if(buyer.getAdena() < totalCost)
				{
					buyer.sendPacket(new SystemMessage(SystemMessage.YOU_DO_NOT_HAVE_ENOUGH_ADENA));
					seller.sendPacket(new SystemMessage(SystemMessage.THE_ATTEMPT_TO_SELL_HAS_FAILED));
					seller.sendActionFailed();
					return;
				}

				buyer.getTradeList().buySellItems(buyer, buyList, seller, sellList);
				buyer.saveTradeList();
			}
			finally
			{
				seller.getInventory().writeInvUnlock();
				buyer.getInventory().writeInvUnlock();
			}
		}

		if(buyer.getBuyList().isEmpty())
			L2TradeList.cancelStore(buyer);

		seller.sendChanges();
		buyer.sendChanges();

		seller.sendActionFailed();
	}
}