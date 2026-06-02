package l2p.gameserver.serverpackets;

import l2p.gameserver.network.ServerPacketOpcodes;

import java.util.ArrayList;
import java.util.List;

public final class ExAcquirableSkillListByClass extends L2GameServerPacket
{
	private List<Skill> _skills;
	private SkillType _skillType;
	
	private static class Skill
	{
		public int id;
		public int nextLevel;
		public int maxLevel;
		public int spCost;
		public int requirements;
		
		public Skill(int pId, int pNextLevel, int pMaxLevel, int pSpCost, int pRequirements)
		{
			id = pId;
			nextLevel = pNextLevel;
			maxLevel = pMaxLevel;
			spCost = pSpCost;
			requirements = pRequirements;
		}
	}
	
	public static enum SkillType
	{
		Usual,
		Fishing,
		Clan
	}
	
	public ExAcquirableSkillListByClass(SkillType type)
	{
		_skillType = type;
	}
	
	public void addSkill(int id, int nextLevel, int maxLevel, int spCost, int requirements)
	{
		if(_skills == null)
			_skills = new ArrayList<Skill>();
		_skills.add(new Skill(id, nextLevel, maxLevel, spCost, requirements));
	}
	
	@Override
	protected final void writeImpl()
	{
		writeH(_skillType.ordinal());
		writeH(_skills.size());
		for(Skill temp : _skills)
		{
			writeD(temp.id);
			writeH(temp.nextLevel);
			writeH(temp.maxLevel);
			writeC(temp.requirements);
			writeQ(temp.spCost);
			writeC(0x01); // UNK
		}
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_skillType.ordinal());
		writeD(_skills.size());
		for(Skill temp : _skills)
		{
			writeD(temp.id);
			writeD(temp.nextLevel);
			writeD(temp.maxLevel);
			writeD(temp.spCost);
			writeD(temp.requirements);
		}
	}

	@Override
	protected ServerPacketOpcodes getOpcodes()
	{
		if(isIT())
			return ServerPacketOpcodes.AcquireSkillList; // TODO [V] - нужен такой класс реализовать?
		return super.getOpcodes();
	}
}