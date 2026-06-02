package l2p.gameserver.serverpackets;

public class ExEventMatchMessage extends L2GameServerPacket
{
	public static final ExEventMatchMessage FINISH = new ExEventMatchMessage(1);
	public static final ExEventMatchMessage START = new ExEventMatchMessage(2);
	public static final ExEventMatchMessage GAMEOVER = new ExEventMatchMessage(3);
	public static final ExEventMatchMessage COUNT1 = new ExEventMatchMessage(4);
	public static final ExEventMatchMessage COUNT2 = new ExEventMatchMessage(5);
	public static final ExEventMatchMessage COUNT3 = new ExEventMatchMessage(6);
	public static final ExEventMatchMessage COUNT4 = new ExEventMatchMessage(7);
	public static final ExEventMatchMessage COUNT5 = new ExEventMatchMessage(8);

	private int _type;
	private String _message;

	public ExEventMatchMessage(int type)
	{
		_type = type;
		_message = "";
	}

	public ExEventMatchMessage(String message)
	{
		_type = 0;
		_message = message;
	}

	@Override
	protected void writeImpl()
	{
		writeC(_type);
		writeS(_message);
	}

	@Override
	protected void writeImplIT()
	{
		writeC(_type);
		writeS(_message);
	}
}