package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2World;

public class RequestReload extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;

		player.sendUserInfo(true);
		L2World.showObjectsToPlayer(player, false);
	}
}