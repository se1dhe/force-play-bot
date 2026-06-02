package l2p.gameserver.serverpackets;

import l2p.gameserver.Config;
import l2p.gameserver.model.instances.L2ItemInstance;

public class SpawnItem extends L2GameServerPacket
{
	private int _objectId;
	private int _itemId;
	private int _x, _y, _z;
	private int _stackable;
	private int _count;
	private final int _enchantLevel;
	private final boolean _augmented;
	private final int _ensoulCount;

	public SpawnItem(L2ItemInstance item)
	{
		_objectId = item.getObjectId();
		_itemId = item.getItemId();
		_x = item.getX();
		_y = item.getY();
		_z = item.getZ();
		_stackable = item.isStackable() ? 0x01 : 0x00;
		_count = item.getIntegerLimitedCount();
		_enchantLevel = item.getEnchantLevel();
		_augmented = item.isAugmented();
		_ensoulCount = 0x00;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_objectId);
		writeD(_itemId);

		writeD(_x);
		writeD(_y);
		writeD(_z + Config.CLIENT_Z_SHIFT);
		writeD(_stackable);
		writeQ(_count);
		writeD(0x00); //c2
		writeC(_enchantLevel);
		writeC(_augmented);
		writeC(_ensoulCount);
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_objectId);
		writeD(_itemId);

		writeD(_x);
		writeD(_y);
		writeD(_z + Config.CLIENT_Z_SHIFT);
		// only show item count if it is a stackable item
		writeD(_stackable);
		writeD(_count);
		writeD(0x00); //c2
	}
}