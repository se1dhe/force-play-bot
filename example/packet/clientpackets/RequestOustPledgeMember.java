package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.dao.AcademiciansDAO;
import l2p.gameserver.model.L2Clan;
import l2p.gameserver.model.L2ClanMember;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Zone;
import l2p.gameserver.model.entity.events.impl.SiegeEvent;
import l2p.gameserver.model.recruitment.Academician;
import l2p.gameserver.model.recruitment.AcademiciansStorage;
import l2p.gameserver.model.recruitment.AcademyRequest;
import l2p.gameserver.model.recruitment.AcademyRequestStorage;
import l2p.gameserver.serverpackets.PledgeShowMemberListDelete;
import l2p.gameserver.serverpackets.PledgeShowMemberListDeleteAll;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.utils.Log;
import org.apache.commons.lang3.ArrayUtils;

public class RequestOustPledgeMember extends L2GameClientPacket
{
	private String _target;

	@Override
	public void readImpl()
	{
		_target = readS();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();

		if(activeChar == null || !((activeChar.getClanPrivileges() & L2Clan.CP_CL_DISMISS) == L2Clan.CP_CL_DISMISS))
			return;

		L2Clan clan = activeChar.getClan();
		L2ClanMember member = clan.getClanMember(_target);
		if(member == null)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.THE_TARGET_MUST_BE_A_CLAN_MEMBER));
			return;
		}

		if(member.isOnline() && member.getPlayer().isInCombat())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.A_CLAN_MEMBER_MAY_NOT_BE_DISMISSED_DURING_COMBAT));
			return;
		}
		if(member.isClanLeader())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.THE_CLAN_LEADER_CANNOT_WITHDRAW));
			return;
		}
		if(Config.ENABLE_SUPPORT_GUILD && Config.SUPPORT_GUILD_DISABLE_OUST_MEMBER_CLAN && member.getPledgeType() == L2Clan.SUBUNIT_SUPPORT)
		{
			boolean resultInZone = false;
			for(L2Zone zone : activeChar.getZones())
			{
				if(ArrayUtils.contains(Config.SUPPORT_GUILD_DISPLAY_IN_ZONE_IDS, zone.getId()))
				{
					resultInZone = true;
					break;
				}
			}

			if(resultInZone)
			{
				if(activeChar.isLangRus())
					activeChar.sendMessage("Вы не можете выгнать персонажа из гильдии с клана в этой зоне.");
				else
					activeChar.sendMessage("You cannot kick a character from a guild from a clan in this zone.");
				return;
			}
		}
		clan.removeClanMember(_target);
		clan.broadcastToOnlineMembers(new SystemMessage(SystemMessage.CLAN_MEMBER_S1_HAS_BEEN_EXPELLED).addString(_target));
		clan.broadcastToOnlineMembers(new PledgeShowMemberListDelete(_target));
		boolean isAcademyMember = member.getPledgeType() == L2Clan.SUBUNIT_ACADEMY;
		if(!isAcademyMember || !Config.ACADEMY_DISABLE_OUST_PENALTY)
		{
			clan.setExpelledMember();
		}

		if(Config.BBS_RECRUITMENT_ALLOW)
		{
			Academician academic = AcademiciansStorage.getInstance().get(member.getObjectId());

			if(academic != null)
			{
				AcademyRequest request = AcademyRequestStorage.getInstance().getRequest(academic.getClanId());
				Log.addLog("[Academy→Oust] objId=" + academic.getObjId() + ", clanId=" + academic.getClanId() + ", seatsBefore=" + (request!=null?request.getSeats():"<no req>"), "academy");
				AcademiciansDAO.getInstance().delete(academic);
				AcademiciansStorage.getInstance().removeAcademic(academic);
				request.updateSeats();
				AcademyRequestStorage.getInstance().updateList();
			}
		}

		L2Player player = member.getPlayer();
		if(player != null)
		{
			if(isAcademyMember)
			{
				clan.removeAcademyBuffs(activeChar);
			}

			SiegeEvent<?, ?> siegeEvent = player.getEvent(SiegeEvent.class);
			if(siegeEvent != null)
				player.removeEvent(siegeEvent);
			player.setClan(null);
			if(!player.isNoble())
				player.setTitle("");
			player.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_RECENTLY_BEEN_DISMISSED_FROM_A_CLAN_YOU_ARE_NOT_ALLOWED_TO_JOIN_ANOTHER_CLAN_FOR_24_HOURS));
			if(Config.PENALTY_BY_CLAN_DISMISS && (!isAcademyMember || !Config.ACADEMY_DISABLE_OUST_PENALTY))
				player.setLeaveClanCurTime();

			player.broadcastUserInfo(false);

			// disable clan tab
			player.sendPacket(new PledgeShowMemberListDeleteAll());
		}
	}
}