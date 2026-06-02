package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Player;

public class CharSit extends L2GameServerPacket
{
	private L2Player _activeChar;
	private int _staticObjectId;

	public CharSit(L2Player player, int staticObjectId)
	{
		_activeChar = player;
		_staticObjectId = staticObjectId;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_activeChar.getObjectId());
		writeD(_staticObjectId);
	}
}