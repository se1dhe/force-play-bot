package l2p.gameserver.clientpackets;

import java.util.HashMap;

import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.model.instances.L2ItemInstance.ItemClass;
import l2p.gameserver.model.instances.L2NpcInstance;
import l2p.gameserver.model.items.PcFreight;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.model.items.Warehouse;
import l2p.gameserver.utils.Log;

public class RequestPackageSend extends L2GameClientPacket
{
	private int _objectID;
	private HashMap<Integer, Integer> _items;

	private static int _FREIGHT_FEE = 1000;

	@Override
	public void readImpl()
	{
		_objectID = readD();
		int itemsCount = readD();
		if(itemsCount * (getClient().isITClient() ? 8 : 12) > _buf.remaining() || itemsCount > Short.MAX_VALUE || itemsCount <= 0)
		{
			_items = null;
			return;
		}
		_items = new HashMap<Integer, Integer>(itemsCount + 1, 0.999f);
		for(int i = 0; i < itemsCount; i++)
		{
			int obj_id = readD(); // this is some id sent in PackageSendableList
			// TODO [V] - long
			int itemQuantity = getClient().isITClient() ? readD() : (int) readQ();
			if(itemQuantity < 0)
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
		if(activeChar == null || _items == null || !activeChar.getPlayerAccess().UseWarehouse)
			return;

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
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isTradeBannedByGM())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(Config.SERVICES_DISABLE_PACKAGE_SEND && activeChar.isTradeKeyBlocked())
		{
			activeChar.sendMessage(activeChar.isLangRus() ? "Предмет нельзя передать, отключите Lock." : "The item cannot be transferred, turn off Lock.");
			activeChar.sendActionFailed();
			return;
		}

		long adenaDeposit = 0;
		int adenaObjId;
		PcInventory inventory = activeChar.getInventory();
		inventory.writeInvLock();
		try
		{
			L2ItemInstance adena = inventory.getItemByItemId(57);
			if(adena != null)
				adenaObjId = adena.getObjectId();
			else
				adenaObjId = -1;
			for(Integer itemObjectId : _items.keySet())
			{
				L2ItemInstance item = inventory.getItemByObjectId(itemObjectId);
				if(item == null || item.isEquipped())
					return;

				if(_items.get(itemObjectId) < 0)
					return;

				if(itemObjectId == adenaObjId)
					adenaDeposit = _items.get(itemObjectId);
			}

			L2NpcInstance freighter = activeChar.getLastNpc();
			if(freighter == null || !freighter.isInActingRange(activeChar))
			{
				activeChar.sendPacket(Msg.YOU_FAILED_AT_SENDING_THE_PACKAGE_BECAUSE_YOU_ARE_TOO_FAR_FROM_THE_WAREHOUSE);
				return;
			}

			int fee = _items.size() * _FREIGHT_FEE;

			if(fee + adenaDeposit > activeChar.getAdena())
			{
				activeChar.sendPacket(Msg.YOU_LACK_THE_FUNDS_NEEDED_TO_PAY_FOR_THIS_TRANSACTION);
				return;
			}

			Warehouse warehouse = new PcFreight(_objectID);

			// Item Max Limit Check
			if(_items.size() + warehouse.listItems(ItemClass.ALL).length > activeChar.getFreightLimit())
			{
				activeChar.sendPacket(Msg.THE_CAPACITY_OF_THE_WAREHOUSE_HAS_BEEN_EXCEEDED);
				return;
			}

			// Transfer the items from activeChar's Inventory Instance to destChar's Freight Instance
			for(Integer itemObjectId : _items.keySet())
			{
				L2ItemInstance found = inventory.getItemByObjectId(itemObjectId);
				if(found == null || !found.canBeFreighted(activeChar))
				{
					fee -= _FREIGHT_FEE;
				}
				else
				{
					L2ItemInstance item = inventory.dropItem(found, _items.get(itemObjectId), false, "<DropItemRequestPackageSend>");
					Log.LogItem(activeChar, Log.FreightDeposit, item);
					warehouse.addItem(item);
				}
			}
			if(fee <= 0)
				return;
			activeChar.reduceAdena(fee, true);
		}
		finally
		{
			inventory.writeInvUnlock();
		}
		activeChar.updateStats();

		// Delete destination L2Player used for freight
		activeChar.sendPacket(Msg.THE_TRANSACTION_IS_COMPLETE);
	}
}