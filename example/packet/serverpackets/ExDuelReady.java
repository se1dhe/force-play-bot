package l2p.gameserver.serverpackets;

public class ExDuelReady extends L2GameServerPacket
{
	private int _unk1;

	public ExDuelReady(int unk1)
	{
		_unk1 = unk1;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_unk1);
	}
}