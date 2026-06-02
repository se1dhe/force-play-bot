package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.serverpackets.FriendList;

/**
 * @author VISTALL
 * @date 23:36/22.03.2011
 */
public class RequestExFriendListForPostBox extends L2GameClientPacket
{
	@Override
	protected void readImpl() throws Exception
	{

	}

	@Override
	protected void runImpl() throws Exception
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;

		player.sendPacket(new FriendList(player));
	}
}