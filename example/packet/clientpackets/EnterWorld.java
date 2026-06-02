package l2p.gameserver.clientpackets;

import l2p.gameserver.Announcements;
import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.communitybbs.Manager.BuffBBSManager;
import l2p.gameserver.data.htm.HtmCache;
import l2p.gameserver.data.xml.holder.ResidenceHolder;
import l2p.gameserver.instancemanager.*;
import l2p.gameserver.model.*;
import l2p.gameserver.model.entity.SevenSigns;
import l2p.gameserver.model.entity.events.impl.ClanHallAuctionEvent;
import l2p.gameserver.model.entity.olympiad.Olympiad;
import l2p.gameserver.model.entity.olympiad.OlympiadRankSkillManager;
import l2p.gameserver.model.entity.residence.ClanHall;
import l2p.gameserver.model.quest.Quest;
import l2p.gameserver.multilang.CustomMessage;
import l2p.gameserver.network.L2GameClient;
import l2p.gameserver.network.L2GameClient.GameClientState;
import l2p.gameserver.serverpackets.*;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.utils.Location;
import l2p.gameserver.utils.Log;
import l2p.gameserver.utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnterWorld extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(EnterWorld.class);

	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		L2GameClient client = getClient();
		L2Player activeChar = client.getActiveChar();

		if(activeChar == null)
		{
			client.close(Msg.ServerClose);
			return;
		}

		boolean first = activeChar.entering;

		if(first)
		{
			if(activeChar.isGM() && !Config.SHOW_GM_LOGIN && !Config.EVERYBODY_HAS_ADMIN_RIGHTS)
				activeChar.setInvisible(true);
			activeChar.spawnMe();

			if(activeChar.isInStoreMode())
				if(!activeChar.checksForShop(activeChar.getPrivateStoreType() == L2Player.STORE_PRIVATE_MANUFACTURE))
				{
					activeChar.setPrivateStoreType(L2Player.STORE_PRIVATE_NONE);
					activeChar.standUp();
					activeChar.broadcastUserInfo(false);
				}

			activeChar.setRunning();
			activeChar.standUp();
			BuffBBSManager.initSchemes(activeChar);
			activeChar.startAutoSaveTask();
			activeChar.startPcBangPointsTask();
			activeChar.startVipTask();
			if(Config.LOG_ENTER_WORLD)
				Log.addLog("Player with IP " + client.getIpAddr() + " and HWID " + client.getHWID() + " enter in char " + activeChar.getName() + ", account " + client.getLoginName(), "enter_char");
		}
		else if(activeChar.isTeleporting())
			activeChar.onTeleported();
		else if(!activeChar.isVisible())
			activeChar.spawnMe();

		if(Config.OLY_RANK_SKILLS_ENABLED)
		{
			OlympiadRankSkillManager.getInstance().onPlayerEnter(activeChar);
		}

		if(client.getState() == GameClientState.ENTER_GAME)
			client.setState(GameClientState.IN_GAME);

		activeChar.getMacroses().sendUpdate();
		activeChar.sendPacket(new SSQInfo(), new HennaInfo(activeChar));
		activeChar.sendPacket(new SkillList(activeChar));
		activeChar.sendPacket(new ExAdenaInvenCount(activeChar));
		activeChar.sendPacket(new ExEnterWorld());
		activeChar.sendPacket(new ExUISetting(activeChar));
		activeChar.sendPacket(new SystemMessage(SystemMessage.WELCOME_TO_THE_WORLD_OF_LINEAGE_II));

		Announcements.getInstance().showAnnouncements(activeChar);

		activeChar.classWindow();
		// Вызов всех хэндлеров, определенных в скриптах
		if(first)
			activeChar.getListeners().onEnter();

		SevenSigns.getInstance().sendCurrentPeriodMsg(activeChar);

		if(activeChar.isJailed())
		{
			int time = activeChar.getVarInt("jailed", 0);
			if(System.currentTimeMillis() / 1000L < time)
				activeChar.sendMessage((activeChar.isLangRus() ? "Окончание срока: " : "The end of the prison term: ") + TimeUtils.toSimpleFormat(time * 1000L));
		}
		if(Config.SHOW_HTML_WELCOME && activeChar.getClan() == null)
		{
			String text = HtmCache.getInstance().getNullable("welcome.htm", activeChar);
			if(text != null)
				sendPacket(new NpcHtmlMessage(5).setHtml(text));
		}

		if(activeChar.getClan() != null)
		{
			notifyClanMembers(activeChar);
			sendPacket(new PledgeShowMemberListAll(activeChar.getClan(), activeChar), new PledgeShowInfoUpdate(activeChar.getClan()), new PledgeSkillList(activeChar.getClan()));
		}

		activeChar.unsetWearVariables();

		if(first && Config.ALLOW_WEDDING)
		{
			CoupleManager.getInstance().engage(activeChar);
			CoupleManager.getInstance().notifyPartner(activeChar);
		}

		if(first)
		{
			activeChar.getFriendList().notifyFriends(true);
			loadTutorial(activeChar);
			activeChar.restoreDisableSkills(false);
			activeChar.sendPacket(new SkillCoolTime(activeChar));
		}

		sendPacket(new L2FriendList(activeChar), new ExStorageMaxCount(activeChar), new QuestList(activeChar), new EtcStatusUpdate(activeChar));

		activeChar.checkHpMessages(activeChar.getMaxHp(), activeChar.getCurrentHp());
		activeChar.checkDayNightMessages();

		if(Config.PETITIONING_ALLOWED)
			PetitionManager.getInstance().checkPetitionMessages(activeChar);

		if(!first)
		{
			if(activeChar.isCastingNow())
				activeChar.abortCast(true, false);

			if(activeChar.isInVehicle())
				sendPacket(activeChar.getVehicle().getOnPacket(activeChar, activeChar.getInVehiclePosition()));

			if(activeChar.isMoving() || activeChar.isFollowing())
				sendPacket(activeChar.movePacket());

			if(activeChar.isMounted())
				sendPacket(new Ride(activeChar));

			if(activeChar.isFishing())
				activeChar.stopFishing();

			activeChar.stopDeleteTask();
		}

		activeChar.entering = false;
		activeChar.sendUserInfo(true);
		activeChar.setActiveTime();

		if(activeChar.isSitting())
			activeChar.sendPacket(new ChangeWaitType(activeChar, ChangeWaitType.WT_SITTING));
		if(activeChar.getPrivateStoreType() != L2Player.STORE_PRIVATE_NONE)
		{
			if(activeChar.getPrivateStoreType() == L2Player.STORE_PRIVATE_BUY)
				sendPacket(new PrivateStoreBuyMsg(activeChar, false));
			else if(activeChar.getPrivateStoreType() == L2Player.STORE_PRIVATE_SELL || activeChar.getPrivateStoreType() == L2Player.STORE_PRIVATE_SELL_PACKAGE)
				sendPacket(new PrivateStoreMsgSell(activeChar, false));
			else if(activeChar.getPrivateStoreType() == L2Player.STORE_PRIVATE_MANUFACTURE)
				sendPacket(new RecipeShopMsg(activeChar, false));
		}

		if(activeChar.isDead())
			sendPacket(new Die(activeChar));

		activeChar.unsetVar("offline");

		// на всякий случай
		activeChar.sendActionFailed();

		if(first && activeChar.isGM() && Config.SAVE_GM_EFFECTS && activeChar.getPlayerAccess().CanUseGMCommand)
		{
			//silence
			if(activeChar.getVarB("gm_silence"))
			{
				activeChar.setMessageRefusal(true);
				activeChar.sendPacket(new SystemMessage(SystemMessage.MESSAGE_REFUSAL_MODE));
			}
			//invul
			if(activeChar.getVarB("gm_invul"))
			{
				activeChar.setIsInvul(true);
				activeChar.sendMessage(activeChar.getName() + " is now immortal.");
			}
			//gmspeed
			try
			{
				int var_gmspeed = Integer.parseInt(activeChar.getVar("gm_gmspeed"));
				if(var_gmspeed >= 1 && var_gmspeed <= 4)
					activeChar.doCast(SkillTable.getInstance().getInfo(7029, var_gmspeed), activeChar, true);
			}
			catch (Exception E)
			{}
		}

		PlayerMessageStack.getInstance().CheckMessages(activeChar);

		sendPacket(new ClientSetTime(), new ExSetCompassZoneCode(activeChar));

		if(activeChar.isReviveRequested())
		{
			int remainingTime = 0;
			if(Config.REVIVE_TIME > 0)
			{
				long remaining = (activeChar.getLastResurrectionTime() + Config.REVIVE_TIME * 1000L) - System.currentTimeMillis();
				if(remaining <= 0)
				{
					activeChar.cancelReviveRequest();
				}
				else
				{
					remainingTime = (int) remaining;
					ConfirmDlg cd = new ConfirmDlg(SystemMessage.S1_IS_MAKING_AN_ATTEMPT_AT_RESURRECTION, remainingTime).addString(activeChar.isLangRus() ? "Кто-то" : "Somebody");
					cd.setRequestId(2);
					sendPacket(cd);
				}
			}
			else
			{
				ConfirmDlg cd = new ConfirmDlg(SystemMessage.S1_IS_MAKING_AN_ATTEMPT_AT_RESURRECTION, 0).addString(activeChar.isLangRus() ? "Кто-то" : "Somebody");
				cd.setRequestId(2);
				sendPacket(cd);
			}
		}

		if(activeChar.isCursedWeaponEquipped())
			CursedWeaponsManager.getInstance().showUsageTime(activeChar, activeChar.getCursedWeaponEquippedId());
		if(!first)
		{
			if(activeChar.inObserverMode())
			{
				if(activeChar.getObserverMode() == 1)
				{
					Location loc = activeChar.getObservePoint().getLoc();
					if(activeChar.isInOlympiadObserverMode())
						sendPacket(new TeleportToLocation(activeChar, loc.x, loc.y, loc.z, loc.h), new ExTeleportToLocationActivate(activeChar, loc.x, loc.y, loc.z, loc.h));
					else
						sendPacket(new ObserverStart(loc.x, loc.y, loc.z));
				}
				else if(activeChar.getObserverMode() == 2)
					activeChar.returnFromObserverMode();
				else if(activeChar.isInOlympiadObserverMode())
					activeChar.leaveOlympiadObserverMode();
				else
					activeChar.leaveObserverMode();
			}
			else if(activeChar.isVisible())
				L2World.showObjectsToPlayer(activeChar, false);

			if(activeChar.getPet() != null)
				activeChar.getPet().sendPetInfo();
			if(activeChar.getAgathion() != null)
				activeChar.getAgathion().sendPetInfo();
			if(activeChar.getWearAgathion() != null)
				activeChar.deleteWearAgathion();

			if(activeChar.isInParty())
			{
				L2Summon member_pet;
				//sends new member party window for all members
				//we do all actions before adding member to a list, this speeds things up a little
				sendPacket(new PartySmallWindowAll(activeChar.getParty(), activeChar.getObjectId()));

				for(L2Player member : activeChar.getParty().getPartyMembers())
					if(member != activeChar)
					{
						sendPacket(new PartySpelled(member, true));
						sendPacket(RelationChanged.update(activeChar, member, activeChar));
					}

				// Если партия уже в СС, то вновь прибывшем посылаем пакет открытия окна СС
				if(activeChar.getParty().isInCommandChannel())
					sendPacket(Msg.ExMPCCOpen);
			}

			activeChar.broadcastUserInfo(false);
		}
		else
		{
			if(activeChar.inObserverMode())
			{
				_log.warn("In Observ entering: " + activeChar.toString() + " obsMode: " + activeChar.getObserverMode() + " olympiadObserveId: " + activeChar.getOlympiadObserveId());
				if(activeChar.getOlympiadObserveId() == -1)
					activeChar.leaveObserverMode();
				else
					activeChar.leaveOlympiadObserverMode();
			}
			activeChar.sendUserInfo(false); // Отобразит права в клане
		}

		activeChar.sendItemList(false);
		activeChar.sendPacket(new ShortCutInit(activeChar));
		activeChar.sendPacket(new ExBasicActionList(activeChar));
		if(!first)
		{
			for(int shotId : activeChar.getAutoSoulShot())
				sendPacket(new ExAutoSoulShot(shotId, true, 0));
		}
		else
		{
			if(!getClient().isITClient())
			{
				sendPacket(new ExAutoSoulShot(0, true, 0));
				sendPacket(new ExAutoSoulShot(0, true, 1));
				sendPacket(new ExAutoSoulShot(0, true, 2));
				sendPacket(new ExAutoSoulShot(0, true, 3));
				activeChar.sendUserInfo();
			}
		}
		activeChar.updateEffectIcons();
		activeChar.updateStats();
		activeChar.getFarmSystem().restoreVariables(activeChar);
		if(Config.ALT_PCBANG_POINTS_ENABLED)
			activeChar.sendPacket(new ExPCCafePointInfo(activeChar, 0, 1, 2, 12));
		if(Config.SERVICES_CHAR_KEY && activeChar.isKeyBlocked())
			activeChar.sendMessage(new CustomMessage("l2p.KeyFrozen", activeChar));
		if(Config.CUSTOM_SEND_MESSAGE_ON_PA_ACCEPT && activeChar.isPremium())
			activeChar.sendMessage("Premium=1");
		if(Config.OLY_MSG_ON_ENTER && Config.ENABLE_OLYMPIAD && Olympiad.inCompPeriod())
			activeChar.sendPacket(new SystemMessage(SystemMessage.THE_OLYMPIAD_GAME_HAS_STARTED));
	}

	private static void notifyClanMembers(L2Player activeChar)
	{
		L2Clan clan = activeChar.getClan();
		if(clan == null || clan.getClanMember(activeChar.getObjectId()) == null)
			return;

		clan.getClanMember(activeChar.getObjectId()).setPlayerInstance(activeChar);

		int sponsor = activeChar.getSponsor();
		int apprentice = activeChar.getApprentice();
		SystemMessage msg = new SystemMessage(SystemMessage.CLAN_MEMBER_S1_HAS_LOGGED_INTO_GAME).addString(activeChar.getName());
		PledgeShowMemberListUpdate memberUpdate = new PledgeShowMemberListUpdate(activeChar);
		for(L2Player clanMember : clan.getOnlineMembers(activeChar.getObjectId()))
		{
			clanMember.sendPacket(memberUpdate);
			if(clanMember.getObjectId() == sponsor)
				clanMember.sendPacket(new SystemMessage(SystemMessage.S1_YOUR_CLAN_ACADEMYS_APPRENTICE_HAS_LOGGED_IN).addString(activeChar.getName()));
			else if(clanMember.getObjectId() == apprentice)
				clanMember.sendPacket(new SystemMessage(SystemMessage.S1_YOUR_CLAN_ACADEMYS_SPONSOR_HAS_LOGGED_IN).addString(activeChar.getName()));
			else
				clanMember.sendPacket(msg);
		}

		activeChar.clanMsg();

		if(Config.ACADEMY_BUFFS_ON_LOGIN)
		{
			clan.applyAcademyBuffs(activeChar);
		}

		if(!activeChar.isClanLeader())
			return;

		clan.onLeaderEnter();

		ClanHall clanHall = clan.getHasHideout() > 0 ? ResidenceHolder.getInstance().getResidence(ClanHall.class, clan.getHasHideout()) : null;
		if(clanHall == null || clanHall.getAuctionLength() != 0)
			return;

		if(clanHall.getSiegeEvent().getClass() != ClanHallAuctionEvent.class)
			return;

		if(clan.getWarehouse().getAdenaCount() < clanHall.getRentalFee())
			activeChar.sendPacket(new SystemMessage(SystemMessage.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW).addNumber(clanHall.getRentalFee()));
	}

	private static void loadTutorial(L2Player player)
	{
		Quest q = QuestManager.getQuest(255);
		if(q != null)
			player.processQuestEvent(q.getName(), "UC", null);
	}
}
