package l2p.gameserver.serverpackets;

import l2p.gameserver.Config;
import l2p.gameserver.model.L2Object;

public class TeleportToLocation extends L2GameServerPacket
{
	private int _targetId;
	private int _x;
	private int _y;
	private int _z;
	private int _h;

	public TeleportToLocation(L2Object cha, int x, int y, int z)
	{
		_targetId = cha.getObjectId();
		_x = x;
		_y = y;
		_z = z;
		_h = cha.getHeading();
	}

	public TeleportToLocation(L2Object cha, int x, int y, int z, int h)
	{
		_targetId = cha.getObjectId();
		_x = x;
		_y = y;
		_z = z;
		_h = h;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_targetId);
		writeD(_x);
		writeD(_y);
		writeD(_z + Config.CLIENT_Z_SHIFT);
		writeD(0x00); //IsValidation
		writeD(_h);
		writeD(0); // ??? 0
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_targetId);
		writeD(_x);
		writeD(_y);
		writeD(_z + Config.CLIENT_Z_SHIFT);
	}
}