package l2p.gameserver.serverpackets;

import l2p.gameserver.utils.Location;

public class GetOnVehicle extends L2GameServerPacket
{
	private int _x;
	private int _y;
	private int _z;
	private int _char_obj_id;
	private int _boat_obj_id;
	private boolean can_write;

	public GetOnVehicle(int char_obj_id, int boat_obj_id, Location loc)
	{
		if(loc != null)
		{
			_x = loc.x;
			_y = loc.y;
			_z = loc.z;
			_char_obj_id = char_obj_id;
			_boat_obj_id = boat_obj_id;
			can_write = true;
		}
	}

	@Override
	protected boolean canWrite()
	{
		return can_write;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_char_obj_id);
		writeD(_boat_obj_id);
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
}