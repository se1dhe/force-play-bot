package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Character;

public class ChangeWaitType extends L2GameServerPacket
{
	private int _objectId;
	private int _moveType;
	private int _x;
	private int _y;
	private int _z;
	public static final int WT_SITTING = 0;
	public static final int WT_STANDING = 1;
	public static final int WT_START_FAKEDEATH = 2;
	public static final int WT_STOP_FAKEDEATH = 3;

	public ChangeWaitType(L2Character cha, int newMoveType)
	{
		_objectId = cha.getObjectId();
		_moveType = newMoveType;
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_objectId);
		writeD(_moveType);
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
}