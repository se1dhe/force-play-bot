package l2p.gameserver.clientpackets;

import javolution.text.TextBuilder;
import l2p.gameserver.Config;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2ShortCut;
import l2p.gameserver.model.L2Skill;
import l2p.gameserver.model.SkillLearn;
import l2p.gameserver.model.base.L2PledgeSkillLearn;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.model.instances.L2NpcInstance;
import l2p.gameserver.model.instances.L2VillageMasterInstance;
import l2p.gameserver.multilang.CustomMessage;
import l2p.gameserver.scripts.Functions;
import l2p.gameserver.serverpackets.*;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.tables.SkillTree;
import l2p.gameserver.tables.Spellbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class RequestAquireSkill extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(RequestAquireSkill.class);
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
	protected void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;
		L2Skill skill = SkillTable.getInstance().getInfo(_id, _level);
		if(skill == null)
		{
			player.sendActionFailed();
			return;
		}
		if(Config.CSS_SKILLS && _skillType == 0 && (boolean) Functions.callScripts("services.ClassesSkills", "done", new Object[] { player, SkillTable.getSkillHashCode(_id, _level) }))
			return;
		L2NpcInstance trainer = player.getLastNpc();
		if(!L2NpcInstance.canBypassCheck(player, trainer))
			return;
		int npcid = trainer.getNpcId();
		player.setSkillLearningClassId(player.getClassId());
		if(Math.max(player.getSkillLevel(_id), 0) + 1 != _level)
		{
			player.sendActionFailed();
			return;
		}
		if(!(skill.isCommon() || SkillTree.getInstance().isSkillPossible(player, _id, _level)))
		{
			player.sendMessage("Unavailable skill!");
			player.sendActionFailed();
			return;
		}
		boolean canteach = false;
		int _requiredSp = 100000000;
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
				L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
				if(sk == null || sk != skill || !sk.getCanLearn(player.getSkillLearningClassId()) || !sk.canTeachBy(npcid))
					continue;
				_requiredSp = SkillTree.getInstance().getSkillCost(player, skill);
				canteach = true;
				break;
			}
			if(!canteach)
			{
				player.sendActionFailed();
				return;
			}
			if(player.getSp() >= _requiredSp)
			{
				if(!Config.ALT_DISABLE_SPELLBOOKS)
				{
					int spbId = Spellbook.getInstance().getBookForSkill(skill.getId(), _level);
					if(spbId > 0)
					{
						L2ItemInstance spb = player.getInventory().getItemByItemId(spbId);
						if(spb == null)
						{
							player.sendPacket(new SystemMessage(SystemMessage.YOU_DO_NOT_HAVE_ENOUGH_ITEMS_TO_LEARN_SKILLS));
							return;
						}
						L2ItemInstance ri = player.getInventory().destroyItem(spb.getObjectId(), 1, true, "<DestroyItemAquireSkillNormal>");
						player.sendPacket(SystemMessage.removeItems(ri.getItemId(), 1));
					}
				}
			}
			else
			{
				player.sendPacket(new SystemMessage(SystemMessage.YOU_DO_NOT_HAVE_ENOUGH_SP_TO_LEARN_SKILLS));
				return;
			}
		}
		else if(_skillType == 1)
		{
			int costid = 0;
			int costcount = 0;
			SkillLearn[] skillsc = SkillTree.getInstance().getAvailableSkills(player);
			for(SkillLearn s : skillsc)
			{
				if(s.getId() == _id && s.getLevel() == _level)
				{
					costid = s.getIdCost();
					costcount = s.getCostCount();
					_requiredSp = s.getSpCost();
					canteach = true;
					break;
				}
			}
			if(!canteach)
			{
				player.sendActionFailed();
				return;
			}
			if(player.getSp() >= _requiredSp)
			{
				if(costid > 0)
				{
					L2ItemInstance spb = player.getInventory().getItemByItemId(costid);
					if(spb == null || spb.getCount() < costcount)
					{
						player.sendPacket(new SystemMessage(SystemMessage.YOU_DO_NOT_HAVE_ENOUGH_ITEMS_TO_LEARN_SKILLS));
						return;
					}
					L2ItemInstance ri = player.getInventory().destroyItem(spb, costcount, true, "<DestroyItemAquireSkillFish>");
					player.sendPacket(SystemMessage.removeItems(ri.getItemId(), costcount));
				}
			}
			else
			{
				player.sendPacket(new SystemMessage(SystemMessage.YOU_DO_NOT_HAVE_ENOUGH_SP_TO_LEARN_SKILLS));
				return;
			}
		}
		else
		{
			if(_skillType != 2)
			{
				_log.warn(player.toString() + " disconnected. Recived Wrong Packet Data in Aquired Skill: " + _skillType);
				player.logout(true);
				return;
			}

			if(!player.isClanLeader())
			{
				player.sendPacket(new SystemMessage(SystemMessage.ONLY_THE_CLAN_LEADER_IS_ENABLED));
				return;
			}
			int itemId = 0;
			int count = 1;
			int repCost = 100000000;
			L2PledgeSkillLearn[] skills = SkillTree.getInstance().getAvailablePledgeSkills(player);
			for(L2PledgeSkillLearn s : skills)
			{
				if(s.getId() == _id && s.getLevel() == _level)
				{
					itemId = s.getItemId();
					count = s.getCount();
					repCost = s.getRepCost();
					canteach = true;
					break;
				}
			}
			if(!canteach || !(trainer instanceof L2VillageMasterInstance))
			{
				player.sendActionFailed();
				return;
			}
			if(player.getClan().getReputationScore() >= repCost)
			{
				if(!Config.ALT_DISABLE_EGGS && itemId > 0)
				{
					L2ItemInstance spb = player.getInventory().getItemByItemId(itemId);
					if(spb == null || spb.getCount() < count)
					{
						player.sendPacket(new SystemMessage(SystemMessage.YOU_DO_NOT_HAVE_ENOUGH_ITEMS_TO_LEARN_SKILLS));
						return;
					}
					player.getInventory().destroyItem(spb, count, true, "<DestroyItemAquireSkillPledge>");
					player.sendPacket(SystemMessage.removeItems(itemId, count));
				}
			}
			else
			{
				player.sendPacket(new SystemMessage(SystemMessage.THE_ATTEMPT_TO_ACQUIRE_THE_SKILL_HAS_FAILED_BECAUSE_OF_AN_INSUFFICIENT_CLAN_REPUTATION_SCORE));
				return;
			}
			player.getClan().incReputation(-repCost, false, "AquireSkill");
			player.getClan().addNewSkill(skill, true);
			player.getClan().addAndShowSkillsToPlayer(player);
			player.getClan().broadcastToOnlineMembers(new SystemMessage(SystemMessage.THE_CLAN_SKILL_S1_HAS_BEEN_ADDED).addSkillName(_id, _level));
			((L2VillageMasterInstance) trainer).showClanSkillWindow(player);
			return;
		}

		player.addSkill(skill, true);
		player.sendPacket(new SkillList(player));
		player.setSp(player.getSp() - _requiredSp);
		player.updateStats();
		player.sendPacket(new SystemMessage(SystemMessage.SP_HAS_DECREASED_BY_S1).addNumber(_requiredSp));
		player.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_EARNED_S1).addSkillName(_id, _level));
		if(_level > 1)
		{
			Collection<L2ShortCut> allShortCuts = player.getAllShortCuts();
			for(L2ShortCut sc : allShortCuts)
				if(sc.id == _id && sc.type == L2ShortCut.TYPE_SKILL)
				{
					L2ShortCut newsc = new L2ShortCut(sc.slot, sc.page, sc.type, sc.id, _level);
					player.sendPacket(new ShortCutRegister(newsc));
					player.registerShortCut(newsc);
				}
		}
		if(_id >= 1368 && _id <= 1372) // if skill is expand sendpacket :)
			player.sendPacket(new ExStorageMaxCount(player));
		if(trainer != null)
		{
			if(_skillType == 0)
				trainer.showSkillList(player);
			else if(_skillType == 1)
				trainer.showFishingSkillList(player);
		}
	}
}
