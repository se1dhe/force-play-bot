package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.serverpackets.ExVariationCancelResult;
import l2p.gameserver.serverpackets.InventoryUpdate;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.templates.L2Item;
import l2p.gameserver.utils.Log;

public final class RequestRefineCancel extends L2GameClientPacket
{
	//format: (ch)d
	private int _targetItemObjId;

	@Override
	protected void readImpl()
	{
		_targetItemObjId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		L2ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(_targetItemObjId);

		// cannot remove augmentation from a not augmented item
		if(targetItem == null || !targetItem.isAugmented())
		{
			activeChar.sendPacket(new ExVariationCancelResult(0), Msg.AUGMENTATION_REMOVAL_CAN_ONLY_BE_DONE_ON_AN_AUGMENTED_ITEM);
			return;
		}

		if(activeChar.isCastingNow())
		{
			activeChar.sendPacket(new ExVariationCancelResult(0));
			activeChar.sendMessage("You can't do it while casting.");
			return;
		}

		if(activeChar.isInFightClub())
		{
			activeChar.sendActionFailed();
			return;
		}

		// get the price
		int price = 0;
		switch(targetItem.getItem().getItemGrade())
		{
			case L2Item.CRYSTAL_C:
				if(targetItem.getItem().getCrystalCount() < 1720)
					price = 95000;
				else if(targetItem.getItem().getCrystalCount() < 2452)
					price = 150000;
				else
					price = 210000;
				break;
			case L2Item.CRYSTAL_B:
				if(targetItem.getItem().getCrystalCount() < 1746)
					price = 240000;
				else
					price = 270000;
				break;
			case L2Item.CRYSTAL_A:
				if(targetItem.getItem().getCrystalCount() < 2160)
					price = 330000;
				else if(targetItem.getItem().getCrystalCount() < 2824)
					price = 390000;
				else
					price = 420000;
				break;
			case L2Item.CRYSTAL_S:
				price = 480000;
				break;
			default:
				return;
		}

		price *= Config.AUGMENT_CANCEL_PRICE_MOD;

		if(activeChar.getAdena() < price)
		{
			activeChar.sendPacket(new ExVariationCancelResult(0), Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}
		activeChar.reduceAdena(price, true);

		activeChar.abortCast(true, false);

		Log.LogItem(activeChar, Log.RefineCancel, targetItem);
		Log.LogEnchant(activeChar, Log.ItemLog.CancelRefine, targetItem, 0);

		targetItem.getAugmentation().removeBoni(activeChar);

		// remove the augmentation
		targetItem.removeAugmentation(true);

		// send inventory update
		InventoryUpdate iu = new InventoryUpdate();
		iu.addModifiedItem(targetItem, activeChar.isEnchantLimit());

		// send system message
		SystemMessage sm = new SystemMessage(SystemMessage.AUGMENTATION_HAS_BEEN_SUCCESSFULLY_REMOVED_FROM_YOUR_S1);
		sm.addItemName(targetItem.getItemId());
		activeChar.sendPacket(new ExVariationCancelResult(1), iu, sm);

		if(targetItem.isEquipped())
			activeChar.broadcastUserInfo(false);
	}
}