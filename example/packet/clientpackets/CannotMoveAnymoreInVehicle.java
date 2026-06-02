package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.entity.Vehicle;
import l2p.gameserver.utils.Location;

public class CannotMoveAnymoreInVehicle extends L2GameClientPacket
{
	private Location _loc = new Location();
	private int _boatid;

	@Override
	protected void readImpl()
	{
		_boatid = readD();
		_loc.x = readD();
		_loc.y = readD();
		_loc.z = readD();
		_loc.h = readD();
	}

	@Override
	protected void runImpl()
	{
		final L2Player player = getClient().getActiveChar();
		if(player == null)
			return;

		final Vehicle boat = player.getVehicle();
		if(boat != null && boat.getObjectId() == _boatid && _loc.distance3D(0, 0, 0) < 1000)
		{
			player.setInVehiclePosition(_loc);
			player.setHeading(_loc.h);
			player.broadcastPacket(boat.inStopMovePacket(player));
		}
	}
}