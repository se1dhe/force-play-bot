package l2p.gameserver.clientpackets;

import l2p.gameserver.instancemanager.PartyRoomManager;
import l2p.gameserver.model.L2Player;

public class RequestExitPartyMatchingWaitingRoom extends L2GameClientPacket
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

		PartyRoomManager.getInstance().removeFromWaitingList(player);
	}
}