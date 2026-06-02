package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2Clan;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.model.instances.L2NpcInstance;
import l2p.gameserver.model.items.ClanWarehousePool;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.model.items.Warehouse;
import l2p.gameserver.model.items.Warehouse.WarehouseType;
import l2p.gameserver.multilang.CustomMessage;
import l2p.gameserver.utils.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendWareHouseWithDrawList extends L2GameClientPacket
{
	//Format: cdb, b - array of (dd)
	private static Logger _log = LoggerFactory.getLogger(SendWareHouseWithDrawList.class);

	private int _count;
	private int[] _items;
	private int[] counts;

	@Override
	public void readImpl()
	{
		_count = readD();
		if(_count * (getClient().isITClient() ? 8 : 12) > _buf.remaining() || _count > Short.MAX_VALUE || _count <= 0)
		{
			_items = null;
			return;
		}
		_items = new int[_count * 2];
		counts = new int[_count];
		for(int i = 0; i < _count; i++)
		{
			_items[i * 2 + 0] = readD(); // item object id
			// TODO [V] - long
			_items[i * 2 + 1] = getClient().isITClient() ? readD() : (int) readQ(); // count
			if(_items[i * 2 + 0] < 1 || _items[i * 2 + 1] < 0)
			{
				_items = null;
				break;
			}
		}
	}

	@Override
	public void runImpl()
	{
		if(_items == null)
			return;

		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(Config.SERVICES_DISABLE_WH_WITH_DRAW_LIST && activeChar.isTradeKeyBlocked())
		{
			activeChar.sendMessage(activeChar.isLangRus() ? "Предмет нельзя положить и забрать со склада, отключите Lock." : "The item cannot be put and taken from the warehouse, turn off Lock.");
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isTradeBannedByGM())
		{
			activeChar.sendMessage("You can't use trade.");
			return;
		}

		if(!activeChar.isCommunityWh)
		{
			L2NpcInstance whkeeper = activeChar.getLastNpc();
			if(whkeeper == null || !whkeeper.isInActingRange(activeChar))
			{
				activeChar.sendPacket(Msg.WAREHOUSE_IS_TOO_FAR);
				return;
			}
		}

		boolean canWithdrawCWH = false;
		int clanId = 0;
		if(activeChar.getClan() != null)
		{
			clanId = activeChar.getClan().getClanId();
			if(((activeChar.getClanPrivileges() & L2Clan.CP_CL_VIEW_WAREHOUSE) == L2Clan.CP_CL_VIEW_WAREHOUSE) && (Config.ALT_ALLOW_OTHERS_WITHDRAW_FROM_CLAN_WAREHOUSE || activeChar.getClan().getLeaderId() == activeChar.getObjectId()))
				canWithdrawCWH = true;
		}

		if(activeChar.getUsingWarehouseType() == WarehouseType.CLAN && !canWithdrawCWH)
			return;

		int weight = 0;
		int finalCount = 0;
		int[] olditems = new int[_count];

		PcInventory inventory = activeChar.getInventory();
		inventory.writeInvLock();
		try
		{
			for(int i = 0; i < _count; i++)
			{
				int itemObjId = _items[i * 2 + 0];
				int count = _items[i * 2 + 1];
				L2ItemInstance oldinst = L2ItemInstance.restoreFromDb(itemObjId, false);

				if(count < 0)
				{
					activeChar.sendPacket(Msg.INCORRECT_ITEM_COUNT);
					return;
				}

				if(oldinst == null)
				{
					activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.SendWareHouseWithDrawList.Changed", activeChar));
					return;
				}

				if(oldinst.getOwnerId() != activeChar.getObjectId()) // с чужих складов можно брать если это фрейт или квх при наличии прав
					if(oldinst.getOwnerId() == clanId)
					{
						if(!canWithdrawCWH)
							continue;
					}
					else if(!activeChar.getAccountChars().containsKey(oldinst.getOwnerId()))
						continue;

				if(oldinst.getIntegerLimitedCount() < count)
					count = oldinst.getIntegerLimitedCount();

				counts[i] = count;
				olditems[i] = oldinst.getObjectId();
				weight += oldinst.getItem().getWeight() * count;
				finalCount++;

				if(oldinst.getItem().isStackable() && inventory.getItemByItemId(oldinst.getItemId()) != null)
					finalCount--;
			}

			if(!inventory.validateCapacity(finalCount))
			{
				activeChar.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
				return;
			}

			if(!inventory.validateWeight(weight))
			{
				activeChar.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
				return;
			}

			Warehouse warehouse = null;
			String logType = null;
			if(activeChar.getUsingWarehouseType() == WarehouseType.PRIVATE)
			{
				warehouse = activeChar.getWarehouse();
				logType = Log.WarehouseWithdraw;
			}
			else if(activeChar.getUsingWarehouseType() == WarehouseType.CLAN)
			{
				ClanWarehousePool.getInstance().AddWork(activeChar, olditems, counts);
				return;
			}
			else if(activeChar.getUsingWarehouseType() == WarehouseType.FREIGHT)
			{
				warehouse = activeChar.getFreight();
				logType = Log.FreightWithdraw;
			}
			else
			{
				// Something went wrong!
				_log.warn("Error retrieving a warehouse object for char " + activeChar.toString() + " - using warehouse type: " + activeChar.getUsingWarehouseType());
				return;
			}

			for(int i = 0; i < olditems.length; i++)
			{
				L2ItemInstance TransferItem = warehouse.takeItemByObj(olditems[i], counts[i]);
				if(TransferItem != null)
				{
					Log.LogItem(activeChar, logType, TransferItem);
					inventory.addItem(TransferItem, true, false, true, "<SendWareHouseWithDrawList>");
				}
			}
		}
		finally
		{
			inventory.writeInvUnlock();
		}

		activeChar.sendChanges();
	}
}