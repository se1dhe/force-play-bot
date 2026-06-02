package l2p.gameserver.serverpackets;

import l2p.gameserver.model.instances.L2DoorInstance;

public class DoorStatusUpdate extends L2GameServerPacket
{
	private int obj_id, door_id, _opened, dmg, isenemy, curHp, maxHp;

	public DoorStatusUpdate(L2DoorInstance door, boolean enemy)
	{
		obj_id = door.getObjectId();
		door_id = door.getDoorId();
		_opened = door.isOpen() ? 0 : 1;
		dmg = door.getDamage();
		isenemy = enemy ? 1 : 0;
		curHp = (int) door.getCurrentHp();
		maxHp = door.getMaxHp();
	}

	@Override
	protected boolean canWriteIT()
	{
		return true;
	}

	@Override
	protected boolean canWrite()
	{
		return false;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(obj_id);
		writeD(_opened);
		writeD(dmg);
		writeD(isenemy);
		writeD(door_id);
		writeD(maxHp);
		writeD(curHp);
	}
}