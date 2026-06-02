package l2p.gameserver.serverpackets;

import l2p.gameserver.model.instances.L2DoorInstance;

public class DoorInfo extends L2GameServerPacket
{
	private int obj_id;
	private int door_id;
	private int view_hp;

	public DoorInfo(L2DoorInstance door)
	{
		obj_id = door.getObjectId();
		door_id = door.getDoorId();
		view_hp = door.isHPVisible() ? 1 : 0;
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
		writeD(door_id);
		writeD(view_hp); // отображать ли хп у двери или стены
	}
}