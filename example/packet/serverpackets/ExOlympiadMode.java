package l2p.gameserver.serverpackets;

public class ExOlympiadMode extends L2GameServerPacket
{
	private int _mode;

	public ExOlympiadMode(int mode)
	{
		_mode = mode;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(_mode);
	}

	@Override
	protected final void writeImplIT()
	{
		writeC(_mode);
	}
}