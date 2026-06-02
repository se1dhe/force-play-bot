package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.serverpackets.ExPutItemResultForVariationCancel;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.templates.L2Item;

public class RequestConfirmCancelItem extends L2GameClientPacket
{
	// format: (ch)d
	private int _itemId;

	@Override
	public void readImpl()
	{
		_itemId = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_itemId);

		if(item == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(!item.isAugmented())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.AUGMENTATION_REMOVAL_CAN_ONLY_BE_DONE_ON_AN_AUGMENTED_ITEM));
			return;
		}

		if(activeChar.isInFightClub())
		{
			activeChar.sendActionFailed();
			return;
		}

		int price = 0;
		switch(item.getItem().getItemGrade())
		{
			case L2Item.CRYSTAL_C:
				if(item.getItem().getCrystalCount() < 1720)
					price = 95000;
				else if(item.getItem().getCrystalCount() < 2452)
					price = 150000;
				else
					price = 210000;
				break;
			case L2Item.CRYSTAL_B:
				if(item.getItem().getCrystalCount() < 1746)
					price = 240000;
				else
					price = 270000;
				break;
			case L2Item.CRYSTAL_A:
				if(item.getItem().getCrystalCount() < 2160)
					price = 330000;
				else if(item.getItem().getCrystalCount() < 2824)
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

		activeChar.sendPacket(new ExPutItemResultForVariationCancel(item, price));
	}
}