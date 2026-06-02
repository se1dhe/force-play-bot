package l2p.gameserver.serverpackets;

import l2p.commons.util.GArray;
import l2p.gameserver.model.L2CommandChannel;
import l2p.gameserver.model.L2Party;
import l2p.gameserver.model.L2Player;

public class ExMultiPartyCommandChannelInfo extends L2GameServerPacket
{
	private String ChannelLeaderName;
	private int MemberCount;
	private GArray<ChannelPartyInfo> parties;

	public ExMultiPartyCommandChannelInfo(L2CommandChannel channel)
	{
		if(channel == null)
			return;
		ChannelLeaderName = channel.getChannelLeader().getName();
		MemberCount = channel.getMemberCount();

		parties = new GArray<ChannelPartyInfo>();
		for(L2Party party : channel.getParties())
			if(party != null)
			{
				L2Player leader = party.getPartyLeader();
				if(leader != null)
					parties.add(new ChannelPartyInfo(leader.getName(), leader.getObjectId(), party.getMemberCount()));
			}
	}

	@Override
	protected void writeImpl()
	{
		if(parties == null)
			return;

		writeS(ChannelLeaderName); // имя лидера CC
		writeD(0); // Looting type?
		writeD(MemberCount); // общее число человек в СС
		writeD(parties.size()); // общее число партий в СС

		for(ChannelPartyInfo party : parties)
		{
			writeS(party.Leader_name); // имя лидера партии
			writeD(party.Leader_obj_id); // ObjId пати лидера
			writeD(party.MemberCount); // количество мемберов в пати
		}

		parties.clear();
	}

	static class ChannelPartyInfo
	{
		public String Leader_name;
		public int Leader_obj_id, MemberCount;

		public ChannelPartyInfo(String _Leader_name, int _Leader_obj_id, int _MemberCount)
		{
			Leader_name = _Leader_name;
			Leader_obj_id = _Leader_obj_id;
			MemberCount = _MemberCount;
		}
	}
}