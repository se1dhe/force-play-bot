package l2p.gameserver.serverpackets;

import l2p.gameserver.utils.Location;

public class RadarControl extends L2GameServerPacket
{
	private int _x;
	private int _y;
	private int _z;
	private int _type;
	private int _showRadar;

	public RadarControl(int showRadar, int type, Location loc)
	{
		this(showRadar, type, loc.x, loc.y, loc.z);
	}

	public RadarControl(int showRadar, int type, int x, int y, int z)
	{
		_showRadar = showRadar; // showRader?? 0 = showradar; 1 = delete radar;
		_type = type; // 1 - только стрелка над головой, 2 - флажок на карте
		_x = x;
		_y = y;
		_z = z;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_showRadar);
		writeD(_type); //maybe type
		writeD(_x); //x
		writeD(_y); //y
		writeD(_z); //z
	}
}