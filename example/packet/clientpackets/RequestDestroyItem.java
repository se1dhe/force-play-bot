package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Skill;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.tables.ItemTable;
import l2p.gameserver.utils.Log;

public class RequestDestroyItem extends L2GameClientPacket
{
	private int _objectId;
	private int _count;

	/**
	 * packet type id 0x1f
	 *
	 * sample
	 *
	 * 59
	 * 0b 00 00 40		// object id
	 * 01 00 00 00		// count
	 */
	@Override
	public void readImpl()
	{
		_objectId = readD();
		// TODO [V] - long
		if(getClient().isITClient())
			_count = readD();
		else
			_count = (int) readQ();
	}

	@Override
	public void runImpl()
	{
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

		int count = _count;

		L2ItemInstance itemToRemove = activeChar.getInventory().getItemByObjectId(_objectId);

		if(itemToRemove == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(count < 1)
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_DESTROY_IT_BECAUSE_THE_NUMBER_IS_INCORRECT);
			return;
		}

		if(Config.ENABLE_DESTROY_HERO_WEAPONS)
		{
			if(itemToRemove.getItemId() == 6842)
			{
				activeChar.sendPacket(Msg.HERO_WEAPONS_CANNOT_BE_DESTROYED);
				return;
			}
		}
		else
		{
			if(itemToRemove.isHeroWeapon())
			{
				activeChar.sendPacket(Msg.HERO_WEAPONS_CANNOT_BE_DESTROYED);
				return;
			}
		}

		if(!itemToRemove.canBeDestroyed(activeChar))
		{
			activeChar.sendPacket(Msg.THIS_ITEM_CANNOT_BE_DISCARDED);
			return;
		}

		if(Config.SERVICES_DISABLE_DESTROY_ITEM && activeChar.isTradeKeyBlocked())
		{
			activeChar.sendMessage(activeChar.isLangRus() ? "Предмет нельзя удалить, отключите Lock." : "Item cannot be removed, turn off Lock.");
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.getPrivateStoreType() != L2Player.STORE_PRIVATE_NONE)
		{
			activeChar.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}

		if(activeChar.isFishing())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
			return;
		}

		if(activeChar.getPet() != null && activeChar.getPet().getControlItemId() == itemToRemove.getObjectId())
		{
			activeChar.sendPacket(Msg.THE_PET_HAS_BEEN_SUMMONED_AND_CANNOT_BE_DELETED);
			return;
		}

		if(activeChar.getAgathion() != null && activeChar.getAgathion().getControlItemId() == itemToRemove.getObjectId())
		{
			activeChar.sendMessage(activeChar.isLangRus() ? "Агатион был вызван и не может быть удален" : "The agathion has been summoned and cannot be deleted.");
			return;
		}

		if(activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(count > itemToRemove.getIntegerLimitedCount())
			count = itemToRemove.getIntegerLimitedCount();

		itemToRemove.setWhFlag(false);

		boolean broadcast = itemToRemove.isEquipped() && !itemToRemove.isArrow();

		if(itemToRemove.canBeCrystallized(activeChar, false))
		{
			int level = activeChar.getSkillLevel(L2Skill.SKILL_CRYSTALLIZE);
			if(!(level < 1 || itemToRemove.getItem().getCrystalType() > level))
			{
				activeChar.getInventory().destroyItem(itemToRemove, 1, true, "<DestroyItemCrystallizeDestroy>");

				// add crystals
				int crystalAmount = itemToRemove.getItem().getCrystalCount();
				short crystalId = RequestCrystallizeItem._crystalId[itemToRemove.getItem().getCrystalType()];

				L2ItemInstance createditem = ItemTable.getInstance().createItem(crystalId);
				createditem.setCount(crystalAmount);
				L2ItemInstance addedItem = activeChar.getInventory().addItem(createditem, "<Crystal>");
				if(broadcast)
					activeChar.sendDisarmMessage(itemToRemove);
				activeChar.sendPacket(Msg.THE_ITEM_HAS_BEEN_SUCCESSFULLY_CRYSTALLIZED);
				activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_OBTAINED_S2_S1).addItemName(crystalId).addNumber(crystalAmount));

				Log.LogItem(activeChar, Log.Crystalize, itemToRemove);

				activeChar.updateStats();
				return;
			}
		}

		L2ItemInstance removedItem = activeChar.getInventory().destroyItem(_objectId, count, true, "<DestroyItemRequest>");

		if(!broadcast)
			activeChar.sendPacket(SystemMessage.removeItems(removedItem.getItemId(), count, removedItem.getEnchantLevel()));

		if(broadcast)
			activeChar.sendDisarmMessage(removedItem);
		activeChar.updateStats();
	}
}