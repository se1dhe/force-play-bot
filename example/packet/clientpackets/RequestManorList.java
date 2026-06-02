package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.serverpackets.ExSendManorList;

public class RequestManorList extends L2GameClientPacket
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

		if(!Config.ALLOW_MANOR)
		{
			player.sendMessage("Manor disabled.");
			player.sendActionFailed();
			return;
		}

		player.sendPacket(ExSendManorList.STATIC_PACKET);
	}
}