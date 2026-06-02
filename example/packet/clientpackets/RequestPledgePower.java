package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Clan;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.serverpackets.ManagePledgePower;

public class RequestPledgePower extends L2GameClientPacket
{
	private int _rank;
	private int _action;
	private int _privs;

	@Override
	public void readImpl()
	{
		_rank = readD();
		_action = readD();
		if(_action == 2)
			_privs = readD();
		else
			_privs = 0;
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(activeChar.isOutOfControl() || activeChar.getClan() == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		if(_action == 2)
		{
			if((activeChar.getClanPrivileges() & L2Clan.CP_CL_MANAGE_RANKS) == L2Clan.CP_CL_MANAGE_RANKS)
			{
				if(_rank == 9)
					_privs = (_privs & L2Clan.CP_CH_ENTRY_EXIT) + (_privs & L2Clan.CP_CS_ENTRY_EXIT);
				activeChar.getClan().setRankPrivs(_rank, _privs);
				activeChar.getClan().updatePrivsForRank(_rank);
			}
		}
		else
			activeChar.sendPacket(new ManagePledgePower(activeChar, _action, _rank));
	}
}