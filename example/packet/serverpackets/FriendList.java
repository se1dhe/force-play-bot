package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Friend;
import l2p.gameserver.model.L2Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FriendList extends L2GameServerPacket
{
	private List<FriendInfo> _list = Collections.emptyList();

	public FriendList(L2Player player)
	{
		Map<Integer, Friend> list = player.getFriendList().getList();
		_list = new ArrayList<FriendInfo>(list.size());
		for(Map.Entry<Integer, Friend> entry : list.entrySet())
		{
			FriendInfo f = new FriendInfo();
			f._objectId = entry.getKey();
			f._name = entry.getValue().getName();
			f._online = entry.getValue().isOnline();
			f._classId = entry.getValue().getClassId();
			f._level = entry.getValue().getLevel();
			_list.add(f);
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_list.size());
		for(FriendInfo friendInfo : _list)
		{
			writeD(friendInfo._objectId);
			writeS(friendInfo._name);
			writeD(friendInfo._online ? 1 : 0);
			writeD(friendInfo._objectId);
			writeD(friendInfo._level);
			writeD(friendInfo._classId);
		}
	}

	@Override
	protected boolean canWriteIT()
	{
		return false;
	}

	private static class FriendInfo
	{
		private int _objectId;
		private String _name;
		private boolean _online;
		private int _level;
		private int _classId;
	}
}