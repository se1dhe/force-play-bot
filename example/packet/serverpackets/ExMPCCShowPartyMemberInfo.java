package l2p.gameserver.serverpackets;

import l2p.commons.util.GArray;
import l2p.gameserver.model.L2Party;
import l2p.gameserver.model.L2Player;

public class ExMPCCShowPartyMemberInfo extends L2GameServerPacket
{
	private GArray<PartyMemberInfo> members;

	public ExMPCCShowPartyMemberInfo(L2Player partyLeader)
	{
		if(!partyLeader.isInParty())
			return;

		L2Party _party = partyLeader.getParty();
		if(_party == null)
			return;

		if(!_party.isInCommandChannel())
			return;

		members = new GArray<PartyMemberInfo>();
		for(L2Player _member : _party.getPartyMembers())
			members.add(new PartyMemberInfo(_member.getName(), _member.getObjectId(), _member.getClassId().getId()));
	}

	@Override
	protected final void writeImpl()
	{
		if(members == null)
			return;

		writeD(members.size()); // Количество членов в пати
		for(PartyMemberInfo _member : members)
		{
			writeS(_member.name); // Имя члена пати
			writeD(_member.object_id); // object Id члена пати
			writeD(_member.class_id); // id класса члена пати
		}
		members.clear();
	}

	static class PartyMemberInfo
	{
		public String name;
		public int object_id, class_id;

		public PartyMemberInfo(String _name, int _object_id, int _class_id)
		{
			name = _name;
			object_id = _object_id;
			class_id = _class_id;
		}
	}
}