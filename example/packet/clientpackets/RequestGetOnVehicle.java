package l2p.gameserver.clientpackets;

import l2p.gameserver.data.BoatHolder;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.entity.Vehicle;
import l2p.gameserver.utils.Location;

public class RequestGetOnVehicle extends L2GameClientPacket
{
	private int _objectId;
	private Location _loc = new Location();

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_loc.x = readD();
		_loc.y = readD();
		_loc.z = readD();
	}

	@Override
	protected void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if(player == null || player.isInVehicle())
			return;

		Vehicle boat = BoatHolder.getInstance().getBoat(_objectId);
		if(boat == null || boat.isMoving() || !boat.isInRange(player, 600L) || _loc.distance3D(0, 0, 0) > 1000)
			return;

		player._stablePoint = boat.getCurrentWay().getReturnLoc();
		boat.addPlayer(player, _loc);
	}
}