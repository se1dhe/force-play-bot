package l2p.gameserver.serverpackets;

/**
 * @author Bonux
 **/
// TODO [V] - если будет нужно, то реализовать
public class ExWorldChatCnt extends L2GameServerPacket
{
	private final int _count;

	public ExWorldChatCnt(int count)
	{
		_count = Math.max(0, count);
	}

	@Override
	protected void writeImpl()
	{
		writeD(_count);
	}

	@Override
	protected boolean canWriteIT()
	{
		return false;
	}
}
