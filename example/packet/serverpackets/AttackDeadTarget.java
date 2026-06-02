package l2p.gameserver.serverpackets;

public class AttackDeadTarget extends L2GameServerPacket
{
	private int _objectId;

	public AttackDeadTarget(int objectId)
	{
		_objectId = objectId;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_objectId);
	}
}