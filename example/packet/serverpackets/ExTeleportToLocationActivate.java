package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Object;

/**
 * @author Bonux
 **/
public class ExTeleportToLocationActivate extends L2GameServerPacket
{
	private int _targetId;
	private int _x;
	private int _y;
	private int _z;
	private int _h;

	public ExTeleportToLocationActivate(L2Object cha, int x, int y, int z)
	{
		_targetId = cha.getObjectId();
		_x = x;
		_y = y;
		_z = z;
		_h = cha.getHeading();
	}

	public ExTeleportToLocationActivate(L2Object cha, int x, int y, int z, int h)
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
		writeD(_z);
		writeD(0x00); //IsValidation
		writeD(_h);
		writeD(0); // ??? 0
	}

	@Override
	protected boolean canWriteIT()
	{
		return false;
	}
}