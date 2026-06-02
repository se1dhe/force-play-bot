package l2p.gameserver.serverpackets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import l2p.gameserver.model.L2Clan;
import org.apache.commons.lang3.StringUtils;
import l2p.gameserver.data.xml.holder.ResidenceHolder;
import l2p.gameserver.model.entity.events.impl.ClanHallAuctionEvent;
import l2p.gameserver.model.entity.events.impl.ClanHallMiniGameEvent;
import l2p.gameserver.model.entity.events.impl.SiegeEvent;
import l2p.gameserver.model.entity.residence.ClanHall;
import l2p.gameserver.tables.ClanTable;

public class ExShowAgitInfo extends L2GameServerPacket
{
	private List<AgitInfo> _clanHalls = Collections.emptyList();

	public ExShowAgitInfo()
	{
		List<ClanHall> chs = ResidenceHolder.getInstance().getResidenceList(ClanHall.class);
		_clanHalls = new ArrayList<AgitInfo>(chs.size());

		for(ClanHall clanHall : chs)
		{
			int ch_id = clanHall.getId();
			int getType;
			SiegeEvent<?,?> siegeEvent = clanHall.getSiegeEvent();
			if(siegeEvent != null)
			{
				if(siegeEvent.getClass() == ClanHallAuctionEvent.class)
					getType = 0;
				else if(siegeEvent.getClass() == ClanHallMiniGameEvent.class)
					getType = 2;
				else
					getType = 1;
			}
			else
				getType = 1;

			L2Clan clan = ClanTable.getInstance().getClan(clanHall.getOwnerId());
			String clan_name = clanHall.getOwnerId() == 0 || clan == null ? StringUtils.EMPTY : clan.getName();
			String leader_name = clanHall.getOwnerId() == 0 || clan == null ? StringUtils.EMPTY : clan.getLeaderName();
			_clanHalls.add(new AgitInfo(clan_name, leader_name, ch_id, getType));
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_clanHalls.size());
		for(AgitInfo info : _clanHalls)
		{
			writeD(info.ch_id);
			writeS(info.clan_name);
			writeS(info.leader_name);
			writeD(info.getType);
		}
	}

	@Override
	protected boolean canWriteIT()
	{
		return false;
	}

	static class AgitInfo
	{
		public String clan_name, leader_name;
		public int ch_id, getType;

		public AgitInfo(String clan_name, String leader_name, int ch_id, int lease)
		{
			this.clan_name = clan_name;
			this.leader_name = leader_name;
			this.ch_id = ch_id;
			this.getType = lease;
		}
	}
}