package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Character;
import l2p.gameserver.utils.Location;

public class FlyToLocation extends L2GameServerPacket
{
	private int _chaObjId;
	private final FlyType _type;
	private Location _loc;
	private Location _destLoc;

	public enum FlyType
	{
		THROW_UP,
		THROW_HORIZONTAL,
		CHARGE,
		DUMMY,
		NONE
	}

	public FlyToLocation(L2Character cha, Location destLoc, FlyType type)
	{
		_destLoc = destLoc;
		_type = type;
		_chaObjId = cha.getObjectId();
		_loc = cha.getLoc();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_chaObjId);
		writeD(_destLoc.getX());
		writeD(_destLoc.getY());
		writeD(_destLoc.getZ());
		writeD(_loc.getX());
		writeD(_loc.getY());
		writeD(_loc.getZ());
		writeD(_type.ordinal());
		// TODO [V] - нужно?
		writeD(0x00/*_flySpeed*/);
		writeD(0x00/*_flyDelay*/);
		writeD(0x00/*_animationSpeed*/);
	}

	@Override
	protected void writeImplIT()
	{
		writeD(_chaObjId);
		writeD(_destLoc.getX());
		writeD(_destLoc.getY());
		writeD(_destLoc.getZ());
		writeD(_loc.getX());
		writeD(_loc.getY());
		writeD(_loc.getZ());
		writeD(_type.ordinal());
	}
}