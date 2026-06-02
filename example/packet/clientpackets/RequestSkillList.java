package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.network.L2GameClient;
import l2p.gameserver.serverpackets.SkillList;

public class RequestSkillList extends L2GameClientPacket
{
	@Override
	public void readImpl()
	{}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		sendPacket(new SkillList(activeChar));
	}
}