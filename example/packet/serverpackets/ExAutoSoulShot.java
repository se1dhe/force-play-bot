package l2p.gameserver.serverpackets;

public class ExAutoSoulShot extends L2GameServerPacket
{
	private final int _itemId;
	private final boolean _enabled;
	private final int _type;

	public ExAutoSoulShot(int itemId, boolean enabled, int type)
	{
		_itemId = itemId;
		_enabled = enabled;
		_type = type;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_itemId);
		writeD(_enabled);
		writeD(_type);
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_itemId);
		writeD(_enabled);
	}
}