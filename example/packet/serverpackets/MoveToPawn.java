package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2Object;

public class MoveToPawn extends L2GameServerPacket
{
	private int _charObjId, _targetId, _minRange;
	private int _x, _y, _z, _tx, _ty, _tz;

	public MoveToPawn(L2Character cha, L2Object target, int minRange)
	{
		if(cha == target)
		{
			_charObjId = 0;
			return;
		}
		if(target == null)
		{
			_charObjId = 0;
			return;
		}
		_charObjId = cha.getObjectId();
		_targetId = target.getObjectId();
		_minRange = minRange;
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_tx = target.getX();
		_ty = target.getY();
		_tz = target.getZ();
	}

	@Override
	protected boolean canWrite()
	{
		if(_charObjId == 0)
			return false;
		return true;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_charObjId);
		writeD(_targetId);
		writeD(_minRange);

		writeD(_x);
		writeD(_y);
		writeD(_z);

		writeD(_tx);
		writeD(_ty);
		writeD(_tz);
	}

	@Override
	protected boolean canWriteIT()
	{
		if(_charObjId == 0)
			return false;
		return true;
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_charObjId);
		writeD(_targetId);
		writeD(_minRange);

		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
}