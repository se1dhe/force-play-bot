package l2p.gameserver.serverpackets;

public class FinishRotating extends L2GameServerPacket
{
	private int _charId;
	private int _degree;
	private int _speed;

	public FinishRotating(int objId, int degree, int speed)
	{
		_charId = objId;
		_degree = degree;
		_speed = speed;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_charId);
		writeD(_degree);
		writeD(_speed);
		writeD(0x00); //??
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_charId);
		writeD(_degree);
		writeD(_speed);
		writeC(0x00);
	}
}