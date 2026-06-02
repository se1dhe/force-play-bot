package events.TvT;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import l2p.commons.util.GArray;
import l2p.commons.util.GCSArray;
import l2p.commons.util.Rnd;
import l2p.gameserver.Bonus;
import l2p.gameserver.Config;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.clientpackets.Say2C;
import l2p.gameserver.configuration.ConfigBattleGround;
import l2p.gameserver.data.htm.HtmCache;
import l2p.gameserver.data.xml.DocumentFactory;
import l2p.gameserver.database.mysql;
import l2p.gameserver.instancemanager.ServerVariables;
import l2p.gameserver.instancemanager.ZoneManager;
import l2p.gameserver.listener.PlayerListener;
import l2p.gameserver.listener.actor.OnDeathListener;
import l2p.gameserver.listener.actor.OnEquipUnEquipItemListener;
import l2p.gameserver.listener.actor.OnMagicUseListener;
import l2p.gameserver.listener.actor.OnPlayerExitListener;
import l2p.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2Effect;
import l2p.gameserver.model.L2Object;
import l2p.gameserver.model.L2ObjectsStorage;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Skill;
import l2p.gameserver.model.L2Summon;
import l2p.gameserver.model.L2Zone;
import l2p.gameserver.model.L2Zone.ZoneType;
import l2p.gameserver.model.base.ClassId;
import l2p.gameserver.model.entity.Hero;
import l2p.gameserver.model.entity.olympiad.Olympiad;
import l2p.gameserver.model.entity.olympiad.OlympiadGame;
import l2p.gameserver.model.instances.L2AgathionInstance;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.multilang.CustomMessage;
import l2p.gameserver.scripts.Functions;
import l2p.gameserver.scripts.ScriptFile;
import l2p.gameserver.serverpackets.Revive;
import l2p.gameserver.serverpackets.Say2;
import l2p.gameserver.serverpackets.SkillList;
import l2p.gameserver.skills.EffectType;
import l2p.gameserver.skills.Env;
import l2p.gameserver.tables.DoorTable;
import l2p.gameserver.tables.ItemTable;
import l2p.gameserver.tables.MapRegionTable;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.utils.Location;
import l2p.gameserver.utils.Log;
import l2p.gameserver.utils.TimeUtils;
import l2p.gameserver.utils.Util;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class TvT extends Functions implements ScriptFile
{
	private static final Logger _log = LoggerFactory.getLogger(TvT.class);

	private static GCSArray<Integer> players_list1;
	private static GCSArray<Integer> players_list2;
	private static GCSArray<Integer> live_list1;
	private static GCSArray<Integer> live_list2;
	private static List<String> _restrict = new ArrayList<String>();

	private static int count1 = 0;
	private static int count2 = 0;
	private static boolean _clearing;
	private static final int _instanceID = Config.TvT_Instance ? 101 : 0;
	private static final boolean useBC = Config.TvT_ReturnPoint.length < 3;

	private static boolean _isRegistrationActive = false;
	private static int _status = 0;

	private static int _time_to_start;

	private static int _category;
	private static int _pre_category;
	private static int _minLevel;
	private static int _maxLevel;
	private static ScheduledFuture<?> _startTask;
	private static ScheduledFuture<?> _endTask;
	private static ScheduledFuture<?> _countdownTask;
	private static L2Zone _zone = ZoneManager.getInstance().getZone(Config.TvT_Zone);
	private static ZoneListener _zoneListener = new ZoneListener();

	private static Calendar _date;
	private static String dateString;

	private static Map<Integer, Future<?>> _tasks = new ConcurrentHashMap<Integer, Future<?>>();
	private static Map<Integer, List<Integer>> _equip = new ConcurrentHashMap<Integer, List<Integer>>();
	private static Map<Integer, List<Integer>> _destroy = new ConcurrentHashMap<Integer, List<Integer>>();
	private static Map<Integer, List<L2Effect>> _sbuffs = new ConcurrentHashMap<Integer, List<L2Effect>>();

	@Override
	public void onLoad()
	{
		_zone.addListener(_zoneListener);

		_active = ServerVariables.getString("TvT", "off").equalsIgnoreCase("on");

		loadCustomItems();

		if(_active)
			executeTask("events.TvT.TvT", "preLoad", new Object[0], 70000L);

		_log.info("Loaded Event: TvT");
	}

	@Override
	public void onReload()
	{
		//_zone.removeListener(_zoneListener);
	}

	@Override
	public void onShutdown()
	{
		onReload();
	}

	private static boolean _active = false;

	public void activateEvent()
	{
		L2Player player = getSelf();
		if(player == null)
			return;
		activeEvent(player, 0);
	}

	public void activateEvent(String[] args)
	{
		L2Player player = getSelf();
		if(player == null)
			return;

		int page = Integer.parseInt(args[0]);
		activeEvent(player, page);
	}

	private void activeEvent(L2Player player, int page)
	{
		if(!player.getPlayerAccess().IsEventGm)
			return;

		if(!_active)
		{
			executeTask("events.TvT.TvT", "preLoad", new Object[0], 10000L);
			ServerVariables.set("TvT", "on");
			_log.info("Event 'TvT' activated.");
			player.sendMessage(new CustomMessage("scripts.events.TvT.AnnounceEventStarted", player));
		}
		else
			player.sendMessage(player.isLangRus() ? "'TvT' эвент уже активен." : "Event 'TvT' already active.");

		_active = true;

		String htm;
		if(page == 0)
			htm = HtmCache.getInstance().getNotNull("admin/events.htm", player);
		else
			htm = HtmCache.getInstance().getNotNull("admin/events-" + page + ".htm", player);
		htm = htm.replace("%page%", String.valueOf(page));
		show(htm, player);
	}

	public void deactivateEvent()
	{
		L2Player player = getSelf();
		if(player == null)
			return;

		deactivateEvent(player, 0);
	}

	public void deactivateEvent(String[] args)
	{
		L2Player player = getSelf();
		if(player == null)
			return;

		int page = Integer.parseInt(args[0]);

		deactivateEvent(player, page);
	}

	private void deactivateEvent(L2Player player, int page)
	{
		if(!player.getPlayerAccess().IsEventGm)
			return;

		if(_active)
		{
			if(_startTask != null)
			{
				_startTask.cancel(true);
				_startTask = null;
			}
			ServerVariables.unset("TvT");
			_log.info("Event 'TvT' deactivated.");
			player.sendMessage(new CustomMessage("scripts.events.TvT.AnnounceEventStoped", player));
		}
		else
			player.sendMessage("Event 'TvT' not active.");

		_active = false;

		String htm;
		if(page == 0)
			htm = HtmCache.getInstance().getNotNull("admin/events.htm", player);
		else
			htm = HtmCache.getInstance().getNotNull("admin/events-" + page + ".htm", player);
		htm = htm.replace("%page%", String.valueOf(page));
		show(htm, player);
	}

	public void stopEvent()
	{
		L2Player player = getSelf();
		if(player == null || !player.getPlayerAccess().IsEventGm)
			return;

		if(_active)
		{
			if(_isRegistrationActive)
			{
				if(_endTask != null)
				{
					_endTask.cancel(false);
					_endTask = null;
				}

				if(_startTask != null)
				{
					_startTask.cancel(false);
					_startTask = null;
				}

				if(_countdownTask != null)
				{
					_countdownTask.cancel(false);
					_countdownTask = null;
				}

				sayToAll("scripts.events.TvT.AnnounceEventCancelled", null);
				for(L2Player target : getPlayers(players_list1, players_list2))
				{
					if(target != null)
						target.setEventReg(0);
				}
				live_list1.clear();
				live_list2.clear();
				players_list1.clear();
				players_list2.clear();
				if(Config.TvT_IP)
					_restrict.clear();
				if(Config.TvT_HWID)
					_restrict.clear();
				_isRegistrationActive = false;
				_status = 0;
				Log.logEvent(Log.EventLog.TvtStop, dateString);
				executeTask("events.TvT.TvT", "preLoad", new Object[0], 10000);
			}
			else if(_status > 0)
			{
				endBattle();
			}
		}
		else
			player.sendMessage("Event 'TvT' not active.");
	}

	public static boolean isRunned(String name)
	{
		return (_isRegistrationActive || _status > 0) && _instanceID == 0 && name.equals(Config.TvT_Zone);
	}

	public static int getMinLevelForCategory(int category)
	{
		int[] levels = Config.TVT_LEVEL_GROUPS.get(category);
		return levels != null ? levels[0] : 0;
	}

	public static int getMaxLevelForCategory(int category)
	{
		int[] levels = Config.TVT_LEVEL_GROUPS.get(category);
		return levels != null ? levels[1] : 80;
	}

	public static int getCategory(int level)
	{
		Integer category = Config.TVT_LEVEL_TO_CATEGORY.get(level);
		return category != null ? category : 0;
	}

	public void start(String[] var)
	{
		L2Player player = getSelf();
		if(player == null || !player.getPlayerAccess().IsEventGm)
			return;
		startOk(var);
	}

	private static void startOk(String[] var)
	{
		if(var.length < 1)
			return;

		if(_instanceID == 0 && (isEventStarted("events.CtF.CtF", Config.TvT_Zone) || isEventStarted("events.lastHero.LastHero", Config.TvT_Zone)))
		{
			_log.info("TvT not started: another event is already running");
			return;
		}
		if(_isRegistrationActive || _status > 0)
			return;

		try
		{
			_category = Integer.parseInt(var[0]);
		}
		catch(Exception e)
		{
			_log.info("TvT not started: can't parse category");
			return;
		}

		if(_category == -1)
		{
			_minLevel = 1;
			_maxLevel = 80;
		}
		else
		{
			_minLevel = getMinLevelForCategory(_category);
			_maxLevel = getMaxLevelForCategory(_category);
		}

		if(_endTask != null)
		{
			_log.info("TvT not started: end task is active");
			return;
		}

		if(_startTask != null)
		{
			_startTask.cancel(false);
			_startTask = null;
		}

		if(_countdownTask != null)
		{
			_countdownTask.cancel(false);
			_countdownTask = null;
		}

		_status = 0;
		_isRegistrationActive = true;
		_clearing = false;
		_time_to_start = Config.TvT_Time;

		players_list1 = new GCSArray<Integer>();
		players_list2 = new GCSArray<Integer>();
		live_list1 = new GCSArray<Integer>();
		live_list2 = new GCSArray<Integer>();
		_restrict = new ArrayList<String>();
		count1 = 0;
		count2 = 0;

		if(_date != null)
			dateString = TimeUtils.toLogFormat(_date.getTimeInMillis());
		else
			dateString = TimeUtils.toLogFormat(System.currentTimeMillis());

		String[] param = {String.valueOf(_time_to_start), String.valueOf(_minLevel), String.valueOf(_maxLevel)};
		sayToAll("scripts.events.TvT.AnnouncePreStart", param);
		sayToAll("scripts.events.TvT.AnnounceReg", null);

		executeTask("events.TvT.TvT", "question", new Object[0], 5000L);
		executeTask("events.TvT.TvT", "announce", new Object[0], 60000L);
		Log.logEvent(Log.EventLog.TvtStartReg, dateString);
	}

	private static void sayToAll(String address, String[] replacements)
	{
		for(L2Player player : L2ObjectsStorage.getAllPlayersForIterate())
			if(player != null)
			{
				CustomMessage cm = new CustomMessage(address, player);
				if(replacements != null)
					for(String s : replacements)
						cm.addString(s);
				player.sendPacket(new Say2(0, Say2C.CRITICAL_ANNOUNCEMENT, "TvT", "TvT: " + cm.toString()));
			}
	}

	private static void sayToParticipants(String address, String[] replacements)
	{
		for(L2Player player : getPlayers(live_list1, live_list2))
			if(player != null)
			{
				CustomMessage cm = new CustomMessage(address, player);
				if(replacements != null)
					for(String s : replacements)
						cm.addString(s);
				player.sendPacket(new Say2(0, Say2C.CRITICAL_ANNOUNCEMENT, "TvT", "TvT: " + cm.toString()));
			}
	}

	public static void question()
	{
		for(L2Player player : L2ObjectsStorage.getAllPlayersForIterate())
			if(isWindowCheck(player))
				player.scriptRequest(new CustomMessage("scripts.events.TvT.AskPlayer", player).toString(), "events.TvT.TvT:addPlayer", new Object[0]);
	}

	private static boolean isWindowCheck(L2Player player)
	{
		if(player == null)
			return false;

		if(player.isDead())
			return false;

		if(player.getLevel() < _minLevel || player.getLevel() > _maxLevel)
			return false;

		if(player.getInstanceId() != 0)
			return false;

		if(player.isInOlympiadMode())
			return false;

		if(Olympiad.isRegistered(player))
			return false;

		if(player.isCursedWeaponEquipped())
			return false;

		if(player.inObserverMode())
			return false;

		if(player.inEvent())
			return false;

		if(player.inEventReg())
			return false;

		if(player.getTeam() != 0)
			return false;

		if(player.isJailed())
			return false;

		if(Config.INVITE_RESTRICTED_ZONES.length > 0 && player.isInZone(Config.INVITE_RESTRICTED_ZONES))
			return false;

		if(player.getVarB("NoEventAsk", Config.EVENT_NO_ASK))
			return false;

		return true;
	}

	public static void announce()
	{
		if(!_isRegistrationActive)
			return;

		if(_time_to_start > 1)
		{
			_time_to_start -= 1;
			String[] param = {String.valueOf(_time_to_start), String.valueOf(_minLevel), String.valueOf(_maxLevel)};
			sayToAll("scripts.events.TvT.AnnouncePreStart", param);
			sayToAll("scripts.events.TvT.AnnounceReg", null);
			executeTask("events.TvT.TvT", "announce", new Object[0], 60000);
		}
		else
		{
			if(players_list1.isEmpty() || players_list2.isEmpty() || players_list1.size() + players_list2.size() < Config.TvT_MinPlayers)
			{
				sayToAll("scripts.events.TvT.AnnounceEventCancelled", null);
				cancelEvent();
				executeTask("events.TvT.TvT", "preLoad", new Object[0], 10000);
				return;
			}
			_status = 1;
			_isRegistrationActive = false;
			sayToAll("scripts.events.TvT.AnnounceEventStarting", null);
			executeTask("events.TvT.TvT", "prepare", new Object[0], 5000);
		}
	}

	public static void countdown(int time, boolean minute)
	{
		if(!Config.TvT_Announce_Countdown_Time)
			return;
		if(_status == 0)
			return;
		if(time > 1)
		{
			time -= 1;
			String[] param = {String.valueOf(time), String.valueOf(count1), String.valueOf(count2)};
			if(minute)
			{
				if(time == 1)
				{
					sayToParticipants("scripts.events.TvT.AnnounceMinutesLeft", param);
					_countdownTask = executeTask("events.TvT.TvT", "countdown", new Object[]{60, false}, 1000);
				}
				else
				{
					sayToParticipants("scripts.events.TvT.AnnounceMinutesLeft", param);
					_countdownTask = executeTask("events.TvT.TvT", "countdown", new Object[]{time, minute}, 60000);
				}
			}
			else
			{
				switch(time)
				{
					case 45:
					case 30:
					case 20:
					case 15:
					case 10:
					case 5:
					case 4:
					case 3:
					case 2:
					case 1:
						sayToParticipants("scripts.events.TvT.AnnounceSecondsLeft", param);
						break;
				}

				_countdownTask = executeTask("events.TvT.TvT", "countdown", new Object[]{time, minute}, 1000);
			}
		}
	}

	private static void cancelEvent()
	{
		for(L2Player target : getPlayers(players_list1, players_list2))
		{
			if(target != null)
				target.setEventReg(0);
		}
		_isRegistrationActive = false;
		_status = 0;
		Log.logEvent(Log.EventLog.TvtStop, dateString);
	}

	public void unreg()
	{
		L2Player player = getSelf();
		if(player == null)
			return;

		if(!_isRegistrationActive)
		{
			player.sendMessage(player.isLangRus() ? "Доступно только в период регистрации." : "Available only during the registration period.");
			return;
		}
		if(players_list1.contains(player.getObjectId()))
		{
			players_list1.remove(Integer.valueOf(player.getObjectId()));
			live_list1.remove(Integer.valueOf(player.getObjectId()));
			player.setEventReg(0);
			if(Config.TvT_IP)
				_restrict.remove(player.getIP());
			if(Config.TvT_HWID)
				_restrict.remove(player.getHWID());
			Log.logEvent(Log.EventLog.TvtUnRegPlayer, player.getName() + " [" + player.getObjectId() + "] Blue", dateString);
		}
		else if(players_list2.contains(player.getObjectId()))
		{
			players_list2.remove(Integer.valueOf(player.getObjectId()));
			live_list2.remove(Integer.valueOf(player.getObjectId()));
			player.setEventReg(0);
			if(Config.TvT_IP)
				_restrict.remove(player.getIP());
			if(Config.TvT_HWID)
				_restrict.remove(player.getHWID());
			Log.logEvent(Log.EventLog.TvtUnRegPlayer, player.getName() + " [" + player.getObjectId() + "] Red", dateString);
		}
		else
		{
			player.sendMessage(player.isLangRus() ? "Вы не являетесь участником TvT." : "You are not a participant of TvT.");
			return;
		}
		player.sendMessage(player.isLangRus() ? "Вы успешно удалены с регистрации на TvT." : "You have been removed from registration on TvT.");
	}

	public void addPlayer()
	{
		L2Player player = getSelf();
		if(player == null || !checkPlayer(player, true))
			return;

		if(players_list1.size() + players_list2.size() >= Config.TvT_MaxPlayers)
		{
			if(player.isLangRus())
				player.sendMessage("Достигнут лимит допустимого кол-ва участников.");
			else
				player.sendMessage("The limit on the number of participants allowed has been reached.");
			return;
		}

		if(Config.TvT_IP)
		{
			if(_restrict.contains(player.getIP()))
			{
				if(player.isLangRus())
					player.sendMessage("Игрок с данным IP уже зарегистрирован.");
				else
					player.sendMessage("The player with this IP is already registered.");
				return;
			}

			_restrict.add(player.getIP());
		}
		if(Config.TvT_HWID)
		{
			if(_restrict.contains(player.getHWID()))
			{
				if(player.isLangRus())
					player.sendMessage("Игрок с данным железом уже зарегистрирован.");
				else
					player.sendMessage("A player with this HWID is already registered.");
				return;
			}

			_restrict.add(player.getHWID());
		}
		int team = 0, size1 = players_list1.size(), size2 = players_list2.size();

		if(size1 > size2)
			team = 2;
		else if(size1 < size2)
			team = 1;
		else
			team = Rnd.get(1, 2);

		if(team == 1)
		{
			players_list1.add(player.getObjectId());
			live_list1.add(player.getObjectId());
			player.setEventReg(1);
			player.sendMessage(new CustomMessage("scripts.events.TvT.Registered", player));
			Log.logEvent(Log.EventLog.TvtRegPlayer, player.getName() + " [" + player.getObjectId() + "] Blue", dateString);
		}
		else if(team == 2)
		{
			players_list2.add(player.getObjectId());
			live_list2.add(player.getObjectId());
			player.setEventReg(1);
			player.sendMessage(new CustomMessage("scripts.events.TvT.Registered", player));
			Log.logEvent(Log.EventLog.TvtRegPlayer, player.getName() + " [" + player.getObjectId() + "] Red", dateString);
		}
		else
			_log.info("WTF??? Command id 0 in TvT...");
	}

	public static boolean checkPlayer(L2Player player, boolean first)
	{
		if(first && !_isRegistrationActive)
		{
			player.sendMessage(new CustomMessage("scripts.events.Late", player));
			return false;
		}

		if(first && (players_list1.contains(player.getObjectId()) || players_list2.contains(player.getObjectId())))
		{
			player.sendMessage(new CustomMessage("scripts.events.TvT.Cancelled", player));
			return false;
		}

		if(player.getLevel() < _minLevel || player.getLevel() > _maxLevel)
		{
			player.sendMessage(new CustomMessage("scripts.events.TvT.CancelledLevel", player));
			return false;
		}

		if(player.isMounted())
		{
			player.sendMessage(player.isLangRus() ? "Верхом на эвент нельзя." : "You can't do it while riding.");
			return false;
		}

		if(player.isInDuel())
		{
			player.sendMessage(new CustomMessage("scripts.events.TvT.CancelledDuel", player));
			return false;
		}

		if(player.getTeam() != 0)
		{
			player.sendMessage(new CustomMessage("scripts.events.TvT.CancelledOtherEvent", player));
			return false;
		}

		if(first && player.inEventReg())
		{
			player.sendMessage(new CustomMessage("scripts.events.TvT.CancelledOtherEvent", player));
			return false;
		}

		if(player.getOlympiadGameId() > 0 || player.isInOlympiadMode() || Olympiad.isRegistered(player))
		{
			player.sendMessage(new CustomMessage("scripts.events.TvT.CancelledOlympiad", player));
			return false;
		}

		if(player.isInParty() && player.getParty().isInDimensionalRift())
		{
			player.sendMessage(new CustomMessage("scripts.events.TvT.CancelledOtherEvent", player));
			return false;
		}

		if(player.isTeleporting())
		{
			player.sendMessage(new CustomMessage("scripts.events.TvT.CancelledTeleport", player));
			return false;
		}
		if(player.isCursedWeaponEquipped())
		{
			player.sendMessage(player.isLangRus() ? "С проклятым оружием на эвент нельзя." : "You can't do it with cursed weapon.");
			return false;
		}
		if(player.inObserverMode())
		{
			player.sendMessage(player.isLangRus() ? "В режиме просмотра на эвент нельзя." : "You can't do it while observing.");
			return false;
		}
		if(player.isJailed())
		{
			player.sendMessage(player.isLangRus() ? "Не получиться сбежать." : "You can't do it in jail.");
			return false;
		}
		if(Config.TvT_CustomItems && player.getActiveClassId() < 88)
		{
			player.sendMessage(player.isLangRus() ? "Необходима третья профессия." : "You must have 3rd class.");
			return false;
		}
		if(ArrayUtils.contains(Config.TVT_RESTRICTED_CLASS_IDS, player.getActiveClassId()))
		{
			player.sendMessage(player.isLangRus() ? "Данному классу запрещено участвовать в ивенте." : "This class is not allowed to participate in the event.");
			return false;
		}
		return true;
	}

	public static void prepare()
	{
		if(_instanceID == 0 && _zone.getIndex() == 4 && _zone.getType() == ZoneType.battle_zone)
		{
			DoorTable.getInstance().getDoor(24190002).closeMe();
			DoorTable.getInstance().getDoor(24190003).closeMe();
		}

		cleanPlayers();
		int size = players_list1.size() + players_list2.size();
		if(players_list1.isEmpty() || players_list2.isEmpty() || size < Config.TvT_MinPlayers)
		{
			sayToAll("scripts.events.TvT.AnnounceEventCancelled", null);
			cancelEvent();
			executeTask("events.TvT.TvT", "preLoad", new Object[0], 10000L);
			return;
		}

		Log.logEvent(Log.EventLog.TvtPrepare, "Size: " + size + " (B:"+players_list1.size() + ";R:" + players_list2.size() + ")", dateString);

		clearArena();
		_sbuffs = new ConcurrentHashMap<Integer, List<L2Effect>>(size);
		if(Config.TvT_CustomItems)
		{
			_equip = new ConcurrentHashMap<Integer, List<Integer>>(size);
			_destroy = new ConcurrentHashMap<Integer, List<Integer>>(size);
		}

		executeTask("events.TvT.TvT", "paralyzePlayers", new Object[0], 100L);
		executeTask("events.TvT.TvT", "teleportPlayersToColiseum", new Object[0], 3000L);
		executeTask("events.TvT.TvT", "go", new Object[0], Config.TvT_Time_Paralyze * 1000);
		sayToParticipants("scripts.events.TvT.AnnounceFinalCountdown", new String[]{String.valueOf(Config.TvT_Time_Paralyze)});
	}

	public static void go()
	{
		_status = 2;
		if(Config.TvT_CancelAllBuff)
			removeBuff();
		else
			upParalyzePlayers();
		if(checkInZone())
		{
			clearArena();
			return;
		}
		clearArena();
		buffPlayers();
		recheckPremiumTime();
		sayToParticipants("scripts.events.TvT.AnnounceFight", null);
		_endTask = executeTask("events.TvT.TvT", "endBattle", new Object[0], Config.TvT_Time_Battle * 60000);
		if(Config.TvT_Announce_Countdown_Time)
			_countdownTask = executeTask("events.TvT.TvT", "countdown", new Object[] { Config.TvT_Time_Battle, true }, 60000);
	}

	private static void recheckPremiumTime()
	{
		for(L2Player player : getPlayers(players_list1, players_list2))
		{
			if(player != null && player.isConnected())
			{
				String premBuffVariable = player.getVar(Config.PREM_BUFF_VAL);
				if(premBuffVariable != null)
				{
					long premiumBuffEndTime = Long.parseLong(premBuffVariable);
					if(premiumBuffEndTime > System.currentTimeMillis())
						player.setVar(Config.PREM_BUFF_VAL, String.valueOf(premiumBuffEndTime + (Config.TvT_Time_Battle * 60000)));
				}

				if(player.isPremium())
				{
					Bonus.giveBonusNoMsg(player, Config.TvT_Time_Battle * 60000, TimeUnit.MILLISECONDS);
				}
			}
		}
	}

	private static void saveBuffs(L2Player player)
	{
		List<L2Effect> effectList = player.getEffectList().getAllEffects();
		List<L2Effect> effects = new ArrayList<L2Effect>(effectList.size());
		for(L2Effect e : effectList)
			if(!e.getSkill().isToggle() && e.getSkill().isSaveable() && (!Config.DEL_AUGMENT_BUFFS || !e.getSkill().isItemSkill()))
			{
				L2Effect effect = e.getTemplate().getEffect(new Env(e.getEffector(), e.getEffected(), e.getSkill()));
				effect.setCount(e.getCount());
				effect.setPeriod(e.getCount() == 1 ? e.getPeriod() - e.getTime() : e.getPeriod());
				effects.add(effect);
			}
		if(!effects.isEmpty())
			_sbuffs.put(player.getObjectId(), effects);
	}

	private static void buffPlayers()
	{
		if(Config.EVENT_BUFFS_FIGHTER.length < 2 && Config.EVENT_BUFFS_MAGE.length < 2)
			return;
		for(L2Player player : getPlayers(players_list1, players_list2))
			if(player != null)
			{
				if(player.isMageClass())
				{
					if(Config.EVENT_BUFFS_MAGE.length > 1)
					{
						int n = 0;
						for(int i = 0; i < Config.EVENT_BUFFS_MAGE.length; i += 2)
							OlympiadGame.giveBuff(player, SkillTable.getInstance().getInfo(Config.EVENT_BUFFS_MAGE[i], Config.EVENT_BUFFS_MAGE[i + 1]), n++);
					}
				}
				else
				{
					if(Config.EVENT_BUFFS_FIGHTER.length > 1)
					{
						int n = 0;
						for(int i = 0; i < Config.EVENT_BUFFS_FIGHTER.length; i += 2)
							OlympiadGame.giveBuff(player, SkillTable.getInstance().getInfo(Config.EVENT_BUFFS_FIGHTER[i], Config.EVENT_BUFFS_FIGHTER[i + 1]), n++);
					}
				}
			}
	}

	private static boolean checkInZone()
	{
		for(L2Player player : getPlayers(players_list1, players_list2))
			if(player != null && !player.isInZone(_zone))
			{
				removePlayer(player);
				if(useBC && player.getVar("TvT_backCoords") == null)
					player.teleToClosestTown();
				else
					backPlayer(player);
			}

		if(players_list1.size() < 1 || players_list2.size() < 1)
		{
			endBattle();
			return true;
		}
		return false;
	}

	private static void removeBuff()
	{
		Map<String, Pair<Integer, Integer>> playersHaveAugments = new HashMap<>();
		for(L2Player player : getPlayers(players_list1, players_list2))
		{
			if(player != null)
			{
				try
				{
					for(L2Effect effect : player.getEffectList().getAllEffects())
					{
						if(effect == null)
							continue;
						L2Skill skill = effect.getSkill();
						if(skill != null && skill.isItemSkill())
						{
							playersHaveAugments.put(player.getName(), Pair.of(skill.getId(), skill.getLevel()));
						}
					}
					player.getEffectList().stopAllEffects();
					L2Summon summon = player.getPet();
					if(summon != null)
					{
						summon.getEffectList().stopAllEffects();
						if(summon.isPet() || (Config.EVENT_RESTRICTED_SUMMONS.length > 0 && ArrayUtils.contains(Config.EVENT_RESTRICTED_SUMMONS, summon.getNpcId())))
							summon.unSummon();
					}
					L2AgathionInstance agathion = player.getAgathion();
					if(agathion != null)
					{
						agathion.unSummon();
					}
					if(Config.TvT_NonActionDelay > 0)
					{
						player.eventAct = false;
						_tasks.put(player.getObjectId(), ThreadPoolManager.getInstance().schedule(new ActionCheck(player.getObjectId()), Config.TvT_NonActionDelay * 1000));
					}
				}
				catch(Exception e)
				{
					_log.error("TvT: fail on removeBuff", e);
				}
			}
		}

		if(!playersHaveAugments.isEmpty())
		{
			String playerAugmentInfo = "Remove augments: ";
			for(Map.Entry<String, Pair<Integer, Integer>> entry : playersHaveAugments.entrySet())
			{
				if(!playerAugmentInfo.isEmpty())
					playerAugmentInfo += ";";
				playerAugmentInfo += " Name=" + entry.getKey() + " SkillId=" + entry.getValue().getLeft() + " SkillLevel=" + entry.getValue().getRight();
			}
			Log.logEvent(Log.EventLog.TvtRemoveAugment, playerAugmentInfo, dateString);
		}
	}

	public static void endBattle()
	{
		if(_clearing)
			return;
		_clearing = true;
		try
		{
			if(_countdownTask != null)
			{
				_countdownTask.cancel(false);
				_countdownTask = null;
			}
		}
		catch(Exception e)
		{
		}
		try
		{
			if(_endTask != null)
			{
				_endTask.cancel(false);
				_endTask = null;
			}
		}
		catch(Exception e)
		{
		}

		if(_status == 0)
			return;

		_status = 0;
		removeAura();

		if(_instanceID == 0 && _zone.getIndex() == 4 && _zone.getType() == ZoneType.battle_zone)
		{
			DoorTable.getInstance().getDoor(24190002).openMe();
			DoorTable.getInstance().getDoor(24190003).openMe();
		}

		for(L2Player player : getPlayers(players_list1, players_list2))
		{
			player.getListeners().onTvtEvent(false);
		}

		String[] param = {String.valueOf(count1), String.valueOf(count2)};
		if(live_list1.isEmpty() && live_list2.isEmpty())
		{
			sayToAll("scripts.events.TvT.AnnounceFinishedDraw", param);
			if(Config.TvT_reward_draw.length > 1 || Config.TvT_reward_draw_premium.length > 1)
				giveItemsToWinner(0);
		}
		else if(live_list1.isEmpty())
		{
			sayToAll("scripts.events.TvT.AnnounceFinishedRedWins", param);
			giveItemsToWinner(2);
		}
		else if(live_list2.isEmpty())
		{
			sayToAll("scripts.events.TvT.AnnounceFinishedBlueWins", param);
			giveItemsToWinner(1);
		}
		else if(count1 > count2)
		{
			sayToAll("scripts.events.TvT.AnnounceFinishedBlueWins", param);
			giveItemsToWinner(1);
		}
		else if(count2 > count1)
		{
			sayToAll("scripts.events.TvT.AnnounceFinishedRedWins", param);
			giveItemsToWinner(2);
		}
		else
		{
			sayToAll("scripts.events.TvT.AnnounceFinishedDraw", param);
			if(Config.TvT_reward_draw.length > 1 || Config.TvT_reward_draw_premium.length > 1)
				giveItemsToWinner(0);
		}
		if(Config.TvT_NonActionDelay > 0)
		{
			for(Future<?> task : _tasks.values())
			{
				try
				{
					task.cancel(true);
				}
				catch(Exception e)
				{}
			}

			_tasks.clear();
		}
		sayToParticipants("scripts.events.TvT.AnnounceEnd", new String[]{String.valueOf(Config.EVENTS_TIME_BACK)});
		executeTask("events.TvT.TvT", "end", new Object[0], Config.EVENTS_TIME_BACK * 1000);
		_isRegistrationActive = false;
		_sbuffs.clear();
		count1 = 0;
		count2 = 0;
		_clearing = false;
	}

	public static void end()
	{
		ressurectPlayers();
		executeTask("events.TvT.TvT", "teleportPlayersToSavedCoords", new Object[0], 100L);
		executeTask("events.TvT.TvT", "preLoad", new Object[0], 10000L);
	}

	private static void giveItemsToWinner(int team)
	{
		for(L2Player player : getWinningTeamPlayers(team))
		{
			if(player != null)
			{
				int[] reward = getWinnerReward(player, team);
				String winnerTeam = team == 1 ? "Blue" : "Red";

				if(player.eventKills >= getMinWinnerKill(player))
				{
					String rewards = "";
					for(int i = 0; i < reward.length; i += 2)
					{
						if(!rewards.isEmpty())
							rewards += ";";
						int itemId = reward[i];
						long itemCount = reward[i + 1];
						addItem(player, itemId, itemCount, "<TvtRew1>");
						rewards += itemId + "," + itemCount;
					}

					Log.logEvent(Log.EventLog.TvtWinnerReward, player.getName() + " (Team: " + winnerTeam + ") Kills: " + player.eventKills + " WinnerReward: " + rewards, dateString);
				}

				player.getListeners().onTvtEvent(true);
			}
		}
		if(team != 0 && (Config.TvT_reward_losers.length > 1 || Config.TvT_reward_losers_premium.length > 1))
		{
			String looserTeam = team == 2 ? "Blue" : "Red";
			for(L2Player player : getLooserTeamPlayers(team))
			{
				if(player != null)
				{
					int[] reward = getLooserReward(player);

					if(player.eventKills >= getMinLooserKill(player))
					{
						String rewards = "";
						for(int i = 0; i < reward.length; i += 2)
						{
							if(!rewards.isEmpty())
								rewards += ";";
							int itemId = reward[i];
							long itemCount = reward[i + 1];
							addItem(player, itemId, itemCount, "<TvtRew2>");
							rewards += itemId + "," + itemCount;
						}
						Log.logEvent(Log.EventLog.TvtLooserReward, player.getName() + " (Team: " + looserTeam + ") Kills: " + player.eventKills + " LooserReward: " + rewards, dateString);
					}
				}
			}
		}
	}

	private static int[] getWinnerReward(L2Player player, int team)
	{
		if(team == 0)
		{
			if(player.isPremium())
				return Config.TvT_reward_draw_premium;
			else
				return Config.TvT_reward_draw;
		}
		else
		{
			if(player.isPremium())
				return Config.TvT_reward_final_premium;
			else
				return Config.TvT_reward_final;
		}
	}

	private static int[] getLooserReward(L2Player player)
	{
		if(player.isPremium())
			return Config.TvT_reward_losers_premium;
		else
			return Config.TvT_reward_losers;
	}

	private static GArray<L2Player> getWinningTeamPlayers(int team)
	{
		if(team == 1)
			return getPlayers(players_list1);
		else if(team == 2)
			return getPlayers(players_list2);
		else
			return getPlayers(players_list1, players_list2);
	}

	private static GArray<L2Player> getLooserTeamPlayers(int team)
	{
		if(team == 1)
			return getPlayers(players_list2);
		else
			return getPlayers(players_list1);
	}

	private static int getMinWinnerKill(L2Player player)
	{
		return player.isPremium() ? Config.TvT_MinKillsPremium : Config.TvT_MinKills;
	}

	private static int getMinLooserKill(L2Player player)
	{
		return player.isPremium() ? Config.TvT_LosersMinKillsPremium : Config.TvT_LosersMinKills;
	}

	public static void teleportPlayersToColiseum()
	{
		for(L2Player player : getPlayers(players_list1, players_list2))
		{
			if(player == null)
				continue;
			player.eventKills = 0;
			unRide(player);
			unSummonPet(player, true);
			if(useBC && !player.isInZone(ZoneType.no_restart) && !player.isInZone(ZoneType.epic))
				player.setVar("TvT_backCoords", player.getX() + " " + player.getY() + " " + player.getZ());
			player.leaveParty();
			List<Location> teamLoc = player.getTeam() == 1 ? Config.TvT_BlueTeamLoc : Config.TvT_RedTeamLoc;
			player.teleToLocation(Location.findAroundPosition(Rnd.get(teamLoc), 0, 100), _instanceID);
		}
	}

	public static void teleportPlayersToSavedCoords()
	{
		for(L2Player player : getPlayers(players_list1, players_list2))
			if(player != null)
				backPlayer(player);
		unsetLastCoords();
	}

	public static void paralyzePlayers()
	{
		L2Skill revengeSkill = SkillTable.getInstance().getInfo(L2Skill.SKILL_RAID_CURSE, 1);
		for(L2Player player : getPlayers(players_list1, players_list2))
		{
			if(player == null)
				continue;
			healPlayer(player);
			player.inEvent = true;
			player.inTvT = true;
			player.setEventReg(0);
			player.noToTown = true;
			player.addListener(_listeners);
			saveBuffs(player);
			if(Config.TvT_CustomItems)
			{
				List<Integer> items = new ArrayList<Integer>();
				for(L2ItemInstance item : player.getInventory().getPaperdollItems())
					if(item != null)
					{
						player.getInventory().unEquipItem(item);
						items.add(item.getObjectId());
					}
				if(!items.isEmpty())
					_equip.put(player.getObjectId(), items);
				List<Integer> cm = new ArrayList<Integer>();
				int cid = player.getActiveClassId();
				if(cid < 88)
				{
					if(ClassId.values()[cid].getLevel() < 3)
					{
						removePlayer(player);
						continue;
					}
					for(ClassId id : ClassId.values())
					{
						if(id.level() == 3 && id.getParent().getId() == cid)
						{
							cid = id.getId();
							break;
						}
					}
				}
				for(int id : Config.TVT_CUSTOM_ITEMS.get(cid))
				{
					L2ItemInstance item = ItemTable.getInstance().createItem(id);
					if(item.canBeEnchanted())
						item.setEnchantLevel(Config.TvT_CustomItemsEnchant);
					player.getInventory().addItem(item, false, false, "");
					if(item.isEquipable() && !item.isArrow())
						player.getInventory().equipItem(item, false);
					cm.add(item.getObjectId());
				}
				if(!cm.isEmpty())
					_destroy.put(player.getObjectId(), cm);
			}
			if(Config.EVENT_RESTRICTED_ITEMS.length > 0)
				for(L2ItemInstance item : player.getInventory().getItems())
					if(item != null && item.isEquipped() && ArrayUtils.contains(Config.EVENT_RESTRICTED_ITEMS, item.getItemId()))
						player.getInventory().unEquipItem(item);
			if(Config.TVT_RESTRICTED_SKILLS.length > 0)
				for(L2Skill skill : player.getAllSkills())
					if(ArrayUtils.contains(Config.TVT_RESTRICTED_SKILLS, skill.getId()))
						player.addUnActiveSkill(skill);
			if(Config.EVENT_DIS_HS)
			{
				if(player.isHero())
				{
					if(Config.ENABLE_OLYMPIAD && Hero.getInstance().getHeroes() != null && Hero.getInstance().getHeroes().containsKey(player.getObjectId()))
					{
						Hero.unActivateHeroSkills(player);
					}
					else if(Config.SERVICES_HERO_ALLOW_SKILLS)
					{
						Hero.unActivateHeroSkills(player);
					}
				}
			}
			player.getEffectList().stopEffects(EffectType.EffectImmunity);
			player.getEffectList().stopEffects(EffectType.Invulnerable);
			player.getEffectList().stopEffects(EffectType.Paralyze);
			player.getEffectList().stopEffects(EffectType.Petrification);
			OlympiadGame.giveBuff(player, revengeSkill, 0);
			if(player.getPet() != null)
				OlympiadGame.giveBuff(player.getPet(), revengeSkill, 0);
			if(player.getAgathion() != null)
				player.getAgathion().unSummon();
		}
	}

	public static void upParalyzePlayers()
	{
		for(L2Player player : getPlayers(players_list1, players_list2))
			if(player != null)
			{
				player.getEffectList().stopEffect(L2Skill.SKILL_RAID_CURSE);
				if(player.getPet() != null)
					player.getPet().getEffectList().stopEffect(L2Skill.SKILL_RAID_CURSE);
				if(Config.TvT_NonActionDelay > 0)
				{
					player.eventAct = false;
					_tasks.put(player.getObjectId(), ThreadPoolManager.getInstance().schedule(new ActionCheck(player.getObjectId()), Config.TvT_NonActionDelay * 1000));
				}
			}
	}

	private static void ressurectPlayers()
	{
		for(L2Player player : getPlayers(players_list1, players_list2))
		{
			healPlayer(player);
			skillsOn(player);
		}
	}

	private static void skillsOn(L2Player player)
	{
		if(player == null)
			return;

		boolean updateSkillList = false;
		if(Config.TVT_RESTRICTED_SKILLS.length > 0)
		{
			for(L2Skill skill : player.getAllSkills())
			{
				if(ArrayUtils.contains(Config.TVT_RESTRICTED_SKILLS, skill.getId()))
				{
					player.removeUnActiveSkill(skill);
					updateSkillList = true;
				}
			}
		}
		if(Config.EVENT_DIS_HS)
		{
			if(player.isHero())
			{
				if(Config.ENABLE_OLYMPIAD && Hero.getInstance().getHeroes() != null && Hero.getInstance().getHeroes().containsKey(player.getObjectId()))
				{
					Hero.activateHeroSkills(player);
					updateSkillList = true;
				}
				else if(Config.SERVICES_HERO_ALLOW_SKILLS)
				{
					Hero.activateHeroSkills(player);
					updateSkillList = true;
				}
			}
		}

		// Обновляем скилл лист, после добавления скилов
		if(updateSkillList)
			player.sendPacket(new SkillList(player));
	}

	private static void cleanPlayers()
	{
		for(L2Player player : getPlayers(players_list1))
			if(player != null)
				if(!checkPlayer(player, false))
					removePlayer(player);
				else
					player.setTeam(1, true);
		for(L2Player player : getPlayers(players_list2))
			if(player != null)
				if(!checkPlayer(player, false))
					removePlayer(player);
				else
					player.setTeam(2, true);
	}

	private static void checkLive()
	{
		GCSArray<Integer> new_live_list1 = new GCSArray<Integer>();
		GCSArray<Integer> new_live_list2 = new GCSArray<Integer>();

		for(Integer objId : live_list1)
		{
			L2Player player = L2ObjectsStorage.getPlayer(objId);
			if(player != null)
				new_live_list1.add(player.getObjectId());
		}

		for(Integer stId : live_list2)
		{
			L2Player player = L2ObjectsStorage.getPlayer(stId);
			if(player != null)
				new_live_list2.add(player.getObjectId());
		}

		live_list1 = new_live_list1;
		live_list2 = new_live_list2;

		if(live_list1.size() < 1 || live_list2.size() < 1)
			endBattle();
	}

	private static void removeAura()
	{
		for(L2Player player : getPlayers(live_list1, live_list2))
			if(player != null)
			{
				player.removeListener(_listeners);
				buffsItems(player);
				player.setTeam(0, false);
				player.inEvent = false;
				player.inTvT = false;
				player.setEventReg(0);
				player.noToTown = false;
			}
	}

	private static void clearArena()
	{
		for(L2Object obj : _zone.getObjects())
			if(obj != null)
			{
				L2Player player = obj.getPlayer();
				if(player != null && !live_list1.contains(player.getObjectId()) && !live_list2.contains(player.getObjectId()) && !player.isGM() && player.getInstanceId() == _instanceID)
					player.teleToClosestTown();
			}
	}

	private static void damex(L2Player player)
	{
		if(_status == 2 && player != null && player.getTeam() > 0)
		{
			removePlayer(player);
			checkLive();
		}
	}

	private static final PlayerListener _listeners = new PlayerListenerImpl();

	private static final class PlayerListenerImpl implements OnPlayerExitListener, OnDeathListener, OnMagicUseListener, OnEquipUnEquipItemListener
	{
		@Override
		public void onPlayerExit(L2Player player)
		{
			if(_status == 0 && _isRegistrationActive)
			{
				removePlayer(player);
				return;
			}
			if(_status == 1)
			{
				removePlayer(player);
				backPlayer(player);
				return;
			}
			if(_status == 2 && player != null && player.getTeam() > 0)
			{
				removePlayer(player);
				backPlayer(player);
				checkLive();
			}
		}

		@Override
		public void onDeath(L2Character actor, L2Character killer)
		{
			if(_status == 2 && actor != null && actor.getTeam() > 0)
			{
				L2Player pk = killer.getPlayer();
				if(pk != null && actor.getTeam() != pk.getTeam())
				{
					String killerTeam;
					String killedTeam;
					if(pk.getTeam() == 1)
					{
						count1++;
						killerTeam = "Blue";
						killedTeam = "Red";
					}
					else
					{
						count2++;
						killerTeam = "Red";
						killedTeam = "Blue";
					}

					pk.eventKills++;
					if(Config.TvT_reward.length > 1)
					{
						String rewards = "";
						for(int i = 0; i < Config.TvT_reward.length; i += 2)
						{
							if(!rewards.isEmpty())
								rewards += ";";
							int itemId = Config.TvT_reward[i];
							long itemCount = Math.round((Config.TvT_rate ? pk.getLevel() : 1) * Config.TvT_reward[i + 1]);
							addItem(pk, itemId, itemCount, "<TvtKill>");
							rewards += itemId + "," + itemCount;
						}
						Log.logEvent(Log.EventLog.TvtKillReward, pk.getName() + " (Team: " + killerTeam + ") KillReward: " + rewards, dateString);
					}

					String info = pk.getName() + " (Team: " + killerTeam + ") kill " + actor.getName() + " (Team: " + killedTeam + ")";
					Log.logEvent(Log.EventLog.TvtKill, info, dateString);
				}
				ThreadPoolManager.getInstance().schedule(new TeleportRes(actor.getObjectId()), Config.TvTResDelay * 1000L);
				actor.sendMessage(new CustomMessage("scripts.events.TvT.Ressurection", actor).addNumber(Config.TvTResDelay));
				checkLive();
			}
		}

		@Override
		public void onMagicUse(L2Character actor, L2Skill skill, L2Character target, boolean alt)
		{
			if(Config.TvT_CustomItems && actor != null && actor.isPlayer() && skill != null && skill.isItemSkill())
			{
				L2Player player = actor.getPlayer();
				String itemInfo = "";
				L2ItemInstance weaponItem = actor.getActiveWeaponInstance();
				if(weaponItem != null)
				{
					itemInfo += "id=" + weaponItem.getItemId() + " enchant=" + weaponItem.getEnchantLevel();
					if(weaponItem.isAugmented() && weaponItem.getAugmentation() != null && weaponItem.getAugmentation().getAugmentSkill() != null)
						itemInfo += " augmentId=" + weaponItem.getAugmentation().getAugmentSkill().getId() + " augmentLevel=" + weaponItem.getAugmentation().getAugmentSkill().getLevel();
				}
				String info = player.getName() + " _status=" + _status + " use augment skill " + skill.getName() + "(" + skill.getId() +":" + skill.getLevel() + ") " + itemInfo;
				Log.logEvent(Log.EventLog.TvtUseSkill, info, dateString);
				ThreadPoolManager.getInstance().schedule(() -> {
					player.getEffectList().stopEffect(skill.getId());
				}, 500);
			}
		}

		@Override
		public void onEquipUnEquipItem(L2Player player, L2ItemInstance item, boolean equip)
		{
			if(Config.TvT_CustomItems && item != null)
			{
				String itemInfo = "";
				if(item != null)
				{
					itemInfo += "id=" + item.getItemId() + " enchant=" + item.getEnchantLevel();
					if(item.isAugmented() && item.getAugmentation() != null && item.getAugmentation().getAugmentSkill() != null)
						itemInfo += " augmentId=" + item.getAugmentation().getAugmentSkill().getId() + " augmentLevel=" + item.getAugmentation().getAugmentSkill().getLevel();
				}
				String info = player.getName() + " _status=" + _status + " equip=" + (equip ? "true" : "false") + " " + itemInfo;
				Log.logEvent(Log.EventLog.TvtEquipUnequip, info, dateString);
			}
		}
	}

	private static void backPlayer(L2Player player)
	{
		boolean useBackCoord = Config.TvT_ReturnPoint.length < 3;
		if(useBackCoord)
		{
			try
			{
				String var = player.getVar("TvT_backCoords");
				if(var != null)
				{
					if(player.isLogoutStarted())
					{
						if(var.isEmpty())
						{
							player.setInstanceId(0);
							player.setXYZInvisible(MapRegionTable.getTeleToClosestTown(player));
							player.unsetVar("TvT_backCoords");
						}
						else
						{
							String[] coords = var.split(" ");
							if(coords.length >= 3)
							{
								player.setInstanceId(0);
								player.setXYZInvisible(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]));
								player.unsetVar("TvT_backCoords");
							}
						}
					}
					else
					{
						if(var.isEmpty())
						{
							player.teleToLocation(MapRegionTable.getTeleToClosestTown(player));
							player.unsetVar("TvT_backCoords");
						}
						else
						{
							String[] coords = var.split(" ");
							if(coords.length >= 3)
							{
								player.teleToLocation(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]));
								player.unsetVar("TvT_backCoords");
							}
						}
					}
				}
				else
				{
					if(player.isLogoutStarted())
					{
						player.setInstanceId(0);
						player.setXYZInvisible(MapRegionTable.getTeleToClosestTown(player));
					}
					else
					{
						player.teleToLocation(MapRegionTable.getTeleToClosestTown(player));
						player.unsetVar("TvT_backCoords");
					}
				}
			}
			catch(Exception e)
			{
				_log.error("TvT: fail teleport player on backPlayer", e);
			}
		}
		else
		{
			if(player.isLogoutStarted())
			{
				player.setInstanceId(0);
				player.setXYZInvisible(Location.findAroundPosition(Config.TvT_ReturnPoint[0], Config.TvT_ReturnPoint[1], Config.TvT_ReturnPoint[2], 0, 150, 0));
			}
			else
			{
				player.teleToLocation(Location.findAroundPosition(Config.TvT_ReturnPoint[0], Config.TvT_ReturnPoint[1], Config.TvT_ReturnPoint[2], 0, 150, 0));
			}
		}
	}

	private static class ZoneListener implements OnZoneEnterLeaveListener
	{
		@Override
		public void onZoneEnter(L2Zone zone, L2Character object)
		{
			if(object == null)
				return;
			L2Player player = object.getPlayer();
			if(_status > 0 && player != null && !live_list1.contains(player.getObjectId()) && !live_list2.contains(player.getObjectId()) && !player.isGM() && player.getInstanceId() == _instanceID)
				ThreadPoolManager.getInstance().schedule(new TeleportTask(object, MapRegionTable.getTeleToClosestTown(player)), 3000);
		}

		@Override
		public void onZoneLeave(L2Zone zone, L2Character object)
		{
			if(object == null)
				return;
			L2Player player = object.getPlayer();
			if(_status == 2 && player != null && player.getTeam() > 0 && (live_list1.contains(player.getObjectId()) || live_list2.contains(player.getObjectId())) && player.getInstanceId() == _instanceID)
			{
				double angle = Util.convertHeadingToDegree(object.getHeading()); // угол в градусах
				double radian = Math.toRadians(angle - 90); // угол в радианах
				int x = (int) (object.getX() + 50 * Math.sin(radian));
				int y = (int) (object.getY() - 50 * Math.cos(radian));
				int z = object.getZ();
				ThreadPoolManager.getInstance().schedule(new TeleportTask(object, new Location(x, y, z)), 3000);
			}
		}
	}

	public static class TeleportTask implements Runnable
	{
		Location loc;
		L2Character target;

		public TeleportTask(L2Character target, Location loc)
		{
			this.target = target;
			this.loc = loc;
			target.startStunning();
		}

		@Override
		public void run()
		{
			target.stopStunning();
			target.teleToLocation(loc, _instanceID);
		}
	}

	private static void unsetLastCoords()
	{
		if(useBC)
			mysql.set("DELETE FROM `character_variables` WHERE `name`='TvT_backCoords'");
	}

	private static void removePlayer(L2Player player)
	{
		if(player != null)
		{
			player.removeListener(_listeners);
			live_list1.remove(Integer.valueOf(player.getObjectId()));
			live_list2.remove(Integer.valueOf(player.getObjectId()));
			players_list1.remove(Integer.valueOf(player.getObjectId()));
			players_list2.remove(Integer.valueOf(player.getObjectId()));
			buffsItems(player);
			player.setTeam(0, false);
			player.inEvent = false;
			player.inTvT = false;
			player.setEventReg(0);
			player.noToTown = false;
			if(Config.TvT_NonActionDelay > 0)
				_tasks.remove(player.getObjectId());
			skillsOn(player);
		}
	}

	private static void buffsItems(L2Player player)
	{
		try
		{
			if(Config.TvT_CustomItems)
			{
				List<Integer> items = _destroy.remove(player.getObjectId());
				if(items != null)
					for(int id : items)
					{
						L2ItemInstance item = player.getInventory().getItemByObjectId(id);
						if(item != null && !item.isArrow())
							player.getInventory().destroyItem(item);
					}
				items = _equip.remove(player.getObjectId());
				if(items != null)
					for(int id : items)
					{
						L2ItemInstance item = player.getInventory().getItemByObjectId(id);
						if(item != null && item.isEquipable() && !item.isArrow())
							player.getInventory().equipItem(item, false);
					}
			}
			player.getEffectList().stopAllEffects();
			List<L2Effect> effectList = _sbuffs.remove(player.getObjectId());
			if(effectList != null)
				for(L2Effect e : effectList)
					player.getEffectList().addEffect(e);
		}
		catch(Exception e)
		{
			_log.error("TvT: failed restore buffs and items", e);
		}
	}

	private static class TeleportRes implements Runnable
	{
		private final int playerObjId;

		public TeleportRes(int id)
		{
			playerObjId = id;
		}

		@Override
		public void run()
		{
			L2Player player = L2ObjectsStorage.getPlayer(playerObjId);
			if(player != null && _status == 2 && player.getTeam() > 0)
			{
				if(player.isDead())
				{
					player.restoreExp();
					player.broadcastPacket(new Revive(player));
					player.setPendingRevive(true);
				}
				List<Location> teamLoc = player.getTeam() == 1 ? Config.TvT_BlueTeamResLoc : Config.TvT_RedTeamResLoc;
				player.teleToLocation(Location.findAroundPosition(teamLoc.get(Rnd.get(teamLoc.size() - 1)), 0, 100), _instanceID);
				if(player.isMageClass())
				{
					if(Config.EVENT_BUFFS_MAGE.length > 1)
					{
						int n = 0;
						for(int i = 0; i < Config.EVENT_BUFFS_MAGE.length; i += 2)
							OlympiadGame.giveBuff(player, SkillTable.getInstance().getInfo(Config.EVENT_BUFFS_MAGE[i], Config.EVENT_BUFFS_MAGE[i + 1]), n++);
					}
				}
				else if(Config.EVENT_BUFFS_FIGHTER.length > 1)
				{
					int n = 0;
					for(int i = 0; i < Config.EVENT_BUFFS_FIGHTER.length; i += 2)
						OlympiadGame.giveBuff(player, SkillTable.getInstance().getInfo(Config.EVENT_BUFFS_FIGHTER[i], Config.EVENT_BUFFS_FIGHTER[i + 1]), n++);
				}
			}
		}
	}

	private static class ActionCheck implements Runnable
	{
		private final int _id;
		private int state = 0;

		public ActionCheck(int id)
		{
			_id = id;
		}

		@Override
		public void run()
		{
			if(_status != 2)
				return;
			L2Player player = L2ObjectsStorage.getPlayer(_id);
			if(player == null)
			{
				_tasks.remove(_id);
				return;
			}
			if(state == 0)
			{
				if(!player.eventAct)
				{
					Functions.show("<br><font color=\"LEVEL\">Внимание! В случае бездействия в течении " + Config.TvT_NonActionDelay + " сек, Вы будете исключены с эвента!</font>", player);
					state = 1;
				}
			}
			else
			{
				if(!player.eventAct)
				{
					damex(player);
					backPlayer(player);
					Functions.show("<br><font color=\"LEVEL\">Вы исключены с эвента за бездействие!</font>", player);
					return;
				}

				state = 0;
			}
			player.eventAct = false;
			_tasks.put(_id, ThreadPoolManager.getInstance().schedule(this, Config.TvT_NonActionDelay * 1000));
		}
	}

	private static void loadCustomItems()
	{
		if(Config.TvT_CustomItems)
			try
			{
				File file = new File(Config.DATAPACK_ROOT, "config/Advanced/tvt_items.xml");
				if(!file.exists())
				{
					_log.error("TvT: not found config/Advanced/tvt_items.xml !!!");
					return;
				}
				Config.TVT_CUSTOM_ITEMS = new ConcurrentHashMap<Integer, List<Integer>>(31);
				Document doc = DocumentFactory.getInstance().loadDocument(file);
				Node n = doc.getFirstChild();
				for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					if("class".equalsIgnoreCase(d.getNodeName()))
					{
						NamedNodeMap attrs = d.getAttributes();
						int classId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
						List<Integer> is = null;
						for(Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
							if("equipment".equalsIgnoreCase(cd.getNodeName()))
							{
								attrs = cd.getAttributes();
								String items = attrs.getNamedItem("items").getNodeValue().trim();
								if(items != null)
								{
									String[] itemsSplit = items.split(",");
									is = new ArrayList<Integer>(itemsSplit.length);
									for(String element : itemsSplit)
										is.add(Integer.parseInt(element));
								}
							}
						if(is != null)
							Config.TVT_CUSTOM_ITEMS.put(classId, is);
					}
			}
			catch(Exception e)
			{
				_log.error("TvT: error load tvt_items.xml", e);
			}
	}

	public static void preLoad()
	{
		if(!_active)
			return;

		if(noStart(false))
		{
			_date.add(Config.TvT_Allow_Calendar_Day ? 2 : 5, 1);
			noStart(true);
		}

		if(Config.TVT_ALLOW_INFO_LOG_SCHEDULE)
			printInfo();
	}

	private static void printInfo()
	{
		StringBuilder sb = new StringBuilder();

		if(Config.TvT_Allow_Calendar_Day)
		{
			for(int day = 4, i = 0; i < Config.TvT_Time_Start.length; i += day)
			{
				Calendar date = Calendar.getInstance();
				date.set(Calendar.SECOND, 5);

				date.set(Calendar.HOUR_OF_DAY, Config.TvT_Time_Start[i + 1]);
				date.set(Calendar.MINUTE, Config.TvT_Time_Start[i + 2]);
				date.set(Calendar.DAY_OF_MONTH, Config.TvT_Time_Start[i]);

				if(date.getTimeInMillis() > System.currentTimeMillis())
				{
					if(!sb.toString().isEmpty())
						sb.append(";");
					sb.append(TimeUtils.toSimpleFormat(date.getTimeInMillis()));
				}
			}
		}
		else
		{
			// Добавляем цикл для двух дней
			for(int d = 0; d < 2; d++)
			{
				for(int day = 3, i = 0; i < Config.TvT_Time_Start.length; i += day)
				{
					Calendar date = Calendar.getInstance();
					date.set(Calendar.SECOND, 5);
					date.set(Calendar.HOUR_OF_DAY, Config.TvT_Time_Start[i]);
					date.set(Calendar.MINUTE, Config.TvT_Time_Start[i + 1]);
					date.add(Calendar.DAY_OF_MONTH, d); // Добавляем d дней

					if(date.getTimeInMillis() > System.currentTimeMillis())
					{
						if(!sb.toString().isEmpty())
							sb.append(";");
						sb.append(TimeUtils.toSimpleFormat(date.getTimeInMillis()));
					}
				}
			}
		}

		_log.info("TvT: competitions schedule at [" + sb.toString() + "]");
	}

	private static boolean noStart(boolean msg)
	{
		_date = Calendar.getInstance();
		_date.set(Calendar.SECOND, 5);
		boolean isEventScheduled = false;
		for(int day = Config.TvT_Allow_Calendar_Day ? 4 : 3, i = 0; i < Config.TvT_Time_Start.length; i += day)
		{
			if(Config.TvT_Allow_Calendar_Day)
			{
				_date.set(Calendar.DAY_OF_MONTH, Config.TvT_Time_Start[i]);
				_date.set(Calendar.HOUR_OF_DAY, Config.TvT_Time_Start[i + 1]);
				_date.set(Calendar.MINUTE, Config.TvT_Time_Start[i + 2]);
			}
			else
			{
				_date.set(Calendar.HOUR_OF_DAY, Config.TvT_Time_Start[i]);
				_date.set(Calendar.MINUTE, Config.TvT_Time_Start[i + 1]);
			}
			if(_date.getTimeInMillis() > System.currentTimeMillis() + 2000L)
			{
				_pre_category = Config.TvT_Time_Start[i + (Config.TvT_Allow_Calendar_Day ? 3 : 2)];
				try
				{
					if(_startTask != null)
					{
						_startTask.cancel(false);
					}
				}
				catch(Exception e)
				{}
				_startTask = executeTask("events.TvT.TvT", "preStartTask", new Object[0], (int) (_date.getTimeInMillis() - System.currentTimeMillis()));
				isEventScheduled = true;
				break;
			}
		}

		if(!isEventScheduled && !Config.TvT_Allow_Calendar_Day)
		{
			// Если все события на текущий день уже прошли, установим дату на следующий день и запланируем первое событие
			_date.add(Calendar.DAY_OF_MONTH, 1);
			_date.set(Calendar.HOUR_OF_DAY, Config.TvT_Time_Start[0]);
			_date.set(Calendar.MINUTE, Config.TvT_Time_Start[1]);
			_pre_category = Config.TvT_Time_Start[Config.TvT_Allow_Calendar_Day ? 3 : 2];
			try
			{
				if(_startTask != null)
				{
					_startTask.cancel(false);
				}
			}
			catch(Exception e)
			{}
			_startTask = executeTask("events.TvT.TvT", "preStartTask", new Object[0], (int) (_date.getTimeInMillis() - System.currentTimeMillis()));
			isEventScheduled = true; // Устанавливаем isEventScheduled в true, так как мы запланировали событие на следующий день
		}

		if(msg && !isEventScheduled)
			_log.warn("TvT not loaded! Check TvT_Time_Start in events.properties");
		return !isEventScheduled;
	}

	public static void preStartTask()
	{
		if(_active)
			startOk(new String[]{String.valueOf(_pre_category)});
	}
}