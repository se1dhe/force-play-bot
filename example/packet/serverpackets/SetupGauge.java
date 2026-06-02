package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Character;

public class SetupGauge extends L2GameServerPacket
{
	public static final int BLUE = 0;
	public static final int RED = 1;
	public static final int CYAN = 2;
	public static final int GREEN = 3;
	private int _charId;
	private int _color;
	private int _time;
	private int _timeLeft;

	public SetupGauge(L2Character cha, int color, int time)
	{
		_charId = cha.getObjectId();
		_color = color; // color  0-blue   1-red  2-cyan  3-green
		_time = time;
		_timeLeft = time;
	}

	public SetupGauge(L2Character cha, int color, int time, int timeLeft)
	{
		_charId = cha.getObjectId();
		_color = color; // color  0-blue   1-red  2-cyan  3-green
		_time = time;
		_timeLeft = timeLeft;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_charId);
		writeD(_color);
		writeD(_timeLeft);
		writeD(_time);
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_color);
		writeD(_timeLeft);
		writeD(_time); //c2
	}
}