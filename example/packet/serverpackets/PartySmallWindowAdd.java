package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Player;

public class PartySmallWindowAdd extends L2GameServerPacket
{
	//dddSdddddddddd
	private int member_obj_id, member_level, member_class_id;
	private int member_curHp, member_maxHp, member_curCp, member_maxCp, member_curMp, member_maxMp;
	private String member_name;
	private final int _leaderId, _distribution;
	private boolean cond = true;

	public PartySmallWindowAdd(L2Player member, int leaderId, int distribution)
	{
		_leaderId = leaderId;
		_distribution = distribution;
		member_obj_id = member.getObjectId();
		member_name = member.getName();
		member_curCp = (int) member.getCurrentCp();
		member_maxCp = member.getMaxCp();
		member_curHp = (int) member.getCurrentHp();
		member_maxHp = member.getMaxHp();
		member_curMp = (int) member.getCurrentMp();
		member_maxMp = member.getMaxMp();
		member_level = member.getLevel();
		member_class_id = member.getClassId().getId();
		cond = false;
	}

	@Override
	protected boolean canWrite()
	{
		return !cond;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_leaderId);
		writeD(_distribution);
		writeD(member_obj_id);
		writeS(member_name);
		writeD(member_curCp);
		writeD(member_maxCp);
		writeD(member_curHp);
		writeD(member_maxHp);
		writeD(member_curMp);
		writeD(member_maxMp);
		writeD(0x00);
		writeC(member_level);
		writeH(member_class_id);
		writeC(0);
		writeH(0);
	}

	@Override
	protected boolean canWriteIT()
	{
		return !cond;
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_leaderId);
		writeD(_distribution);
		writeD(member_obj_id);
		writeS(member_name);
		writeD(member_curCp);
		writeD(member_maxCp);
		writeD(member_curHp);
		writeD(member_maxHp);
		writeD(member_curMp);
		writeD(member_maxMp);
		writeD(member_level);
		writeD(member_class_id);
		writeD(0);//writeD(0x01); ??
		writeD(0);
	}
}