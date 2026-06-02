package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Skill;
import l2p.gameserver.model.base.CurrentEnchantSkillType;
import l2p.gameserver.model.base.L2EnchantSkillLearn;
import l2p.gameserver.model.instances.L2NpcInstance;
import l2p.gameserver.serverpackets.ExEnchantSkillInfo;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.tables.SkillTree;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestExEnchantSkillInfo extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(RequestExEnchantSkillInfo.class);

	private int _skillId;
    private int _skillLvl;

    @Override
    public void readImpl()
    {
        _skillId = readD();
        _skillLvl = readD();
    }

    @Override
    public void runImpl()
    {
		if(_skillId <= 0 || _skillLvl <= 0)
			return;
        L2Player activeChar = getClient().getActiveChar();
        if(activeChar == null)
            return;
	    if(activeChar.getLevel() < 76)
	    {
		    activeChar.sendMessage(activeChar.isLangRus() ? "Для заточки умений необходим 76+ уровень." : "Need 76+ level.");
		    return;
	    }
	    if(activeChar.getClassId().getLevel() < 4)
	    {
		    activeChar.sendMessage(activeChar.isLangRus() ? "Для заточки умений необходима третья профессия." : "Need third class.");
		    return;
	    }
	    if(activeChar.getCurrentEnchantSkillType() == CurrentEnchantSkillType.BUY_SERVICE)
	    {
		    L2Skill skill = SkillTable.getInstance().getInfo(_skillId, _skillLvl);
		    if(skill == null || skill.getId() != _skillId)
		    {
			    _log.warn("RequestExEnchantSkillInfo: skillId " + _skillId + " level " + _skillLvl + " not found in Datapack.");
			    activeChar.sendPacket(new SystemMessage(SystemMessage.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT));
			    return;
		    }
		    boolean canteach = false;
		    L2EnchantSkillLearn[] skills = SkillTree.getInstance().getAvailableEnchantSkills(activeChar);
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

			    if(s.getId() == _skillId && skillLevel == _skillLvl)
			    {
				    canteach = true;
				    break;
			    }
		    }
		    if(!canteach)
		    {
			    sendPacket(new SystemMessage(SystemMessage.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT));
			    return;
		    }

		    ExEnchantSkillInfo asi = new ExEnchantSkillInfo(skill.getId(), skill.getLevel(), 0, 0, 100);
		    if(Config.SERVICE_BUY_ENCHANT_SKILL_SPECIAL_PRICE.containsKey(skill.getId()))
		    {
			    Pair<Integer, Integer> pair = Config.SERVICE_BUY_ENCHANT_SKILL_SPECIAL_PRICE.get(skill.getId());
			    asi.addRequirement(4, pair.getLeft(), pair.getRight(), 0);
		    }
		    else
		    {
			    asi.addRequirement(4, Config.SERVICE_BUY_ENCHANT_SKILL_ITEM_ID, Config.SERVICE_BUY_ENCHANT_SKILL_ITEM_COUNT, 0);
		    }
		    activeChar.sendPacket(asi);
	    }
	    else if(activeChar.getCurrentEnchantSkillType() == CurrentEnchantSkillType.SAFE_BOOK)
	    {
		    L2Skill skill = SkillTable.getInstance().getInfo(_skillId, _skillLvl);
		    if(skill == null || skill.getId() != _skillId)
		    {
			    _log.warn("RequestExEnchantSkillInfo: skillId " + _skillId + " level " + _skillLvl + " not found in Datapack.");
			    activeChar.sendPacket(new SystemMessage(SystemMessage.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT));
			    return;
		    }
		    boolean canteach = false;
		    L2EnchantSkillLearn[] skills = SkillTree.getInstance().getAvailableEnchantSkills(activeChar);
		    for(L2EnchantSkillLearn s : skills)
			    if(s.getId() == _skillId && s.getLevel() == _skillLvl)
			    {
				    canteach = true;
				    break;
			    }
		    if(!canteach)
		    {
			    sendPacket(new SystemMessage(SystemMessage.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT));
			    return;
		    }
		    int requiredSp = SkillTree.getInstance().getSkillSpCost(activeChar, skill);
		    int requiredExp = SkillTree.getInstance().getSkillExpCost(activeChar, skill);
		    int rate = SkillTree.getInstance().getSkillRate(activeChar, skill);
		    ExEnchantSkillInfo asi = new ExEnchantSkillInfo(skill.getId(), skill.getLevel(), requiredSp, requiredExp, rate);
			asi.addRequirement(4, Config.ENCHANT_SKILL_SAFE_BOOK_ID, 1, 0);
		    activeChar.sendPacket(asi);
	    }
		else if(activeChar.getCurrentEnchantSkillType() == CurrentEnchantSkillType.VOICE)
	    {
		    L2Skill skill = SkillTable.getInstance().getInfo(_skillId, _skillLvl);
		    if(skill == null || skill.getId() != _skillId)
		    {
			    _log.warn("RequestExEnchantSkillInfo: skillId " + _skillId + " level " + _skillLvl + " not found in Datapack.");
			    activeChar.sendPacket(new SystemMessage(SystemMessage.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT));
			    return;
		    }
		    boolean canteach = false;
		    L2EnchantSkillLearn[] skills = SkillTree.getInstance().getAvailableEnchantSkills(activeChar);
		    for(L2EnchantSkillLearn s : skills)
			    if(s.getId() == _skillId && s.getLevel() == _skillLvl)
			    {
				    canteach = true;
				    break;
			    }
		    if(!canteach)
		    {
			    sendPacket(new SystemMessage(SystemMessage.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT));
			    return;
		    }
		    int requiredSp = SkillTree.getInstance().getSkillSpCost(activeChar, skill);
		    int requiredExp = SkillTree.getInstance().getSkillExpCost(activeChar, skill);
		    int rate = SkillTree.getInstance().getSkillRate(activeChar, skill);
		    ExEnchantSkillInfo asi = new ExEnchantSkillInfo(skill.getId(), skill.getLevel(), requiredSp, requiredExp, rate);
			int bookId = 6622;
		    if(_skillLvl == 101 || _skillLvl == 141)
			    asi.addRequirement(4, bookId, 1, 0);
		    activeChar.sendPacket(asi);
	    }
		else
	    {
		    L2Skill skill = SkillTable.getInstance().getInfo(_skillId, _skillLvl);
		    if(skill == null || skill.getId() != _skillId)
		    {
			    _log.warn("RequestExEnchantSkillInfo: skillId " + _skillId + " level " + _skillLvl + " not found in Datapack.");
			    activeChar.sendPacket(new SystemMessage(SystemMessage.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT));
			    return;
		    }
		    L2NpcInstance trainer = activeChar.getLastNpc();
			if(trainer == null || !trainer.isInActingRange(activeChar) && !activeChar.isGM())
			{
			    activeChar.sendActionFailed();
			    return;
		    }
		    if(trainer.getNpcId() != Config.ALLOW_ESL && !(trainer.getTemplate().canTeach(activeChar.getClassId()) || trainer.getTemplate().canTeach(activeChar.getClassId().getParent())))
		    {
			    activeChar.sendMessage(activeChar.isLangRus() ? "Вам необходимо найти мастера для вашей текущей профессии." : "You need to find the master for your current class.");
			    return;
		    }
		    boolean canteach = false;
		    L2EnchantSkillLearn[] skills = SkillTree.getInstance().getAvailableEnchantSkills(activeChar);
		    for(L2EnchantSkillLearn s : skills)
			    if(s.getId() == _skillId && s.getLevel() == _skillLvl)
			    {
				    canteach = true;
				    break;
			    }
		    if(!canteach)
		    {
			    sendPacket(new SystemMessage(SystemMessage.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT));
			    return;
		    }
		    int requiredSp = SkillTree.getInstance().getSkillSpCost(activeChar, skill);
		    int requiredExp = SkillTree.getInstance().getSkillExpCost(activeChar, skill);
		    int rate = SkillTree.getInstance().getSkillRate(activeChar, skill);
		    ExEnchantSkillInfo asi = new ExEnchantSkillInfo(skill.getId(), skill.getLevel(), requiredSp, requiredExp, rate);
			int bookId = 6622;
		    if(_skillLvl == 101 || _skillLvl == 141)
			    asi.addRequirement(4, bookId, 1, 0);
		    activeChar.sendPacket(asi);
	    }
    }
}
