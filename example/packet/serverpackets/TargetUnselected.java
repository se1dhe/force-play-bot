package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Character;
import l2p.gameserver.utils.Location;

public class TargetUnselected extends L2GameServerPacket
{
	private int _targetId;
	private Location _loc;

	public TargetUnselected(L2Character cha)
	{
		_targetId = cha.getObjectId();
		_loc = cha.getLoc();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_targetId);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
		writeD(0x00); // иногда бывает 1
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_targetId);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
	}
}