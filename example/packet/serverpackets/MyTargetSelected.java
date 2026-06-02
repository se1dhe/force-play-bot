package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2StaticObjectInstance;

public class MyTargetSelected extends L2GameServerPacket
{
	private final int _objectId;
	private final int _color;

	public MyTargetSelected(L2Player player, L2Character target)
	{
		_objectId = target.getObjectId();
		_color = target.isSummon() || (target.isNpc() && !(target instanceof L2StaticObjectInstance)) ? player.getLevel() - target.getLevel() : 0;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(/*_success ? */0x01/* : 0x00*/);
		writeD(_objectId);
		writeH(_color);
		writeD(/*_actionMenu ? 0x03 : */0x00);
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_objectId);
		writeH(_color);
	}
}