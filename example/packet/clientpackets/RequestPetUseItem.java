package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.model.instances.L2PetInstance;
import l2p.gameserver.serverpackets.SystemMessage;

public class RequestPetUseItem extends L2GameClientPacket
{
	private int _objectId;

	@Override
	public void readImpl()
	{
		_objectId = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;

		if(player.isActionsDisabled())
		{
			player.sendActionFailed();
			return;
		}

		if(player.isFishing())
		{
			player.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_DO_THAT_WHILE_FISHING));
			player.sendActionFailed();
			return;
		}

		player.setActive();

		if(player.getPet() == null || !player.getPet().isPet())
		{
			player.sendActionFailed();
			return;
		}
		L2PetInstance pet = (L2PetInstance)player.getPet();

		L2ItemInstance item = pet.getInventory().getItemByObjectId(_objectId);

		if(item == null || item.getIntegerLimitedCount() < 1)
		{
			player.sendActionFailed();
			return;
		}

		if(player.isAlikeDead() || pet.isDead() || pet.isOutOfControl())
		{
			player.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(item.getItemId()));
			player.sendActionFailed();
			return;
		}

		if(pet.tryEquipItem(item, true))
			return;

		if(feed(player, pet, item, false))
			return;

		player.sendPacket(new SystemMessage(SystemMessage.ITEM_NOT_AVAILABLE_FOR_PETS));
		player.sendActionFailed();
	}

	public static boolean feed(L2Player player, L2PetInstance pet, L2ItemInstance item, boolean my)
	{
		int itemId = item.getItemId();
		if(pet.getTemplate().food.contains(itemId))
		{
			if(pet.getCurrentFed() >= pet.getMaxMeal())
			{
				player.sendActionFailed();
				return true;
			}
			if(my)
				player.getInventory().destroyItem(item, 1L, false, "<DestroyItemPetUseItemFeed>");
			else
				pet.removeItemFromInventory(item, 1, true);

			pet.setCurrentFed(pet.getCurrentFed() + (itemId == 4038 ? 150 : (itemId == 5168 || itemId == 5169 || itemId == 6316 ? 200 : 100)));
			pet.broadcastStatusUpdate();
			return true;
		}
		return false;
	}
}
