package l2p.gameserver.clientpackets;

import l2p.commons.util.Rnd;
import l2p.gameserver.Config;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.L2Clan;
import l2p.gameserver.model.L2Object;
import l2p.gameserver.model.L2ObjectsStorage;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.base.Transaction;
import l2p.gameserver.model.base.Transaction.TransactionType;
import l2p.gameserver.multilang.CustomMessage;
import l2p.gameserver.serverpackets.AskJoinPledge;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.utils.Util;
import l2p.gameserver.utils.ZoneRestrictionUtil;
import org.apache.commons.lang3.ArrayUtils;

public class RequestJoinPledge extends L2GameClientPacket
{
	private int _target;
	private int _pledgeType;

	@Override
	public void readImpl()
	{
		_target = readD();
		_pledgeType = readD();
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

		L2Clan clan = activeChar.getClan();

		if(clan == null || !clan.canInvite())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.AFTER_A_CLAN_MEMBER_IS_DISMISSED_FROM_A_CLAN_THE_CLAN_MUST_WAIT_AT_LEAST_A_DAY_BEFORE_ACCEPTING_A_NEW_MEMBER));
			return;
		}

		if(activeChar.isInTransaction())
		{
			activeChar.sendPacket(Msg.WAITING_FOR_ANOTHER_REPLY);
			return;
		}

		if(_target == activeChar.getObjectId())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_ASK_YOURSELF_TO_APPLY_TO_A_CLAN));
			return;
		}

		//is the activeChar have privilege to invite players
		if((activeChar.getClanPrivileges() & L2Clan.CP_CL_JOIN_CLAN) != L2Clan.CP_CL_JOIN_CLAN)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.ONLY_THE_LEADER_CAN_GIVE_OUT_INVITATIONS));
			return;
		}

		L2Object object = activeChar.getVisibleObject(_target);
		if(object == null || !object.isPlayer())
			return;
		L2Player member = (L2Player) object;

		if(!activeChar.getPlayerAccess().CanJoinClan)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_JOIN_THE_CLAN_BECAUSE_ONE_DAY_HAS_NOT_YET_PASSED_SINCE_HE_SHE_LEFT_ANOTHER_CLAN).addString(member.getName()));
			member.sendPacket(new SystemMessage(SystemMessage.FAILED_TO_JOIN_THE_CLAN));
			return;
		}
		if(!member.getPlayerAccess().CanJoinClan)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_JOIN_THE_CLAN_BECAUSE_ONE_DAY_HAS_NOT_YET_PASSED_SINCE_HE_SHE_LEFT_ANOTHER_CLAN).addString(member.getName()));
			member.sendPacket(new SystemMessage(SystemMessage.FAILED_TO_JOIN_THE_CLAN));
			return;
		}

		if(member.getClanId() != 0)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.S1_IS_WORKING_WITH_ANOTHER_CLAN).addString(member.getName()));
			return;
		}

		if(member.isInTransaction())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.S1_IS_BUSY_PLEASE_TRY_AGAIN_LATER).addString(member.getName()));
			return;
		}

		if(_pledgeType == L2Clan.SUBUNIT_ACADEMY && (member.getLevel() > 40 || member.getClassId().getLevel() > 2))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.TO_JOIN_A_CLAN_ACADEMY_CHARACTERS_MUST_BE_LEVEL_40_OR_BELOW_NOT_BELONG_ANOTHER_CLAN_AND_NOT_YET_COMPLETED_THEIR_2ND_CLASS_TRANSFER));
			return;
		}

		if(Config.ENABLE_SUPPORT_GUILD && _pledgeType == L2Clan.SUBUNIT_SUPPORT && !ArrayUtils.contains(Config.SUPPORT_GUILD_ALLOWED_CLASS_IDS, member.getActiveClassId()))
		{
			if(activeChar.isLangRus())
				activeChar.sendMessage("Выбранная профессия не может быть добавлена в Гильдию Поддержки.");
			else
				activeChar.sendMessage("The selected profession cannot be added to the Support Guild.");
			return;
		}

		if(clan.getSubPledgeMembersCount(_pledgeType) >= clan.getSubPledgeLimit(_pledgeType))
		{
			if(_pledgeType == 0)
				activeChar.sendPacket(new SystemMessage(SystemMessage.S1_IS_FULL_AND_CANNOT_ACCEPT_ADDITIONAL_CLAN_MEMBERS_AT_THIS_TIME).addString(clan.getName()));
			else
				activeChar.sendPacket(new SystemMessage(SystemMessage.THE_ACADEMY_ROYAL_GUARD_ORDER_OF_KNIGHTS_IS_FULL_AND_CANNOT_ACCEPT_NEW_MEMBERS_AT_THIS_TIME));
			return;
		}

		if(clan.block_invite > System.currentTimeMillis())
		{
			String cn = String.valueOf(Math.max((clan.block_invite - System.currentTimeMillis()) / 1000L, 1L));
			activeChar.sendMessage(activeChar.isLangRus() ? ("Вы не можете приглашать пока приглашенный игрок не ответит, либо подождите " + cn + " " + Util.secondFormat(true, cn) + ".") : ("You can't invite until invited player does not respond, or wait " + cn + " " + Util.secondFormat(false, cn) + "."));
			return;
		}

		if(!ZoneRestrictionUtil.canJoinClanInZone(member, clan))
		{
			activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestAnswerJoinPledge.ZoneRestrictionRequestor", activeChar).addString(member.getName()));
			return;
		}

		clan.block_invite = System.currentTimeMillis() + 15000L;
		new Transaction(TransactionType.CLAN, activeChar, member, 10000);
		member.setPledgeType(_pledgeType);

		member.sendPacket(new AskJoinPledge(activeChar.getObjectId(), activeChar.getClan().getName()));
		if(Config.BOTS_CAN_JOIN_CLAN && member.isFashion && Rnd.chance(Config.BOTS_CHANCE_JOIN_CLAN))
		{
			final int ans = Rnd.chance(Config.BOTS_CHANCE_JOIN_CLAN) ? 1 : (Rnd.chance(Config.BOTS_CHANCE_REFUSE_CLAN) ? 0 : 2);
			if(ans < 2)
			{
				final long id = member.getStoredId();
				ThreadPoolManager.getInstance().schedule(new Runnable()
				{
					@Override
					public void run()
					{
						L2Player bot = L2ObjectsStorage.getAsPlayer(id);
						if(bot != null)
							RequestAnswerJoinPledge.answer(bot, ans);
					}
				}, Rnd.get(2500, 8500));
			}
		}
	}
}