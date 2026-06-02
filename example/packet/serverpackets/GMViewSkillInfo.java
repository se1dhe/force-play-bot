package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Skill;
import l2p.gameserver.tables.SkillTable;

import java.util.Collection;

public class GMViewSkillInfo extends L2GameServerPacket
{
	private String char_name;
	private Collection<L2Skill> _skills;
	private L2Player _targetChar;

	public GMViewSkillInfo(L2Player cha)
	{
		char_name = cha.getName();
		_skills = cha.getAllSkills();
		_targetChar = cha;
	}

	// TODO [V] - так?
	@Override
	protected final void writeImpl()
	{
		writeS(char_name);
		writeD(_skills.size());
		for(L2Skill skill : _skills)
		{
			if(skill.isHideList())
				continue; // fake skills to change base stats

			writeD(skill.isLikePassive() ? 1 : 0);
			writeD(skill.getDisplayLevel());
			writeD(skill.getDisplayId());
			writeD(skill.getId());
			writeD(0x00);
			writeC(_targetChar.isUnActiveSkill(skill.getId()) ? 1 : 0);
			writeC(SkillTable.getInstance().getMaxLevel(skill.getId()) > 100 ? 1 : 0);
		}
	}

	@Override
	protected final void writeImplIT()
	{
		writeS(char_name);
		writeD(_skills.size());
		for(L2Skill skill : _skills)
		{
			if(skill.isHideList())
				continue; // fake skills to change base stats

			writeD(skill.isLikePassive() ? 1 : 0);
			writeD(skill.getDisplayLevel());
			writeD(skill.getId());
			writeC(_targetChar.isUnActiveSkill(skill.getId()) ? 1 : 0);
		}
	}
}