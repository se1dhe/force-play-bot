package l2p.gameserver.serverpackets;

import l2p.commons.util.GArray;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Skill;
import l2p.gameserver.tables.SkillTree;
import l2p.gameserver.utils.SkillsComparator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SkillList extends L2GameServerPacket
{
	private L2Skill[] _skills;
	private L2Player activeChar;
	private int _learnedSkillId = 0;
	private final List<SkillListEntry> _skillListClassic = new ArrayList<SkillListEntry>();
	private final List<SkillListEntry> _skillListIT = new ArrayList<SkillListEntry>();

	public SkillList(L2Player p)
	{
		GArray<L2Skill> vals = new GArray<L2Skill>();
		for(L2Skill skill : p.getAllSkills())
		{
			if(skill == null || skill.isHideList())
				continue;

			int skillLevel = skill.getDisplayLevel();
			int baseLevel = skillLevel;
			int subLevel = 0;
			if(skillLevel > 100)
			{
				baseLevel = SkillTree.getBaseLevels().get(skill.getId());
				int step = skillLevel % 100;
				subLevel = (1 + step / 40) * 1000 + step % 40;
			}
			vals.add(skill);
			int skillIdClassic = skill.isDisplayedInClassic() ? skill.getDisplayId() : skill.getId();
			boolean isDisabled = p.isUnActiveSkill(skill.getId());
			_skillListClassic.add(new SkillListEntry(skillIdClassic, baseLevel, subLevel, -1, !skill.isActive() && !skill.isToggle(), isDisabled, SkillTree.getInstance().isEnchantable(skill) != 0));
			_skillListIT.add(new SkillListEntry(skill.getDisplayId(), baseLevel, subLevel, -1, !skill.isActive() && !skill.isToggle(), isDisabled, SkillTree.getInstance().isEnchantable(skill) != 0));
		}
		_skills = vals.toArray(new L2Skill[vals.size()]);
		Arrays.sort(_skills, SkillsComparator.getInstance());
		activeChar = p;
	}

	public void setLastLearnedSkillId(int learnedSkillId)
	{
		_learnedSkillId = learnedSkillId;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_skillListClassic.size());
		for(SkillListEntry skill : _skillListClassic)
		{
			writeD(skill.passive ? 1 : 0);
			writeH(skill.level);
			writeH(skill.subLevel);
			writeD(skill.id);
			writeD(skill.reuseDelayGroup);
			writeC(skill.disabled ? 1 : 0);
			writeC(skill.enchanted ? 1 : 0);
		}

		writeD(_learnedSkillId);
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_skills.length);

		for(L2Skill skill : _skills)
		{
			writeD(skill.isLikePassive() ? 1 : 0); // deprecated? клиентом игнорируется
			writeD(skill.getDisplayLevel());
			writeD(skill.getId());
			writeC(activeChar.isUnActiveSkill(skill.getId()) ? 1 : 0); // иконка скилла серая если не 0
		}
	}

	static class SkillListEntry
	{
		public int id;
		public int level;
		public int subLevel;
		public int reuseDelayGroup;
		public boolean passive;
		public boolean disabled;
		public boolean enchanted;

		SkillListEntry(int id, int level, int subLevel, int reuseDelayGroup, boolean passive, boolean disabled, boolean enchanted)
		{
			this.id = id;
			this.level = level;
			this.subLevel = subLevel;
			this.reuseDelayGroup = reuseDelayGroup;
			this.passive = passive;
			this.disabled = disabled;
			this.enchanted = enchanted;
		}
	}
}