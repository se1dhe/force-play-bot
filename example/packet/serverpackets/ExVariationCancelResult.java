package l2p.gameserver.serverpackets;

public class ExVariationCancelResult extends L2GameServerPacket
{
	private int _unk1;
	private int _unk2;

	public ExVariationCancelResult(int result)
	{
		_unk1 = 1;
		_unk2 = result;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_unk2);
		writeD(_unk1);
	}

	@Override
	protected void writeImplIT()
	{
		writeD(_unk1);
		writeD(_unk2);
	}
}