package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.entity.Vehicle;
import l2p.gameserver.utils.Location;

public class MoveToLocationInVehicle extends L2GameServerPacket
{
	private int char_id, boat_id;
	private Location _origin, _destination;

	public MoveToLocationInVehicle(L2Player cha, Vehicle boat, Location origin, Location destination)
	{
		char_id = cha.getObjectId();
		boat_id = boat.getObjectId();
		_origin = origin;
		_destination = destination;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(char_id);
		writeD(boat_id);
		writeD(_destination.x);
		writeD(_destination.y);
		writeD(_destination.z);
		writeD(_origin.x);
		writeD(_origin.y);
		writeD(_origin.z);
	}
}