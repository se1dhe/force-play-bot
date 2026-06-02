package l2p.gameserver.clientpackets;

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
import l2p.gameserver.serverpackets.ExEnchantSkillList;
import l2p.gameserver.serverpackets.ShortCutRegister;
import l2p.gameserver.serverpackets.SkillList;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.skills.TimeStamp;
import l2p.gameserver.tables.ItemTable;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.tables.SkillTree;
import l2p.gameserver.utils.Log;
import org.apache.commons.lang3.tuple.Pair;

public class RequestExEnchantSkill extends L2GameClientPacket
{
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
        L2Player activeChar = getClient().getActiveChar();
        if(activeChar == null)
            return;
        if(activeChar.isOutOfControl())
        {
            activeChar.sendActionFailed();
            return;
        }
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
            L2Skill oldSkill = activeChar.getKnownSkill(_skillId);
            if(oldSkill == null || oldSkill.getLevel() >= _skillLvl)
            {
                sendPacket(new SystemMessage(SystemMessage.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT));
                return;
            }
            L2Skill skill = SkillTable.getInstance().getInfo(_skillId, _skillLvl);
            if(skill == null)
            {
                sendPacket(new SystemMessage(SystemMessage.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT));
                return;
            }
            int counts = 0;
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

                L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), skillLevel);
                if(sk == null)
                    continue;

                if(sk != skill)
                    continue;

                counts++;
            }
            if(counts == 0)
            {
                sendPacket(new SystemMessage(SystemMessage.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT));
                return;
            }

            int itemId = 0;
            int itemCount = 0;
            if(Config.SERVICE_BUY_ENCHANT_SKILL_SPECIAL_PRICE.containsKey(skill.getId()))
            {
                Pair<Integer, Integer> pair = Config.SERVICE_BUY_ENCHANT_SKILL_SPECIAL_PRICE.get(skill.getId());
                if(activeChar.getInventory().getCountOf(pair.getLeft()) < pair.getRight())
                {
                    sendPacket(new SystemMessage(SystemMessage.ITEMS_REQUIRED_FOR_SKILL_ENCHANT_ARE_INSUFFICIENT));
                    return;
                }
                Functions.removeItem(activeChar, pair.getLeft(), pair.getRight(), "<RemoveItemEnchantSkill1>");

                itemId = pair.getLeft();
                itemCount = pair.getRight();
            }
            else
            {
                if(activeChar.getInventory().getCountOf(Config.SERVICE_BUY_ENCHANT_SKILL_ITEM_ID) < Config.SERVICE_BUY_ENCHANT_SKILL_ITEM_COUNT)
                {
                    sendPacket(new SystemMessage(SystemMessage.ITEMS_REQUIRED_FOR_SKILL_ENCHANT_ARE_INSUFFICIENT));
                    return;
                }
                Functions.removeItem(activeChar, Config.SERVICE_BUY_ENCHANT_SKILL_ITEM_ID, Config.SERVICE_BUY_ENCHANT_SKILL_ITEM_COUNT, "<RemoveItemEnchantSkill2>");
                itemId = Config.SERVICE_BUY_ENCHANT_SKILL_ITEM_ID;
                itemCount = Config.SERVICE_BUY_ENCHANT_SKILL_ITEM_COUNT;
            }

            TimeStamp ts = null;
            if(Config.SKILL_ENCHANT_UPDATE_REUSE)
            {
                ts = activeChar.getSharedGroupReuse(oldSkill);
            }

            activeChar.addSkill(skill, true);
            activeChar.updateStats();
            activeChar.sendPacket(new SystemMessage(SystemMessage.SUCCEEDED_IN_ENCHANTING_SKILL_S1).addSkillName(_skillId, _skillLvl));
            Log.addLog(activeChar.toString() + " bought skill enchantment +" + Config.SERVICE_BUY_ENCHANT_SKILL_LEVEL + " " + skill.getName() + "(" + skill.getId() + ")" + " for " + itemCount + " " + ItemTable.getInstance().getTemplate(itemId).getName() + ".", "services");
            activeChar.getListeners().onEnchantSkill(skill, true);

            if(ts != null && ts.hasNotPassed())
                activeChar.disableSkill(skill, ts.getReuseCurrent());

            Functions.callScripts("services.BuyEnchantSkill", "showEnchantSkillList", new Object[]{activeChar});
            sendPacket(new SkillList(activeChar));
            updateSkillShortcuts(activeChar, _skillLvl);
        }
        else if(activeChar.getCurrentEnchantSkillType() == CurrentEnchantSkillType.SAFE_BOOK)
        {
            L2Skill oldSkill = activeChar.getKnownSkill(_skillId);
            if(oldSkill == null || oldSkill.getLevel() >= _skillLvl)
            {
                sendPacket(new SystemMessage(SystemMessage.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT));
                return;
            }
            L2Skill skill = SkillTable.getInstance().getInfo(_skillId, _skillLvl);
            if(skill == null)
            {
                sendPacket(new SystemMessage(SystemMessage.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT));
                return;
            }
            int counts = 0;
            int _requiredSp = 10000000;
            int _requiredExp = 100000;
            int _rate = 0;
            int _baseLvl = 1;
            L2EnchantSkillLearn[] skills = SkillTree.getInstance().getAvailableEnchantSkills(activeChar);
            for(L2EnchantSkillLearn s : skills)
            {
                L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
                if(sk == null || sk != skill)
                    continue;
                counts++;
                _requiredSp = s.getSpCost();
                _requiredExp = s.getExp();
                _rate = s.getRate(activeChar);
                _baseLvl = s.getBaseLevel();
            }
            if(counts == 0)
            {
                sendPacket(new SystemMessage(SystemMessage.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT));
                return;
            }
            if(_rate == 0)
            {
                activeChar.sendMessage("Wrong enchant chance skill: " + skill.getId() + " " + skill.getLevel());
                activeChar.sendActionFailed();
                return;
            }
            if(activeChar.getSp() >= _requiredSp)
            {
                if(activeChar.getExp() - Experience.LEVEL[76] >= _requiredExp)
                {
                    L2ItemInstance spb = activeChar.getInventory().getItemByItemId(Config.ENCHANT_SKILL_SAFE_BOOK_ID);
                    if(spb == null)
                    {
                        sendPacket(new SystemMessage(SystemMessage.ITEMS_REQUIRED_FOR_SKILL_ENCHANT_ARE_INSUFFICIENT));
                        return;
                    }
                    activeChar.getInventory().destroyItem(spb, 1, true, "<DestroyItemEnchantSkill>");
                }
            }
            else
            {
                sendPacket(new SystemMessage(SystemMessage.SP_REQUIRED_FOR_SKILL_ENCHANT_IS_INSUFFICIENT));
                return;
            }
            int typeOk = 0;
            TimeStamp ts = null;
            if(Config.SKILL_ENCHANT_UPDATE_REUSE)
            {
                ts = activeChar.getSharedGroupReuse(oldSkill);
            }
            if(Rnd.chance(_rate))
            {
                activeChar.addSkill(skill, true);
                activeChar.setSp(activeChar.getSp() - _requiredSp);
                activeChar.addExp(-_requiredExp);
                if(activeChar.getExp() < Experience.LEVEL[activeChar.getLevel()])
                    activeChar.decreaseLevel(true);
                activeChar.updateStats();
                activeChar.sendPacket(new SystemMessage(SystemMessage.EXPERIENCE_HAS_DECREASED_BY_S1).addNumber(_requiredExp));
                activeChar.sendPacket(new SystemMessage(SystemMessage.SP_HAS_DECREASED_BY_S1).addNumber(_requiredSp));
                activeChar.sendPacket(new SystemMessage(SystemMessage.SUCCEEDED_IN_ENCHANTING_SKILL_S1).addSkillName(_skillId, _skillLvl));
                Log.LogEnchantSkill(activeChar, _skillId, _skillLvl, _rate, Log.EnchantSkillLog.Success);
                activeChar.getListeners().onEnchantSkill(skill, true);
            }
            else
            {
                activeChar.sendMessage(new CustomMessage("SKILL_ENCHANT_FAILED_CURRENT_LEVEL_OF_ENCHANT_SKILL_S1_WILL_REMAIN_UNCHANGED", activeChar).addSkillName(oldSkill));
                typeOk = 2;
                Log.LogEnchantSkill(activeChar, _skillId, _skillLvl, _rate, Log.EnchantSkillLog.Bless);
                activeChar.getListeners().onEnchantSkill(skill, false);
            }

            if(ts != null && ts.hasNotPassed())
                activeChar.disableSkill(skill, ts.getReuseCurrent());

            showEnchantBookList(activeChar);

            sendPacket(new SkillList(activeChar));
            if(typeOk == 0)
                updateSkillShortcuts(activeChar, _skillLvl);
            else if(typeOk == 1)
                updateSkillShortcuts(activeChar, _baseLvl);
        }
        else if(activeChar.getCurrentEnchantSkillType() == CurrentEnchantSkillType.VOICE)
        {
            L2Skill oldSkill = activeChar.getKnownSkill(_skillId);
            if(oldSkill == null || oldSkill.getLevel() >= _skillLvl)
            {
                sendPacket(new SystemMessage(SystemMessage.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT));
                return;
            }
            L2Skill skill = SkillTable.getInstance().getInfo(_skillId, _skillLvl);
            if(skill == null)
            {
                sendPacket(new SystemMessage(SystemMessage.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT));
                return;
            }
            int counts = 0;
            int _requiredSp = 10000000;
            int _requiredExp = 100000;
            int _rate = 0;
            int _baseLvl = 1;
            L2EnchantSkillLearn[] skills = SkillTree.getInstance().getAvailableEnchantSkills(activeChar);
            for(L2EnchantSkillLearn s : skills)
            {
                L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
                if(sk == null || sk != skill)
                    continue;
                counts++;
                _requiredSp = s.getSpCost();
                _requiredExp = s.getExp();
                _rate = s.getRate(activeChar);
                _baseLvl = s.getBaseLevel();
            }
            if(counts == 0)
            {
                sendPacket(new SystemMessage(SystemMessage.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT));
                return;
            }
            if(_rate == 0)
            {
                activeChar.sendMessage("Wrong enchant chance skill: " + skill.getId() + " " + skill.getLevel());
                activeChar.sendActionFailed();
                return;
            }
            if(activeChar.getSp() >= _requiredSp)
            {
                if(activeChar.getExp() - Experience.LEVEL[76] >= _requiredExp)
                {
                    if(_skillLvl == 101 || _skillLvl == 141) // only first lvl requires book (101,
                    {
                        int bookId = 6622;
                        L2ItemInstance spb = activeChar.getInventory().getItemByItemId(bookId);
                        if(spb == null)
                        {
                            sendPacket(new SystemMessage(SystemMessage.ITEMS_REQUIRED_FOR_SKILL_ENCHANT_ARE_INSUFFICIENT));
                            return;
                        }
                        activeChar.getInventory().destroyItem(spb, 1, true, "<DestroyItemEnchantSkill>");
                    }
                }
                else
                {
                    sendPacket(new SystemMessage(SystemMessage.EXP_REQUIRED_FOR_SKILL_ENCHANT_IS_INSUFFICIENT));
                    return;
                }
            }
            else
            {
                sendPacket(new SystemMessage(SystemMessage.SP_REQUIRED_FOR_SKILL_ENCHANT_IS_INSUFFICIENT));
                return;
            }
            int typeOk = 0;
            TimeStamp ts = null;
            if(Config.SKILL_ENCHANT_UPDATE_REUSE)
            {
                ts = activeChar.getSharedGroupReuse(oldSkill);
            }
            if(Rnd.chance(_rate))
            {
                activeChar.addSkill(skill, true);
                activeChar.setSp(activeChar.getSp() - _requiredSp);
                activeChar.addExp(-_requiredExp);
                if(activeChar.getExp() < Experience.LEVEL[activeChar.getLevel()])
                    activeChar.decreaseLevel(true);
                activeChar.updateStats();
                activeChar.sendPacket(new SystemMessage(SystemMessage.EXPERIENCE_HAS_DECREASED_BY_S1).addNumber(_requiredExp));
                activeChar.sendPacket(new SystemMessage(SystemMessage.SP_HAS_DECREASED_BY_S1).addNumber(_requiredSp));
                activeChar.sendPacket(new SystemMessage(SystemMessage.SUCCEEDED_IN_ENCHANTING_SKILL_S1).addSkillName(_skillId, _skillLvl));
                Log.LogEnchantSkill(activeChar, _skillId, _skillLvl, _rate, Log.EnchantSkillLog.Success);
                activeChar.getListeners().onEnchantSkill(skill, true);
            }
            else
            {
                skill = SkillTable.getInstance().getInfo(_skillId, _baseLvl);
                activeChar.addSkill(skill, true);
                activeChar.sendPacket(new SystemMessage(SystemMessage.FAILED_IN_ENCHANTING_SKILL_S1).addSkillName(_skillId, _skillLvl));
                typeOk = 1;
                Log.LogEnchantSkill(activeChar, _skillId, _skillLvl, _rate, Log.EnchantSkillLog.Fail);
                activeChar.getListeners().onEnchantSkill(skill, false);
            }

            if(Config.CSS_SKILLS)
                Functions.callScripts("services.ClassesSkills", "enchant", new Object[]{activeChar, SkillTable.getSkillHashCode(oldSkill.getId(), oldSkill.getLevel()), SkillTable.getSkillHashCode(skill.getId(), skill.getLevel())});
            if(ts != null && ts.hasNotPassed())
                activeChar.disableSkill(skill, ts.getReuseCurrent());

            showEnchantVoiceCommandList(activeChar);
            sendPacket(new SkillList(activeChar));
            if(typeOk == 0)
                updateSkillShortcuts(activeChar, _skillLvl);
            else if(typeOk == 1)
                updateSkillShortcuts(activeChar, _baseLvl);
        }
        else
        {
            L2Skill oldSkill = activeChar.getKnownSkill(_skillId);
            if(oldSkill == null || oldSkill.getLevel() >= _skillLvl)
            {
                sendPacket(new SystemMessage(SystemMessage.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT));
                return;
            }
            L2Skill skill = SkillTable.getInstance().getInfo(_skillId, _skillLvl);
            if(skill == null)
            {
                sendPacket(new SystemMessage(SystemMessage.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT));
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
            int counts = 0;
            int _requiredSp = 10000000;
            int _requiredExp = 100000;
            int _rate = 0;
            int _baseLvl = 1;
            L2EnchantSkillLearn[] skills = SkillTree.getInstance().getAvailableEnchantSkills(activeChar);
            for(L2EnchantSkillLearn s : skills)
            {
                L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
                if(sk == null || sk != skill)
                    continue;
                counts++;
                _requiredSp = s.getSpCost();
                _requiredExp = s.getExp();
                _rate = s.getRate(activeChar);
                _baseLvl = s.getBaseLevel();
            }
            if(counts == 0)
            {
                sendPacket(new SystemMessage(SystemMessage.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT));
                return;
            }
            if(_rate == 0)
            {
                activeChar.sendMessage("Wrong enchant chance skill: " + skill.getId() + " " + skill.getLevel());
                activeChar.sendActionFailed();
                return;
            }
            if(activeChar.getSp() >= _requiredSp)
            {
                if(activeChar.getExp() - Experience.LEVEL[76] >= _requiredExp)
                {
                    if(_skillLvl == 101 || _skillLvl == 141) // only first lvl requires book (101,
                    {
                        int bookId = 6622;
                        L2ItemInstance spb = activeChar.getInventory().getItemByItemId(bookId);
                        if(spb == null)
                        {
                            sendPacket(new SystemMessage(SystemMessage.ITEMS_REQUIRED_FOR_SKILL_ENCHANT_ARE_INSUFFICIENT));
                            return;
                        }
                        activeChar.getInventory().destroyItem(spb, 1, true, "<DestroyItemEnchantSkill>");
                    }
                }
                else
                {
                    sendPacket(new SystemMessage(SystemMessage.EXP_REQUIRED_FOR_SKILL_ENCHANT_IS_INSUFFICIENT));
                    return;
                }
            }
            else
            {
                sendPacket(new SystemMessage(SystemMessage.SP_REQUIRED_FOR_SKILL_ENCHANT_IS_INSUFFICIENT));
                return;
            }
            int typeOk = 0;
            TimeStamp ts = null;
            if(Config.SKILL_ENCHANT_UPDATE_REUSE)
            {
                ts = activeChar.getSharedGroupReuse(oldSkill);
            }
            if(Rnd.chance(_rate))
            {
                activeChar.addSkill(skill, true);
                activeChar.setSp(activeChar.getSp() - _requiredSp);
                activeChar.addExp(-_requiredExp);
                if(activeChar.getExp() < Experience.LEVEL[activeChar.getLevel()])
                    activeChar.decreaseLevel(true);
                activeChar.updateStats();
                activeChar.sendPacket(new SystemMessage(SystemMessage.EXPERIENCE_HAS_DECREASED_BY_S1).addNumber(_requiredExp));
                activeChar.sendPacket(new SystemMessage(SystemMessage.SP_HAS_DECREASED_BY_S1).addNumber(_requiredSp));
                activeChar.sendPacket(new SystemMessage(SystemMessage.SUCCEEDED_IN_ENCHANTING_SKILL_S1).addSkillName(_skillId, _skillLvl));
                Log.LogEnchantSkill(activeChar, _skillId, _skillLvl, _rate, Log.EnchantSkillLog.Success);
                activeChar.getListeners().onEnchantSkill(skill, true);
            }
            else
            {
                skill = SkillTable.getInstance().getInfo(_skillId, _baseLvl);
                activeChar.addSkill(skill, true);
                activeChar.sendPacket(new SystemMessage(SystemMessage.FAILED_IN_ENCHANTING_SKILL_S1).addSkillName(_skillId, _skillLvl));
                typeOk = 1;
                Log.LogEnchantSkill(activeChar, _skillId, _skillLvl, _rate, Log.EnchantSkillLog.Fail);
                activeChar.getListeners().onEnchantSkill(skill, false);
            }

            if(Config.CSS_SKILLS)
                Functions.callScripts("services.ClassesSkills", "enchant", new Object[]{activeChar, SkillTable.getSkillHashCode(oldSkill.getId(), oldSkill.getLevel()), SkillTable.getSkillHashCode(skill.getId(), skill.getLevel())});
            if(ts != null && ts.hasNotPassed())
                activeChar.disableSkill(skill, ts.getReuseCurrent());

            trainer.showEnchantSkillList(activeChar);
            sendPacket(new SkillList(activeChar));
            if(typeOk == 0)
                updateSkillShortcuts(activeChar, _skillLvl);
            else if(typeOk == 1)
                updateSkillShortcuts(activeChar, _baseLvl);
        }
    }

    private void showEnchantBookList(L2Player player)
    {
        L2EnchantSkillLearn[] skills = SkillTree.getInstance().getAvailableEnchantSkills(player);
        ExEnchantSkillList esl = new ExEnchantSkillList();
        int counts = 0;

        for(L2EnchantSkillLearn s : skills)
        {
            L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
            if(sk == null)
                continue;
            counts++;
            esl.addSkill(s.getId(), s.getLevel(), s.getSpCost(), s.getExp());
        }
        if(counts == 0)
        {
            player.setCurrentEnchantSkillType(CurrentEnchantSkillType.NORMAL);
            player.sendPacket(Msg.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT);
        }
        else
        {
            player.setCurrentEnchantSkillType(CurrentEnchantSkillType.SAFE_BOOK);
            player.sendPacket(esl);
        }
    }

    private void showEnchantVoiceCommandList(L2Player player)
    {
        L2EnchantSkillLearn[] skills = SkillTree.getInstance().getAvailableEnchantSkills(player);
        ExEnchantSkillList esl = new ExEnchantSkillList();
        int counts = 0;

        for(L2EnchantSkillLearn s : skills)
        {
            L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
            if(sk == null)
                continue;
            counts++;
            esl.addSkill(s.getId(), s.getLevel(), s.getSpCost(), s.getExp());
        }
        if(counts == 0)
        {
            player.setCurrentEnchantSkillType(CurrentEnchantSkillType.NORMAL);
            player.sendPacket(Msg.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT);
        }
        else
        {
            player.setCurrentEnchantSkillType(CurrentEnchantSkillType.VOICE);
            player.sendPacket(esl);
        }
    }

    private void updateSkillShortcuts(L2Player player, int lvl)
    {
        Collection<L2ShortCut> allShortCuts = player.getAllShortCuts();
        for(L2ShortCut sc : allShortCuts)
            if(sc.id == _skillId && sc.type == L2ShortCut.TYPE_SKILL)
            {
                L2ShortCut newsc = new L2ShortCut(sc.slot, sc.page, sc.type, sc.id, lvl);
                player.sendPacket(new ShortCutRegister(newsc));
                player.registerShortCut(newsc);
            }
    }
}