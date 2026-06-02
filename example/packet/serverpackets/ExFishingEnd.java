package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Player;

public class ExFishingEnd extends L2GameServerPacket
{
	private int _charId;
	private boolean _win;

	public ExFishingEnd(L2Player character, boolean win)
	{
		_charId = character.getObjectId();
		_win = win;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_charId);
		writeC(_win ? 1 : 0);
	}
}