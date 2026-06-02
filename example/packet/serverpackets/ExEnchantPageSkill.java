package l2p.gameserver.serverpackets;

import java.util.Collection;
import l2p.commons.util.Rnd;
import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2ShortCut;
import l2p.gameserver.model.L2Skill;
import l2p.gameserver.model.base.CurrentEnchantSkillType;
import l2p.gameserver.model.base.Experience;
import l2p.gameserver.model.base.L2EnchantSkillLearn;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.model.instances.L2NpcInstance;
import l2p.gameserver.multilang.CustomMessage;
import l2p.gameserver.scripts.Functions;
import l2p.gameserver.skills.TimeStamp;
import l2p.gameserver.tables.ItemTable;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.tables.SkillTree;
import l2p.gameserver.utils.Log;
import org.apache.commons.lang3.tuple.Pair;

public class ExEnchantPageSkill
{
	public static String EX_ENCHANT_SKILL_BYPASS = "ExEnchantSkill";
	public static String BUY_ENCHANT_SKILL_BYPASS = "BuyEnchantSkill";

	public static L2GameServerPacket packetFor(L2Player player, L2NpcInstance trainer, String... vars)
	{
		if(vars.length < 2)
			return Msg.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT;
		if(player.isOutOfControl())
			return Msg.ActionFail;
		if(player.getLevel() < 76)
		{
			player.sendMessage(player.isLangRus()?"Для заточки умений необходим 76+ уровень.":"Need 76+ level.");
			return Msg.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT;
		}
		if(player.getClassId().getLevel() < 4)
		{
			player.sendMessage(player.isLangRus()?"Для заточки умений необходима третья профессия.":"Need third class.");
			return Msg.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT;
		}
		int id = 0;
		int lvl = 0;
		int page = 0;
		try
		{
			id = Integer.parseInt(vars[0]);
			lvl = Integer.parseInt(vars[1]);
			if(vars.length > 2)
				page = Integer.parseInt(vars[2]);
		}
		catch(Exception e)
		{
			return Msg.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT;
		}

		L2Skill oldSkill = player.getKnownSkill(id);
		if(oldSkill == null || oldSkill.getLevel() >= lvl)
			return Msg.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT;
		L2Skill skill = SkillTable.getInstance().getInfo(id, lvl);
		if(skill == null)
			return Msg.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT;
		if(player.getCurrentEnchantSkillType() == CurrentEnchantSkillType.SAFE_BOOK)
		{
			int counts = 0;
			int _requiredSp = 10000000;
			int _requiredExp = 100000;
			int _rate = 0;
			int _baseLvl = 1;
			L2EnchantSkillLearn[] skills = SkillTree.getInstance().getAvailableEnchantSkills(player);
			for(L2EnchantSkillLearn s : skills)
			{
				L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
				if(sk == null || sk != skill)
					continue;
				counts++;
				_requiredSp = s.getSpCost();
				_requiredExp = s.getExp();
				_rate = s.getRate(player);
				_baseLvl = s.getBaseLevel();
			}
			if(counts == 0)
				return Msg.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT;
			if(_rate == 0)
			{
				player.sendMessage("Wrong enchant chance skill: "+skill.getId()+" "+skill.getLevel());
				return Msg.ActionFail;
			}
			if(player.getSp() >= _requiredSp)
			{
				if(player.getExp() - Experience.LEVEL[76] >= _requiredExp)
				{
					L2ItemInstance spb = player.getInventory().getItemByItemId(Config.ENCHANT_SKILL_SAFE_BOOK_ID);
					if(spb == null)
						return new SystemMessage(SystemMessage.ITEMS_REQUIRED_FOR_SKILL_ENCHANT_ARE_INSUFFICIENT);
					player.getInventory().destroyItem(spb, 1, true, "<DestroyItemExEnchantPageSkill>");
				}
				else
					return new SystemMessage(SystemMessage.EXP_REQUIRED_FOR_SKILL_ENCHANT_IS_INSUFFICIENT);
			}
			else
				return new SystemMessage(SystemMessage.SP_REQUIRED_FOR_SKILL_ENCHANT_IS_INSUFFICIENT);

			TimeStamp ts = Config.SKILL_ENCHANT_UPDATE_REUSE ? player.getSharedGroupReuse(oldSkill) : null;
			if(Rnd.chance(_rate))
			{
				player.addSkill(skill, true);
				player.setSp(player.getSp() - _requiredSp);
				player.addExp(-_requiredExp);
				if(player.getExp() < Experience.LEVEL[player.getLevel()])
					player.decreaseLevel(true);
				player.updateStats();
				player.sendPacket(new SystemMessage(SystemMessage.EXPERIENCE_HAS_DECREASED_BY_S1).addNumber(_requiredExp));
				player.sendPacket(new SystemMessage(SystemMessage.SP_HAS_DECREASED_BY_S1).addNumber(_requiredSp));
				player.sendPacket(new SystemMessage(SystemMessage.SUCCEEDED_IN_ENCHANTING_SKILL_S1).addSkillName(id, _baseLvl));
				player.getListeners().onEnchantSkill(skill, true);
			}
			else
			{
				player.sendMessage(new CustomMessage("SKILL_ENCHANT_FAILED_CURRENT_LEVEL_OF_ENCHANT_SKILL_S1_WILL_REMAIN_UNCHANGED", player).addSkillName(oldSkill));
				Log.LogEnchantSkill(player, id, lvl, _rate, Log.EnchantSkillLog.Bless);
				player.getListeners().onEnchantSkill(skill, false);
			}
			if(oldSkill != skill)
			{
				if(ts != null && ts.hasNotPassed())
					player.disableSkill(skill, ts.getReuseCurrent());
				player.sendPacket(new SkillList(player));
				updateSkillShortcuts(player, skill.getId(), skill.getLevel());
			}
		}
		else
		{
			if(!L2NpcInstance.canBypassCheck(player, trainer))
				return Msg.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT;
			if(trainer.getNpcId() != Config.ALLOW_ESL && !(trainer.getTemplate().canTeach(player.getClassId()) || trainer.getTemplate().canTeach(player.getClassId().getParent())))
			{
				player.sendMessage(player.isLangRus()?"Вам необходимо найти мастера для вашей текущей профессии.":"You need to find the master for your current class.");
				return Msg.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT;
			}
			int counts = 0;
			int _requiredSp = 10000000;
			int _requiredExp = 100000;
			int _rate = 0;
			int _baseLvl = 1;
			L2EnchantSkillLearn[] skills = SkillTree.getInstance().getAvailableEnchantSkills(player);
			for(L2EnchantSkillLearn s : skills)
			{
				L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
				if(sk == null || sk != skill)
					continue;
				counts++;
				_requiredSp = s.getSpCost();
				_requiredExp = s.getExp();
				_rate = s.getRate(player);
				_baseLvl = s.getBaseLevel();
			}
			if(counts == 0)
				return Msg.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT;
			if(_rate == 0)
			{
				player.sendMessage("Wrong enchant chance skill: "+skill.getId()+" "+skill.getLevel());
				return Msg.ActionFail;
			}
			if(player.getSp() >= _requiredSp)
			{
				if(player.getExp() - Experience.LEVEL[76] >= _requiredExp)
				{
					if(lvl == 101 || lvl == 141)
					{
						int bookId;
						if(Config.ENCHANT_SKILL_SAFE_BOOK_ID > 0)
							bookId = player.getInventory().getCountOf(Config.ENCHANT_SKILL_SAFE_BOOK_ID) > 0 ? Config.ENCHANT_SKILL_SAFE_BOOK_ID : 6622;
						else
							bookId = 6622;
						L2ItemInstance spb = player.getInventory().getItemByItemId(bookId);
						if(spb == null)
							return new SystemMessage(SystemMessage.ITEMS_REQUIRED_FOR_SKILL_ENCHANT_ARE_INSUFFICIENT);
						player.getInventory().destroyItem(spb, 1, true, "<DestroyItemExEnchantPageSkill>");
					}
				}
				else
					return new SystemMessage(SystemMessage.EXP_REQUIRED_FOR_SKILL_ENCHANT_IS_INSUFFICIENT);
			}
			else
				return new SystemMessage(SystemMessage.SP_REQUIRED_FOR_SKILL_ENCHANT_IS_INSUFFICIENT);

			TimeStamp ts = Config.SKILL_ENCHANT_UPDATE_REUSE ? player.getSharedGroupReuse(oldSkill) : null;
			if(Rnd.chance(_rate))
			{
				player.addSkill(skill, true);
				player.setSp(player.getSp() - _requiredSp);
				player.addExp(-_requiredExp);
				if(player.getExp() < Experience.LEVEL[player.getLevel()])
					player.decreaseLevel(true);
				player.updateStats();
				player.sendPacket(new SystemMessage(SystemMessage.EXPERIENCE_HAS_DECREASED_BY_S1).addNumber(_requiredExp));
				player.sendPacket(new SystemMessage(SystemMessage.SP_HAS_DECREASED_BY_S1).addNumber(_requiredSp));
				player.sendPacket(new SystemMessage(SystemMessage.SUCCEEDED_IN_ENCHANTING_SKILL_S1).addSkillName(id, _baseLvl));
				player.getListeners().onEnchantSkill(skill, true);
			}
			else
			{
				skill = SkillTable.getInstance().getInfo(id, _baseLvl);
				player.addSkill(skill, true);
				player.sendPacket(new SystemMessage(SystemMessage.FAILED_IN_ENCHANTING_SKILL_S1).addSkillName(id, lvl));
				Log.LogEnchantSkill(player, id, lvl, _rate, Log.EnchantSkillLog.Fail);
				player.getListeners().onEnchantSkill(skill, false);
			}
			if(oldSkill != skill)
			{
				if(ts != null && ts.hasNotPassed())
					player.disableSkill(skill, ts.getReuseCurrent());
				player.sendPacket(new SkillList(player));
				updateSkillShortcuts(player, skill.getId(), skill.getLevel());
			}
		}
		return ExEnchantPageSkillList.packetFor(player, player.getLastNpc(), page);
	}

