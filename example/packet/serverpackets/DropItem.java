package l2p.gameserver.serverpackets;

import l2p.gameserver.Config;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.utils.Location;

public class DropItem extends L2GameServerPacket
{
	private Location _loc;
	private int _playerId, item_obj_id, item_id, stackable, _count;
	private int _enchantLevel;
	private boolean _augmented;
	private int _ensoulCount;

	public DropItem(L2ItemInstance item, int playerId)
	{
		_playerId = playerId;
		item_obj_id = item.getObjectId();
		item_id = item.getItemId();
		_loc = item.getLoc();
		stackable = item.isStackable() ? 1 : 0;
		_count = item.getIntegerLimitedCount();
		_enchantLevel = item.getEnchantLevel();
		_augmented = item.isAugmented();
		_ensoulCount = 0;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_playerId);
		writeD(item_obj_id);
		writeD(item_id);
		writeD(_loc.getX());
		writeD(_loc.getY());
		writeD(_loc.getZ() + Config.CLIENT_Z_SHIFT);
		writeC(stackable);
		writeQ(_count);
		writeC(1); // unknown
		writeC(_enchantLevel);
		writeC(_augmented);
		writeC(_ensoulCount);
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_playerId);
		writeD(item_obj_id);
		writeD(item_id);
		writeD(_loc.getX());
		writeD(_loc.getY());
		writeD(_loc.getZ() + Config.CLIENT_Z_SHIFT);
		writeD(stackable);
		writeD(_count);
		writeD(1); // unknown
	}
}