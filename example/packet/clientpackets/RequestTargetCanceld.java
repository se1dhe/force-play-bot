package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Skill;

public class RequestTargetCanceld extends L2GameClientPacket
{
	private int _unselect;

	@Override
	public void readImpl()
	{
		_unselect = readH();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(_unselect == 0)
		{
			if(activeChar.isCastingNow())
			{
				L2Skill skill = activeChar.getCastingSkill();
				activeChar.abortCast(skill != null && (skill.isHandler() || skill.getHitTime() > 1000), false);
			}
			else if(activeChar.getTarget() != null)
				activeChar.setTarget(null);
		}
		else if(activeChar.getTarget() != null)
			activeChar.setTarget(null);
	}
}