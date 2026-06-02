package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.serverpackets.updatetype.PartySmallWindowUpdateType;

public class PartySmallWindowUpdate extends L2GameServerPacket
{
	private int obj_id, class_id, level;
	private int curCp, maxCp, curHp, maxHp, curMp, maxMp;
	private String obj_name;
	private boolean cond = true;
	private int _flags = 0;

	public PartySmallWindowUpdate(L2Player member, boolean addAllFlags)
	{
		obj_id = member.getObjectId();
		obj_name = member.getName();
		curCp = (int) member.getCurrentCp();
		maxCp = member.getMaxCp();
		curHp = (int) member.getCurrentHp();
		maxHp = member.getMaxHp();
		curMp = (int) member.getCurrentMp();
		maxMp = member.getMaxMp();
		level = member.getLevel();
		class_id = member.getClassId().getId();
		cond = false;

		if(addAllFlags)
		{
			for(PartySmallWindowUpdateType type : PartySmallWindowUpdateType.values())
				addUpdateType(type);
		}
	}

	public PartySmallWindowUpdate(L2Player member)
	{
		this(member, true);
	}

	public void addUpdateType(PartySmallWindowUpdateType type)
	{
		_flags |= type.getMask();
	}

	@Override
	protected boolean canWrite()
	{
		return !cond;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(obj_id);
		writeH(_flags);
		if(containsMask(_flags, PartySmallWindowUpdateType.CURRENT_CP))
			writeD(curCp); // c4

		if(containsMask(_flags, PartySmallWindowUpdateType.MAX_CP))
			writeD(maxCp); // c4

		if(containsMask(_flags, PartySmallWindowUpdateType.CURRENT_HP))
			writeD(curHp);

		if(containsMask(_flags, PartySmallWindowUpdateType.MAX_HP))
			writeD(maxHp);

		if(containsMask(_flags, PartySmallWindowUpdateType.CURRENT_MP))
			writeD(curMp);

		if(containsMask(_flags, PartySmallWindowUpdateType.MAX_MP))
			writeD(maxMp);

		if(containsMask(_flags, PartySmallWindowUpdateType.LEVEL))
			writeC(level);

		if(containsMask(_flags, PartySmallWindowUpdateType.CLASS_ID))
			writeH(class_id);

		if(containsMask(_flags, PartySmallWindowUpdateType.PARTY_SUBSTITUTE))
			writeC(0x00);

		if(containsMask(_flags, PartySmallWindowUpdateType.VITALITY_POINTS))
			writeD(0x00);
	}

	@Override
	protected boolean canWriteIT()
	{
		return !cond;
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(obj_id);
		writeS(obj_name);
		writeD(curCp);
		writeD(maxCp);
		writeD(curHp);
		writeD(maxHp);
		writeD(curMp);
		writeD(maxMp);
		writeD(level);
		writeD(class_id);
	}
}