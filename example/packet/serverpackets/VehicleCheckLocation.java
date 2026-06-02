package l2p.gameserver.serverpackets;

import l2p.gameserver.model.entity.Vehicle;
import l2p.gameserver.utils.Location;

public class VehicleCheckLocation extends L2GameServerPacket
{
	private int _boatObjId;
	private Location _loc;

	public VehicleCheckLocation(Vehicle instance)
	{
		_boatObjId = instance.getObjectId();
		_loc = instance.getLoc();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_boatObjId);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
		writeD(_loc.h);
	}
}