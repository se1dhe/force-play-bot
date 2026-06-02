package l2p.gameserver.clientpackets;

import java.nio.BufferUnderflowException;

import l2p.gameserver.instancemanager.PartyRoomManager;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.PartyRoom;

public class RequestPartyMatchDetail extends L2GameClientPacket
{
	private int _roomId;
	private int _locations;
	private int _level;
	private boolean _fail;

	@Override
	protected void readImpl()
	{
		try
		{
			_roomId = readD(); // room id, если 0 то autojoin
			_locations = readD(); // location
			_level = readD(); // 1 - all, 0 - my level (только при autojoin)
		}
		catch (BufferUnderflowException e)
		{
			_fail = true;
		}
	}

	@Override
	protected void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;
		if(_fail)
		{
			player.sendActionFailed();
			return;
		}

		if(player.getPartyRoom() != null)
			return;

		if(_roomId > 0)
		{
			PartyRoom room = PartyRoomManager.getInstance().getMatchingRoom(_roomId);
			if(room == null)
				return;

			room.addMember(player);
		}
		else
		{
			for(PartyRoom room : PartyRoomManager.getInstance().getMatchingRooms(_locations, _level == 1, player))
				if(room.addMember(player))
					break;
		}
	}
}