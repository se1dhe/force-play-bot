package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Friend;
import l2p.gameserver.model.L2Alliance;
import l2p.gameserver.model.L2Clan;

import java.util.Calendar;

public class ExFriendDetailInfo extends L2GameServerPacket
{
	private String _name;
	private boolean _isOnline;
	private int _objectId;
	private int _level;
	private int _classId;
	private int _objId;
	private int _clanId;
	private int _clanCrestId;
	private int _allyId;
	private int _allyCrestId;
	private String _clanName;
	private String _allyName;
	private int _lastAccessDelay;
	private String _memo;
	private int _createDay;
	private int _createMonth;

	public ExFriendDetailInfo(int objId, Friend friend)
	{
		_objId = objId;
		_name = friend.getName();
		_isOnline = friend.isOnline();
		_objectId = friend.getObjectId();
		_level = friend.getLevel();
		_classId = friend.getClassId();
		L2Clan clan = friend.getClan();
		if(clan != null)
		{
			_clanId = clan.getClanId();
			_clanCrestId = clan.getCrestId();
			_clanName = clan.getName();
			L2Alliance alliance = clan.getAlliance();
			if(alliance != null)
			{
				_allyId = alliance.getAllyId();
				_allyName = alliance.getAllyName();
				_allyCrestId = alliance.getAllyCrestId();
			}
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(friend.getCreateTime());
		_createDay = calendar.get(Calendar.DAY_OF_MONTH);
		_createMonth = calendar.get(Calendar.MONTH) + 1;
		_lastAccessDelay = (int) (friend.isOnline() ? 0 : (System.currentTimeMillis() - friend.getLastAccess()) / 1000L);
	}

	@Override
	protected void writeImpl()
	{
		writeD(_objId);
		writeS(_name);
		writeD(_isOnline);
		writeD(_objectId);
		writeH(_level);
		writeH(_classId);
		writeD(_clanId);
		writeD(_clanCrestId);
		writeS(_clanName);
		writeD(_allyId);
		writeD(_allyCrestId);
		writeS(_allyName);
		writeC(_createMonth);
		writeC(_createDay);
		writeD(_isOnline ? -1 : _lastAccessDelay);
		writeS(_memo);
	}

	@Override
	protected boolean canWriteIT()
	{
		return false;
	}
}

