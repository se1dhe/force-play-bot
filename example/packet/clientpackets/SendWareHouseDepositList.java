package l2p.gameserver.clientpackets;

import l2p.commons.util.GArray;
import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.model.instances.L2ItemInstance.ItemClass;
import l2p.gameserver.model.instances.L2NpcInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.model.items.Warehouse;
import l2p.gameserver.model.items.Warehouse.WarehouseType;
import l2p.gameserver.utils.Log;

import java.util.HashMap;

/**
 * Format: cdb, b - array of (dd)
 */
public class SendWareHouseDepositList extends L2GameClientPacket
{
	private static final int _WAREHOUSE_FEE = 30;
	private HashMap<Integer, Integer> _items;

	@Override
	public void readImpl()
	{
		int itemsCount = readD();
		if(itemsCount * (getClient().isITClient() ? 8 : 12) > _buf.remaining() || itemsCount > Short.MAX_VALUE || itemsCount < 0)
		{
			_items = null;
			return;
		}
		_items = new HashMap<Integer, Integer>(itemsCount + 1, 0.999f);
		for(int i = 0; i < itemsCount; i++)
		{
			int obj_id = readD();
			// TODO [V] - long
			int itemQuantity = getClient().isITClient() ? readD() : (int) readQ();
			if(obj_id < 1 || itemQuantity < 1)
			{
				_items = null;
				return;
			}
			_items.put(obj_id, itemQuantity);
		}
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null || _items == null)
			return;

		// Проверяем наличие npc и расстояние до него
		if(!activeChar.isCommunityWh)
		{
			L2NpcInstance whkeeper = activeChar.getLastNpc();
			if(whkeeper == null || !whkeeper.isInActingRange(activeChar))
			{
				activeChar.sendPacket(Msg.WAREHOUSE_IS_TOO_FAR);
				return;
			}
		}

		if(activeChar.isInStoreMode())
		{
			activeChar.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}

		if(activeChar.isTradeBannedByGM())
		{
			activeChar.sendMessage("You can't use trade.");
			return;
		}

		if(Config.SERVICES_DISABLE_WH_DEPOSIT_LIST && activeChar.isTradeKeyBlocked())
		{
			activeChar.sendMessage(activeChar.isLangRus() ? "Предмет нельзя положить и забрать со склада, отключите Lock." : "The item cannot be put and taken from the warehouse, turn off Lock.");
			activeChar.sendActionFailed();
			return;
		}

		Warehouse warehouse;
		PcInventory inventory = activeChar.getInventory();
		boolean privatewh = activeChar.getUsingWarehouseType() != WarehouseType.CLAN;
		int slotsleft = 0;
		long adenaDeposit = 0;

		inventory.writeInvLock();
		try
		{
			// Список предметов, уже находящихся на складе
			L2ItemInstance[] itemsOnWarehouse;
			if(privatewh)
			{
				warehouse = activeChar.getWarehouse();
				itemsOnWarehouse = warehouse.listItems(ItemClass.ALL);
				slotsleft = activeChar.getWarehouseLimit() - itemsOnWarehouse.length;
			}
			else
			{
				if(activeChar.getClan() == null)
					return;
				warehouse = activeChar.getClan().getWarehouse();
				itemsOnWarehouse = warehouse.listItems(ItemClass.ALL);
				slotsleft = Config.WAREHOUSE_SLOTS_CLAN - itemsOnWarehouse.length;
			}

			// Список стекуемых предметов, уже находящихся на складе
			HashMap<Integer, Integer> stackableList = new HashMap<Integer, Integer>();
			for(L2ItemInstance i : itemsOnWarehouse)
				if(i.isStackable())
					stackableList.put(i.getItemId(), i.getIntegerLimitedCount());

			// Создаем новый список передаваемых предметов, на основе полученных данных
			GArray<L2ItemInstance> itemsToStoreList = new GArray<L2ItemInstance>(_items.size() + 1);
			for(Integer itemObjectId : _items.keySet())
			{
				L2ItemInstance item = inventory.getItemByObjectId(itemObjectId);
				if(item == null || !item.canBeStored(activeChar, privatewh)) // а его вообще положить можно?
					continue;
				if(!item.isStackable() || !stackableList.containsKey(item.getItemId())) // вещь требует слота
				{
					if(slotsleft <= 0) // если слоты кончились нестекуемые вещи и отсутствующие стекуемые пропускаем
						continue;
					slotsleft--; // если слот есть то его уже нет
				}
				if(item.isStackable())
				{
					long cn = _items.get(itemObjectId);
					if(cn > Integer.MAX_VALUE || (stackableList.containsKey(item.getItemId()) && (cn + stackableList.get(item.getItemId()) > Integer.MAX_VALUE)))
						continue;
					if(item.getItemId() == 57)
						adenaDeposit = cn;
				}
				itemsToStoreList.add(item);
			}

			// Проверяем, хватит ли у нас денег на уплату налога
			long fee = itemsToStoreList.size() * _WAREHOUSE_FEE;
			if(fee + adenaDeposit > activeChar.getAdena())
			{
				activeChar.sendPacket(Msg.YOU_LACK_THE_FUNDS_NEEDED_TO_PAY_FOR_THIS_TRANSACTION);
				return;
			}

			// Сообщаем о том, что слоты кончились
			if(slotsleft <= 0)
				activeChar.sendPacket(Msg.YOUR_WAREHOUSE_IS_FULL);

			// Перекидываем
			for(L2ItemInstance itemToStore : itemsToStoreList)
			{
				L2ItemInstance item = inventory.dropItem(itemToStore, _items.get(itemToStore.getObjectId()), true, "<DropItemSendWareHouseDepositList>");
				if(item == null)
					continue;
				if(privatewh)
					Log.LogItem(activeChar, Log.WarehouseDeposit, item);
				else
					Log.LogClanItem(activeChar, Log.ClanWarehouseDeposit, item);
				warehouse.addItem(item);
			}

			activeChar.reduceAdena(fee, true);
		}
		finally
		{
			inventory.writeInvUnlock();
		}

		// Обновляем параметры персонажа
		activeChar.updateStats();
	}
}