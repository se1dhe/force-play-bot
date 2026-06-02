package l2p.gameserver.serverpackets;

import java.util.ArrayList;
import java.util.List;

public class ExEnchantSkillInfo extends L2GameServerPacket
{
	private class EnchantSkill
	{
		public int _id;
		public int _count;
		public int _type;
		public int _unk;

		EnchantSkill(int type, int id, int count, int unk)
		{
			_id = id;
			_type = type;
			_count = count;
			_unk = unk;
		}
	}

	private List<EnchantSkill> _skill;
	private int _id;
	private int _level;
	private int _spCost;
	private long _xpCost;
	private int _rate;

	public ExEnchantSkillInfo(int id, int level, int spCost, long xpCost, int rate)
	{
		_skill = new ArrayList<EnchantSkill>();
		_id = id;
		_level = level;
		_spCost = spCost;
		_xpCost = xpCost;
		_rate = rate;
	}

	public void addRequirement(int type, int id, int count, int unk)
	{
		_skill.add(new EnchantSkill(type, id, count, unk));
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
		writeD(_id);
		writeD(_level);
		writeD(_spCost);
		writeQ(_xpCost);
		writeD(_rate);
		writeD(_skill.size());
		for(EnchantSkill temp : _skill)
		{
			writeD(temp._type);
			writeD(temp._id);
			writeD(temp._count);
			writeD(temp._unk);
		}
	}
}