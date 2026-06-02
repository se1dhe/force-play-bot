package l2p.gameserver.serverpackets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import l2p.gameserver.instancemanager.PartyRoomManager;
import l2p.gameserver.model.L2ObjectsStorage;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.PartyRoom;

public class ListPartyWaiting extends L2GameServerPacket
{
	private Collection<PartyRoom> _rooms;
	private int _fullSize;

	public ListPartyWaiting(int region, boolean allLevels, int page, L2Player activeChar)
	{
		int first = (page - 1) * 64;
		int firstNot = page * 64;
		_rooms = new ArrayList<PartyRoom>();

		int i = 0;
		List<PartyRoom> temp = PartyRoomManager.getInstance().getMatchingRooms(region, allLevels, activeChar);
		_fullSize = temp.size();
		for(PartyRoom room : temp)
		{
			if(i < first || i >= firstNot)
				continue;
			_rooms.add(room);
			i++;
		}
	}

	// TODO [V] - такая структура?
	@Override
	protected final void writeImpl()
	{
		writeD(_fullSize);
		writeD(_rooms.size());

		for(PartyRoom room : _rooms)
		{
			writeD(room.getId()); //room id
			writeS(room.getLeader() == null ? "None" : room.getLeader().getName());
			writeD(room.getLocationId());
			writeD(room.getMinLevel()); //min level
			writeD(room.getMaxLevel()); //max level
			writeD(room.getMaxMembersSize()); //max members coun
			writeS(room.getTopic()); // room name

			Vector<Long> players = room.getPlayers();
			writeD(players.size()); //members count

			for(Long storedId : players)
			{
				L2Player player;
				if((player = L2ObjectsStorage.getAsPlayer(storedId)) != null)
				{
					writeD(player.getClassId().getId());
					writeS(player.getName());
				}
			}
		}

		writeD(0x00);
		writeD(0x00);
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_fullSize);
		writeD(_rooms.size());

		for(PartyRoom room : _rooms)
		{
			writeD(room.getId()); //room id
			writeS(room.getLeader() == null ? "None" : room.getLeader().getName());
			writeD(room.getLocationId());
			writeD(room.getMinLevel()); //min level
			writeD(room.getMaxLevel()); //max level
			writeD(room.getMaxMembersSize()); //max members coun
			writeS(room.getTopic()); // room name

			Vector<Long> players = room.getPlayers();
			writeD(players.size()); //members count

			for(Long storedId : players)
			{
				L2Player player;
				if((player = L2ObjectsStorage.getAsPlayer(storedId)) != null)
				{
					writeD(player.getClassId().getId());
					writeS(player.getName());
				}
			}
		}
	}
}