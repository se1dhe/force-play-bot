package l2p.gameserver.serverpackets;

public class ExPutCommissionResultForVariationMake extends L2GameServerPacket
{
	private int _gemstoneObjId;
	private int _unk1;
	private int _gemstoneCount;
	private int _unk2;
	private int _unk3;

	public ExPutCommissionResultForVariationMake(int gemstoneObjId, int count)
	{
		_gemstoneObjId = gemstoneObjId;
		_unk1 = 1;
		_gemstoneCount = count;
		_unk2 = 1;
		_unk3 = 1;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_gemstoneObjId);
		writeD(_unk1);
		writeQ(_gemstoneCount);
		writeQ(_unk2);
		writeD(_unk3);
	}

	@Override
	protected void writeImplIT()
	{
		writeD(_gemstoneObjId);
		writeD(_unk1);
		writeD(_gemstoneCount);
		writeD(_unk2);
		writeD(_unk3);
	}
}