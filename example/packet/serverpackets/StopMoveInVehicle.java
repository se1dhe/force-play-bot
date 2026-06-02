package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.utils.Location;

public class StopMoveInVehicle extends L2GameServerPacket
{
	private int _boatObjectId;
	private int _playerObjectId;
	private int _heading;
	private Location _loc;

	public StopMoveInVehicle(L2Player player)
	{
		_boatObjectId = player.getVehicle().getObjectId();
		_playerObjectId = player.getObjectId();
		_loc = player.getInVehiclePosition();
		_heading = player.getHeading();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_playerObjectId);
		writeD(_boatObjectId);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
		writeD(_heading);
	}
}