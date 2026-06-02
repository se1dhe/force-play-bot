package l2p.gameserver.clientpackets;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import l2p.gameserver.Config;
import l2p.gameserver.communitybbs.CommunityBoard;
import l2p.gameserver.data.xml.holder.MultiSellHolder;
import l2p.gameserver.handler.*;
import l2p.gameserver.instancemanager.PartyHighlightManager;
import l2p.gameserver.listener.actor.ReplaceIconEffectAnswer;
import l2p.gameserver.model.L2Effect;
import l2p.gameserver.model.L2Object;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Skill;
import l2p.gameserver.model.base.CurrentEnchantSkillType;
import l2p.gameserver.model.entity.Hero;
import l2p.gameserver.model.entity.olympiad.Olympiad;
import l2p.gameserver.model.instances.L2NpcInstance;
import l2p.gameserver.model.instances.L2TrainerInstance;
import l2p.gameserver.model.instances.L2WarehouseInstance;
import l2p.gameserver.multilang.CustomMessage;
import l2p.gameserver.scripts.Scripts;
import l2p.gameserver.serverpackets.*;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.utils.BypassStorage.ValidBypass;
import l2p.gameserver.utils.Log;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestBypassToServer extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(RequestBypassToServer.class);
	private String _bypass = null;

	@Override
	protected void readImpl()
	{
		_bypass = readS();
	}

	@Override
	protected void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if(player == null || _bypass.isEmpty())
			return;

		ValidBypass bp = player.getBypassStorage().validate(_bypass);
		if(bp == null)
		{
			player.sendActionFailed();
			final L2Object nc = player.getTarget();
			Log.addLog("Direct access to bypass: " + _bypass + "| Player: " + player + (nc != null && nc.isNpc() ? (" target: " + nc.getNpcId() + "[" + nc.getObjectId() + "] " + nc.getX() + "," + nc.getY() + "," + nc.getZ()) : ""), "bypass");
			return;
		}
		if(player.isKeyBlocked() && !_bypass.startsWith("scripts_services.RateBonus:keyCheck") && !_bypass.startsWith("user_charkeyset") && !_bypass.startsWith("user_lang"))
		{
			player.sendActionFailed();
			return;
		}
		if(player.isFrozen() && !player.isReg() && !_bypass.startsWith("user_ag_"))
		{
			player.sendActionFailed();
			return;
		}
		try
		{
			L2NpcInstance npc = player.getLastNpc();
			L2Object target = player.getTarget();
			if(target != null && target.isNpc())
				npc = (L2NpcInstance) target;

			if(bp.bbs)
			{
				if(!Config.ALLOW_COMMUNITYBOARD)
					player.sendPacket(new SystemMessage(SystemMessage.THE_COMMUNITY_SERVER_IS_CURRENTLY_OFFLINE));
				else
				{
					ICommunityBoardHandler communityBoardHandler = CommunityBoardManager.getInstance().getCommunityHandler(_bypass, player);
					if(communityBoardHandler != null)
					{
						communityBoardHandler.onBypassCommand(player, _bypass);
					}
					else
					{
						CommunityBoard.getInstance().handleCommands(getClient(), _bypass);
					}
				}
			}
			else if(_bypass.startsWith("admin_"))
				AdminCommandHandler.getInstance().useAdminCommandHandler(player, _bypass);
			else if(_bypass.startsWith("player_help "))
				playerHelp(player, _bypass.substring(12), npc);
			else if(_bypass.startsWith("scripts_"))
			{
				String command = _bypass.substring(8).trim();
				String[] word = command.split("\\s+");
				String[] args = command.substring(word[0].length()).trim().split("\\s+");
				String[] path = word[0].split(":");
				if(path.length != 2)
				{
					_log.warn("Bad Script bypass!");
					return;
				}

				Map<String, Object> variables = null;
				if(npc != null)
				{
					variables = new HashMap<String, Object>(1);
					variables.put("npc", npc.getRef());
				}

				if(word.length == 1)
					Scripts.getInstance().callScripts(player, path[0], path[1], variables);
				else
					Scripts.getInstance().callScripts(player, path[0], path[1], new Object[] { args }, variables);
			}
			else if(_bypass.startsWith("user_"))
			{
				String command = _bypass.substring(5).trim();
				String word = command.split("\\s+")[0];
				String args = command.substring(word.length()).trim();
				IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(word);

				if(vch != null)
					vch.useVoicedCommand(word, player, args);
				else
					_log.warn("Unknow voiced command '" + word + "'");
			}
			else if(_bypass.startsWith("npc_"))
			{
				int endOfId = _bypass.indexOf('_', 5);
				String id;
				if(endOfId > 0)
					id = _bypass.substring(4, endOfId);
				else
					id = _bypass.substring(4);
				L2Object object = player.getVisibleObject(Integer.parseInt(id));
				if(object != null && object.isNpc() && endOfId > 0 && object.isInActingRange(player))
				{
					player.setLastNpc((L2NpcInstance) object);
					((L2NpcInstance) object).onBypassFeedback(player, _bypass.substring(endOfId + 1));
				}
			}
			//else if(Config.ALLOW_BYPASS_DISPEL && _bypass.startsWith(Config.BYPASS_DISPEL))
			//{
			//	RequestDispel.dispel(player, Integer.parseInt(_bypass.substring(Config.BYPASS_DISPEL.length())));
			//}
			else if(Config.CUSTOM_PATCH_OPTIONS_ENABLE && _bypass.startsWith("interface?autoloot_on"))
			{
				player.setAutoLootItems(true);
				player.sendMessage(player.isLangRus() ? "Автоматическое подбор всех предметов включен." : "Autolooting all.");
			}
			else if(Config.CUSTOM_PATCH_OPTIONS_ENABLE && _bypass.startsWith("interface?autoloot_off"))
			{
				player.setAutoLootItems(false);
				//player.setAutoLootHerbs(false);
				//player.setAutoLootAdena(false);
				//player.setAutoLootList(false);
				player.sendMessage(player.isLangRus() ? "Автоматическое подбор выключен." : "Autolooting off.");
			}
			else if(Config.CUSTOM_PATCH_OPTIONS_ENABLE && _bypass.startsWith("interface?exp_on"))
			{
				player.unsetVar("NoExp");
				player.sendMessage(player.isLangRus() ? "Блокировка получения уровня выключена." : "Level increase unblocked.");
			}
			else if(Config.CUSTOM_PATCH_OPTIONS_ENABLE && _bypass.startsWith("interface?exp_off"))
			{
				player.setVar("NoExp", "1", -1);
				player.sendMessage(player.isLangRus() ? "Блокировка получения уровня включена." : "Level increase blocked.");
			}
			else if(Config.CUSTOM_PATCH_OPTIONS_ENABLE && _bypass.startsWith("interface?donate_ru"))
			{
				player.sendPacket(new BrowserBypassPacket(Config.CUSTOM_PATCH_DONATE_URL));
			}
			else if(Config.CUSTOM_PATCH_OPTIONS_ENABLE && _bypass.startsWith("interface?vk"))
			{
				player.sendPacket(new BrowserBypassPacket(Config.CUSTOM_PATCH_VK_URL));
			}
			else if(Config.CUSTOM_PATCH_OPTIONS_ENABLE && _bypass.startsWith("interface?facebook"))
			{
				player.sendPacket(new BrowserBypassPacket(Config.CUSTOM_PATCH_FACEBOOK_URL));
			}
			else if(Config.CUSTOM_DISPELL_EFFECTS && (_bypass.startsWith("_dispel") || _bypass.startsWith("interface?dispell=")))
			{
				String command = _bypass.substring(10).trim();
				String word = command.split("=")[0];
				String args = command.substring(word.length() + 1).trim();

				if(player.isDead())
					return;
				if(player.isInOlympiadMode() && !Config.CUSTOM_DISPELL_EFFECTS_ON_OLYMPIAD)
					return;

				int skillId = Integer.parseInt(args);

				if(Config.CUSTOM_DISPEL_EXCLUDED.contains(skillId))
					return;

				for(L2Effect e : player.getEffectList().getAllEffects())
				{
					L2Skill skill = e.getSkill();
					if(skill.getId() == skillId && !e.isOffensive() && (!e.getSkill().isMusic() || Config.CUSTOM_DISPELL_MUSIC_EFFECTS) && e.getSkill().isSelfDispellable())
					{
						e.exit();
						player.sendPacket(new SystemMessage(SystemMessage.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(skill.getId(), skill.getDisplayLevel()));
					}
				}
			}
			else if(Config.CUSTOM_REPLACE_EFFECTS && _bypass.startsWith("interface?switch_effect_bar="))
			{
				String command = _bypass.substring("interface?switch_effect_bar=".length()).trim();

				int skillId;
				try
				{
					skillId = Integer.parseInt(command);
				}
				catch(NumberFormatException e)
				{
					_log.warn("Invalid skillId for switch_effect_bar: " + command);
					return;
				}

				handleSwitchEffect(player, skillId);
			}
			else if(_bypass.startsWith("oly_"))
			{
				if(!Config.ENABLE_OLYMPIAD_SPECTATING || !player.isInOlympiadObserverMode())
					return;

				String cmd = _bypass.substring(4); // "page_2" или "15"

				try
				{
					if(cmd.startsWith("page_"))
					{
						int page = Integer.parseInt(cmd.substring(5));
						Olympiad.showCompetitionList(player, null, page);
					}
					else if(cmd.startsWith("close"))
					{
						player.sendActionFailed();
					}
					else
					{
						int arenaId = Integer.parseInt(cmd);
						if(player.getOlympiadObserveId() != arenaId
								&& Olympiad._manager != null
								&& Olympiad._manager.getOlympiadInstance(arenaId) != null)
						{
							player.switchOlympiadObserverArena(arenaId);
						}
					}
				}
				catch (Exception e)
				{
					_log.warn("Invalid oly_ bypass: " + _bypass, e);
				}
			}
			else if(_bypass.startsWith("_diary"))
			{
				String params = _bypass.substring(_bypass.indexOf("?") + 1);
				StringTokenizer st = new StringTokenizer(params, "&");
				int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
				int heropage = Integer.parseInt(st.nextToken().split("=")[1]);
				int heroid = Hero.getInstance().getHeroByClass(heroclass);
				if(heroid > 0)
					Hero.getInstance().showHeroDiary(player, heroclass, heroid, heropage);
			}
			else if(_bypass.startsWith("manor_menu_select?")) // Navigate throught Manor windows
			{
				if(!Config.ALLOW_MANOR)
				{
					player.sendMessage("Manor disabled.");
					player.sendActionFailed();
					return;
				}
				L2Object object = player.getTarget();
				if(object != null && object.isNpc())
					((L2NpcInstance) object).onBypassFeedback(player, _bypass);
			}
			else if(_bypass.startsWith("Quest "))
			{
				String p = _bypass.substring(6).trim();
				int idx = p.indexOf(' ');
				if(idx < 0)
					player.processQuestEvent(p, "", npc);
				else
					player.processQuestEvent(p.substring(0, idx), p.substring(idx).trim(), npc);
			}
			else if(_bypass.startsWith("lang"))
			{
				StringTokenizer st = new StringTokenizer(_bypass);
				st.nextToken();
				String param = st.nextToken();
				if(param.startsWith("ru"))
				{
					player.setVar("lang@", "ru", -1);
					player.sendMessage(new CustomMessage("usercommandhandlers.LangRu", player));
					if(st.hasMoreTokens())
					{
						NpcHtmlMessage html = new NpcHtmlMessage(5);
						html.setFile(st.nextToken());
						player.sendPacket(html);
					}
				}
				else if(param.startsWith("en"))
				{
					player.setVar("lang@", "en", -1);
					player.sendMessage(new CustomMessage("usercommandhandlers.LangEn", player));
					if(st.hasMoreTokens())
					{
						NpcHtmlMessage html = new NpcHtmlMessage(5);
						html.setFile(st.nextToken());
						player.sendPacket(html);
					}
				}
			}
			else if(_bypass.startsWith("notice_edit "))
			{
				if(!Config.CLAN_NOTICE || !player.isClanLeader())
				{
					player.sendActionFailed();
					return;
				}
				Scripts.getInstance().callScripts(player, "services.ClanNotice", "set", new Object[]{player, _bypass.substring(12)});
			}
			else if(!Config.CUSTOM_PCBANG_MULTISELL_BYPASS.isEmpty() && _bypass.startsWith(Config.CUSTOM_PCBANG_MULTISELL_BYPASS))
			{
				MultiSellHolder.getInstance().SeparateAndSend(Config.CUSTOM_PCBANG_MULTISELL_ID, player, 0.);
			}
			else if(_bypass.startsWith("partyph"))
			{
				if(!Config.PARTY_HIGHLIGHT_ENABLED)
				{
					return;
				}

				String[] params = _bypass.split(";");
				if(params.length >= 15)
				{
					try
					{
						int[] highlightColors = new int[11];
						for(int i = 1; i <= 11; i++)
						{
							highlightColors[i-1] = Integer.parseInt(params[i]);
						}

						boolean coloredNicknameShow = Integer.parseInt(params[12]) == 1;
						boolean cardShow = Integer.parseInt(params[13]) == 1;
						boolean ressurectHighlightShow = Integer.parseInt(params[14]) == 1;

						player.setPartyHighlightColors(highlightColors);
						player.setPartyNicknameColorEnabled(coloredNicknameShow);
						player.setPartyCardShowEnabled(cardShow);
						player.setPartyRessurectHighlightEnabled(ressurectHighlightShow);
						player.savePartyHighlightSettings();
						PartyHighlightManager.getInstance().onPlayerHighlightSettingsChanged(player);
					}
					catch(NumberFormatException e)
					{
						_log.warn("[PartyHighlight] Invalid party highlight parameters for player: " + player.getName(), e);
					}
				}
			}
			else if(_bypass.startsWith("setSet "))
			{
				try
				{
					String command = _bypass.substring(7).trim();
					String[] parts = command.split("\\s+");
					if(parts.length != 5)
					{
						player.sendMessage(player.isLangRus() ? "Некорректный формат сета." : "Incorrect set format.");
						return;
					}

					Integer[] itemIds = new Integer[5];
					for(int i = 0; i < 5; i++)
					{
						String part = parts[i].trim();
						if(part.isEmpty() || part.equals("0"))
						{
							itemIds[i] = 0;
						}
						else
						{
							itemIds[i] = Integer.parseInt(part);
						}
					}
					player.addAutoEquipSet(itemIds);
				}
				catch(NumberFormatException e)
				{
					_log.warn("EquipSet: Invalid equipment set parameters: " + _bypass, e);
				}
			}
			else if(_bypass.startsWith("setDel "))
			{
				try
				{
					int setId = Integer.parseInt(_bypass.substring(7).trim());
					player.removeAutoEquipSet(setId);
				}
				catch (NumberFormatException e)
				{
					_log.warn("EquipSet: Invalid set ID for deletion: " + _bypass, e);
				}
			}
			else if(_bypass.startsWith("setUse "))
			{
				try
				{
					int setId = Integer.parseInt(_bypass.substring(7).trim());
					if(!player.useAutoEquipSet(setId))
					{
						player.sendMessage(player.isLangRus() ? "Не удалось использовать сет экипировки." : "Failed to use equipment set.");
					}
				}
				catch (NumberFormatException e)
				{
					_log.warn("EquipSet: Invalid set ID for usage: " + _bypass, e);
				}
			}
			else if(_bypass.startsWith("refSet "))
			{
				try
				{
					int objectId = Integer.parseInt(_bypass.substring(7).trim());
					player.addRefineryItem(objectId);
				}
				catch(NumberFormatException e)
				{
					_log.warn("Refinery: Invalid object ID for refSet: " + _bypass, e);
				}
			}
			else if(_bypass.startsWith("refUse "))
			{
				try
				{
					int objectId = Integer.parseInt(_bypass.substring(7).trim());
					player.useRefineryItem(objectId);
				}
				catch(NumberFormatException e)
				{
					_log.warn("Refinery: Invalid object ID for refUse: " + _bypass, e);
				}
			}
			else if(_bypass.startsWith("refDel "))
			{
				try
				{
					int objectId = Integer.parseInt(_bypass.substring(7).trim());
					player.removeRefineryItem(objectId);
				}
				catch (NumberFormatException e)
				{
					_log.warn("Refinery: Invalid object ID for refDel: " + _bypass, e);
				}
			}
			else if(!player.isITClient())
			{
				if(_bypass.startsWith(ExEnchantPageSkillList.EX_ENCHANT_SKILLLIST_BYPASS))
				{
					boolean allowedCheck;
					if(player.getCurrentEnchantSkillType() == CurrentEnchantSkillType.SAFE_BOOK || player.getCurrentEnchantSkillType() == CurrentEnchantSkillType.VOICE)
						allowedCheck = true;
					else
						allowedCheck = L2NpcInstance.canBypassCheck(player, npc) && (npc instanceof L2TrainerInstance || npc instanceof L2WarehouseInstance || npc.getNpcId() == Config.ALLOW_ESL);
					if(allowedCheck)
					{
						String str = _bypass.substring(ExEnchantPageSkillList.EX_ENCHANT_SKILLLIST_BYPASS.length()).trim();
						if(StringUtils.isNumeric(str))
							player.sendPacket(ExEnchantPageSkillList.packetFor(player, npc, Integer.parseInt(str)));
					}
				}
				else if(_bypass.startsWith(ExEnchantPageSkillInfo.EX_ENCHANT_SKILLINFO_BYPASS))
				{
					boolean allowedCheck;
					if(player.getCurrentEnchantSkillType() == CurrentEnchantSkillType.SAFE_BOOK || player.getCurrentEnchantSkillType() == CurrentEnchantSkillType.VOICE)
						allowedCheck = true;
					else
						allowedCheck = L2NpcInstance.canBypassCheck(player, npc) && (npc instanceof L2TrainerInstance || npc instanceof L2WarehouseInstance || npc.getNpcId() == Config.ALLOW_ESL);
					if(allowedCheck)
					{
						String str = _bypass.substring(ExEnchantPageSkillInfo.EX_ENCHANT_SKILLINFO_BYPASS.length()).trim();
						player.sendPacket(ExEnchantPageSkillInfo.packetFor(player, npc, StringUtils.split(str, ' ')));
					}
				}
				else if(_bypass.startsWith(ExEnchantPageSkill.EX_ENCHANT_SKILL_BYPASS))
				{
					boolean allowedCheck;
					if(player.getCurrentEnchantSkillType() == CurrentEnchantSkillType.SAFE_BOOK || player.getCurrentEnchantSkillType() == CurrentEnchantSkillType.VOICE)
						allowedCheck = true;
					else
						allowedCheck = L2NpcInstance.canBypassCheck(player, npc) && (npc instanceof L2TrainerInstance || npc instanceof L2WarehouseInstance || npc.getNpcId() == Config.ALLOW_ESL);
					if(allowedCheck)
					{
						String str = _bypass.substring(ExEnchantPageSkill.EX_ENCHANT_SKILL_BYPASS.length()).trim();
						player.sendPacket(ExEnchantPageSkill.packetFor(player, npc, StringUtils.split(str, ' ')));
					}
				}
				else if(_bypass.startsWith(ExEnchantPageSkillList.BUY_ENCHANT_SKILLLIST_BYPASS))
				{
					if(L2NpcInstance.canBypassCheck(player, npc))
					{
						String str = _bypass.substring(ExEnchantPageSkillList.BUY_ENCHANT_SKILLLIST_BYPASS.length()).trim();
						if(StringUtils.isNumeric(str))
							player.sendPacket(ExEnchantPageSkillList.packetBuyFor(player, npc, Integer.parseInt(str)));
					}
				}
				else if(_bypass.startsWith(ExEnchantPageSkillInfo.BUY_ENCHANT_SKILLINFO_BYPASS))
				{
					if(L2NpcInstance.canBypassCheck(player, npc))
					{
						String str = _bypass.substring(ExEnchantPageSkillInfo.BUY_ENCHANT_SKILLINFO_BYPASS.length()).trim();
						player.sendPacket(ExEnchantPageSkillInfo.packetBuyFor(player, npc, StringUtils.split(str, ' ')));
					}
				}
				else if(_bypass.startsWith(ExEnchantPageSkill.BUY_ENCHANT_SKILL_BYPASS))
				{
					if(L2NpcInstance.canBypassCheck(player, npc))
					{
						String str = _bypass.substring(ExEnchantPageSkill.BUY_ENCHANT_SKILL_BYPASS.length()).trim();
						player.sendPacket(ExEnchantPageSkill.packetBuyFor(player, npc, StringUtils.split(str, ' ')));
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			String st = "Bad RequestBypassToServer: " + _bypass;
			final L2Object target = player.getTarget();
			if(target != null && target.isNpc())
				st = st + " via NPC #" + ((L2NpcInstance) target).getNpcId();
			_log.error(st, e);
		}
	}

	private void handleSwitchEffect(L2Player player, int skillId)
	{
		int originalSkillId = skillId >= 60000 ? skillId - 60000 : skillId;
		boolean isAlternativeView = skillId >= 60000;

		L2Skill checkSkill = SkillTable.getInstance().getInfo(originalSkillId, 1);
		if(checkSkill == null)
		{
			_log.warn("Skill with skillId {} not found.", originalSkillId);
			return;
		}

		if(!Config.CUSTOM_REPLACE_EFFECTS_SKILLS.contains(originalSkillId))
		{
			String messageKey = checkSkill.isDebuff() ? "custom.bypass.switch.CannotBeMovedToBuff" : "custom.bypass.switch.CannotBeMovedToDebuff";
			player.sendMessage(new CustomMessage(messageKey, player).addSkillName(checkSkill));
			return;
		}

		L2Effect effect = player.getEffectList().getEffectBySkillId(originalSkillId);
		if(effect == null)
		{
			_log.warn("Effect with skillId {} not found for player {}", originalSkillId, player.getName());
			return;
		}

		L2Skill skill = SkillTable.getInstance().getInfo(originalSkillId, effect.getDisplayLevel());
		if(skill == null)
		{
			_log.warn("Skill with skillId {} and level {} not found.", originalSkillId, effect.getDisplayLevel());
			return;
		}

		String messageKey;
		if(isAlternativeView)
			messageKey = !skill.isDebuff() ? "custom.bypass.switch.RequestDebuffToBuff" : "custom.bypass.switch.RequestBuffToDebuff";
		else
			messageKey = skill.isDebuff() ? "custom.bypass.switch.RequestDebuffToBuff" : "custom.bypass.switch.RequestBuffToDebuff";

		String message = new CustomMessage(messageKey, player).addSkillName(skill).toString();

		ConfirmDlg packet = new ConfirmDlg(SystemMessage.S1_S2, 0).addString(message);
		player.ask(packet, new ReplaceIconEffectAnswer(player, originalSkillId));
	}

	public static void openURL(L2Player player, String url)
	{
		player.sendPacket(new OpenURL(url));
	}

	private void playerHelp(L2Player player, String path, L2NpcInstance npc)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(player, npc);
		html.setFile(path);
		player.sendPacket(html);
	}
}