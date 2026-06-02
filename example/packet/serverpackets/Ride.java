package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.utils.Location;

public class Ride extends L2GameServerPacket
{
	private boolean _canWriteImpl = false;
	private int _mountType, _id, _rideClassID;
	private Location _loc;

	public Ride(L2Player cha)
	{
		if(cha == null)
			return;

		_id = cha.getObjectId();
		_mountType = cha.getMountEngine().getMountType();
		_rideClassID = cha.getMountEngine().getMountNpcId() + 1000000;
		_loc = cha.getLoc();

		_canWriteImpl = true;
	}

	@Override
	protected boolean canWrite()
	{
		return _canWriteImpl;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_id);
		writeD(_mountType == 0 ? 0 : 1);
		writeD(_mountType);
		writeD(_rideClassID);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
	}
}