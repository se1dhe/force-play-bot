package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Player;

/**
 * @reworked by Bonux
 **/
public class ExUserInfoInvenWeight extends L2GameServerPacket
{
	private final int _objectId;
	private final int _currentLoad;
	private final int _maxLoad;

	public ExUserInfoInvenWeight(L2Player player)
	{
		_objectId = player.getObjectId();
		_currentLoad = player.getCurrentLoad();
		_maxLoad = player.getMaxLoad();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_objectId);
		writeD(_currentLoad);
		writeD(_maxLoad);
	}

	@Override
	protected boolean canWriteIT()
	{
		return false;
	}
}