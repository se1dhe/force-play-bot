package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.serverpackets.ExPutItemResultForVariationMake;
import l2p.gameserver.serverpackets.InventoryUpdate;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.templates.L2Item;

public class RequestConfirmTargetItem extends L2GameClientPacket
{
	// format: (ch)d
	private int _itemObjId;

	@Override
	public void readImpl()
	{
		_itemObjId = readD(); // object_id шмотки
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_itemObjId);

		if(item == null)
			return;

		if(activeChar.getLevel() < 46)
		{
			activeChar.sendMessage("You have to be level 46 in order to augment an item");
			return;
		}

		if(activeChar.isInFightClub())
		{
			activeChar.sendActionFailed();
			return;
		}

		// check if the item is augmentable
		int itemGrade = item.getItem().getItemGrade();
		int itemType = item.getItem().getType2();

		if(item.isAugmented())
		{
			if(!activeChar.isITClient())
			{
				if(!isValid(activeChar))
				{
					return;
				}

				item.getAugmentation().removeBoni(activeChar);

				// remove the augmentation
				item.removeAugmentation(true);

				// send inventory update
				InventoryUpdate iu = new InventoryUpdate();
				iu.addModifiedItem(item, activeChar.isEnchantLimit());
				activeChar.sendPacket(iu);

				if(item.isEquipped())
					activeChar.broadcastUserInfo(true);

				activeChar.sendPacket(new ExPutItemResultForVariationMake(item.getObjectId()));
			}
			else
				activeChar.sendPacket(new SystemMessage(SystemMessage.ONCE_AN_ITEM_IS_AUGMENTED_IT_CANNOT_BE_AUGMENTED_AGAIN));
			return;
		}
		else if(itemGrade < L2Item.CRYSTAL_C || itemType != L2Item.TYPE2_WEAPON || item.isHeroItem() || item.isCursed() || item.isShadowItem() || item.isTemporalItem())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.THIS_IS_NOT_A_SUITABLE_ITEM));
			return;
		}

		if(!isValid(activeChar))
		{
			return;
		}

		activeChar.sendPacket(new ExPutItemResultForVariationMake(_itemObjId));
		activeChar.sendPacket(new SystemMessage(SystemMessage.SELECT_THE_CATALYST_FOR_AUGMENTATION));
	}

	private boolean isValid(L2Player activeChar)
	{
		// check if the player can augment
		if(activeChar.getPrivateStoreType() != L2Player.STORE_PRIVATE_NONE)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_AUGMENT_ITEMS_WHILE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP_IS_IN_OPERATION));
			return false;
		}
		if(activeChar.isDead())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_AUGMENT_ITEMS_WHILE_DEAD));
			return false;
		}
		if(activeChar.isParalyzed())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_AUGMENT_ITEMS_WHILE_PARALYZED));
			return false;
		}
		if(activeChar.isFishing())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_AUGMENT_ITEMS_WHILE_FISHING));
			return false;
		}
		if(activeChar.isSitting())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_AUGMENT_ITEMS_WHILE_SITTING_DOWN));
			return false;
		}
		return true;
	}
}