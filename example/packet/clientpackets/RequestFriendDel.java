package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.model.L2Player;

public class RequestFriendDel extends L2GameClientPacket
{
	private String _name;

	@Override
	protected void readImpl()
	{
		_name = readS(Config.CNAME_MAXLEN);
	}

	@Override
	protected void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;
		if(player.isOutOfControl())
		{
			player.sendActionFailed();
			return;
		}
		player.getFriendList().removeFriend(_name);
	}
}