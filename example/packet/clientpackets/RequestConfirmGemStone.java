package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.serverpackets.ExPutCommissionResultForVariationMake;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.templates.L2Item;

public class RequestConfirmGemStone extends L2GameClientPacket
{
	// format: (ch)dddd
	private int _targetItemObjId;
	private int _refinerItemObjId;
	private int _gemstoneItemObjId;
	private int _gemstoneCount;

	@Override
	public void readImpl()
	{
		_targetItemObjId = readD();
		_refinerItemObjId = readD();
		_gemstoneItemObjId = readD();
		// TODO [V] - long
		if(getClient().isITClient())
			_gemstoneCount = readD();
		else
			_gemstoneCount = (int) readQ();
	}

	@Override
	public void runImpl()
	{
		if(_gemstoneCount <= 0)
			return;

		L2Player activeChar = getClient().getActiveChar();
		L2ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(_targetItemObjId);
		L2ItemInstance refinerItem = activeChar.getInventory().getItemByObjectId(_refinerItemObjId);
		L2ItemInstance gemstoneItem = activeChar.getInventory().getItemByObjectId(_gemstoneItemObjId);

		if(targetItem == null || refinerItem == null || gemstoneItem == null)
			return;

		int itemGrade = targetItem.getItem().getItemGrade();
		if(itemGrade < 2)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.THIS_IS_NOT_A_SUITABLE_ITEM));
			return;
		}

		if(activeChar.isInFightClub())
		{
			activeChar.sendActionFailed();
			return;
		}

		int gemstoneItemId = gemstoneItem.getItem().getItemId();
		switch(itemGrade)
		{
			case L2Item.CRYSTAL_C:
				if(gemstoneItemId != 2130)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.THIS_IS_NOT_A_SUITABLE_ITEM));
					return;
				}
				if(_gemstoneCount < 20)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.GEMSTONE_QUANTITY_IS_INCORRECT));
					return;
				}
				_gemstoneCount = 20;
				break;
			case L2Item.CRYSTAL_B:
				if(gemstoneItemId != 2130)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.THIS_IS_NOT_A_SUITABLE_ITEM));
					return;
				}
				if(_gemstoneCount < 30)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.GEMSTONE_QUANTITY_IS_INCORRECT));
					return;
				}
				_gemstoneCount = 30;
				break;
			case L2Item.CRYSTAL_A:
				if(gemstoneItemId != 2131)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.THIS_IS_NOT_A_SUITABLE_ITEM));
					return;
				}
				if(_gemstoneCount < 20)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.GEMSTONE_QUANTITY_IS_INCORRECT));
					return;
				}
				_gemstoneCount = 20;
				break;
			case L2Item.CRYSTAL_S:
				if(gemstoneItemId != 2131)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.THIS_IS_NOT_A_SUITABLE_ITEM));
					return;
				}
				if(_gemstoneCount < 25)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.GEMSTONE_QUANTITY_IS_INCORRECT));
					return;
				}
				_gemstoneCount = 25;
				break;
		}

		activeChar.sendPacket(new ExPutCommissionResultForVariationMake(_gemstoneItemObjId, _gemstoneCount));
		activeChar.sendPacket(new SystemMessage(SystemMessage.PRESS_THE_AUGMENT_BUTTON_TO_BEGIN));
	}
}