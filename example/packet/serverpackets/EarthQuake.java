package l2p.gameserver.serverpackets;

import l2p.gameserver.utils.Location;

public class EarthQuake extends L2GameServerPacket
{
	private Location _loc;
	private int _intensity;
	private int _duration;

	public EarthQuake(Location loc, int intensity, int duration)
	{
		_loc = loc;
		_intensity = intensity;
		_duration = duration;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_loc.getX());
		writeD(_loc.getY());
		writeD(_loc.getZ());
		writeD(_intensity);
		writeD(_duration);
		writeD(0x00); // Unknown
	}
}