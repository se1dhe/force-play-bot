package l2p.gameserver.clientpackets;

import l2p.gameserver.Bonus;
import l2p.gameserver.Config;
import l2p.gameserver.data.xml.holder.BonusStartRewardHolder;
import l2p.gameserver.data.xml.holder.InitialShortCutsHolder;
import l2p.gameserver.data.xml.holder.ObtStartRewardHolder;
import l2p.gameserver.database.mysql;
import l2p.gameserver.instancemanager.PlayerManager;
import l2p.gameserver.instancemanager.QuestManager;
import l2p.gameserver.model.L2Macro;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2ShortCut;
import l2p.gameserver.model.SkillLearn;
import l2p.gameserver.model.base.ClassId;
import l2p.gameserver.model.base.Experience;
import l2p.gameserver.model.entity.olympiad.OlympiadGame;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.model.quest.Quest;
import l2p.gameserver.network.L2GameClient;
import l2p.gameserver.serverpackets.CharacterCreateFail;
import l2p.gameserver.serverpackets.CharacterCreateSuccess;
import l2p.gameserver.serverpackets.CharacterSelectionInfo;
import l2p.gameserver.tables.ItemTable;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.tables.SkillTree;
import l2p.gameserver.templates.L2PlayerTemplate;
import l2p.gameserver.templates.start.BonusStartTemplate;
import l2p.gameserver.templates.start.BonusStartType;
import l2p.gameserver.templates.start.StartItem;
import l2p.gameserver.utils.Location;
import l2p.gameserver.utils.Log;
import l2p.gameserver.utils.Util;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CharacterCreate extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(CharacterCreate.class);

	private String _name;
	private int _sex;
	private int _classId;
	private int _hairStyle;
	private int _hairColor;
	private int _face;

	@Override
	public void readImpl()
	{
		_name = readS();
		readD(); // race
		_sex = readD();
		_classId = readD();
		readD(); // int
		readD(); // str
		readD(); // con
		readD(); // men
		readD(); // dex
		readD(); // wit
		_hairStyle = readD();
		_hairColor = readD();
		_face = readD();
	}

	@Override
	public void runImpl()
	{
		for(ClassId cid : ClassId.values())
			if(cid.getId() == _classId && cid.getLevel() != 1)
				return;
		if(PlayerManager.accountCharNumber(getClient().getLoginName()) >= 8)
		{
			sendPacket(CharacterCreateFail.REASON_TOO_MANY_CHARACTERS);
			return;
		}
		if(Config.OFF_NAME_LENGTH && (_name.length() < 3 || _name.length() > 16))
		{
			sendPacket(CharacterCreateFail.REASON_16_ENG_CHARS);
			return;
		}
		if(!Util.isMatchingRegexp(_name, Config.CNAME_TEMPLATE) || Config.CNAME_DENY_PATTERN.matcher(_name.toLowerCase()).find())
		{
			sendPacket(CharacterCreateFail.REASON_INCORRECT_NAME);
			return;
		}
		if(PlayerManager.getObjectIdByName(_name) > 0 || (Config.BOTS_USED_NAMES && L2Player.bots_names.contains(_name)))
		{
			sendPacket(CharacterCreateFail.REASON_NAME_ALREADY_EXISTS);
			return;
		}
		if(_face > 2 || _face < 0)
		{
			sendPacket(CharacterCreateFail.REASON_CREATION_FAILED);
			return;
		}
		if(_hairStyle < 0 || (_sex == 0 && _hairStyle > 4) || (_sex != 0 && _hairStyle > 6))
		{
			sendPacket(CharacterCreateFail.REASON_CREATION_FAILED);
			return;
		}
		if(_hairColor > 3 || _hairColor < 0)
		{
			sendPacket(CharacterCreateFail.REASON_CREATION_FAILED);
			return;
		}

		L2Player newChar = L2Player.create(_classId, _sex, getClient().getLoginName(), _name, _hairStyle, _hairColor, _face);
		if(newChar == null)
			return;

		sendPacket(CharacterCreateSuccess.STATIC_PACKET);

		initNewChar(getClient(), newChar);
	}

	private void initNewChar(L2GameClient client, L2Player newChar)
	{
		L2PlayerTemplate template = newChar.getTemplate();
		newChar.setVar("FakeCostume", "1");
		newChar.setVar("PartyAura", "1");
		newChar.setVar("lang@", Config.DEFAULT_LANG);
		if(Config.DL_ICONS > 1)
			newChar.setVar("DroplistIcons", "1");

		L2Player.restoreCharSubClasses(newChar);

		if(Config.STARTING_ADENA > 0)
			newChar.addAdena(Config.STARTING_ADENA);

		startSpawn(newChar, template);

		newChar.setTitle(Config.CHAR_TITLE);

		if(Config.EVENT_OBT_ENABLE)
		{
			obtStartItems(newChar);
			startItems(newChar, template);
		}
		else
		{
			startItems(newChar, template);
		}

		if(Config.ALLOW_START_BUFFS)
		{
			int n = 0;
			if(newChar.isMageClass())
				for(int i = 0; i < Config.START_BUFFS_MAGE.length; i += 2)
					OlympiadGame.giveBuff(newChar, SkillTable.getInstance().getInfo(Config.START_BUFFS_MAGE[i], Config.START_BUFFS_MAGE[i + 1]), n++);
			else
				for(int i = 0; i < Config.START_BUFFS_FIGHTER.length; i += 2)
					OlympiadGame.giveBuff(newChar, SkillTable.getInstance().getInfo(Config.START_BUFFS_FIGHTER[i], Config.START_BUFFS_FIGHTER[i + 1]), n++);
		}

		if(Config.START_PA > 0)
			Bonus.newbieBonus(newChar.getObjectId(), Config.START_RATE_INDEX_PA, Config.START_PA, client);

		if(Config.START_PREMIUM_BUFF > 0)
			Bonus.newbiePremiumBuff(newChar.getObjectId(), Config.START_PREMIUM_BUFF, client, newChar);

		SkillLearn[] skills = SkillTree.getInstance().getAvailableSkills(newChar, newChar.getClassId());
		for(SkillLearn s : skills)
			newChar.addSkill(SkillTable.getInstance().getInfo(s.getId(), s.getLevel()), true);

		Map<Integer, Integer> initedMacroses = new HashMap<Integer, Integer>();
		for(L2Macro macro : InitialShortCutsHolder.getInstance().getInitialMacroses())
		{
			if(!macro.isEnabled())
				continue;

			if(newChar.getMacroses().getAllMacroses().length > 48)
			{
				_log.warn("Character Initial Macro Failure: Cannot register more than 48 macros!");
				break;
			}

			L2Macro newMacro = new L2Macro(0, macro.icon, macro.name, macro.descr, macro.acronym, macro.commands, true);
			newChar.registerMacro(newMacro);
			initedMacroses.put(macro.id, newMacro.id);
		}

		for(L2ShortCut shortCut : InitialShortCutsHolder.getInstance().getInitialShortCuts(newChar.getRace(), newChar.isMageClass() ? 1 : 0))
		{
			if(shortCut.getType() == L2ShortCut.TYPE_MACRO)
			{
				Integer initedMacroId = initedMacroses.get(shortCut.getId());
				if(initedMacroId != null)
					newChar.registerShortCut(new L2ShortCut(shortCut.getSlot(), shortCut.getPage(), shortCut.getType(), initedMacroId, 0));
				continue;
			}
			else if(shortCut.getType() == L2ShortCut.TYPE_ITEM)
			{
				L2ItemInstance item = newChar.getInventory().getItemByItemId(shortCut.getId());
				if(item != null)
					newChar.registerShortCut(new L2ShortCut(shortCut.getSlot(), shortCut.getPage(), shortCut.getType(), item.getObjectId(), -1));
				continue;
			}

			newChar.registerShortCut(shortCut);
		}

		giveLevel(newChar);
		if(Config.STARTING_SP > 0)
			newChar.getSubClasses().get(newChar.getActiveClassId()).setSp(Config.STARTING_SP);

		startTutorialQuest(newChar);
		if(Config.START_QUESTS_COMPLETED.length > 0)
			for(int id : Config.START_QUESTS_COMPLETED)
			{
				Quest q = QuestManager.getQuest(id);
				if(q != null)
					q.newQuestState(newChar, 3);
			}
		newChar.setCurrentHpMp(newChar.getMaxHp(), newChar.getMaxMp(), false);
		newChar.setCurrentCp(0); // по оффу новый персонаж начинает игру с 0 сп
		newChar.setOnlineStatus(false);

		newChar.storeHWID(client.getHWID());
		PlayerManager.saveCharToDisk(newChar);
		newChar.deleteMe();

		final CharacterSelectionInfo csi = new CharacterSelectionInfo(client.getLoginName(), client.getSessionId().playOkID1);
		client.setPacketCharSelection(csi);
		client.sendPacket(csi);
		client.setCharSelection(csi.getCharInfo());
	}

	private static void startSpawn(L2Player newChar, L2PlayerTemplate template)
	{
		if(Config.EVENT_OBT_ENABLE)
		{
			if(Config.EVENT_OBT_POS_XYZ.length >= 15)
			{
				if(newChar.getRace().ordinal() == 0)
					newChar.setXYZInvisible(Location.findAroundPosition(Config.EVENT_OBT_POS_XYZ[0], Config.EVENT_OBT_POS_XYZ[1], Config.EVENT_OBT_POS_XYZ[2], 0, 200, 0));
				else if(newChar.getRace().ordinal() == 1)
					newChar.setXYZInvisible(Location.findAroundPosition(Config.EVENT_OBT_POS_XYZ[3], Config.EVENT_OBT_POS_XYZ[4], Config.EVENT_OBT_POS_XYZ[5], 0, 200, 0));
				else if(newChar.getRace().ordinal() == 2)
					newChar.setXYZInvisible(Location.findAroundPosition(Config.EVENT_OBT_POS_XYZ[6], Config.EVENT_OBT_POS_XYZ[7], Config.EVENT_OBT_POS_XYZ[8], 0, 200, 0));
				else if(newChar.getRace().ordinal() == 3)
					newChar.setXYZInvisible(Location.findAroundPosition(Config.EVENT_OBT_POS_XYZ[9], Config.EVENT_OBT_POS_XYZ[10], Config.EVENT_OBT_POS_XYZ[11], 0, 200, 0));
				else
					newChar.setXYZInvisible(Location.findAroundPosition(Config.EVENT_OBT_POS_XYZ[12], Config.EVENT_OBT_POS_XYZ[13], Config.EVENT_OBT_POS_XYZ[14], 0, 200, 0));
			}
			else if(Config.EVENT_OBT_POS_XYZ.length < 3)
				newChar.setXYZInvisible(template.getStartLoc());
			else
				newChar.setXYZInvisible(Location.findAroundPosition(Config.EVENT_OBT_POS_XYZ[0], Config.EVENT_OBT_POS_XYZ[1], Config.EVENT_OBT_POS_XYZ[2], 0, 200, 0));
		}
		else if(Config.EVENT_BONUS_START_ENABLE)
		{
			if(Config.EVENT_BONUS_START_POS_XYZ.length >= 15)
			{
				if(newChar.getRace().ordinal() == 0)
					newChar.setXYZInvisible(Location.findAroundPosition(Config.EVENT_BONUS_START_POS_XYZ[0], Config.EVENT_BONUS_START_POS_XYZ[1], Config.EVENT_BONUS_START_POS_XYZ[2], 0, 200, 0));
				else if(newChar.getRace().ordinal() == 1)
					newChar.setXYZInvisible(Location.findAroundPosition(Config.EVENT_BONUS_START_POS_XYZ[3], Config.EVENT_BONUS_START_POS_XYZ[4], Config.EVENT_BONUS_START_POS_XYZ[5], 0, 200, 0));
				else if(newChar.getRace().ordinal() == 2)
					newChar.setXYZInvisible(Location.findAroundPosition(Config.EVENT_BONUS_START_POS_XYZ[6], Config.EVENT_BONUS_START_POS_XYZ[7], Config.EVENT_BONUS_START_POS_XYZ[8], 0, 200, 0));
				else if(newChar.getRace().ordinal() == 3)
					newChar.setXYZInvisible(Location.findAroundPosition(Config.EVENT_BONUS_START_POS_XYZ[9], Config.EVENT_BONUS_START_POS_XYZ[10], Config.EVENT_BONUS_START_POS_XYZ[11], 0, 200, 0));
				else
					newChar.setXYZInvisible(Location.findAroundPosition(Config.EVENT_BONUS_START_POS_XYZ[12], Config.EVENT_BONUS_START_POS_XYZ[13], Config.EVENT_BONUS_START_POS_XYZ[14], 0, 200, 0));
			}
			else if(Config.EVENT_BONUS_START_POS_XYZ.length < 3)
				newChar.setXYZInvisible(template.getStartLoc());
			else
				newChar.setXYZInvisible(Location.findAroundPosition(Config.EVENT_BONUS_START_POS_XYZ[0], Config.EVENT_BONUS_START_POS_XYZ[1], Config.EVENT_BONUS_START_POS_XYZ[2], 0, 200, 0));
		}
		else
		{
			if(Config.START_XYZ.length >= 15)
			{
				if(newChar.getRace().ordinal() == 0)
					newChar.setXYZInvisible(Location.findAroundPosition(Config.START_XYZ[0], Config.START_XYZ[1], Config.START_XYZ[2], 0, 200, 0));
				else if(newChar.getRace().ordinal() == 1)
					newChar.setXYZInvisible(Location.findAroundPosition(Config.START_XYZ[3], Config.START_XYZ[4], Config.START_XYZ[5], 0, 200, 0));
				else if(newChar.getRace().ordinal() == 2)
					newChar.setXYZInvisible(Location.findAroundPosition(Config.START_XYZ[6], Config.START_XYZ[7], Config.START_XYZ[8], 0, 200, 0));
				else if(newChar.getRace().ordinal() == 3)
					newChar.setXYZInvisible(Location.findAroundPosition(Config.START_XYZ[9], Config.START_XYZ[10], Config.START_XYZ[11], 0, 200, 0));
				else
					newChar.setXYZInvisible(Location.findAroundPosition(Config.START_XYZ[12], Config.START_XYZ[13], Config.START_XYZ[14], 0, 200, 0));
			}
			else if(Config.START_XYZ.length < 3)
				newChar.setXYZInvisible(template.getStartLoc());
			else
				newChar.setXYZInvisible(Location.findAroundPosition(Config.START_XYZ[0], Config.START_XYZ[1], Config.START_XYZ[2], 0, 200, 0));
		}
	}

	private static void giveLevel(L2Player newChar)
	{
		if(Config.EVENT_OBT_ENABLE)
		{
			if(Config.EVENT_OBT_STARTING_LEVEL > 1)
			{
				newChar.addExpAndSp((Experience.LEVEL[Config.EVENT_OBT_STARTING_LEVEL + 1] - Experience.LEVEL[Config.EVENT_OBT_STARTING_LEVEL]) / 2 + Experience.LEVEL[Config.EVENT_OBT_STARTING_LEVEL], 0L, false, false);
				newChar.getSubClasses().get(newChar.getActiveClassId()).setExp(newChar.getExp());
				newChar.getSubClasses().get(newChar.getActiveClassId()).setLevel(newChar.getLevel());
			}
			else
			{
				if(Config.STARTING_LEVEL > 1)
				{
					newChar.addExpAndSp((Experience.LEVEL[Config.STARTING_LEVEL + 1] - Experience.LEVEL[Config.STARTING_LEVEL]) / 2 + Experience.LEVEL[Config.STARTING_LEVEL], 0L, false, false);
					newChar.getSubClasses().get(newChar.getActiveClassId()).setExp(newChar.getExp());
					newChar.getSubClasses().get(newChar.getActiveClassId()).setLevel(newChar.getLevel());
				}
			}
		}
		else if(Config.EVENT_BONUS_START_ENABLE)
		{
			if(Config.EVENT_BONUS_START_STARTING_LEVEL > 1)
			{
				newChar.addExpAndSp((Experience.LEVEL[Config.EVENT_BONUS_START_STARTING_LEVEL + 1] - Experience.LEVEL[Config.EVENT_BONUS_START_STARTING_LEVEL]) / 2 + Experience.LEVEL[Config.EVENT_BONUS_START_STARTING_LEVEL], 0L, false, false);
				newChar.getSubClasses().get(newChar.getActiveClassId()).setExp(newChar.getExp());
				newChar.getSubClasses().get(newChar.getActiveClassId()).setLevel(newChar.getLevel());
			}
			else
			{
				if(Config.STARTING_LEVEL > 1)
				{
					newChar.addExpAndSp((Experience.LEVEL[Config.STARTING_LEVEL + 1] - Experience.LEVEL[Config.STARTING_LEVEL]) / 2 + Experience.LEVEL[Config.STARTING_LEVEL], 0L, false, false);
					newChar.getSubClasses().get(newChar.getActiveClassId()).setExp(newChar.getExp());
					newChar.getSubClasses().get(newChar.getActiveClassId()).setLevel(newChar.getLevel());
				}
			}
		}
		else
		{
			if(Config.STARTING_LEVEL > 1)
			{
				newChar.addExpAndSp((Experience.LEVEL[Config.STARTING_LEVEL + 1] - Experience.LEVEL[Config.STARTING_LEVEL]) / 2 + Experience.LEVEL[Config.STARTING_LEVEL], 0L, false, false);
				newChar.getSubClasses().get(newChar.getActiveClassId()).setExp(newChar.getExp());
				newChar.getSubClasses().get(newChar.getActiveClassId()).setLevel(newChar.getLevel());
			}
		}
	}

	private boolean obtStartItems(L2Player player)
	{
		List<StartItem> startItems = ObtStartRewardHolder.getInstance().getStartItemsById(player.getActiveClassId());
		if(startItems == null)
			startItems = ObtStartRewardHolder.getInstance().getStartItemsById(player.isMageClass() ? -2 : -1);
		if(startItems == null)
		{
			_log.warn("ObtStart: Couldn't find a reward for NEWBIE the class " + player.getActiveClassId());
			return false;
		}

		if(!startItems.isEmpty())
		{
			for(StartItem startItem : startItems)
			{
				L2ItemInstance item = ItemTable.getInstance().createItem(startItem.getItemId());
				if(startItem.getEnchantLevel() > 0)
					item.setEnchantLevel(startItem.getEnchantLevel());

				final long count = startItem.getCount();
				if(item.isStackable())
				{
					item.setCount(count);
					player.getInventory().addItem(item, true, false, true, "<ObtStartCreateItemStackable>");
				}
				else
				{
					for(long i = 0; i < count; i++)
					{
						item = ItemTable.getInstance().createItem(startItem.getItemId());
						if(startItem.getEnchantLevel() > 0)
							item.setEnchantLevel(startItem.getEnchantLevel());
						player.getInventory().addItem(item, true, false, true, "<ObtStartCreateItem>");
					}

					if(item.isEquipable() && startItem.isEquiped())
						player.getInventory().equipItem(item, false);
				}
			}
		}
		return true;
	}

	private void startItems(L2Player newChar, L2PlayerTemplate template)
	{
		for(StartItem startItem : template.getItems())
		{
			L2ItemInstance item = ItemTable.getInstance().createItem(startItem.getItemId());
			if(startItem.getEnchantLevel() > 0)
				item.setEnchantLevel(startItem.getEnchantLevel());

			final long count = startItem.getCount();
			if(item.isStackable())
			{
				item.setCount(count);
				newChar.getInventory().addItem(item, true, false, true, "<CreateItemStackable>");
			}
			else
			{
				for(long i = 0; i < count; i++)
				{
					item = ItemTable.getInstance().createItem(startItem.getItemId());
					if(startItem.getEnchantLevel() > 0)
						item.setEnchantLevel(startItem.getEnchantLevel());
					newChar.getInventory().addItem(item, true, false, true, "<CreateItem>");
				}

				if(item.isEquipable() && startItem.isEquiped())
					newChar.getInventory().equipItem(item, false);
			}
			if(item.getItemId() == 5588) // tutorial book
				newChar.registerShortCut(new L2ShortCut(11, 0, L2ShortCut.TYPE_ITEM, item.getObjectId(), -1));
		}

		if(Config.ALLOW_START_ITEMS)
		{
			if(newChar.isMageClass())
			{
				for(int i = 0; i < Config.START_ITEMS_MAGE.length; i += 2)
				{
					L2ItemInstance item = ItemTable.getInstance().createItem(Config.START_ITEMS_MAGE[i]);
					item.setCount(Config.START_ITEMS_MAGE[i + 1]);
					newChar.getInventory().addItem(item, true, false, true, "<CreateItemStackable2>");
					if(item.isEquipable() && !item.isArrow())
						newChar.getInventory().equipItem(item, false);
				}
			}
			else
			{
				for(int i = 0; i < Config.START_ITEMS_FIGHTER.length; i += 2)
				{
					L2ItemInstance item = ItemTable.getInstance().createItem(Config.START_ITEMS_FIGHTER[i]);
					item.setCount(Config.START_ITEMS_FIGHTER[i + 1]);
					newChar.getInventory().addItem(item, true, false, true, "<CreateItem2>");
					if(item.isEquipable() && !item.isArrow())
						newChar.getInventory().equipItem(item, false);
				}
			}
		}
	}

	private static void startTutorialQuest(L2Player player)
	{
		Quest q = QuestManager.getQuest(255);
		if(q != null)
			q.newQuestState(player, Quest.CREATED);
	}
}