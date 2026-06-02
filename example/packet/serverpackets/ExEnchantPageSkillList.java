package l2p.gameserver.serverpackets;

import java.util.ArrayList;
import java.util.List;

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

public class ExEnchantPageSkillList extends NpcHtmlMessage
{
	public static final String EX_ENCHANT_SKILLLIST_BYPASS = "ExEnchantSkillList";
	public static final String BUY_ENCHANT_SKILLLIST_BYPASS = "BuyEnchantSkillList";

	private static final int PAGES = 9;

	private final List<SkillEnchantEntry> _skills = new ArrayList<>();

	private int page;

	public ExEnchantPageSkillList(L2Player player, L2NpcInstance npc, int va)
	{
		super(player, npc);
		page = va;
	}

	public static ExEnchantPageSkillList packetFor(L2Player player, L2NpcInstance npc)
	{
		return packetFor(player, npc, 0);
	}

	public static ExEnchantPageSkillList packetFor(L2Player player, L2NpcInstance npc, int va)
	{
		L2EnchantSkillLearn[] skills = SkillTree.getInstance().getAvailableEnchantSkills(player);
		ExEnchantPageSkillList esl = new ExEnchantPageSkillList(player, npc, va);

		for(L2EnchantSkillLearn s : skills)
		{
			L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
			if(sk == null)
				continue;
			esl.addSkill(s);
		}

		return esl;
	}

	public static ExEnchantPageSkillList packetBuyFor(L2Player player, L2NpcInstance npc)
	{
		return packetBuyFor(player, npc, 0);
	}

	public static ExEnchantPageSkillList packetBuyFor(L2Player player, L2NpcInstance npc, int page)
	{
		SkillTree skillTree = SkillTree.getInstance();
		L2EnchantSkillLearn[] skills = skillTree.getAvailableEnchantSkills(player);
		ExEnchantPageSkillList esl = new ExEnchantPageSkillList(player, npc, page);

		for(L2EnchantSkillLearn s : skills)
		{
			L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
			if(sk == null)
				continue;

			int skillLevel = sk.getLevel();
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

			esl.addSkill(skillLearn);
		}

		return esl;
	}

	@Override
	public void processHtml(L2GameClient client)
	{
		try
		{
			L2Player player = client.getActiveChar();
			if(player == null)
				return;
			if(_skills.size() == 0)
			{
				player.sendPacket(Msg.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT);
				return;
			}
			setFile("trainer/ExEnchantSkillList.htm");
			StringBuilder sb = new StringBuilder();
			int i = _skills.size() / PAGES + Math.min(1, _skills.size() % PAGES);
			page = Math.min(page, i);
			if(!_skills.isEmpty())
			{
				int j = page * PAGES;
				int k = Math.max(0, Math.min((page + 1) * PAGES - 1, _skills.size() - 1));
				for(int m = j; m <= k; m++)
				{
					SkillEnchantEntry skillEnchantEntry = _skills.get(m);
					sb.append(skillEnchantEntry.toHtml(player));
				} 
			}
			replace("%skill_enchant_list%", sb.toString());
			if(i > 1)
			{
				sb.setLength(0);
				String padding = new CustomMessage("l2p.gameserver.ExEnchantSkillList.paging", player).toString();
				for(byte b = 0; b < i; b++)
				{
					sb.append("<td>");
					if(b == page)
						sb.append("&nbsp;").append(b + 1).append("&nbsp;");
					else
					{
						if(player.getCurrentEnchantSkillType() == CurrentEnchantSkillType.BUY_SERVICE)
							sb.append(" <a action=\"bypass -h ").append(BUY_ENCHANT_SKILLLIST_BYPASS).append(" ").append(b).append("\">&nbsp;").append(b + 1).append("&nbsp;</a>");
						else
							sb.append(" <a action=\"bypass -h ").append(EX_ENCHANT_SKILLLIST_BYPASS).append(" ").append(b).append("\">&nbsp;").append(b + 1).append("&nbsp;</a>");
					}
					sb.append("</td>");
				}
				replace("%paging%", padding.replace("%pages%", sb.toString()));
				sb.setLength(0);
			}
			else
				replace("%paging%", "");
		}
		finally
		{
			super.processHtml(client);
		} 
	}

	public void addSkill(L2EnchantSkillLearn ve)
	{
		_skills.add(new SkillEnchantEntry(ve));
	}

	class SkillEnchantEntry
	{
		private final L2EnchantSkillLearn se;

		public SkillEnchantEntry(L2EnchantSkillLearn ve)
		{
			se = ve;
		}

		public String toHtml(L2Player player)
		{
			String txt = StringUtils.EMPTY;
			L2Skill skill = SkillTable.getInstance().getInfo(se.getId(), se.getLevel());
			if(skill == null)
				return txt; 
			txt = new CustomMessage("l2p.gameserver.ExEnchantSkillList.SkillEntry", player).toString();
			txt = StringUtils.replace(txt, "%skill_icon%", skill.getIcon());
			txt = StringUtils.replace(txt, "%skill_name%", skill.getName());
			txt = StringUtils.replace(txt, "%skill_enchant_type%", se.getType());
			txt = StringUtils.replace(txt, "%skill_enchant_level%", String.valueOf(se.getLevel()));
			if(player.getCurrentEnchantSkillType() == CurrentEnchantSkillType.BUY_SERVICE)
				txt = StringUtils.replace(txt, "%skill_enchant_bypass%", ExEnchantPageSkillInfo.BUY_ENCHANT_SKILLINFO_BYPASS + " " + se.getId() + " " + se.getLevel() + " " + page);
			else
				txt = StringUtils.replace(txt, "%skill_enchant_bypass%", ExEnchantPageSkillInfo.EX_ENCHANT_SKILLINFO_BYPASS + " " + se.getId() + " " + se.getLevel() + " " + page);
			return txt;
		}
	}

	@Override
	protected ServerPacketOpcodes getOpcodes()
	{
		return ServerPacketOpcodes.NpcHtmlMessage;
	}
}