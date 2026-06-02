package l2p.gameserver.serverpackets;

import l2p.commons.util.GArray;
import l2p.gameserver.model.L2Clan;
import l2p.gameserver.model.L2Skill;

public class PledgeSkillList extends L2GameServerPacket
{
	private GArray<SkillInfo> infos = new GArray<SkillInfo>();
	private GArray<SkillInfo> infosSub = new GArray<SkillInfo>();

	public PledgeSkillList(L2Clan clan)
	{
		for(L2Skill sk : clan.getAllSkills())
			infos.add(new SkillInfo(sk.getId(), sk.getLevel()));
	}

	// TODO [V] - не полное значение
	@Override
	protected final void writeImpl()
	{
		writeD(infos.size());
		writeD(infos.size());
		for(SkillInfo _info : infos)
		{
			writeD(_info._id);
			writeD(_info.level);
		}
		infos.clear();
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(infos.size());
		for(SkillInfo _info : infos)
		{
			writeD(_info._id);
			writeD(_info.level);
		}
		infos.clear();
	}

	static class SkillInfo
	{
		public int _id, level;

		public SkillInfo(int __id, int _level)
		{
			_id = __id;
			level = _level;
		}
	}
}