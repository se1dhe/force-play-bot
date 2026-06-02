package l2p.gameserver.serverpackets;

public class ActionFail extends L2GameServerPacket
{
	public static final ActionFail STATIC = new ActionFail(0);
	public static final ActionFail STATIC_MOVE = new ActionFail(1);

	private final int id;

	public ActionFail(int id)
	{
		this.id = id;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(id);
	}

	@Override
	protected final void writeImplIT()
	{
		//
	}
}