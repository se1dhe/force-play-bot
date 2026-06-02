package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.PartyRoom;

public class RequestDismissPartyRoom extends L2GameClientPacket
{
	private int _roomId;

	@Override
	protected void readImpl()
	{
		_roomId = readD(); //room id
	}

	@Override
	protected void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;

		PartyRoom room = player.getPartyRoom();
		if(room == null || room.getId() != _roomId)
			return;

		if(room.getLeader() != player)
			return;

		room.disband();
	}
}