package l2p.gameserver.serverpackets;

public class StartRotating extends L2GameServerPacket
{
	private int _charId;
	private int _degree;
	private int _side;
	private int _speed;

	public StartRotating(int objId, int degree, int side, int speed)
	{
		_charId = objId;
		_degree = degree;
		_side = side;
		_speed = speed;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_charId);
		writeD(_degree);
		writeD(_side);
		writeD(_speed);
	}
}