package l2p.gameserver.clientpackets;

import javolution.text.TextBuilder;
import l2p.gameserver.Config;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Skill;
import l2p.gameserver.model.SkillLearn;
import l2p.gameserver.model.base.L2PledgeSkillLearn;
import l2p.gameserver.model.instances.L2NpcInstance;
import l2p.gameserver.multilang.CustomMessage;
import l2p.gameserver.scripts.Functions;
import l2p.gameserver.serverpackets.AcquireSkillInfo;
import l2p.gameserver.serverpackets.NpcHtmlMessage;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.tables.SkillTree;
import l2p.gameserver.tables.Spellbook;

public class RequestAquireSkillInfo extends L2GameClientPacket
{
	private int _id;
	private int _level;
	private int _skillType;

	@Override
	protected void readImpl()
	{
		_id = readD();
		_level = readD();
		_skillType = readD();
	}

	@Override
	public void runImpl()
	{
		if(_id <= 0 || _level <= 0) // если читит
			return;
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;
		L2Skill skill = SkillTable.getInstance().getInfo(_id, _level);
		if(skill == null)
			return;
		if(Config.CSS_SKILLS && _skillType == 0 && (boolean) Functions.callScripts("services.ClassesSkills", "info", new Object[] { player, SkillTable.getSkillHashCode(_id, _level) }))
			return;
		L2NpcInstance trainer = player.getLastNpc();
		if(!L2NpcInstance.canBypassCheck(player, trainer))
			return;
		boolean canteach = false;
		if(skill == null)
		{
			player.sendActionFailed();
			return;
		}
		if(_skillType == 0)
		{
			if(!Config.ALLOW_LEARN_SKILLS_FROM_ANY_TRAINER)
			{
				if(!trainer.getTemplate().canTeach(player.getSkillLearningClassId()))
				{
					NpcHtmlMessage html = new NpcHtmlMessage(trainer.getObjectId());
					TextBuilder sb = new TextBuilder();
					sb.append("<html><head><body>");
					sb.append(new CustomMessage("l2p.gameserver.model.instances.L2NpcInstance.WrongTeacherClass", player));
					sb.append("</body></html>");
					html.setHtml(sb.toString());
					player.sendPacket(html);
					return;
				}
			}
			SkillLearn[] skills = SkillTree.getInstance().getAvailableSkills(player, player.getSkillLearningClassId());
			for(SkillLearn s : skills)
			{
				if(s.getId() == _id && s.getLevel() == _level)
				{
					canteach = true;
					break;
				}
			}
			if(!canteach)
			{
				player.sendActionFailed();
				return;
			}
			int requiredSp = SkillTree.getInstance().getSkillCost(player, skill);
			AcquireSkillInfo asi = new AcquireSkillInfo(skill.getId(), skill.getLevel(), requiredSp, 0);
			int spbId = Spellbook.getInstance().getBookForSkill(skill.getId(), _level);
			if(spbId > 0)
				asi.addRequirement(99, spbId, 1, 50);
			sendPacket(asi);
		}
		else if(_skillType == 2)
		{
			int requiredRep = 0;
			int itemId = 0;
			int count = 1;
			L2PledgeSkillLearn[] skills = SkillTree.getInstance().getAvailablePledgeSkills(player);
			for(L2PledgeSkillLearn s : skills)
				if(s.getId() == _id && s.getLevel() == _level)
				{
					canteach = true;
					requiredRep = s.getRepCost();
					itemId = s.getItemId();
					count = s.getCount();
					break;
				}
			if(!canteach)
			{
				player.sendActionFailed();
				return;
			}
			AcquireSkillInfo asi = new AcquireSkillInfo(skill.getId(), skill.getLevel(), requiredRep, 2);
			if(!Config.ALT_DISABLE_EGGS && itemId > 0)
				asi.addRequirement(1, itemId, count, 0);
			sendPacket(asi);
		}
		else
		{
			int costid = 0;
			int costcount = 0;
			int spcost = 0;
			SkillLearn[] skillsc = SkillTree.getInstance().getAvailableSkills(player);
			for(SkillLearn s : skillsc)
			{
				if(s.getId() == _id && s.getLevel() == _level)
				{
					canteach = true;
					costid = s.getIdCost();
					costcount = s.getCostCount();
					spcost = s.getSpCost();
					break;
				}
			}
			if(!canteach)
			{
				player.sendActionFailed();
				return;
			}
			AcquireSkillInfo asi = new AcquireSkillInfo(skill.getId(), skill.getLevel(), spcost, 1);
			if(costid > 0)
				asi.addRequirement(4, costid, costcount, 0);

			sendPacket(asi);
		}
	}
}
