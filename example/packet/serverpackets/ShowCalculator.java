package l2p.gameserver.serverpackets;

public class ShowCalculator extends L2GameServerPacket
{
	private int _calculatorId;

	public ShowCalculator(int calculatorId)
	{
		_calculatorId = calculatorId;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_calculatorId);
	}
}