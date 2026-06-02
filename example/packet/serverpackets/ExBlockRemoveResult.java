package l2p.gameserver.serverpackets;

/**
 * @author Bonux
 **/
public class ExBlockRemoveResult extends L2GameServerPacket
{
	private final boolean _result;
	private final String _blockName;

	public ExBlockRemoveResult(boolean result, String name)
	{
		_result = result;
		_blockName = name;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_result);
		writeS(_blockName);
	}

	@Override
	protected boolean canWriteIT()
	{
		return false;
	}
}