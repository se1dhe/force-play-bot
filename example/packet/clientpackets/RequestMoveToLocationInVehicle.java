package l2p.gameserver.clientpackets;

import l2p.gameserver.data.BoatHolder;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.entity.Vehicle;
import l2p.gameserver.utils.Location;

public class RequestMoveToLocationInVehicle extends L2GameClientPacket
{
	private Location _pos = new Location();
	private Location _originPos = new Location();
	private int _boatObjectId;

	@Override
	protected void readImpl()
	{
		_boatObjectId = readD();
		_pos.x = readD();
		_pos.y = readD();
		_pos.z = readD();
		_originPos.x = readD();
		_originPos.y = readD();
		_originPos.z = readD();
	}

	@Override
	protected void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;

		Vehicle boat;
		if(player.isInVehicle())
		{
			boat = player.getVehicle();
			if(boat.getObjectId() != _boatObjectId || !player.isInRange(boat, 1000))
			{
				player.sendActionFailed();
				return;
			}
		}
		else
		{
			boat = BoatHolder.getInstance().getBoat(_boatObjectId);
			if(boat == null || !player.isInRange(boat, 500))
			{
				player.sendActionFailed();
				return;
			}
		}

		if(_pos.distance3D(0, 0, 0) > 1000 || _originPos.distance3D(0, 0, 0) > 1000)
		{
			player.sendActionFailed();
			return;
		}

		boat.moveInBoat(player, _originPos, _pos);
	}
}