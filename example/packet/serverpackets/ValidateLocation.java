package l2p.gameserver.serverpackets;

import l2p.gameserver.Config;
import l2p.gameserver.model.L2Character;
import l2p.gameserver.utils.Location;

public class ValidateLocation extends L2GameServerPacket
{
	private int _chaObjId;
	private Location _loc;

	public ValidateLocation(L2Character cha)
	{
		_chaObjId = cha.getObjectId();
		_loc = cha.getLoc();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_chaObjId);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z + Config.CLIENT_Z_SHIFT);
		writeD(_loc.h);
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_chaObjId);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z + Config.CLIENT_Z_SHIFT);
		writeD(_loc.h);
	}
}