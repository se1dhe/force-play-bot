package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.serverpackets.SkillCoolTime;

public class RequestSkillCoolTime extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{

	}

	@Override
	protected void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;

		player.sendPacket(new SkillCoolTime(player));
	}

	@Override
	public boolean isFilter()
	{
		return false;
	}
}