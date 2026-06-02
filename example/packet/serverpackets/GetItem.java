package l2p.gameserver.serverpackets;

import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.utils.Location;

public class GetItem extends L2GameServerPacket
{
	private int _playerId;
	private int _itemObjId;
	private Location _loc;

	public GetItem(L2ItemInstance item, int playerId)
	{
		_itemObjId = item.getObjectId();
		_loc = item.getLoc();
		_playerId = playerId;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_playerId);
		writeD(_itemObjId);
		writeD(_loc.getX());
		writeD(_loc.getY());
		writeD(_loc.getZ());
	}
}