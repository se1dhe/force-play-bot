package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.entity.Vehicle;
import l2p.gameserver.utils.Location;

public class RequestGetOffVehicle extends L2GameClientPacket
{
	// Format: cdddd
	private int _objectId;
	private Location _location = new Location();

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_location.x = readD();
		_location.y = readD();
		_location.z = readD();
	}

	@Override
	protected void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;

		Vehicle boat = player.getVehicle();
		if(boat == null || boat.isMoving() || boat.getObjectId() != _objectId || !boat.isInRange(_location, 500L))
		{
			player.sendActionFailed();
			return;
		}

		boat.oustPlayer(player, _location, false);
	}
}