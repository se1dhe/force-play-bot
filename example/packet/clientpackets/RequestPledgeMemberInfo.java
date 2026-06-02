package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Clan;
import l2p.gameserver.model.L2ClanMember;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.serverpackets.PledgeReceiveMemberInfo;

public class RequestPledgeMemberInfo extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int _pledgeType;
	private String _target;

	@Override
	public void readImpl()
	{
		_pledgeType = readD();
		_target = readS();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		L2Clan clan = activeChar.getClan();
		if(clan != null)
		{
			L2ClanMember cm = clan.getClanMember(_target);
			if(cm != null)
				activeChar.sendPacket(new PledgeReceiveMemberInfo(cm));
		}
	}
}