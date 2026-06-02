package l2p.gameserver.serverpackets;

public class ExDuelStart extends L2GameServerPacket
{
	int _duelType;

	public ExDuelStart(int duelType)
	{
		_duelType = duelType;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_duelType); // неизвестный, возможно тип дуэли.
	}
}