package l2p.gameserver.serverpackets;

// TODO [V] - реализованы только в GK
public class ExAskCoupleAction extends L2GameServerPacket
{
	private int _objectId, _socialId;

	public ExAskCoupleAction(int objectId, int socialId)
	{
		_objectId = objectId;
		_socialId = socialId;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_socialId);
		writeD(_objectId);
	}

	@Override
	protected boolean canWriteIT()
	{
		return false;
	}
}