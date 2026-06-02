package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Player;

public class FriendAddRequestResult extends L2GameServerPacket
{
	private final int _result;
	private final int _charId;
	private final String _charName;
	private final int _isOnline;
	private final int _charObjectId;
	private final int _charLevel;
	private final int _charClassId;

	public FriendAddRequestResult(L2Player player, int result)
	{
		_result = result;
		_charId = player.getObjectId();
		_charName = player.getName();
		_isOnline = player.isOnline() ? 1 : 0;
		_charObjectId = player.getObjectId();
		_charLevel = player.getLevel();
		_charClassId = player.getActiveClassId();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_result);
		writeD(_charId);
		writeS(_charName);
		writeD(_isOnline);
		writeD(_charObjectId);
		writeD(_charLevel);
		writeD(_charClassId);
		writeH(0x00); // Always 0 on retail
	}

	// TODO [V] - так?
	@Override
	protected boolean canWriteIT()
	{
		return false;
	}
}
