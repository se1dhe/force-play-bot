package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2ObjectsStorage;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.PartyRoom;

public class RequestOustFromPartyRoom extends L2GameClientPacket
{
	private int _objectId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2Player player = getClient().getActiveChar();

		PartyRoom room = player.getPartyRoom();
		if(room == null)
			return;

		if(room.getLeader() != player)
			return;

		L2Player member = L2ObjectsStorage.getPlayer(_objectId);
		if(member == null)
			return;

		if(member == room.getLeader())
			return;

		room.removeMember(member, true);
	}
}