package l2p.gameserver.serverpackets;

public class AutoAttackStart extends L2GameServerPacket
{
	private int _targetId;

	public AutoAttackStart(int targetId)
	{
		_targetId = targetId;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_targetId);
	}
}