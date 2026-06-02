package l2p.gameserver.serverpackets;

public class ObserverStart extends L2GameServerPacket
{
	private int _x;
	private int _y;
	private int _z;

	public ObserverStart(int x, int y, int z)
	{
		_x = x;
		_y = y;
		_z = z;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(0x00);
		writeD(0x00);
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeC(0x00);
		writeC(0xC0);
		writeC(0x00);
	}
}