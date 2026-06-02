package l2p.gameserver.serverpackets;

import l2p.gameserver.Config;
import l2p.gameserver.model.L2Clan;

public class PledgeStatusChanged extends L2GameServerPacket
{
	private final int leader_id;
	private final int clan_id;
	private final int crest_id;
	private final int ally_id;
	private final int crest_ally_id;

	public PledgeStatusChanged(L2Clan clan)
	{
		leader_id = clan.getLeaderId();
		clan_id = clan.getClanId();
		crest_id = clan.getCrestId();
		ally_id = clan.getAllyId();
		crest_ally_id = clan.getAllyCrestId();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(Config.REQUEST_ID);
		writeD(leader_id);
		writeD(clan_id);
		writeD(crest_id);
		writeD(ally_id);
		writeD(crest_ally_id);
		writeD(0);
		writeD(0);
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(leader_id);
		writeD(clan_id);
		writeD(crest_id);
		writeD(ally_id);
		writeD(crest_ally_id);
		writeD(0);
		writeD(0);
	}
}