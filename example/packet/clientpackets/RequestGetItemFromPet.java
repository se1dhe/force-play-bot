package l2p.gameserver.clientpackets;

import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2PetInstance;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.model.items.PetInventory;
import l2p.gameserver.serverpackets.PetItemList;
import l2p.gameserver.utils.Log;

public class RequestGetItemFromPet extends L2GameClientPacket
{
	private int _objectId;
	private int _amount;
	@SuppressWarnings("unused")
	private int _unknown;

	@Override
	public void readImpl()
	{
		_objectId = readD();
		// TODO [V] - long
		if(getClient().isITClient())
			_amount = readD();
		else
			_amount = (int) readQ();
		_unknown = readD(); // = 0 for most trades
	}

	@Override
	public void runImpl()
	{
		if(_amount < 1)
			return;
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		L2PetInstance pet = (L2PetInstance) activeChar.getPet();
		if(pet == null)
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
			L2ItemInstance petItem = petInventory.getItemByObjectId(_objectId);

			if(petItem == null)
				return;

			if(petItem.isEquipped())
			{
				activeChar.sendActionFailed();
				return;
			}

			if(activeChar.isInFightClub())
			{
				activeChar.sendActionFailed();
				return;
			}

			long finalLoad = petItem.getItem().getWeight() * _amount;
			int slots = 0;
			if(!petItem.getItem().isStackable() || playerInventory.getItemByItemId(petItem.getItemId()) == null)
				slots = 1;

			if(!playerInventory.validateWeight(finalLoad))
			{
				sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
				return;
			}
			if(!playerInventory.validateCapacity(slots) || (playerInventory.getItemByItemId(petItem.getItemId()) != null && playerInventory.getItemByItemId(petItem.getItemId()).getCount() + _amount > Integer.MAX_VALUE) || _amount > Integer.MAX_VALUE)
			{
				sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
				return;
			}

			L2ItemInstance item = petInventory.dropItem(_objectId, _amount, "<DropItemRequestGetItemFromPet>");
			Log.LogItem(activeChar, Log.FromPet, item);
			item.setCustomFlags(item.getCustomFlags() & ~L2ItemInstance.FLAG_PET_EQUIPPED, true);
			playerInventory.addItem(item, true, false, true, "<RequestGetItemFromPet");
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