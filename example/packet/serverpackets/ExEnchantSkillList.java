package l2p.gameserver.serverpackets;

import java.util.ArrayList;

public class ExEnchantSkillList extends L2GameServerPacket
{
	private class Skill
	{
		public int _id;
		public int _nextLevel;
		public long _exp;
		public int _sp;

		Skill(int id, int nextLevel, int sp, long exp)
		{
			_id = id;
			_nextLevel = nextLevel;
			_exp = exp;
			_sp = sp;
		}
	}

	private final ArrayList<Skill> _skills;

	public ExEnchantSkillList()
	{
		_skills = new ArrayList<Skill>();
	}

	public void addSkill(int id, int level, int exp, long sp)
	{
		_skills.add(new Skill(id, level, exp, sp));
	}

	@Override
	protected boolean canWrite()
	{
		return false;
	}

	@Override
	protected final void writeImpl()
	{

	}

	@Override
	protected boolean canWriteIT()
	{
		return true;
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_skills.size());
		for(Skill sk : _skills)
		{
			writeD(sk._id);
			writeD(sk._nextLevel);
			writeD(sk._sp);
			writeQ(sk._exp);
		}
	}
}