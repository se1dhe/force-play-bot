package l2p.gameserver.serverpackets;

import l2p.commons.util.GArray;
import l2p.gameserver.utils.Location;

import java.util.Arrays;
import java.util.List;

public class ExCursedWeaponLocation extends L2GameServerPacket
{
	private GArray<CursedWeaponInfo> _cursedWeaponInfo;

	public ExCursedWeaponLocation(GArray<CursedWeaponInfo> cursedWeaponInfo)
	{
		_cursedWeaponInfo = cursedWeaponInfo;
	}

	@Override
	protected final void writeImpl()
	{
		if(!_cursedWeaponInfo.isEmpty())
		{
			writeD(_cursedWeaponInfo.size());
			for(CursedWeaponInfo w : _cursedWeaponInfo)
			{
				writeD(w._id);
				writeD(w._status);

				writeD(w._pos.x);
				writeD(w._pos.y);
				writeD(w._pos.z);
			}
		}
		else
		{
			writeD(0);
			writeD(0);
		}
	}

	public static class CursedWeaponInfo
	{
		public Location _pos;
		public int _id;
		public int _status;

		public CursedWeaponInfo(Location p, int ID, int status)
		{
			_pos = p;
			_id = ID;
			_status = status;
		}
	}
}