	public static L2GameServerPacket packetBuyFor(L2Player player, L2NpcInstance trainer, String... vars)
	{
		if(vars.length < 2)
			return Msg.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT;
		if(player.isOutOfControl())
			return Msg.ActionFail;
		if(player.getLevel() < 76)
		{
			player.sendMessage(player.isLangRus()?"Для заточки умений необходим 76+ уровень.":"Need 76+ level.");
			return Msg.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT;
		}
		if(player.getClassId().getLevel() < 4)
		{
			player.sendMessage(player.isLangRus()?"Для заточки умений необходима третья профессия.":"Need third class.");
			return Msg.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT;
		}
		int id = 0;
		int lvl = 0;
		int page = 0;
		try
		{
			id = Integer.parseInt(vars[0]);
			lvl = Integer.parseInt(vars[1]);
			if(vars.length > 2)
				page = Integer.parseInt(vars[2]);
		}
		catch(Exception e)
		{
			return Msg.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT;
		}

		L2Skill oldSkill = player.getKnownSkill(id);
		if(oldSkill == null || oldSkill.getLevel() >= lvl)
			return Msg.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT;
		L2Skill skill = SkillTable.getInstance().getInfo(id, lvl);
		if(skill == null)
			return Msg.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT;
		if(!L2NpcInstance.canBypassCheck(player, trainer))
			return Msg.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT;
		int counts = 0;
		int _baseLvl = 1;
		SkillTree skillTree = SkillTree.getInstance();
		L2EnchantSkillLearn[] skills = skillTree.getAvailableEnchantSkills(player);
		for(L2EnchantSkillLearn s : skills)
		{
			int skillLevel = s.getLevel();
			if(skillLevel >= 101 && skillLevel <= 130)
			{
				int diff = skillLevel - 100;
				if(diff > Config.SERVICE_BUY_ENCHANT_SKILL_LEVEL)
					continue;
				skillLevel = 100 + Config.SERVICE_BUY_ENCHANT_SKILL_LEVEL;
			}
			else if(skillLevel >= 141 && skillLevel <= 170)
			{
				int diff = skillLevel - 140;
				if(diff > Config.SERVICE_BUY_ENCHANT_SKILL_LEVEL)
					continue;
				skillLevel = 140 + Config.SERVICE_BUY_ENCHANT_SKILL_LEVEL;
			}

			L2EnchantSkillLearn skillLearn = skillTree.getEnchantSkillLearn(s.getId(), skillLevel);
			if(skillLearn == null)
				skillLearn = s;

			L2Skill sk = SkillTable.getInstance().getInfo(skillLearn.getId(), skillLevel);
			if(sk == null || sk != skill)
				continue;
			counts++;
			_baseLvl = s.getBaseLevel();
		}
		if(counts == 0)
			return Msg.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT;

		int itemId = 0;
		int itemCount = 0;
		if(Config.SERVICE_BUY_ENCHANT_SKILL_SPECIAL_PRICE.containsKey(skill.getId()))
		{
			Pair<Integer, Integer> pair = Config.SERVICE_BUY_ENCHANT_SKILL_SPECIAL_PRICE.get(skill.getId());
			if(player.getInventory().getCountOf(pair.getLeft()) < pair.getRight())
			{
				return new SystemMessage(SystemMessage.ITEMS_REQUIRED_FOR_SKILL_ENCHANT_ARE_INSUFFICIENT);
			}
			Functions.removeItem(player, pair.getLeft(), pair.getRight(), "<RemoveItemBuyEnchantSkill1>");

			itemId = pair.getLeft();
			itemCount = pair.getRight();
		}
		else
		{
			if(player.getInventory().getCountOf(Config.SERVICE_BUY_ENCHANT_SKILL_ITEM_ID) < Config.SERVICE_BUY_ENCHANT_SKILL_ITEM_COUNT)
			{
				return new SystemMessage(SystemMessage.ITEMS_REQUIRED_FOR_SKILL_ENCHANT_ARE_INSUFFICIENT);
			}
			Functions.removeItem(player, Config.SERVICE_BUY_ENCHANT_SKILL_ITEM_ID, Config.SERVICE_BUY_ENCHANT_SKILL_ITEM_COUNT, "<RemoveItemBuyEnchantSkill2>");

			itemId = Config.SERVICE_BUY_ENCHANT_SKILL_ITEM_ID;
			itemCount = Config.SERVICE_BUY_ENCHANT_SKILL_ITEM_COUNT;
		}

		TimeStamp ts = Config.SKILL_ENCHANT_UPDATE_REUSE ? player.getSharedGroupReuse(oldSkill) : null;

		player.addSkill(skill, true);
		player.updateStats();
		player.sendPacket(new SystemMessage(SystemMessage.SUCCEEDED_IN_ENCHANTING_SKILL_S1).addSkillName(id, _baseLvl));
		Log.addLog(player.toString() + " classic bought skill enchantment +" + Config.SERVICE_BUY_ENCHANT_SKILL_LEVEL + " " + skill.getName() + "(" + skill.getId() + ")" + " for " + itemCount + " " + ItemTable.getInstance().getTemplate(itemId).getName() + ".", "services");

		if(oldSkill != skill)
		{
			if(ts != null && ts.hasNotPassed())
				player.disableSkill(skill, ts.getReuseCurrent());
			player.sendPacket(new SkillList(player));
			updateSkillShortcuts(player, skill.getId(), skill.getLevel());
		}
		return ExEnchantPageSkillList.packetBuyFor(player, player.getLastNpc(), page);
	}

    private static void updateSkillShortcuts(L2Player player, int id, int lvl)
    {
        Collection<L2ShortCut> allShortCuts = player.getAllShortCuts();
        for(L2ShortCut sc : allShortCuts)
            if(sc.id == id && sc.type == L2ShortCut.TYPE_SKILL)
            {
                L2ShortCut newsc = new L2ShortCut(sc.slot, sc.page, sc.type, sc.id, lvl);
                player.sendPacket(new ShortCutRegister(newsc));
                player.registerShortCut(newsc);
            }
    }
}