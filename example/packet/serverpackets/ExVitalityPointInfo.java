package l2p.gameserver.serverpackets;

public class ExVitalityPointInfo extends L2GameServerPacket
{
	private final int _vitality;

	public ExVitalityPointInfo(int vitality)
	{
		_vitality = vitality;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_vitality);
	}

	@Override
	protected boolean canWriteIT()
	{
		return false;
	}
}