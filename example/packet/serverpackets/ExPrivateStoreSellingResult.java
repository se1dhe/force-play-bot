package l2p.gameserver.serverpackets;

// TODO [V] - реализовать
public class ExPrivateStoreSellingResult extends L2GameServerPacket
{
	private final int _itemObjId;
	private final long _itemCount;
	private final String _buyerName;

	public ExPrivateStoreSellingResult(int itemObjId, long itemCount, String buyerName)
	{
		_itemObjId = itemObjId;
		_itemCount = itemCount;
		_buyerName = buyerName;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_itemObjId);
		writeQ(_itemCount);
		writeS(_buyerName);
	}

	@Override
	protected boolean canWriteIT()
	{
		return false;
	}
}