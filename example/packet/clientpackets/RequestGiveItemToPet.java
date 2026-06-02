package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.model.instances.L2PetInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.model.items.PetInventory;
import l2p.gameserver.serverpackets.PetItemList;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.tables.PetDataTable;
import l2p.gameserver.utils.Log;

public class RequestGiveItemToPet extends L2GameClientPacket
{
	private int _objectId;
	private int _amount;

	@Override
	public void readImpl()
	{
		_objectId = readD();
		// TODO [V] - long
		if(getClient().isITClient())
			_amount = readD();
		else
			_amount = (int) readQ();
	}

	@Override
	public void runImpl()
	{
		if(_amount < 1)
			return;
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInFightClub())
		{
			activeChar.sendActionFailed();
			return;
		}

		L2PetInstance pet = (L2PetInstance) activeChar.getPet();
		if(pet == null || pet.isDead())
		{
			sendPacket(new SystemMessage(SystemMessage.CANNOT_GIVE_ITEMS_TO_A_DEAD_PET));
			return;
		}

		if(activeChar.getPrivateStoreType() != L2Player.STORE_PRIVATE_NONE)
		{
			sendPacket(new SystemMessage(SystemMessage.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM));
			return;
		}

		if(activeChar.isInTrade())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isFishing())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_DO_THAT_WHILE_FISHING));
			return;
		}

		if(_objectId == pet.getControlItemId())
		{
			activeChar.sendActionFailed();
			return;
		}

		PetInventory petInventory = pet.getInventory();
		PcInventory playerInventory = activeChar.getInventory();

		petInventory.writeInvLock();
		playerInventory.writeInvLock();
		try
		{
			L2ItemInstance playerItem = playerInventory.getItemByObjectId(_objectId);
			if(playerItem == null || playerItem.getObjectId() == pet.getControlItemId())
			{
				activeChar.sendActionFailed();
				return;
			}

			if(!playerItem.canBeDropped(activeChar) || PetDataTable.isPetControlItem(playerItem))
			{
				activeChar.sendActionFailed();
				return;
			}

			int slots = 0;
			long weight = playerItem.getItem().getWeight() * _amount;
			if(!playerItem.getItem().isStackable() || petInventory.getItemByItemId(playerItem.getItemId()) == null)
				slots = 1;

			if(!petInventory.validateWeight(weight))
			{
				activeChar.sendPacket(new SystemMessage(SystemMessage.EXCEEDED_PET_INVENTORYS_WEIGHT_LIMIT));
				return;
			}

			if(!petInventory.validateCapacity(slots) || (petInventory.getItemByItemId(playerItem.getItemId()) != null && petInventory.getItemByItemId(playerItem.getItemId()).getCount() + _amount > Integer.MAX_VALUE) || _amount > Integer.MAX_VALUE)
			{
				activeChar.sendPacket(new SystemMessage(SystemMessage.DUE_TO_THE_VOLUME_LIMIT_OF_THE_PETS_INVENTORY_NO_MORE_ITEMS_CAN_BE_PLACED_THERE));
				return;
			}

			if(_amount >= playerItem.getIntegerLimitedCount())
			{
				playerInventory.dropItem(_objectId, playerItem.getIntegerLimitedCount(), "<DropItemRequestGiveItemToPet1>");
				Log.LogItem(activeChar, Log.ToPet, playerItem);
				playerItem.setCustomFlags(playerItem.getCustomFlags() | L2ItemInstance.FLAG_PET_EQUIPPED, true);
				petInventory.addItem(playerItem, true, false, true, "<RequestGiveItemToPet1>");
			}
			else
			{
				L2ItemInstance item = playerInventory.dropItem(_objectId, _amount, "<DropItemRequestGiveItemToPet2>");
				Log.LogItem(activeChar, Log.ToPet, item);
				petInventory.addItem(item, true, false, true, "<RequestGiveItemToPet2>");
			}
		}
		finally
		{
			petInventory.writeInvUnlock();
			playerInventory.writeInvUnlock();
		}

		pet.sendChanges();
		activeChar.sendPacket(new PetItemList(pet));
		activeChar.sendChanges();
	}
}