package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Clan.RankPrivs;

public class PledgePowerGradeList extends L2GameServerPacket
{
	private final RankPrivs[] _privs;

	public PledgePowerGradeList(RankPrivs[] privs)
	{
		_privs = privs;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_privs.length);
		for(RankPrivs element : _privs)
		{
			writeD(element.getRank());
			writeD(element.getParty());
		}
	}
}