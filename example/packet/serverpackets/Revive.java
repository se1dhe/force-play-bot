package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Object;

public class Revive extends L2GameServerPacket
{
	private int _objectId;

	public Revive(L2Object obj)
	{
		_objectId = obj.getObjectId();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_objectId);
	}
}