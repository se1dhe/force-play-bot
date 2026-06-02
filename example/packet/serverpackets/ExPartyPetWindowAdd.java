package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Summon;

public class ExPartyPetWindowAdd extends L2GameServerPacket
{
	private final int ownerId, npcId, type, curHp, maxHp, curMp, maxMp, level;
	private final int summonId;
	private final String name;

	public ExPartyPetWindowAdd(L2Summon servitor)
	{
		summonId = servitor.getObjectId();
		ownerId = servitor.getPlayer().getObjectId();
		npcId = servitor.getTemplate().npcId + 1000000;
		type = servitor.getSummonType();
		name = servitor.getName();
		curHp = (int) servitor.getCurrentHp();
		maxHp = servitor.getMaxHp();
		curMp = (int) servitor.getCurrentMp();
		maxMp = servitor.getMaxMp();
		level = servitor.getLevel();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(summonId);
		writeD(npcId);
		writeD(type);
		writeD(ownerId);
		writeS(name);
		writeD(curHp);
		writeD(maxHp);
		writeD(curMp);
		writeD(maxMp);
		writeD(level);
	}

	@Override
	protected boolean canWriteIT()
	{
		return false;
	}
}