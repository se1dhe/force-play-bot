package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;

public class RequestLinkHtml extends L2GameClientPacket
{
	//Format: cS
	//private String _link;

	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if (player == null)
			return;

		player.sendActionFailed();
	}
}