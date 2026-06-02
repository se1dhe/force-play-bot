package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;

public class RequestTargetActionMenu extends L2GameClientPacket
{
	private int objectId;

	@Override
	protected void readImpl() throws Exception
	{
		objectId = readD();
		readH();
	}

	@Override
	protected void runImpl() throws Exception
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;

		Action.onAction(player, objectId, false);
	}
}