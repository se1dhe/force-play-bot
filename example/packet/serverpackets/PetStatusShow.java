package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Summon;

public class PetStatusShow extends L2GameServerPacket
{
	private int _summonType, _summonObjId;

	public PetStatusShow(L2Summon summon)
	{
		_summonType = summon.getSummonType();
		_summonObjId = summon.getObjectId();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_summonType);
		writeD(_summonObjId);
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_summonType);
	}
}