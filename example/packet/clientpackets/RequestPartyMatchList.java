package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.PartyRoom;

public class RequestPartyMatchList extends L2GameClientPacket
{
	private int _lootDist;
	private int _maxMembers;
	private int _minLevel;
	private int _maxLevel;
	private int _roomId;
	private String _roomTitle;

	@Override
	protected void readImpl()
	{
		_roomId = readD();
		_maxMembers = readD();
		_minLevel = readD();
		_maxLevel = readD();
		_lootDist = readD();
		_roomTitle = readS();
	}

	@Override
	protected void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;

		PartyRoom room = player.getPartyRoom();
		if(room == null)
			new PartyRoom(player, _minLevel, _maxLevel, _maxMembers, _lootDist, _roomTitle);
		else if(room.getId() == _roomId && room.getLeader() == player)
		{
			room.setMinLevel(_minLevel);
			room.setMaxLevel(_maxLevel);
			room.setMaxMemberSize(_maxMembers);
			room.setTopic(_roomTitle);
			room.setLootType(_lootDist);
			room.broadCast(room.infoRoomPacket());
		}
	}
}