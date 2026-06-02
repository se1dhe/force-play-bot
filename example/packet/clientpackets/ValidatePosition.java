package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.geodata.GeoEngine;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.entity.Vehicle;
import l2p.gameserver.utils.Location;
import l2p.gameserver.utils.MapUtils;

public class ValidatePosition extends L2GameClientPacket
{
	private final Location _loc = new Location();

	private int _boatObjectId;

	private Location _lastClientPosition;
	private Location _lastServerPosition;

	/**
	 * packet type id 0x48
	 * format:		cddddd
	 */
	@Override
	protected void readImpl()
	{
		_loc.x = readD();
		_loc.y = readD();
		_loc.z = readD();
		_loc.h = readD();
		_boatObjectId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;

		if(player.isTeleporting() || player.inObserverMode() || player.isAlikeDead())
			return;

		_lastClientPosition = player.getLastClientPosition();
		_lastServerPosition = player.getLastServerPosition();

		if(_lastClientPosition == null)
			_lastClientPosition = player.getLoc();
		if(_lastServerPosition == null)
			_lastServerPosition = player.getLoc();

		if(player.getX() == 0 && player.getY() == 0 && player.getZ() == 0)
		{
			if(Config.VALID_TELEPORT)
				player.teleToClosestTown();
			else
				correctPosition(player);
			return;
		}

		final Vehicle boat = player.getVehicle();
		if(boat != null)
		{
			if(boat.getObjectId() == _boatObjectId)
			{
				Location boatLoc = player.getInVehiclePosition();
				if(boatLoc != null && (boatLoc.distance(_loc) > 1024 || Math.abs(_loc.z - boatLoc.z) > 256))
					player.sendPacket(boat.validateLocationPacket(player));
			}
			return;
		}

		if(player.isFalling())
		{
			player.setLastClientPosition(null);
			player.setLastServerPosition(null);
			return;
		}

		double diff = player.getDistance(_loc.x, _loc.y);
		double maxDiff = player.getDistance(_loc.x, _loc.y, _loc.z);
		int dz = Math.abs(_loc.z - player.getZ());
		int maxDiffZ = MapUtils.regionX(player.getX()) == 26 && MapUtils.regionY(player.getY()) == 14 ? (player.isFlying() ? 1024 : Config.MAX_DIFF_Z_VALAKAS_ZONE) : (player.isFlying() ? 1024 : 512);

		if(dz >= maxDiffZ)
		{
			if(player.getIncorrectValidateCount() >= 3 || Config.VALID_TELEPORT)
				player.teleToClosestTown();
			else
			{
				player.teleToLocation(player.getLoc(), player.getInstanceId());
				player.setIncorrectValidateCount(player.getIncorrectValidateCount() + 1);
			}
		}
		else if(dz >= 256)
			player.validateLocation(0);
		else if(_loc.z < Config.MAP_MIN_Z || _loc.z > Config.MAP_MAX_Z)
		{
			if(player.getIncorrectValidateCount() >= 3 || Config.VALID_TELEPORT)
				player.teleToClosestTown();
			else
			{
				correctPosition(player);
				player.setIncorrectValidateCount(player.getIncorrectValidateCount() + 1);
			}
		}
		else if(diff > 1024)
		{
			if(player.getIncorrectValidateCount() >= 3 || Config.VALID_TELEPORT)
				player.teleToClosestTown();
			else
			{
				player.teleToLocation(player.getLoc(), player.getInstanceId());
				player.setIncorrectValidateCount(player.getIncorrectValidateCount() + 1);
			}
		}
		else if(diff > maxDiff)// old: player.getMoveSpeed() * 2
			//  && !player.isFlying() && !player.isInBoat() && !player.isSwimming()
			//TODO реализовать NetPing и вычислять предельное отклонение исходя из пинга по формуле: 16 + (ping * player.getMoveSpeed()) / 1000
			player.validateLocation(1);
		else
			player.setIncorrectValidateCount(0);


		player.setLastClientPosition(_loc.setH(player.getHeading()));
		player.setLastServerPosition(player.getLoc());
	}

	private void correctPosition(L2Player player)
	{
		if(player.isGM())
		{
			player.sendMessage("Server loc: " + player.getLoc());
			player.sendMessage("Correcting position...");
		}
		if(_lastServerPosition.x != 0 && _lastServerPosition.y != 0 && _lastServerPosition.z != 0)
		{
			if(GeoEngine.getNSWE(_lastServerPosition.x, _lastServerPosition.y, _lastServerPosition.z, player.getGeoIndex()) == GeoEngine.NSWE_ALL)
				player.teleToLocation(_lastServerPosition, player.getInstanceId());
			else
				player.teleToClosestTown();
		}
		else if(_lastClientPosition.x != 0 && _lastClientPosition.y != 0 && _lastClientPosition.z != 0)
		{
			if(GeoEngine.getNSWE(_lastClientPosition.x, _lastClientPosition.y, _lastClientPosition.z, player.getGeoIndex()) == GeoEngine.NSWE_ALL)
				player.teleToLocation(_lastClientPosition, player.getInstanceId());
			else
				player.teleToClosestTown();
		}
		else
			player.teleToClosestTown();
	}
}