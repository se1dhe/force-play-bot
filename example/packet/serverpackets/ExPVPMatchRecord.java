package l2p.gameserver.serverpackets;

public class ExPVPMatchRecord extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		// TODO ddddd d[Sdd] d[Sdd]	(currentState:%d blueTeamTotalKillCnt:%d, redTeamTotalKillCnt:%d)
	}

	@Override
	protected boolean canWriteIT()
	{
		return false;
	}
}