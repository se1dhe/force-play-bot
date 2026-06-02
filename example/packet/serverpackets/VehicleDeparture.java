package l2p.gameserver.serverpackets;

import l2p.gameserver.model.entity.Vehicle;
import l2p.gameserver.utils.Location;

public class VehicleDeparture extends L2GameServerPacket
{
	private int _moveSpeed;
	private int _rotationSpeed;
	private int _boatObjId;
	private Location _loc;

	public VehicleDeparture(Vehicle boat)
	{
		_boatObjId = boat.getObjectId();
		_moveSpeed = boat.getMoveSpeed();
		_rotationSpeed = boat.getRotationSpeed();
		_loc = boat.getDestination();
		if(_loc == null)
			_loc = boat.getReturnLoc();
	}

	public VehicleDeparture(Vehicle boat, Location dest)
	{
		_boatObjId = boat.getObjectId();
		_moveSpeed = boat.getMoveSpeed();
		_rotationSpeed = boat.getRotationSpeed();
		_loc = dest;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_boatObjId);
		writeD(_moveSpeed);
		writeD(_rotationSpeed);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
	}
}