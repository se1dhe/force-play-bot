package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.instancemanager.PlayerManager;
import l2p.gameserver.model.Friend;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.serverpackets.ExFriendDetailInfo;

public class RequestFriendDetailInfo extends L2GameClientPacket
{
	private String _name;

	@Override
	protected void readImpl() throws Exception
	{
		_name = readS(Config.CNAME_MAXLEN);
	}

	@Override
	protected void runImpl() throws Exception
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}
		int objectId = PlayerManager.getObjectIdByName(_name);
		if(objectId == 0)
		{
			return;
		}
		Friend friend = player.getFriendList().getList().get(objectId);
		if(friend == null)
		{
			return;
		}
		sendPacket(new ExFriendDetailInfo(player.getObjectId(), friend));
	}
}