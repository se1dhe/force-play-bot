package l2p.gameserver.serverpackets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import l2p.gameserver.instancemanager.PartyRoomManager;
import l2p.gameserver.model.L2ObjectsStorage;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.PartyRoom;

public class ExPartyRoomMember extends L2GameServerPacket
{
	private int _type;
	private List<PartyRoomMemberInfo> _members = Collections.emptyList();

	public ExPartyRoomMember(PartyRoom room, L2Player activeChar)
	{
		_type = room.getMemberType(activeChar);
		_members = new ArrayList<PartyRoomMemberInfo>(room.getPlayers().size());

		for(Long storedId : room.getPlayers())
		{
			L2Player $member;
			if(($member = L2ObjectsStorage.getAsPlayer(storedId)) != null)
				_members.add(new ExPartyRoomMember.PartyRoomMemberInfo($member, room.getMemberType($member)));
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_type);
		writeD(_members.size());
		for(PartyRoomMemberInfo member_info : _members)
		{
			writeD(member_info.objectId);
			writeS(member_info.name);
			writeD(member_info.classId);
			writeD(member_info.level);
			writeD(member_info.location);
			writeD(member_info.memberType);
			writeD(member_info.instanceReuses.length);
			for(int i : member_info.instanceReuses)
				writeD(i);
		}
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_type);
		writeD(_members.size());
		for(PartyRoomMemberInfo member_info : _members)
		{
			writeD(member_info.objectId);
			writeS(member_info.name);
			writeD(member_info.classId);
			writeD(member_info.level);
			writeD(member_info.location);
			writeD(member_info.memberType);
		}
	}

	static class PartyRoomMemberInfo
	{
		public final int objectId, classId, level, location, memberType;
		public final String name;
		public final int[] instanceReuses;

		public PartyRoomMemberInfo(L2Player member, int type)
		{
			objectId = member.getObjectId();
			name = member.getName();
			classId = member.getClassId().ordinal();
			level = member.getLevel();
			location = PartyRoomManager.getInstance().getLocation(member);
			memberType = type;
			instanceReuses = new int[0]/*ArrayUtils.toArray(member.getInstanceReuses().keySet())*/;
		}
	}
}