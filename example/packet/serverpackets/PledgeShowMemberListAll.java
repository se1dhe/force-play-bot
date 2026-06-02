package l2p.gameserver.serverpackets;

import l2p.commons.util.GArray;
import l2p.gameserver.Config;
import l2p.gameserver.model.L2Clan;
import l2p.gameserver.model.L2ClanMember;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Clan.SubPledge;

public class PledgeShowMemberListAll extends L2GameServerPacket
{
	private L2Clan _clan;
	private int clan_id, clan_crest_id, level, rank, rep, ally_id, ally_crest_id;
	private int _pledgeType, HasCastle, HasHideout, AtWar;
	private String clan_name, leader_name, ally_name;
	private GArray<ClanMemberInfo> infos = new GArray<ClanMemberInfo>();

	public PledgeShowMemberListAll(final L2Clan clan, final L2Player activeChar)
	{
		_clan = clan;
		_pledgeType = 0;
		clan_id = _clan.getClanId();
		clan_name = _clan.getName();
		leader_name = _clan.getLeaderName();
		clan_crest_id = _clan.getCrestId();
		level = _clan.getLevel();
		HasCastle = _clan.getHasCastle();
		HasHideout = _clan.getHasHideout();
		rank = _clan.getRank();
		rep = _clan.getReputationScore();
		ally_id = _clan.getAllyId();
		if(_clan.getAlliance() != null)
		{
			ally_name = _clan.getAlliance().getAllyName();
			ally_crest_id = _clan.getAlliance().getAllyCrestId();
		}
		else
		{
			ally_name = "";
			ally_crest_id = 0;
		}
		AtWar = _clan.isAtWarOrUnderAttack();

		for(SubPledge element : _clan.getAllSubPledges())
			activeChar.sendPacket(new PledgeReceiveSubPledgeCreated(element));

		for(L2ClanMember m : _clan.getMembers())
			if(m.getPledgeType() == _pledgeType)
				infos.add(new ClanMemberInfo(m.getName(), m.getLevel(), m.getClassId(), m.getSex(), m.getObjectId(), m.isOnline(), m.hasSponsor() ? 1 : 0, m.getRace()));

			else
				activeChar.sendPacket(new PledgeShowMemberListAdd(m));

		activeChar.sendPacket(new UserInfo(activeChar)); // light on clan leader buttons
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_pledgeType == 0 ? 0 : 1);
		writeD(clan_id);
		writeD(Config.REQUEST_ID);
		writeD(_pledgeType); //c5 - possibly pledge type?
		writeS(clan_name);
		writeS(leader_name);
		writeD(clan_crest_id); // crest id .. is used again
		writeD(level);
		writeD(HasCastle);
		writeD(0);
		writeD(HasHideout);
		writeD(0);
		writeD(rank);
		writeD(rep);
		writeD(0);
		writeD(0);
		writeD(ally_id);
		writeS(ally_name);
		writeD(ally_crest_id);
		writeD(AtWar);
		writeD(0);
		writeD(infos.size());
		for(ClanMemberInfo _info : infos)
		{
			writeS(_info._name);
			writeD(_info.level);
			writeD(_info.class_id);
			writeD(_info.sex); // Пол (не отображается)
			writeD(_info.race); //writeD(1);
			writeD(_info.online ? _info.obj_id : 0); // 1=online 0=offline
			writeD(_info.has_sponsor);
			writeC(0x00);
		}
		infos.clear();
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_pledgeType == 0 ? 0 : 1);
		writeD(clan_id);
		writeD(_pledgeType); //c5 - possibly pledge type?
		writeS(clan_name);
		writeS(leader_name);
		writeD(clan_crest_id); // crest id .. is used again
		writeD(level);
		writeD(HasCastle);
		writeD(HasHideout);
		writeD(rank);
		writeD(rep);
		writeD(0);
		writeD(0);
		writeD(ally_id);
		writeS(ally_name);
		writeD(ally_crest_id);
		writeD(AtWar);
		writeD(infos.size());
		for(ClanMemberInfo _info : infos)
		{
			writeS(_info._name);
			writeD(_info.level);
			writeD(_info.class_id);
			writeD(_info.sex); // Пол (не отображается)
			writeD(_info.race); //writeD(1);
			writeD(_info.online ? _info.obj_id : 0); // 1=online 0=offline
			writeD(_info.has_sponsor);
		}
		infos.clear();
	}

	static class ClanMemberInfo
	{
		public String _name;
		public int level, class_id, sex, obj_id, has_sponsor, race;
		public boolean online;

		public ClanMemberInfo(String __name, int _level, int _class_id, int _sex, int _obj_id, boolean _online, int _has_sponsor, int _race)
		{
			_name = __name;
			level = _level;
			class_id = _class_id;
			race = _race;
			sex = _sex;
			obj_id = _obj_id;
			online = _online;
			has_sponsor = _has_sponsor;
		}
	}
}