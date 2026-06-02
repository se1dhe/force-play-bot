package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2ClanMember;
import l2p.gameserver.model.L2Player;

public class PledgeShowMemberListUpdate extends L2GameServerPacket
{
	private String _name;
	private int _lvl;
	private int _classId;
	private int _race;
	private int _sex;
	private boolean _isOnline;
	private int _objectId;
	private int _pledgeType;
	private int _isApprentice = 0;

	public PledgeShowMemberListUpdate(final L2Player player)
	{
		_name = player.getName();
		_lvl = player.getLevel();
		_classId = player.getClassId().getId();
		_race = player.getRace().ordinal();
		_sex = player.getSex();
		_objectId = player.getObjectId();
		_isOnline = player.isOnline();
		_pledgeType = player.getPledgeType();
		if(player.getClan() != null && player.getClan().getClanMember(_objectId) != null)
			_isApprentice = player.getClan().getClanMember(_objectId).hasSponsor() ? 1 : 0;
	}

	public PledgeShowMemberListUpdate(final L2ClanMember cm)
	{
		_name = cm.getName();
		_lvl = cm.getLevel();
		_classId = cm.getClassId();
		_race = cm.getRace();
		_sex = cm.getSex();
		_objectId = cm.getObjectId();
		_isOnline = cm.isOnline();
		_pledgeType = cm.getPledgeType();
		_isApprentice = cm.hasSponsor() ? 1 : 0;
	}

	@Override
	protected final void writeImpl()
	{
		writeS(_name);
		writeD(_lvl);
		writeD(_classId);
		writeD(_sex);
		writeD(_race);
		writeD(_isOnline ? _objectId : 0); // 1=online 0=offline
		writeD(_pledgeType);
		writeD(_isApprentice); // does a clan member have a sponsor
		writeC(0x00);
	}

	@Override
	protected final void writeImplIT()
	{
		writeS(_name);
		writeD(_lvl);
		writeD(_classId);
		writeD(_sex);
		writeD(_race);
		writeD(_isOnline ? _objectId : 0); // 1=online 0=offline
		writeD(_pledgeType);
		writeD(_isApprentice); // does a clan member have a sponsor
	}
}