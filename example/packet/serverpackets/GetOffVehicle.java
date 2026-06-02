package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.entity.Vehicle;

public class GetOffVehicle extends L2GameServerPacket
{
	private int _x;
	private int _y;
	private int _z;
	private int char_obj_id;
	private int boat_obj_id;

	public GetOffVehicle(L2Player activeChar, Vehicle boat, int x, int y, int z)
	{
		_x = x;
		_y = y;
		_z = z;
		char_obj_id = activeChar.getObjectId();
		boat_obj_id = boat.getObjectId();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(char_obj_id);
		writeD(boat_obj_id);
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
}