package l2p.gameserver.serverpackets;

public class ExDuelEnd extends L2GameServerPacket
{
	int _duelType;

	public ExDuelEnd(int duelType)
	{
		_duelType = duelType;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_duelType);
	}
}