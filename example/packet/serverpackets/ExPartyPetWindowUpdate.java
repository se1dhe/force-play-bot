package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Summon;

public class ExPartyPetWindowUpdate extends L2GameServerPacket
{
	private int owner_obj_id, npc_id, _type, curHp, maxHp, curMp, maxMp, level;
	private int obj_id = 0;
	private String _name;

	public ExPartyPetWindowUpdate(L2Summon servitor)
	{
		obj_id = servitor.getObjectId();
		owner_obj_id = servitor.getPlayer().getObjectId();
		npc_id = servitor.getTemplate().npcId + 1000000;
		_type = servitor.getSummonType();
		_name = servitor.getName();
		curHp = (int) servitor.getCurrentHp();
		maxHp = servitor.getMaxHp();
		curMp = (int) servitor.getCurrentMp();
		maxMp = servitor.getMaxMp();
		level = servitor.getLevel();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(obj_id);
		writeD(npc_id);
		writeD(_type);
		writeD(owner_obj_id);
		writeS(_name);
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