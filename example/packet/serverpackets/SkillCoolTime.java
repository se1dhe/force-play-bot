package l2p.gameserver.serverpackets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Skill;
import l2p.gameserver.skills.TimeStamp;

public class SkillCoolTime extends L2GameServerPacket
{
	private List<Skill> _list = Collections.emptyList();

	public SkillCoolTime(L2Player player)
	{
		Collection<TimeStamp> list = player.getSharedGroupReuses();
		_list = new ArrayList<Skill>(list.size());
		for(TimeStamp stamp : list)
		{
			if(!stamp.hasNotPassed())
				continue;
			int reuseCurrent = (int) Math.round(stamp.getReuseCurrent() / 1000.);
			if(reuseCurrent >= 1)
			{
				if(stamp.getGroup() > 0)
				{
					for(L2Skill gsk : player.getGSkills(stamp.getGroup()))
					{
						Skill sk = new Skill();
						sk.skillId = gsk.getId();
						sk.level = gsk.getLevel();
						sk.reuseBase = (int) Math.round(stamp.getReuseBasic() / 1000.);
						sk.reuseCurrent = reuseCurrent;
						_list.add(sk);
					}
				}
				else
				{
					L2Skill skill = player.getKnownSkill(stamp.getId());
					if(skill == null)
						continue;
					Skill sk = new Skill();
					sk.skillId = skill.getId();
					sk.level = skill.getLevel();
					sk.reuseBase = (int) Math.round(stamp.getReuseBasic() / 1000.);
					sk.reuseCurrent = reuseCurrent;
					_list.add(sk);
				}
			}
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_list.size()); //Size of list
		for(int i = 0; i < _list.size(); i++)
		{
			Skill sk = _list.get(i);
			writeD(sk.skillId); //Skill Id
			writeD(sk.level); //Skill Level
			writeD(sk.reuseBase); //Total reuse delay, seconds
			writeD(sk.reuseCurrent); //Time remaining, seconds
		}
	}

	private static class Skill
	{
		public int skillId;
		public int level;
		public int reuseBase;
		public int reuseCurrent;
	}
}