package l2p.gameserver.serverpackets;

public class AutoAttackStop extends L2GameServerPacket
{
    private int _targetId;

	public AutoAttackStop(int targetId)
	{
		_targetId = targetId;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_targetId);
	}
}