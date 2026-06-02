package l2p.gameserver.serverpackets;

import l2p.gameserver.instancemanager.PartyRoomManager;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.PartyRoom;

public class ExManagePartyRoomMember extends L2GameServerPacket
{
	public static final int ADDED = 0;
	public static final int MODIFIED = 1;
	public static final int REMOVED = 2;
	private int _type;
	private PartyRoomMemberInfo member_info;

	public ExManagePartyRoomMember(int changeType, PartyRoom room, L2Player activeChar)
	{
		_type = changeType;
		member_info = new PartyRoomMemberInfo(activeChar, room.getMemberType(activeChar));
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_type);
		writeD(member_info.objectId);
		writeS(member_info.name);
		writeD(member_info.classId);
		writeD(member_info.level);
		writeD(member_info.location);
		writeD(member_info.memberType);
	}

	static class PartyRoomMemberInfo
	{
		public final int objectId;
		public final int classId;
		public final int level;
		public final int location;
		public final int memberType;
		public final String name;

		public PartyRoomMemberInfo(L2Player member, int type)
		{
			objectId = member.getObjectId();
			name = member.getName();
			classId = member.getClassId().ordinal();
			level = member.getLevel();
			location = PartyRoomManager.getInstance().getLocation(member);
			memberType = type;
		}
	}
}