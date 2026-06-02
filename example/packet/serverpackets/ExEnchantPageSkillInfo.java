package l2p.gameserver.serverpackets;

import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Skill;
import l2p.gameserver.model.base.CurrentEnchantSkillType;
import l2p.gameserver.model.base.L2EnchantSkillLearn;
import l2p.gameserver.model.instances.L2NpcInstance;
import l2p.gameserver.multilang.CustomMessage;
import l2p.gameserver.network.L2GameClient;
import l2p.gameserver.network.ServerPacketOpcodes;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.tables.SkillTree;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExEnchantPageSkillInfo extends NpcHtmlMessage
{
	public static String EX_ENCHANT_SKILLINFO_BYPASS = "ExEnchantSkillInfo";
	public static String BUY_ENCHANT_SKILLINFO_BYPASS = "BuyEnchantSkillInfo";
	private static final Logger _log = LoggerFactory.getLogger(ExEnchantPageSkillInfo.class);

	private L2EnchantSkillLearn esc;
	private int chance = 0;
	private int pnr = 0;
	private int reqId = 0;
	private int reqCount = 1;

	public ExEnchantPageSkillInfo(L2Player player, L2NpcInstance trainer)
	{
		super(player, trainer);
	}

	public static L2GameServerPacket packetFor(L2Player player, L2NpcInstance trainer, String... vars)
	{
		ExEnchantPageSkillInfo exEnchantSkillInfo = new ExEnchantPageSkillInfo(player, trainer);
		if(vars.length < 2)
			return Msg.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT;
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
		int backPageNum = 0;
		try
		{
			id = Integer.parseInt(vars[0]);
			lvl = Integer.parseInt(vars[1]);
			if(vars.length > 2)
				backPageNum = Integer.parseInt(vars[2]);
		}
		catch(Exception e)
		{
			return Msg.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT;
		}

		L2Skill skill = SkillTable.getInstance().getInfo(id, lvl);
		if(skill == null)
		{
			_log.warn("RequestExEnchantPageSkillInfo: skillId " + id + " level " + lvl + " not found in Datapack.");
			return Msg.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT;
		}
		if(player.getCurrentEnchantSkillType() == CurrentEnchantSkillType.SAFE_BOOK)
		{

		}
		else
		{
			if(!L2NpcInstance.canBypassCheck(player, trainer))
			{
				return Msg.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT;
			}
			if(trainer.getNpcId() != Config.ALLOW_ESL && !(trainer.getTemplate().canTeach(player.getClassId()) || trainer.getTemplate().canTeach(player.getClassId().getParent())))
			{
				player.sendMessage(player.isLangRus()?"Вам необходимо найти мастера для вашей текущей профессии.":"You need to find the master for your current class.");
				return Msg.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT;
			}
		}
		L2EnchantSkillLearn[] skills = SkillTree.getInstance().getAvailableEnchantSkills(player);
		L2EnchantSkillLearn esl = null;
		for(L2EnchantSkillLearn s : skills)
			if(s.getId() == id && s.getLevel() == lvl)
			{
				esl = s;
				break;
			}
		if(esl == null)
			return Msg.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT;
		exEnchantSkillInfo.esc = esl;
		exEnchantSkillInfo.chance = SkillTree.getInstance().getSkillRate(player, skill);
		exEnchantSkillInfo.pnr = backPageNum;
		if(lvl == 101 || lvl == 141)
		{
			int bookId;
			if(Config.ENCHANT_SKILL_SAFE_BOOK_ID > 0)
				bookId = player.getInventory().getCountOf(Config.ENCHANT_SKILL_SAFE_BOOK_ID) > 0 ? Config.ENCHANT_SKILL_SAFE_BOOK_ID : 6622;
			else
				bookId = 6622;
			exEnchantSkillInfo.reqId = bookId;
			exEnchantSkillInfo.reqCount = 1;
		}
		return exEnchantSkillInfo;
	}

	public static L2GameServerPacket packetBuyFor(L2Player player, L2NpcInstance trainer, String... vars)
	{
		ExEnchantPageSkillInfo exEnchantSkillInfo = new ExEnchantPageSkillInfo(player, trainer);
		if(vars.length < 2)
			return Msg.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT;
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
		int backPageNum = 0;
		try
		{
			id = Integer.parseInt(vars[0]);
			lvl = Integer.parseInt(vars[1]);
			if(vars.length > 2)
				backPageNum = Integer.parseInt(vars[2]);
		}
		catch(Exception e)
		{
			return Msg.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT;
		}

		L2Skill skill = SkillTable.getInstance().getInfo(id, lvl);
		if(skill == null)
		{
			_log.warn("RequestExEnchantPageSkillInfo: skillId " + id + " level " + lvl + " not found in Datapack.");
			return Msg.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT;
		}
		if(!L2NpcInstance.canBypassCheck(player, trainer))
		{
			return Msg.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT;
		}
		SkillTree skillTree = SkillTree.getInstance();
		L2EnchantSkillLearn[] skills = skillTree.getAvailableEnchantSkills(player);
		L2EnchantSkillLearn esl = null;
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
			if(sk == null)
				continue;

			if(sk == skill)
			{
				esl = skillLearn;
				break;
			}
		}
		if(esl == null)
			return Msg.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT;
		exEnchantSkillInfo.esc = esl;
		exEnchantSkillInfo.chance = 100;
		exEnchantSkillInfo.pnr = backPageNum;

		int itemId = 0;
		int itemCount = 0;
		if(Config.SERVICE_BUY_ENCHANT_SKILL_SPECIAL_PRICE.containsKey(skill.getId()))
		{
			Pair<Integer, Integer> pair = Config.SERVICE_BUY_ENCHANT_SKILL_SPECIAL_PRICE.get(skill.getId());
			itemId = pair.getLeft();
			itemCount = pair.getRight();
		}
		else
		{
			itemId = Config.SERVICE_BUY_ENCHANT_SKILL_ITEM_ID;
			itemCount = Config.SERVICE_BUY_ENCHANT_SKILL_ITEM_COUNT;
		}
		exEnchantSkillInfo.reqId = itemId;
		exEnchantSkillInfo.reqCount = itemCount;
		return exEnchantSkillInfo;
	}

	@Override
	public void processHtml(L2GameClient client)
	{
		try
		{
			L2Player player = client.getActiveChar();
			if(player == null)
				return;
			setFile("trainer/ExEnchantSkillInfo.htm");
			if(esc == null)
				return;
			L2Skill skill = SkillTable.getInstance().getInfo(esc.getId(), esc.getLevel());
			replace("%skill_id%", String.valueOf(esc.getId()));
			replace("%skill_level%", String.valueOf(esc.getLevel()));
			replace("%skill_icon%", skill.getIcon());
			replace("%skill_name%", skill.getName());
			replace("%skill_enchant_type%", esc.getType());
			if(reqId > 0)
			{
				String str = new CustomMessage("l2p.gameserver.ExEnchantSkillInfo.RequiredItem", player).toString();
				str = StringUtils.replace(str, "%item_id%", String.valueOf(reqId));
				str = StringUtils.replace(str, "%item_count%", String.valueOf(reqCount));
				replace("%required_item%", str);
			}
			else
				replace("%required_item%", "&nbsp;");
			replace("%backPageNum%", String.valueOf(pnr));
			if(player.getCurrentEnchantSkillType() == CurrentEnchantSkillType.BUY_SERVICE)
			{
				replace("%required_exp%", String.valueOf(0));
				replace("%required_sp%", String.valueOf(0));
				replace("%bypass_learn%", ExEnchantPageSkill.BUY_ENCHANT_SKILL_BYPASS);
				replace("%bypass_list%", ExEnchantPageSkillList.BUY_ENCHANT_SKILLLIST_BYPASS);
			}
			else
			{
				replace("%required_exp%", String.valueOf(SkillTree.getInstance().getSkillExpCost(player, skill)));
				replace("%required_sp%", String.valueOf(SkillTree.getInstance().getSkillSpCost(player, skill)));
				replace("%bypass_learn%", ExEnchantPageSkill.EX_ENCHANT_SKILL_BYPASS);
				replace("%bypass_list%", ExEnchantPageSkillList.EX_ENCHANT_SKILLLIST_BYPASS);
			}
			replace("%current_exp%", String.valueOf(player.getExp()));
			replace("%current_sp%", String.valueOf(player.getSp()));
			replace("%chance%", String.valueOf(chance));
		}
		finally
		{
			super.processHtml(client);
		}
	}

	@Override
	protected ServerPacketOpcodes getOpcodes()
	{
		return ServerPacketOpcodes.NpcHtmlMessage;
	}
}