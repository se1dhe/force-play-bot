package l2p.gameserver.serverpackets;

public class ExUseSharedGroupItem extends L2GameServerPacket
{
	private int _itemId, _grpId, _remainedTime, _totalTime;

	public ExUseSharedGroupItem(int itemId, int grpId, int remainedTime, int totalTime)
	{
		_itemId = itemId;
		_grpId = grpId;
		_remainedTime = remainedTime / 1000;
		_totalTime = totalTime / 1000;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_itemId);
		writeD(_grpId);
		writeD(_remainedTime);
		writeD(_totalTime);
	}
}