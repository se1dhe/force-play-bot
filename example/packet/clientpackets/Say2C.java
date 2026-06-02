package l2p.gameserver.clientpackets;

import l2p.commons.util.GArray;
import l2p.gameserver.Config;
import l2p.gameserver.cache.ItemInfoCache;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.configuration.ConfigBattleGround;
import l2p.gameserver.data.xml.holder.ChatFilterHolder;
import l2p.gameserver.handler.IVoicedCommandHandler;
import l2p.gameserver.handler.VoicedCommandHandler;
import l2p.gameserver.instancemanager.PetitionManager;
import l2p.gameserver.model.L2CommandChannel;
import l2p.gameserver.model.L2Object;
import l2p.gameserver.model.L2ObjectsStorage;
import l2p.gameserver.model.L2Party;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2World;
import l2p.gameserver.model.L2WorldRegion;
import l2p.gameserver.model.PartyRoom;
import l2p.gameserver.model.chatfilter.ChatFilter;
import l2p.gameserver.model.chatfilter.ChatMsg;
import l2p.gameserver.model.chatfilter.ChatType;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.multilang.CustomMessage;
import l2p.gameserver.scripts.Functions;
import l2p.gameserver.serverpackets.Say2;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.tables.MapRegionTable;
import l2p.gameserver.utils.Location;
import l2p.gameserver.utils.Log;
import l2p.gameserver.utils.SpamFilter;
import l2p.gameserver.utils.Strings;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Say2C extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(Say2C.class);
	/** RegExp для кэширования ссылок на предметы, пример ссылки: \b\tType=1 \tID=268484598 \tColor=0 \tUnderline=0 \tTitle=\u001BAdena\u001B\b */
	private static final Pattern EX_ITEM_LINK_PATTERN = Pattern.compile("[\b]\tType=[0-9]+[\\s]+\tID=([0-9]+)[\\s]+\tColor=[0-9]+[\\s]+\tUnderline=[0-9]+[\\s]+\tClassID=[0-9]+[\\s]+\tTitle=\u001B(.[^\u001B]*)[^\b]");
	private static final Pattern SKIP_ITEM_LINK_PATTERN = Pattern.compile("[\b]\tType=[0-9]+(.[^\b]*)[\b]");

	public final static int ALL = 0;
	public final static int SHOUT = 1; //!
	public final static int TELL = 2; //\"
	public final static int PARTY = 3; //#
	public final static int CLAN = 4; //@
	public final static int GM = 5;
	public final static int PETITION_PLAYER = 6; // used for petition
	public final static int PETITION_GM = 7; //* used for petition
	public final static int TRADE = 8; //+
	public final static int ALLIANCE = 9; //$
	public final static int ANNOUNCEMENT = 10;
	public final static int SYSTEM_MESSAGE = 11;
	public final static int L2FRIEND = 11;
	public static final int PARTY_ROOM = 14;
	public final static int COMMANDCHANNEL_ALL = 15; //`` (pink) команды лидера СС
	public final static int COMMANDCHANNEL_COMMANDER = 16; //` (yellow) команды лидеров партий в СС
	public final static int HERO_VOICE = 17; //%
	public final static int CRITICAL_ANNOUNCEMENT = 18; //dark cyan

	public final static String[] chatNames = { "ALL", "SHOUT", "TELL", "PARTY", "CLAN", "GM", "PETITION_PLAYER", "PETITION_GM", "TRADE", "ALLIANCE", "ANNOUNCEMENT", "", "", "", "PARTY_ROOM", "COMMANDCHANNEL_ALL", "COMMANDCHANNEL_COMMANDER", "HERO_VOICE", "CRITICAL_ANNOUNCEMENT" };

	private static Map<Integer, Long> VIP_DELAYS = new ConcurrentHashMap<Integer, Long>();
	private static final long VIP_DELAY = 300000L;

	private String _text;
	private int _type;
	private String _target;

	@Override
	public void readImpl()
	{
		_text = readS(Config.CHAT_MESSAGE_MAX_LEN);
		try
		{
			_type = readD();
		}
		catch (Exception e)
		{
			_type = 0;
		}
		_target = _type == TELL ? readS(Config.CNAME_MAXLEN) : null;
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(activeChar.isKeyBlocked())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(_type < 0 || _type > chatNames.length || _text == null || _text.isEmpty())
		{
			activeChar.sendActionFailed();
			return;
		}

		_text = filter(_text);

		/*if(_text.contains("\n"))
		{
			String[] lines = _text.split("\n");
			_text = "";
			for(int i = 0; i < lines.length; i++)
			{
				lines[i] = lines[i].trim();
				if(lines[i].length() == 0)
					continue;
				if(_text.length() > 0)
					_text += "\n  >";
				_text += lines[i];
			}
		}*/

		if(_text.length() == 0)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(Config.VIKTORINA_ENABLED && Functions.isEventStarted("events.Viktorina.Viktorina"))
		{
			String answer = _text.trim();
			if(answer.length() > 0)
			{
				Object[] objects = {answer, activeChar};
				Functions.callScripts("events.Viktorina.Viktorina", "checkAnswer", objects);
			}
		}

		if(_text.startsWith("."))
		{
			String fullcmd = _text.substring(1).trim();
			String command = fullcmd.split("\\s+")[0];
			String args = fullcmd.substring(command.length()).trim();

			if(command.length() > 0)
			{
				// then check for VoicedCommands
				IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(command);
				if(vch != null)
				{
					vch.useVoicedCommand(command, activeChar, args);
					return;
				}
			}
			return;
		}

		if(activeChar.inLH)
		{
			activeChar.sendActionFailed();
			return;
		}

		_text = _text.replaceAll("_", " ");

		L2Player receiver = _target == null ? null : L2World.getPlayer(_target);

		long currentTimeMillis = System.currentTimeMillis();

		//пропускать фильтры для GM
		String origText = _text;
		if(!activeChar.getPlayerAccess().CanAnnounce)
		{
			loop: for(ChatFilter f : ChatFilterHolder.getInstance().getFilters())
			{
				if(f.isMatch(activeChar, ChatType.getTypeById(_type), _text, receiver))
				{
					switch(f.getAction())
					{
						case ChatFilter.ACTION_BAN_CHAT:
							activeChar.updateNoChannel(Integer.parseInt(f.getValue()) * 1000L);
							break loop;
						case ChatFilter.ACTION_WARN_MSG:
							activeChar.sendMessage(new CustomMessage(f.getValue(), activeChar));
							return;
						case ChatFilter.ACTION_REPLACE_MSG:
							_text = f.getValue();
							break loop;
						case ChatFilter.ACTION_REDIRECT_MSG:
							_type = Integer.parseInt(f.getValue());
							continue loop;
					}
				}
			}
		}

		if(activeChar.getNoChannel() != 0 && ArrayUtils.contains(Config.BAN_CHANNEL_LIST, _type))
		{
			if(activeChar.getNoChannelRemained() > 0 || activeChar.getNoChannel() < 0)
			{
				if(activeChar.getNoChannel() > 0)
				{
					int timeRemained = Math.max((int)(activeChar.getNoChannelRemained() / 60000), 1);
					activeChar.sendMessage(new CustomMessage("common.ChatBanned", activeChar).addNumber(timeRemained));
				}
				else
					activeChar.sendMessage(new CustomMessage("common.ChatBannedPermanently", activeChar));
				activeChar.sendActionFailed();
				return;
			}
			activeChar.updateNoChannel(0);
		}

		boolean noSpam = activeChar.isGM() ? true : SpamFilter.getInstance().checkSpam(activeChar, _text, _type);

		if(ArrayUtils.contains(Config.FILTER_CHANNEL_LIST, _type) && !activeChar.isGM())
		{
			if(Config.MAT_REPLACE)
			{
				if(Config.MAT_WORDS_REPLACE)
				{
					for(Pattern pattern : Config.MAT_LIST)
						_text = pattern.matcher(_text).replaceAll(Config.MAT_REPLACE_STRING);
				}
				else if(Config.containsAbuseWord(_text))
					_text = Config.MAT_REPLACE_STRING;
			}
			else if(Config.MAT_BANCHAT && Config.containsAbuseWord(_text))
			{
				if(activeChar.isLangRus())
					activeChar.sendMessage("Вы получили " + "бан чата. До " + "окончания бана: " + Config.UNCHATBANTIME + " мин.");
				else
					activeChar.sendMessage("You are banned in chats. Time to unban: " + Config.UNCHATBANTIME + " min.");
				Log.addLog(activeChar + ": " + _text, "abuse");
				activeChar.updateNoChannel(Config.UNCHATBANTIME * 60000);
				activeChar.sendActionFailed();
				return;
			}
		}

		// Кэширование линков предметов
		Matcher m = EX_ITEM_LINK_PATTERN.matcher(_text);
		L2ItemInstance item;
		int objectId = -1;

		while(m.find())
		{
			objectId = Integer.parseInt(m.group(1));
			item = activeChar.getInventory().getItemByObjectId(objectId);

			if(item == null)
			{
				activeChar.sendActionFailed();
				break;
			}

			ItemInfoCache.getInstance().put(item);
		}

		String translit = activeChar.getVar("translit");
		if(translit != null)
		{
			//Исключаем из транслитерации ссылки на предметы
			m = SKIP_ITEM_LINK_PATTERN.matcher(_text);
			StringBuilder sb = new StringBuilder();
			int end = 0;
			while(m.find())
			{
				sb.append(Strings.fromTranslit(_text.substring(end, end = m.start()), translit.equals("tl") ? 1 : 2));
				sb.append(_text.substring(end, end = m.end()));
			}

			_text = sb.append(Strings.fromTranslit(_text.substring(end, _text.length()), translit.equals("tl") ? 1 : 2)).toString();
		}

		String textClassic = _text;
		String textInterlude = _text;

		m = EX_ITEM_LINK_PATTERN.matcher(textInterlude);
		StringBuffer sb = new StringBuffer();
		while(m.find())
		{
			objectId = Integer.parseInt(m.group(1));
			item = activeChar.getInventory().getItemByObjectId(objectId);

			String name;
			if(item.getEnchantLevel() > 0)
				name = "+" + item.getEnchantLevel() + " " + item.getName() + (item.getItem().getAdditionalName().isEmpty() ? "" : " " + item.getItem().getAdditionalName());
			else
				name = item.getName() + (item.getItem().getAdditionalName().isEmpty() ? "" : " " + item.getItem().getAdditionalName());

			m.appendReplacement(sb, Matcher.quoteReplacement(name));
		}

		m.appendTail(sb);
		textInterlude = sb.toString();

		if(Config.VIP_GLOBAL_CHAT && _text.startsWith(">"))
		{
			if(_type != 0 || !activeChar.isVIP())
			{
				activeChar.sendActionFailed();
				return;
			}
			if(!activeChar.isGM())
			{
				if(VIP_DELAYS.containsKey(activeChar.getObjectId()))
				{
					long time = VIP_DELAY + VIP_DELAYS.get(activeChar.getObjectId());
					if (time > System.currentTimeMillis())
					{
						if(activeChar.isLangRus())
							activeChar.sendMessage("VIP чат доступен раз в 5 минут. До повторного использования осталось " + Math.max((time - System.currentTimeMillis()) / 1000L, 1L) + " сек.");
						else
							activeChar.sendMessage("VIP chat is allowed once per 5 minutes. Left to wait " + Math.max((time - System.currentTimeMillis()) / 1000L, 1L) + " sec.");

						activeChar.sendActionFailed();
						return;
					}
				}
				VIP_DELAYS.put(activeChar.getObjectId(), System.currentTimeMillis());
			}
			String msgInterlude = textInterlude.substring(1);
			String msgClassic = textClassic.substring(1);

			Say2 packetClassic = new Say2(activeChar.getObjectId(), COMMANDCHANNEL_COMMANDER, activeChar.inBattleGround && ConfigBattleGround.BATTLE_GROUND_SHOW_KILLS ? "BattleGround" : Config.VIP_CHAT_MESSAGE_APPEND + activeChar.getName(), msgClassic);
			Say2 packetInterlude = new Say2(activeChar.getObjectId(), COMMANDCHANNEL_COMMANDER, activeChar.inBattleGround && ConfigBattleGround.BATTLE_GROUND_SHOW_KILLS ? "BattleGround" : Config.VIP_CHAT_MESSAGE_APPEND + activeChar.getName(), msgInterlude);
			for(L2Player player : L2ObjectsStorage.getAllPlayersForIterate())
				if(!player.isBlockAll() && player != activeChar && (noSpam || activeChar.getHWID().equals(player.getHWID())))
					player.sendPacket(getPacketFor(activeChar, packetClassic, packetInterlude));

			activeChar.sendPacket(getPacketFor(activeChar, packetClassic, packetInterlude));
			if(activeChar.isITClient())
				Log.LogChat("VIP", activeChar.getName(), null, msgInterlude);
			else
				Log.LogChat("VIP", activeChar.getName(), null, msgClassic);
			return;
		}

		boolean allow;
		Say2 packetClassic = new Say2(activeChar.getObjectId(), _type, activeChar.inBattleGround && ConfigBattleGround.BATTLE_GROUND_SHOW_KILLS ? "BattleGround" : activeChar.getName(), textClassic);
		Say2 packetInterlude = new Say2(activeChar.getObjectId(), _type, activeChar.inBattleGround && ConfigBattleGround.BATTLE_GROUND_SHOW_KILLS ? "BattleGround" : activeChar.getName(), textInterlude);
		int mapregion = MapRegionTable.getInstance().getMapRegion(activeChar.getX(), activeChar.getY());
		long curTime = System.currentTimeMillis();
		switch (_type)
		{
			case TELL:
				if(activeChar.getLevel() < Config.TELL_CHAT_MIN_LVL && activeChar.getSubClasses().size() < 2 && !activeChar.isGM())
				{
					activeChar.sendMessage(activeChar.isLangRus() ? ("Приватный чат доступен только с " + Config.TELL_CHAT_MIN_LVL + "-го уровня.") : ("Tell chat is allowed from " + Config.TELL_CHAT_MIN_LVL + " level only."));
					activeChar.sendActionFailed();
					return;
				}
				if(Config.NO_TELL_JAILED && activeChar.isJailed() && !activeChar.isGM())
				{
					activeChar.sendMessage(activeChar.isLangRus() ? "Приватный чат недоступен, пока Вы находитесь в тюрьме." : "Tell chat cannot be used in jail.");
					activeChar.sendActionFailed();
					return;
				}

				if(receiver == null || (!receiver.isConnected() && !receiver.isFashion))
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.S1_IS_NOT_LOGGED_IN).addString(_target), Msg.ActionFail);
					break;
				}
				else if(!receiver.isInBlockList(activeChar))
				{
					allow = activeChar.getLevel() >= Config.TELL_DELAY_LEVEL || Config.TELL_DELAY_LEVEL <= 80 && activeChar.getSubClasses().size() > 1;
					if(activeChar.getLastTellTime() + (allow ? 1000L : Config.TELL_DELAY_TIME * 1000L) > curTime && !activeChar.isGM())
					{
						if(activeChar.isLangRus())
							activeChar.sendMessage("Приватный чат доступен раз в " + (allow ? "секунду." : new StringBuilder().append(Config.TELL_DELAY_TIME).append(" сек до ").append(Config.TELL_DELAY_LEVEL).append("-го уровня.").toString()));
						else
							activeChar.sendMessage("Tell chat is allowed once per " + (allow ? "1 second." : new StringBuilder().append(Config.TELL_DELAY_TIME).append(" seconds before ").append(Config.TELL_DELAY_LEVEL).append(" level.").toString()));
						return;
					}
					activeChar.setLastTellTime(curTime);
					if(!receiver.getMessageRefusal())
					{
						if(noSpam || activeChar.getHWID().equals(receiver.getHWID()))
							receiver.sendPacket(getPacketFor(receiver, packetClassic, packetInterlude));

						packetClassic = new Say2(activeChar.getObjectId(), _type, "->" + receiver.getName(), textClassic);
						packetClassic.setSenderInfo(activeChar, receiver);

						packetInterlude = new Say2(activeChar.getObjectId(), _type, "->" + receiver.getName(), textInterlude);
						packetInterlude.setSenderInfo(activeChar, receiver);

						activeChar.sendPacket(getPacketFor(activeChar, packetClassic, packetInterlude));
					}
					else
						activeChar.sendPacket(new SystemMessage(SystemMessage.THE_PERSON_IS_IN_A_MESSAGE_REFUSAL_MODE));
				}
				else
					activeChar.sendPacket(Msg.YOU_HAVE_BEEN_BLOCKED_FROM_THE_CONTACT_YOU_SELECTED, Msg.ActionFail);
				break;
			case SHOUT:
				if(Config.NO_TS_JAILED && activeChar.isJailed() && !activeChar.isGM())
				{
					if(activeChar.isLangRus())
						activeChar.sendMessage("Крик и торговля в чате недоступны, пока Вы находитесь в тюрьме.");
					else
						activeChar.sendMessage("Shout and trade chatting cannot be used in jail.");
					return;
				}
				if(activeChar.getLevel() < Config.SHOUT_CHAT_MIN_LVL && activeChar.getSubClasses().size() < 2 && !activeChar.isGM())
				{
					if(activeChar.isLangRus())
						activeChar.sendMessage("Крик доступен только с " + Config.SHOUT_CHAT_MIN_LVL + "-го уровня.");
					else
						activeChar.sendMessage("Shout chat is allowed from " + Config.SHOUT_CHAT_MIN_LVL + " level only.");
					return;
				}
				if(activeChar.isCursedWeaponEquipped() && !activeChar.isGM())
				{
					if(activeChar.isLangRus())
						activeChar.sendMessage("Крик и торговля в чате недоступны, пока у Вас есть проклятое оружие.");
					else
						activeChar.sendMessage("Shout and trade chatting cannot be used while possessing a cursed weapon.");
					return;
				}
				if(activeChar.inObserverMode() && !activeChar.isGM())
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_CHAT_LOCALLY_WHILE_OBSERVING));
					return;
				}
				if(activeChar.getLastShoutTime() + Config.SHOUT_CHAT_DELAY * 1000L > curTime && !activeChar.isGM())
				{
					if(activeChar.isLangRus())
						activeChar.sendMessage("Шаут доступен раз в " + Config.SHOUT_CHAT_DELAY + " сек.");
					else
						activeChar.sendMessage("Shout chat is allowed once per " + Config.SHOUT_CHAT_DELAY + " " + "seconds.");
					return;
				}
				activeChar.setLastShoutTime(curTime);
				if(activeChar.getLevel() >= Config.GLOBAL_CHAT || activeChar.isGM())
				{
					for(L2Player player : L2ObjectsStorage.getAllPlayersForIterate())
						if(!player.isBlockAll() && player != activeChar && (noSpam || activeChar.getHWID().equals(player.getHWID())))
							player.sendPacket(getPacketFor(player, packetClassic, packetInterlude));
				}
				else if(Config.SHOUT_CHAT_MODE == 1)
				{
					for(L2Player player : L2World.getAroundPlayers(activeChar, Config.CHAT_RANGE_FIRST_MODE, 5000))
						if(!player.isBlockAll() && player != activeChar && (noSpam || activeChar.getHWID().equals(player.getHWID())))
							player.sendPacket(getPacketFor(player, packetClassic, packetInterlude));
				}
				else
					for(L2Player player : L2ObjectsStorage.getAllPlayersForIterate())
						if(!player.isBlockAll() && player != activeChar && MapRegionTable.getInstance().getMapRegion(player.getX(), player.getY()) == mapregion  && (noSpam || activeChar.getHWID().equals(player.getHWID())))
							player.sendPacket(getPacketFor(player, packetClassic, packetInterlude));
				activeChar.sendPacket(getPacketFor(activeChar, packetClassic, packetInterlude));
				break;
			case TRADE:
				if(Config.NO_TS_JAILED && activeChar.isJailed() && !activeChar.isGM())
				{
					if(activeChar.isLangRus())
						activeChar.sendMessage("Крик и торговля в чате недоступны, пока Вы находитесь в тюрьме.");
					else
						activeChar.sendMessage("Shout and trade chatting cannot be used in jail.");
					return;
				}
				if(activeChar.getLevel() < Config.TRADE_CHAT_MIN_LVL && activeChar.getSubClasses().size() < 2 && !activeChar.isGM())
				{
					if(activeChar.isLangRus())
						activeChar.sendMessage("Торговля в чате доступна только с " + Config.TRADE_CHAT_MIN_LVL + "-го уровня.");
					else
						activeChar.sendMessage("Trade chat is allowed from " + Config.TRADE_CHAT_MIN_LVL + " level only.");
					return;
				}
				if(activeChar.isCursedWeaponEquipped() && !activeChar.isGM())
				{
					if(activeChar.isLangRus())
						activeChar.sendMessage("Крик и торговля в чате недоступны, пока у Вас есть проклятое оружие.");
					else
						activeChar.sendMessage("Shout and trade chatting cannot be used while possessing a cursed weapon.");
					return;
				}
				if(activeChar.inObserverMode() && !activeChar.isGM())
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_CHAT_LOCALLY_WHILE_OBSERVING));
					return;
				}
				if(activeChar.getLastTradeTime() + Config.TRADE_CHAT_DELAY * 1000L > curTime && !activeChar.isGM())
				{
					if(activeChar.isLangRus())
						activeChar.sendMessage("Торговля в чате доступна раз в " + Config.TRADE_CHAT_DELAY + " сек.");
					else
						activeChar.sendMessage("Trade chat is allowed once per " + Config.TRADE_CHAT_DELAY + " seconds.");
					return;
				}
				activeChar.setLastTradeTime(curTime);
				if(activeChar.getLevel() >= Config.GLOBAL_TRADE_CHAT || activeChar.isGM())
				{
					for(L2Player player : L2ObjectsStorage.getAllPlayersForIterate())
						if(!player.isBlockAll() && player != activeChar && (noSpam || activeChar.getHWID().equals(player.getHWID())))
							player.sendPacket(getPacketFor(player, packetClassic, packetInterlude));
				}
				else if(Config.TRADE_CHAT_MODE == 1)
				{
					for(L2Player player : L2World.getAroundPlayers(activeChar, Config.CHAT_RANGE_FIRST_MODE, 5000))
						if(!player.isBlockAll() && player != activeChar  && (noSpam || activeChar.getHWID().equals(player.getHWID())))
							player.sendPacket(getPacketFor(player, packetClassic, packetInterlude));
				}
				else
					for(L2Player player : L2ObjectsStorage.getAllPlayersForIterate())
						if(!player.isBlockAll() && player != activeChar && MapRegionTable.getInstance().getMapRegion(player.getX(), player.getY()) == mapregion && (noSpam || activeChar.getHWID().equals(player.getHWID())))
							player.sendPacket(getPacketFor(player, packetClassic, packetInterlude));
				activeChar.sendPacket(getPacketFor(activeChar, packetClassic, packetInterlude));
				break;
			case ALL:
				if(activeChar.getLevel() < Config.ALL_CHAT_MIN_LVL && activeChar.getSubClasses().size() < 2 && !activeChar.isGM())
				{
					activeChar.sendMessage(activeChar.isLangRus() ? ("Белый чат доступен только с " + Config.ALL_CHAT_MIN_LVL + "-го уровня.") : ("White chat is allowed from " + Config.ALL_CHAT_MIN_LVL + " level only."));
					return;
				}
				if(activeChar.inObserverMode())
				{
					if(Config.SPECTATE_CHAT_RANGE <= 0)
					{
						activeChar.sendMessage(activeChar.isLangRus() ? "Чат в наблюдении недоступен." : "Spectators chat disabled.");
						activeChar.sendActionFailed();
						return;
					}
					if(activeChar.getObservePoint() == null)
						return;
					Location loc = activeChar.getObservePoint().getLoc();
					if(loc == null)
						return;
					L2WorldRegion region = L2World.getRegion(loc);
					if(region == null)
						return;
					int oid = activeChar.getObjectId();
					int rid = activeChar.getInstanceId();
					GArray<L2Player> result = new GArray<L2Player>(64);
					for(L2Object obj : region)
						if((obj.isPlayer() || obj.isObservePoint()) && obj.getObjectId() != oid && obj.getInstanceId() == rid && obj.isInRange(loc, Config.SPECTATE_CHAT_RANGE) && Math.abs(obj.getZ() - activeChar.getZ()) <= 1024)
							result.add(obj.getPlayer());
					for(L2Player player : result)
						if(player != null && !player.isBlockAll() && player != activeChar && (noSpam || activeChar.getHWID().equals(player.getHWID())))
							player.sendPacket(getPacketFor(player, packetClassic, packetInterlude));
				}
				else
					for(L2Player player : L2World.getAroundPlayers(activeChar, Config.ALL_CHAT_RANGE, 1024))
						if(!player.isBlockAll() && player != activeChar && (noSpam || activeChar.getHWID().equals(player.getHWID())))
							player.sendPacket(getPacketFor(player, packetClassic, packetInterlude));

				activeChar.sendPacket(getPacketFor(activeChar, packetClassic, packetInterlude));
				break;
			case CLAN:
				if(activeChar.getClan() != null)
				{
					if(noSpam)
						activeChar.getClan().broadcastToOnlineMembers(packetInterlude);
					else
						activeChar.getClan().broadcastSpam(activeChar.getHWID(), packetInterlude);
				}
				else
					activeChar.sendActionFailed();
				break;
			case ALLIANCE:
				if(activeChar.getClan() != null && activeChar.getClan().getAlliance() != null)
				{
					if(noSpam)
						activeChar.getClan().getAlliance().broadcastToOnlineMembers(packetInterlude);
					else
						activeChar.getClan().getAlliance().broadcastSpam(activeChar.getHWID(), packetInterlude);
				}
				else
					activeChar.sendActionFailed();
				break;
			case PARTY:
				if(activeChar.isInParty())
				{
					if(noSpam)
						activeChar.getParty().broadcastToPartyMembers(packetInterlude);
					else
						activeChar.getParty().broadcastSpam(activeChar.getHWID(), packetInterlude);
				}
				else
					activeChar.sendActionFailed();
				break;
			case PARTY_ROOM:
				PartyRoom r = activeChar.getPartyRoom();
				if(r != null)
					r.broadCast(packetInterlude);
				break;
			case COMMANDCHANNEL_ALL:
				if(!activeChar.isInParty() || !activeChar.getParty().isInCommandChannel())
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_DO_NOT_HAVE_AUTHORITY_TO_USE_THE_COMMAND_CHANNEL));
					return;
				}
				if(activeChar.getParty().getCommandChannel().getChannelLeader() == activeChar)
					if(noSpam)
						activeChar.getParty().getCommandChannel().broadcastToChannelMembers(packetInterlude);
					else
					{
						L2CommandChannel cc = activeChar.getParty().getCommandChannel();
						if(cc.getParties() != null && !cc.getParties().isEmpty())
							for(L2Party party : cc.getParties())
								if(party != null)
									party.broadcastSpam(activeChar.getHWID(), packetInterlude);
					}
				else
					activeChar.sendPacket(new SystemMessage(SystemMessage.ONLY_CHANNEL_OPENER_CAN_GIVE_ALL_COMMAND));
				break;
			case COMMANDCHANNEL_COMMANDER:
				if(!activeChar.isInParty() || !activeChar.getParty().isInCommandChannel())
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_DO_NOT_HAVE_AUTHORITY_TO_USE_THE_COMMAND_CHANNEL));
					return;
				}
				if(activeChar.getParty().isLeader(activeChar))
				{
					if(noSpam)
						activeChar.getParty().getCommandChannel().broadcastToChannelPartyLeaders(packetInterlude);
					else
					{
						L2CommandChannel cc = activeChar.getParty().getCommandChannel();
						if(cc.getParties() != null && !cc.getParties().isEmpty())
							for(L2Party party : cc.getParties())
								if(party != null)
								{
									L2Player leader = party.getPartyLeader();
									if(leader != null && activeChar.getHWID().equals(leader.getHWID()))
										leader.sendPacket(getPacketFor(leader, packetClassic, packetInterlude));
								}
					}
				}
				else
					activeChar.sendPacket(new SystemMessage(SystemMessage.ONLY_A_PARTY_LEADER_CAN_ACCESS_THE_COMMAND_CHANNEL));
				break;
			case HERO_VOICE:
				if(activeChar.isHero() || activeChar.getPlayerAccess().CanAnnounce || activeChar.isPlayerStreamer())
				{
					if(activeChar.isJailed())
					{
						activeChar.sendMessage(activeChar.isLangRus() ? "Героический чат недоступен, пока Вы находитесь в тюрьме." : "Hero chat cannot be used in jail.");
						return;
					}
					// Ограничение только для героев, гм-мы пускай говорят.
					if(!activeChar.getPlayerAccess().CanAnnounce)
					{
						if(activeChar.getLastHeroTime() + Config.HERO_CHAT_DELAY * 1000L > curTime)
						{
							if(activeChar.isLangRus())
								activeChar.sendMessage("Героический чат доступен раз в " + Config.HERO_CHAT_DELAY + " сек.");
							else
								activeChar.sendMessage("Hero chat is allowed once per " + Config.HERO_CHAT_DELAY + " seconds.");
							return;
						}
						activeChar.setLastHeroTime(curTime);
					}

					for(L2Player player : L2ObjectsStorage.getAllPlayersForIterate())
						if(!player.isBlockAll() && player != activeChar && (noSpam || activeChar.getHWID().equals(player.getHWID())))
							player.sendPacket(getPacketFor(player, packetClassic, packetInterlude));

					activeChar.sendPacket(getPacketFor(activeChar, packetClassic, packetInterlude));
				}
				break;
			case PETITION_PLAYER:
			case PETITION_GM:
				if(!PetitionManager.getInstance().isPlayerInConsultation(activeChar))
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_ARE_CURRENTLY_NOT_IN_A_PETITION_CHAT));
					return;
				}
				PetitionManager.getInstance().sendActivePetitionMessage(activeChar, _text);
				break;
			default:
				_log.warn("Player " + activeChar.toString() + " used unknown chat type: " + _type);
		}
		Log.LogChat(chatNames[_type], activeChar.getName(), _target, _text, !_text.equalsIgnoreCase(origText) ? origText : "");
		activeChar.getMessageBucket().addLast(new ChatMsg(ChatType.getTypeById(_type), receiver == null ? 0 : receiver.getObjectId(), _text.hashCode(), (int)(currentTimeMillis / 1000L)));
		activeChar.getListeners().onSay(_type, _target, _text);
	}

	private String filter(String text)
	{
		text = text.replaceAll("№", "");
		text = text.replaceAll("\\\\n", "");
		text = text.replace("\n", "");
		text = text.replace("n\\", "");
		text = text.replace("\r", "");
		text = text.replace("r\\", "");

		return text;
	}

	private Say2 getPacketFor(L2Player p, Say2 classic, Say2 interlude)
	{
		return p.isITClient() ? interlude : classic;
	}
}
