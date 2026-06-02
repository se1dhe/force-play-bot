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

public class RequestWithdrawalPledge extends L2GameClientPacket
{
	@Override
	public void readImpl()
	{}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		//is the guy in a clan  ?
		if(activeChar.getClanId() == 0)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInCombat())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.ONE_CANNOT_LEAVE_ONES_CLAN_DURING_COMBAT));
			return;
		}

		L2Clan clan = activeChar.getClan();
		if(clan == null)
			return;

		L2ClanMember member = clan.getClanMember(activeChar.getObjectId());
		if(member == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		if(member.isClanLeader())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.THE_CLAN_LEADER_CANNOT_WITHDRAW));
			return;
		}
		if(Config.ENABLE_SUPPORT_GUILD && Config.SUPPORT_GUILD_DISABLE_WITHDRAWAL_CLAN && member.getPledgeType() == L2Clan.SUBUNIT_SUPPORT)
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
					activeChar.sendMessage("Находясь в гильдии Вам запрещено выходить из клана в этой зоне.");
				else
					activeChar.sendMessage("While in the guild you are prohibited from leaving the clan in this zone.");
				return;
			}
		}
		SiegeEvent<?, ?> siegeEvent = activeChar.getEvent(SiegeEvent.class);
		if(siegeEvent != null)
			activeChar.removeEvent(siegeEvent);

		int subUnitType = activeChar.getPledgeType();
		boolean isAcademyMember = member.getPledgeType() == L2Clan.SUBUNIT_ACADEMY;

		// this also updated the database
		clan.removeClanMember(activeChar.getObjectId());

		//player withdrawed.
		clan.broadcastToOnlineMembers(new SystemMessage(SystemMessage.S1_HAS_WITHDRAWN_FROM_THE_CLAN).addString(activeChar.getName()));

		// Remove the Player From the Member list
		clan.broadcastToOnlineMembers(new PledgeShowMemberListDelete(activeChar.getName()));

		if(isAcademyMember)
		{
			clan.removeAcademyBuffs(activeChar);
		}

		activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_RECENTLY_BEEN_DISMISSED_FROM_A_CLAN_YOU_ARE_NOT_ALLOWED_TO_JOIN_ANOTHER_CLAN_FOR_24_HOURS));

		if(subUnitType == L2Clan.SUBUNIT_ACADEMY)
		{
			activeChar.setLvlJoinedAcademy(0);
			if(Config.BBS_RECRUITMENT_ALLOW)
			{
				Academician academic = AcademiciansStorage.getInstance().get(member.getObjectId());

				if(academic != null)
				{
					AcademyRequest request = AcademyRequestStorage.getInstance().getRequest(academic.getClanId());
					Log.addLog("[Academy→Withdraw] objId=" + academic.getObjId() + ", clanId=" + academic.getClanId() + ", seatsBefore=" + (request!=null?request.getSeats():"<no req>"), "academy");
					AcademiciansDAO.getInstance().delete(academic);
					AcademiciansStorage.getInstance().removeAcademic(academic);
					request.updateSeats();
					AcademyRequestStorage.getInstance().updateList();
				}
			}
		}

		activeChar.setClan(null);

		if(!activeChar.isNoble())
			activeChar.setTitle("");

		if(!isAcademyMember || !Config.ACADEMY_DISABLE_WITHDRAWAL_PENALTY)
			activeChar.setLeaveClanCurTime();

		activeChar.broadcastUserInfo(false);

		// disable clan tab
		activeChar.sendPacket(new PledgeShowMemberListDeleteAll());
	}
}