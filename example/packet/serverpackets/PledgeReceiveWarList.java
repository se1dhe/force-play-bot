package l2p.gameserver.serverpackets;

import l2p.commons.util.GArray;
import l2p.gameserver.model.L2Clan;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PledgeReceiveWarList extends L2GameServerPacket
{
	private final GArray<String> _list;
	private List<WarInfo> _wars = new ArrayList<WarInfo>();
	private final int _tab;
	private final int _page;

	public PledgeReceiveWarList(GArray<String> list, List<L2Clan> attackerClans, List<L2Clan> enemyClans, int tab, int page)
	{
		_list = list;
		_tab = tab;
		_page = page;

		Set<L2Clan> clans = new HashSet<L2Clan>();
		clans.addAll(attackerClans);
		clans.addAll(enemyClans);
		for(L2Clan clan : clans)
		{
			int state = 0;
			if(attackerClans.contains(clan) && enemyClans.contains(clan))
			{
				state = 2;
			}
			_wars.add(new WarInfo(clan.getName(), state));
		}
	}

	@Override
	protected void writeImpl()
	{
		writeD(_page);
		writeD(_wars.size());
		for(WarInfo warInfo : _wars)
		{
			writeS(warInfo.clan_name);
			writeD(warInfo.state);
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);
		}
	}

	@Override
	protected void writeImplIT()
	{
		writeD(_tab);
		writeD(_page);
		writeD(_tab == 0 ? _list.size() : (_page == 0 ? (_list.size() >= 13 ? 13 : _list.size()) : (_list.size() % (13 * _page))));

		int index = 0;
		for(String name : _list)
		{
			if(_tab != 0)
			{
				if(index < _page * 13)
				{
					index++;
					continue;
				}
				if(index == (_page + 1) * 13)
				{
					break;
				}
				index++;
			}
			writeS(name);
			writeD(_tab);
			writeD(_page);
		}
	}

	static class WarInfo
	{
		public String clan_name;
		public int state;

		public WarInfo(String clan_name, int state)
		{
			this.clan_name = clan_name;
			this.state = state;
		}
	}
}