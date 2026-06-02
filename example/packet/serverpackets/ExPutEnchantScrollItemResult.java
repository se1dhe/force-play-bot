package l2p.gameserver.serverpackets;

/**
 * @author Bonux
 **/
public class ExPutEnchantScrollItemResult extends L2GameServerPacket
{
	public static final L2GameServerPacket FAIL = new ExPutEnchantScrollItemResult(0x00);

	private int _result;

	public ExPutEnchantScrollItemResult(int result)
	{
		_result = result;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_result);
	}

	@Override
	protected boolean canWriteIT()
	{
		return false;
	}
}