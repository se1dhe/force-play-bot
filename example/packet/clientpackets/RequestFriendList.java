package l2p.gameserver.clientpackets;

import java.util.Map;
import java.util.Map.Entry;

import l2p.gameserver.model.Friend;
import l2p.gameserver.model.FriendList;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2World;
import l2p.gameserver.serverpackets.SystemMessage;

public class RequestFriendList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		activeChar.sendPacket(new SystemMessage(SystemMessage._FRIENDS_LIST_));
		Map<Integer, Friend> _list = activeChar.getFriendList().getList();
		for(Map.Entry<Integer, Friend> entry : _list.entrySet())
		{
			L2Player friend = L2World.getPlayer(entry.getKey());
			if(friend != null)
				activeChar.sendPacket(new SystemMessage(SystemMessage.S1_CURRENTLY_ONLINE).addString(friend.getName()));
			else
				activeChar.sendPacket(new SystemMessage(SystemMessage.S1_CURRENTLY_OFFLINE).addString(entry.getValue().getName()));
		}
		activeChar.sendPacket(new SystemMessage(SystemMessage.__EQUALS__));
	}
}