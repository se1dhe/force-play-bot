package l2p.gameserver.serverpackets;

import l2p.gameserver.Config;
import l2p.gameserver.model.L2Clan;

public class PledgeInfo extends L2GameServerPacket
{
	private int clan_id;
	private String clan_name, ally_name;

	public PledgeInfo(L2Clan clan)
	{
		clan_id = clan.getClanId();
		clan_name = clan.getName();
		ally_name = clan.getAllyName();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(Config.REQUEST_ID);
		writeD(clan_id);
		writeS(clan_name);
		writeS(ally_name);
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(clan_id);
		writeS(clan_name);
		writeS(ally_name);
	}
}