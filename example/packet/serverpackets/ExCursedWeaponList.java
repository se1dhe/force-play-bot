package l2p.gameserver.serverpackets;

import l2p.commons.util.GArray;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.utils.Location;

public class ExCursedWeaponList extends L2GameServerPacket
{
	private Location location;
	private int[] cursedWeapon_ids;

	public ExCursedWeaponList(Location location, GArray<Integer> cursedWeaponIds)
	{
		this.location = location;
		cursedWeapon_ids = new int[cursedWeaponIds.size()];
		for(int i = 0; i < cursedWeaponIds.size(); i++)
			cursedWeapon_ids[i] = cursedWeaponIds.get(i);
	}

	// TODO [V] - так?
	@Override
	protected final void writeImpl()
	{
		writeD(cursedWeapon_ids.length);
		for(int element : cursedWeapon_ids)
		{
			writeD(element);
			writeD(1);
			writeD(location.x);
			writeD(location.y);
			writeD(location.z);
			writeQ(5000);
			writeQ(0);
		}
		cursedWeapon_ids = null;
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(cursedWeapon_ids.length);
		for(int element : cursedWeapon_ids)
			writeD(element);
		cursedWeapon_ids = null;
	}
}