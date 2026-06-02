package l2p.gameserver.serverpackets;

import l2p.gameserver.Config;
import l2p.gameserver.model.L2Character;
import l2p.gameserver.utils.Location;
import l2p.gameserver.utils.Log;

public class CharMoveToLocation extends L2GameServerPacket
{
	private int _objectId, _client_z_shift;
	private Location _current;
	private Location _destination;

	public CharMoveToLocation(L2Character cha)
	{
		this(cha, cha.getLoc(), cha.getDestination());
	}

	public CharMoveToLocation(L2Character cha, Location from, Location to)
	{
		_objectId = cha.getObjectId();
		_current = from;
		_destination = to;
		if(!cha.isFlying())
			_client_z_shift = Config.CLIENT_Z_SHIFT;
		if(cha.isInWater())
			_client_z_shift += Config.CLIENT_Z_SHIFT;

		if(_destination == null)
		{
			Log.debug("CharMoveToLocation: desc is null, but moving. L2Character: " + cha.getObjectId() + ":" + cha.getName() + "; Loc: " + _current);
			_destination = _current;
		}
	}

	public CharMoveToLocation(int objectId, Location from, Location to)
	{
		_objectId = objectId;
		_current = from;
		_destination = to;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_objectId);
		writeD(_destination.x);
		writeD(_destination.y);
		writeD(_destination.z + _client_z_shift);
		writeD(_current.x);
		writeD(_current.y);
		writeD(_current.z + _client_z_shift);
	}
}