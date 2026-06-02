package l2p.gameserver.serverpackets;

public class ExPutEnchantSupportItemResult extends L2GameServerPacket
{
	public static final L2GameServerPacket FAIL = new ExPutEnchantSupportItemResult(0);
	public static final L2GameServerPacket SUCCESS = new ExPutEnchantSupportItemResult(1);

	private int _result;

	public ExPutEnchantSupportItemResult(int result)
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