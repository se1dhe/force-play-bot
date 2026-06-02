package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Summon;

public class SetSummonRemainTime extends L2GameServerPacket
{
	private final int _maxFed;
	private final int _curFed;

	public SetSummonRemainTime(L2Summon summon)
	{
		_curFed = summon.getCurrentFed();
		_maxFed = summon.getMaxMeal();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_maxFed);
		writeD(_curFed);
	}
}