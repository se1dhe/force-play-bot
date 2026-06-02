package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.data.xml.holder.EventHolder;
import l2p.gameserver.model.L2Clan;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.base.Transaction;
import l2p.gameserver.model.base.Transaction.TransactionType;
import l2p.gameserver.multilang.CustomMessage;
import l2p.gameserver.serverpackets.*;
import l2p.gameserver.utils.ZoneRestrictionUtil;
import org.apache.commons.lang3.ArrayUtils;

public class RequestAnswerJoinPledge extends L2GameClientPacket
{
	//Format: cd
	private int _response;

	@Override
	public void readImpl()
	{
		if(_buf.hasRemaining())
			_response = readD();
		else
			_response = 0;
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}
		answer(activeChar, _response);
	}

	protected static void answer(L2Player activeChar, int response)
	{
		Transaction transaction = activeChar.getTransaction();

		if(transaction == null)
			return;

		if(!transaction.isValid() || !transaction.isTypeOf(TransactionType.CLAN))
		{
			transaction.cancel();
			activeChar.sendPacket(Msg.TIME_EXPIRED, Msg.ActionFail);
			return;
		}

		L2Player requestor = transaction.getOtherPlayer(activeChar);

		transaction.cancel();

		if(requestor.getClan() == null || activeChar.getClanId() != 0)
			return;

		if(response == 1)
		{
			if(activeChar.isInOlympiadMode())
			{
				activeChar.sendMessage("You can't do it in Olympiad.");
				return;
			}
			int pledgeType = activeChar.getPledgeType();
			L2Clan clan = requestor.getClan();
			if(clan.getSubPledgeMembersCount(pledgeType) >= clan.getSubPledgeLimit(pledgeType))
			{
				if(pledgeType == 0)
					requestor.sendPacket(new SystemMessage(SystemMessage.S1_IS_FULL_AND_CANNOT_ACCEPT_ADDITIONAL_CLAN_MEMBERS_AT_THIS_TIME).addString(clan.getName()));
				else
					requestor.sendPacket(new SystemMessage(SystemMessage.THE_ACADEMY_ROYAL_GUARD_ORDER_OF_KNIGHTS_IS_FULL_AND_CANNOT_ACCEPT_NEW_MEMBERS_AT_THIS_TIME));
				activeChar.sendMessage("You can't do it, because the clan " + clan.getName() + " is full.");
				return;
			}
			if(Config.ENABLE_SUPPORT_GUILD && pledgeType == L2Clan.SUBUNIT_SUPPORT && !ArrayUtils.contains(Config.SUPPORT_GUILD_ALLOWED_CLASS_IDS, activeChar.getActiveClassId()))
			{
				if(requestor.isLangRus())
					requestor.sendMessage("Игрок с данной профессией не может быть добавлена в Гильдию Поддержки.");
				else
					requestor.sendMessage("A player with this profession cannot be added to the Support Guild.");
				if(activeChar.isLangRus())
					activeChar.sendMessage("Выбранная профессия не может быть добавлена в Гильдию Поддержки клана.");
				else
					activeChar.sendMessage("The selected profession cannot be added to the Clan Support Guild.");
				return;
			}

			if(!ZoneRestrictionUtil.canJoinClanInZone(activeChar, clan))
			{
				requestor.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestAnswerJoinPledge.ZoneRestrictionRequestor", requestor));
				return;
			}

			if(activeChar.canJoinClan())
			{
				activeChar.sendPacket(new JoinPledge(requestor.getClanId()));

				clan.broadcastToOnlineMembers(new SystemMessage(SystemMessage.S1_HAS_JOINED_THE_CLAN).addString(activeChar.getName()));
				clan.addClanMember(activeChar);
				activeChar.setClan(clan);
				clan.getClanMember(activeChar.getName()).setPlayerInstance(activeChar);

				if(clan.isAcademy(pledgeType))
					activeChar.setLvlJoinedAcademy(activeChar.getLevel());

				clan.getClanMember(activeChar.getName()).setPowerGrade(clan.getAffiliationRank(pledgeType));

				clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListAdd(clan.getClanMember(activeChar.getName())), activeChar);
				clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));

				// this activates the clan tab on the new member
				activeChar.sendPacket(new SystemMessage(SystemMessage.ENTERED_THE_CLAN), new PledgeShowMemberListAll(clan, activeChar));
				activeChar.setLeaveClanTime(0);
				activeChar.updatePledgeClass();
				clan.addAndShowSkillsToPlayer(activeChar);
				activeChar.sendPacket(new PledgeSkillList(clan));
				activeChar.sendPacket(new SkillList(activeChar));
				EventHolder.getInstance().findEvent(activeChar);
				activeChar.broadcastUserInfo(false);
				activeChar.broadcastRelationChanged();
			}
			else
			{
				requestor.sendPacket(new SystemMessage(SystemMessage.AFTER_A_CLAN_MEMBER_IS_DISMISSED_FROM_A_CLAN_THE_CLAN_MUST_WAIT_AT_LEAST_A_DAY_BEFORE_ACCEPTING_A_NEW_MEMBER));
				activeChar.sendPacket(new SystemMessage(SystemMessage.AFTER_LEAVING_OR_HAVING_BEEN_DISMISSED_FROM_A_CLAN_YOU_MUST_WAIT_AT_LEAST_A_DAY_BEFORE_JOINING_ANOTHER_CLAN));
				activeChar.setPledgeType(0);
			}
		}
		else
		{
			requestor.sendPacket(new SystemMessage(SystemMessage.S1_REFUSED_TO_JOIN_THE_CLAN).addString(activeChar.getName()));
			activeChar.setPledgeType(0);
		}
		requestor.getClan().block_invite = 0;
	}
}