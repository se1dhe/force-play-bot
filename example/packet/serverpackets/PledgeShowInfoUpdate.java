package l2p.gameserver.serverpackets;

import l2p.gameserver.Config;
import l2p.gameserver.model.L2Clan;

public class PledgeShowInfoUpdate extends L2GameServerPacket
{
	private L2Clan _clan;

	public PledgeShowInfoUpdate(L2Clan clan)
	{
		_clan = clan;
	}

	@Override
	protected final void writeImpl()
	{
		//sending empty data so client will ask all the info in response ;)
		writeD(_clan.getClanId());
		writeD(Config.REQUEST_ID);
		writeD(_clan.getCrestId());
		writeD(_clan.getLevel());
		writeD(_clan.getHasCastle());
		writeD(0);
		writeD(_clan.getHasHideout());
		writeD(0);
		writeD(_clan.getRank());
		writeD(_clan.getReputationScore());
		writeD(0);
		writeD(0);
		writeD(_clan.getAllyId());
		writeS(_clan.getAllyName());
		writeD(_clan.getAllyCrestId());
		writeD(_clan.isAtWar());
		writeD(0x00);
		writeD(0x00);
	}

	@Override
	protected final void writeImplIT()
	{
		//sending empty data so client will ask all the info in response ;)
		writeD(_clan.getClanId());
		writeD(_clan.getCrestId());
		writeD(_clan.getLevel());
		writeD(_clan.getHasCastle());
		writeD(_clan.getHasHideout());
		writeD(_clan.getRank());
		writeD(_clan.getReputationScore());
		writeD(0);
		writeD(0);
		writeD(_clan.getAllyId());
		writeS(_clan.getAllyName());
		writeD(_clan.getAllyCrestId());
		writeD(_clan.isAtWar());
	}
}
