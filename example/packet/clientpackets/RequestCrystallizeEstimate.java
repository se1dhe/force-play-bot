package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Skill;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.serverpackets.ExGetCrystalizingEstimation;
import l2p.gameserver.serverpackets.SystemMessage;

public class RequestCrystallizeEstimate extends L2GameClientPacket
{
	//0, d, c, b, a, s ... 0 is for none
	public static short[] _crystalId = { 0, 1458, 1459, 1460, 1461, 1462 };

	private int _objectId;

	@Override
	public void readImpl()
	{
		_objectId = readD();
		if(getClient().isITClient())
			readD();
		else
			readQ();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();

		if(activeChar == null)
			return;

		if(activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInFightClub())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInStoreMode())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM));
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isFishing())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_DO_THAT_WHILE_FISHING));
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInTrade())
		{
			activeChar.sendActionFailed();
			return;
		}

		L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);

		if(item == null || !item.canBeCrystallized(activeChar, true))
		{
			activeChar.sendActionFailed();
			return;
		}

		int level = activeChar.getSkillLevel(L2Skill.SKILL_CRYSTALLIZE);
		if(item.getItem().getItemGrade() > level)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.CANNOT_CRYSTALLIZE_CRYSTALLIZATION_SKILL_LEVEL_TOO_LOW));
			activeChar.sendActionFailed();
			return;
		}

		sendPacket(new ExGetCrystalizingEstimation(item));
	}
}