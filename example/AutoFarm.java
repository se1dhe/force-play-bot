package commands.voiced;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import l2p.gameserver.Config;
import l2p.gameserver.autofarm.AutoFarmContext;
import l2p.gameserver.autofarm.manager.AutoFarmManager;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.data.htm.HtmCache;
import l2p.gameserver.handler.IVoicedCommandHandler;
import l2p.gameserver.handler.VoicedCommandHandler;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Skill;
import l2p.gameserver.model.L2Skill.SkillType;
import l2p.gameserver.model.L2Summon;
import l2p.gameserver.multilang.CustomMessage;
import l2p.gameserver.scripts.Functions;
import l2p.gameserver.scripts.ScriptFile;
import l2p.gameserver.tables.ItemTable;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.templates.L2Item;
import l2p.gameserver.utils.Log;
import l2p.gameserver.utils.Util;

public class AutoFarm implements IVoicedCommandHandler, ScriptFile
{
    /**
     * Команды
     */
    private static final String[] _commands = new String[] {
        "autofarm",
        "autosummonfarm",
        "farmstart",
        "farmstop",
        "buyfarm",
        "buyfarmTime",
        "tryFreeTime",
        "expendLimit",
        "changeSkillType",
        "refreshSkills",
        "removeSkill",
        "addSkill",
        "addNewSkill",
        "editFarmOption",
        "editSummonSkills",
        "removeSummonSkill",
        "addSummonSkill",
        "addNewSummonSkill",
        "editSummonFarmOption"
    };

    @Override
    public boolean useVoicedCommand(String command, L2Player player, String args)
    {
        if(!Config.ALLOW_AUTO_FARM)
            return false;

        if(player == null)
            return false;

        AutoFarmContext autoFarmSystem = player.getFarmSystem();
        int farmType = player.getVarInt("farmType", Config.FARM_TYPE);

        if(command.startsWith("farmstart")) // Вкл автофарм (если он активирован) и вывод html
        {
            String[] param = args.split(" ");
            String spellType = "attack";
            try
            {
                spellType = param[0];
            }
            catch(Exception e)
            {}

            if(spellType == null || spellType.isEmpty())
                spellType = "attack";

            if(autoFarmSystem.isActiveAutofarm())
                autoFarmSystem.startFarmTask();
            else
                player.sendMessage(new CustomMessage("CANT_ACTIVATE_AUTO_FARM_YOU_HAVE_TO_PURCHASE_IT", player));

            sendMainHtml(player, autoFarmSystem, null, farmType, spellType);
            return true;
        }
        else if(command.startsWith("editSummonSkills")) // Редактирование скилов суммона
        {
            String[] param = args.split(" ");
            String spellType = "attack";
            String summonSpellType = "attack";
            try
            {
                summonSpellType = param[0];
            }
            catch(Exception e)
            {}
            try
            {
                spellType = param[1];
            }
            catch(Exception e)
            {}

            if(spellType == null || spellType.isEmpty())
                spellType = "attack";
            if(summonSpellType == null || summonSpellType.isEmpty())
                summonSpellType = "attack";

            if(!autoFarmSystem.isUseSummonSkills())
            {
                player.sendMessage(new CustomMessage("YOU_CANT_EDIT_SETTINGS_THE_OPTION_IS_DISABLED", player));
                sendMainHtml(player, autoFarmSystem, null, farmType, spellType);
                return false;
            }

            if(player.getPet() == null)
            {
                player.sendMessage(new CustomMessage("YOU_CANT_USE_THIS_OPTION", player));
                sendMainHtml(player, autoFarmSystem, null, farmType, spellType);
                return false;
            }

            sendSummonHtml(player, autoFarmSystem, null, farmType, summonSpellType, spellType);
            return true;
        }
        else if(command.startsWith("changeSkillType")) // Сменить тип скила
        {
            String[] param = args.split(" ");
            String spellType = "attack";
            try
            {
                spellType = param[0];
            }
            catch(Exception e)
            {}
            if(spellType == null || spellType.isEmpty())
                spellType = "attack";
            sendMainHtml(player, autoFarmSystem, null, farmType, spellType);
            return true;
        }
        else if(command.startsWith("refreshSkills")) // Обновить скилы на панели шорткатов
        {
            String[] param = args.split(" ");
            String spellType = "attack";
            String shortCutPage = "1";
            try
            {
                spellType = param[0];
            }
            catch(Exception e)
            {}
            try
            {
                shortCutPage = param[1];
            }
            catch(Exception e)
            {}

            if(spellType == null || spellType.isEmpty())
                spellType = "attack";
            if(shortCutPage == null || shortCutPage.isEmpty())
                shortCutPage = "1";

            autoFarmSystem.setShortcutPageValue(Integer.parseInt(shortCutPage));
            autoFarmSystem.checkAllSlots();
            sendMainHtml(player, autoFarmSystem, null, farmType, spellType);
            return true;
        }
        else if(command.startsWith("removeSkill")) // Удалить скил
        {
            String[] param = args.split(" ");
            String spellType = null;
            String skillId = null;

            try
            {
                spellType = param[0];
            }
            catch(Exception e)
            {}

            try
            {
                skillId = param[1];
            }
            catch(Exception e)
            {}

            if(spellType == null || spellType.isEmpty())
                spellType = "attack";

            if(spellType != null && skillId != null)
            {
                List<Integer> skillIds = null;
                switch(spellType)
                {
                    case "attack":
                        skillIds = autoFarmSystem.getAttackSpells();
                        break;
                    case "chance":
                        skillIds = autoFarmSystem.getChanceSpells();
                        break;
                    case "self":
                        skillIds = autoFarmSystem.getSelfSpells();
                        break;
                    case "heal":
                        skillIds = autoFarmSystem.getLowLifeSpells();
                        break;
                }

                if(skillIds != null && skillIds.contains(Integer.parseInt(skillId)))
                {
                    skillIds.remove((Object) Integer.parseInt(skillId));
                    switch(spellType)
                    {
                        case "attack":
                            autoFarmSystem.saveSkills("farmAttackSkills");
                            break;
                        case "chance":
                            autoFarmSystem.saveSkills("farmChanceSkills");
                            break;
                        case "self":
                            autoFarmSystem.saveSkills("farmSelfSkills");
                            break;
                        case "heal":
                            autoFarmSystem.saveSkills("farmHealSkills");
                            break;
                    }
                }
            }
            sendMainHtml(player, autoFarmSystem, null, farmType, spellType);
            return true;
        }
        else if(command.startsWith("removeSummonSkill")) // Удаляем скил суммона
        {
            String[] param = args.split(" ");
            String summonSpellType = null;
            String skillId = null;
            String spellType = null;
            try
            {
                summonSpellType = param[0];
            }
            catch(Exception e)
            {}

            try
            {
                skillId = param[1];
            }
            catch(Exception e)
            {}

            try
            {
                spellType = param[2];
            }
            catch(Exception e)
            {}

            if(summonSpellType != null && skillId != null && spellType != null)
            {
                List<Integer> skillIds = null;
                switch(summonSpellType)
                {
                    case "attack":
                        skillIds = autoFarmSystem.getSummonAttackSpells();
                        break;
                    case "self":
                        skillIds = autoFarmSystem.getSummonSelfSpells();
                        break;
                    case "heal":
                        skillIds = autoFarmSystem.getSummonHealSpells();
                        break;
                }

                if(skillIds != null && skillIds.contains(Integer.parseInt(skillId)))
                {
                    skillIds.remove((Object) Integer.parseInt(skillId));
                    switch(spellType)
                    {
                        case "attack":
                            autoFarmSystem.saveSkills("farmAttackSummonSkills");
                            break;
                        case "self":
                            autoFarmSystem.saveSkills("farmSelfSummonSkills");
                            break;
                        case "heal":
                            autoFarmSystem.saveSkills("farmHealSummonSkills");
                            break;
                    }
                }
            }

            sendSummonHtml(player, autoFarmSystem, null, farmType, summonSpellType, spellType);
            return true;
        }
        else if(command.startsWith("addSkill")) // Добавить скил для фарма
        {
            String[] param = args.split(" ");
            String spellType = null;
            String page = "1";
            try
            {
                spellType = param[0];
            }
            catch(Exception e)
            {}

            try
            {
                page = param[1];
            }
            catch(Exception e)
            {}

            if(spellType != null)
                sendAddSkillHtml(player, autoFarmSystem, spellType, Integer.parseInt(page));
            return true;
        }
        else if(command.startsWith("addSummonSkill")) // добавить скил суммону
        {
            String[] param = args.split(" ");
            String summonSpellType = null;
            String spellType = null;
            String page = "1";
            try
            {
                summonSpellType = param[0];
            }
            catch(Exception e)
            {}
            try
            {
                spellType = param[1];
            }
            catch(Exception e)
            {}
            try
            {
                page = param[2];
            }
            catch(Exception e)
            {}
            if(summonSpellType != null && spellType != null)
                sendAddSummonSkillHtml(player, autoFarmSystem, farmType, summonSpellType, spellType, Integer.parseInt(page));
            return true;
        }
        else if(command.startsWith("addNewSkill")) // Добавить новый скил
        {
            String[] param = args.split(" ");
            String skillId = null;
            String spellType = null;

            try
            {
                skillId = param[0];
            }
            catch(Exception e)
            {}
            try
            {
                spellType = param[1];
            }
            catch(Exception e)
            {}

            if(spellType == null || spellType.isEmpty())
                spellType = "attack";
            if(skillId != null && spellType != null)
            {
                L2Skill knownSkill = player.getKnownSkill(Integer.parseInt(skillId));
                if(knownSkill != null)
                {
                    switch(spellType)
                    {
                        case "attack":
                        {
                            if(autoFarmSystem.getAttackSpells().size() >= 8)
                            {
                                sendMainHtml(player, autoFarmSystem, null, farmType, spellType);
                                return false;
                            }
                            if(!knownSkill.isSpoilSkill()
                                    && !knownSkill.isSweepSkill()
                                    && knownSkill.getId() != 1263
                                    && (knownSkill.getSkillType() == SkillType.AGGRESSION
                                    || knownSkill.getSkillType() == SkillType.PDAM
                                    || knownSkill.getSkillType() == SkillType.MANADAM
                                    || knownSkill.getSkillType() == SkillType.MDAM
                                    || knownSkill.getSkillType() == SkillType.DRAIN
                                    || knownSkill.getSkillType() == SkillType.CPDAM
                                    || knownSkill.getSkillType() == SkillType.STUN))
                            {
                                autoFarmSystem.getAttackSpells().add(knownSkill.getId());
                                autoFarmSystem.saveSkills("farmAttackSkills");
                            }
                            break;
                        }
                        case "chance":
                        {
                            if(autoFarmSystem.getChanceSpells().size() >= 8)
                            {
                                sendMainHtml(player, autoFarmSystem, null, farmType, spellType);
                                return false;
                            }
                            if(knownSkill.getSkillType() == SkillType.DOT
                                    || knownSkill.getSkillType() == SkillType.MDOT
                                    || knownSkill.getSkillType() == SkillType.POISON
                                    || knownSkill.getSkillType() == SkillType.BLEED
                                    || knownSkill.getSkillType() == SkillType.DEBUFF
                                    || knownSkill.getSkillType() == SkillType.SLEEP
                                    || knownSkill.getSkillType() == SkillType.ROOT
                                    || knownSkill.getSkillType() == SkillType.PARALYZE
                                    || knownSkill.getSkillType() == SkillType.MUTE
                                    || knownSkill.isSpoilSkill()
                                    || knownSkill.isSweepSkill()
                                    || knownSkill.getId() == 1263)
                            {
                                autoFarmSystem.getChanceSpells().add(knownSkill.getId());
                                autoFarmSystem.saveSkills("farmChanceSkills");
                            }
                            break;
                        }
                        case "self":
                        {
                            if(autoFarmSystem.getSelfSpells().size() >= 8)
                            {
                                sendMainHtml(player, autoFarmSystem, null, farmType, spellType);
                                return false;
                            }
                            if(!knownSkill.isToggle() && !knownSkill.isMusic() && knownSkill.getSkillType() != SkillType.BUFF && !knownSkill.isCubicSkill())
                                return false;
                            autoFarmSystem.getSelfSpells().add(knownSkill.getId());
                            autoFarmSystem.saveSkills("farmSelfSkills");
                            break;
                        }
                        case "heal":
                        {
                            if(autoFarmSystem.getLowLifeSpells().size() >= 8)
                            {
                                sendMainHtml(player, autoFarmSystem, null, farmType, spellType);
                                return false;
                            }
                            if(knownSkill.getSkillType() != SkillType.DRAIN && knownSkill.getSkillType() != SkillType.HEAL && knownSkill.getSkillType() != SkillType.HEAL_PERCENT && knownSkill.getSkillType() != SkillType.MANAHEAL && knownSkill.getSkillType() != SkillType.MANAHEAL_PERCENT)
                                return false;
                            autoFarmSystem.getLowLifeSpells().add(knownSkill.getId());
                            autoFarmSystem.saveSkills("farmHealSkills");
                            break;
                        }
                    }
                }
            }
            sendMainHtml(player, autoFarmSystem, null, farmType, spellType);
            return true;
        }
        else if(command.startsWith("addNewSummonSkill")) // Добавить новый скил суммону
        {
            String[] param = args.split(" ");
            String skillId = null;
            String type = null;
            String spellType = null;
            try
            {
                skillId = param[0];
            }
            catch(Exception e)
            {}
            try
            {
                type = param[1];
            }
            catch(Exception e)
            {}
            try
            {
                spellType = param[2];
            }
            catch(Exception e)
            {}

            if(skillId != null && type != null && spellType != null)
            {
                if(player.getPet() == null)
                {
                    player.sendMessage(new CustomMessage("YOU_CANT_USE_THIS_OPTION", player));
                    sendSummonHtml(player, autoFarmSystem, null, farmType, type, spellType);
                    return false;
                }

                L2Summon summon = player.getPet();
                if(summon.isPet() && summon.getLevel() - player.getLevel() > 20)
                {
                    player.sendPacket(Msg.THE_PET_IS_TOO_HIGH_LEVEL_TO_CONTROL);
                    sendSummonHtml(player, autoFarmSystem, null, farmType, type, spellType);
                    return false;
                }

                int availableLevel = summon.getTemplate().getAvailableLevel(summon, Integer.parseInt(skillId));
                if(availableLevel > 0)
                {
                    L2Skill skill = SkillTable.getInstance().getInfo(Integer.parseInt(skillId), availableLevel);
                    if(skill != null)
                    {
                        switch(type)
                        {
                            case "attack":
                            {
                                if(autoFarmSystem.getSummonAttackSpells().size() >= 8)
                                {
                                    sendSummonHtml(player, autoFarmSystem, null, farmType, type, spellType);
                                    return false;
                                }
                                if(skill.getSkillType() == SkillType.AGGRESSION
                                        || skill.getSkillType() == SkillType.PDAM
                                        || skill.getSkillType() == SkillType.MANADAM
                                        || skill.getSkillType() == SkillType.MDAM
                                        || skill.getSkillType() == SkillType.DRAIN
                                        || skill.getSkillType() == SkillType.CPDAM
                                        || skill.getSkillType() == SkillType.STUN)
                                {
                                    autoFarmSystem.getSummonAttackSpells().add(skill.getId());
                                    autoFarmSystem.saveSkills("farmAttackSummonSkills");
                                }
                                break;
                            }
                            case "self":
                            {
                                if(autoFarmSystem.getSummonSelfSpells().size() >= 8)
                                {
                                    sendSummonHtml(player, autoFarmSystem, null, farmType, type, spellType);
                                    return false;
                                }
                                if(!skill.isToggle() && !skill.isMusic() && skill.getSkillType() != SkillType.BUFF && !skill.isCubicSkill())
                                {
                                    return false;
                                }
                                autoFarmSystem.getSummonSelfSpells().add(skill.getId());
                                autoFarmSystem.saveSkills("farmSelfSummonSkills");
                                break;
                            }
                            case "heal":
                            {
                                if(autoFarmSystem.getSummonHealSpells().size() >= 8)
                                {
                                    sendSummonHtml(player, autoFarmSystem, null, farmType, type, spellType);
                                    return false;
                                }
                                if(skill.getSkillType() != SkillType.DRAIN
                                        && skill.getSkillType() != SkillType.HEAL
                                        && skill.getSkillType() != SkillType.HEAL_PERCENT
                                        && skill.getSkillType() != SkillType.MANAHEAL
                                        && skill.getSkillType() != SkillType.MANAHEAL_PERCENT)
                                {
                                    return false;
                                }
                                autoFarmSystem.getSummonHealSpells().add(skill.getId());
                                autoFarmSystem.saveSkills("farmHealSummonSkills");
                                break;
                            }
                        }
                    }
                }
            }

            sendSummonHtml(player, autoFarmSystem, null, farmType, type, spellType);
            return true;
        }
        else if(command.startsWith("editFarmOption")) // Настройка опций фарма
        {
            String[] param = args.split(" ");
            String spellType = "attack";
            String farmCommand = null;
            try
            {
                spellType = param[0];
            }
            catch(Exception e)
            {}
            try
            {
                farmCommand = param[1];
            }
            catch(Exception e)
            {}

            if(spellType != null && farmCommand != null)
            {
                boolean successSetVarriable = false;
                switch(farmType)
                {
                    case 0: // Опции для воинов
                    {
                        if(farmCommand.equalsIgnoreCase("farmLeaderAssist"))
                        {
                            boolean isLeaderAssist = autoFarmSystem.isLeaderAssist() ? false : true;
                            autoFarmSystem.setLeaderAssist(isLeaderAssist, false);
                            successSetVarriable = true;
                        }
                        else if(farmCommand.equalsIgnoreCase("farmAssistMonsterAttack"))
                        {
                            boolean isAssistMonsterAttack = autoFarmSystem.isAssistMonsterAttack() ? false : true;
                            autoFarmSystem.setAssistMonsterAttack(isAssistMonsterAttack, false);
                            successSetVarriable = true;
                        }
                        else if(farmCommand.equalsIgnoreCase("farmKeepLocation"))
                        {
                            boolean isKeepLocation = autoFarmSystem.isKeepLocation() ? false : true;
                            autoFarmSystem.setKeepLocation(player.getLoc(), isKeepLocation, false);
                            successSetVarriable = true;
                        }
                        else if(farmCommand.equalsIgnoreCase("farmDelaySkills"))
                        {
                            boolean isExtraDelaySkill = autoFarmSystem.isExtraDelaySkill() ? false : true;
                            autoFarmSystem.setExDelaySkill(isExtraDelaySkill, false);
                            successSetVarriable = true;
                        }
                        break;
                    }
                    case 1: // Опции для лучника
                    {
                        if(farmCommand.equalsIgnoreCase("farmLeaderAssist"))
                        {
                            boolean isLeaderAssist = autoFarmSystem.isLeaderAssist() ? false : true;
                            autoFarmSystem.setLeaderAssist(isLeaderAssist, false);
                            successSetVarriable = true;
                        }
                        else if(farmCommand.equalsIgnoreCase("farmAssistMonsterAttack"))
                        {
                            boolean isAssistMonsterAttack = autoFarmSystem.isAssistMonsterAttack() ? false : true;
                            autoFarmSystem.setAssistMonsterAttack(isAssistMonsterAttack, false);
                            successSetVarriable = true;
                        }
                        else if(farmCommand.equalsIgnoreCase("farmKeepLocation"))
                        {
                            boolean isKeepLocation = autoFarmSystem.isKeepLocation() ? false : true;
                            autoFarmSystem.setKeepLocation(player.getLoc(), isKeepLocation, false);
                            successSetVarriable = true;
                        }
                        else if(farmCommand.equalsIgnoreCase("farmDelaySkills"))
                        {
                            boolean isExtraDelaySkill = autoFarmSystem.isExtraDelaySkill() ? false : true;
                            autoFarmSystem.setExDelaySkill(isExtraDelaySkill, false);
                            successSetVarriable = true;
                        }
                        else if(farmCommand.equalsIgnoreCase("farmRunTargetCloseUp"))
                        {
                            boolean isRunTargetCloseUp = autoFarmSystem.isRunTargetCloseUp() ? false : true;
                            autoFarmSystem.setRunTargetCloseUp(isRunTargetCloseUp, false);
                            successSetVarriable = true;
                        }
                        break;
                    }
                    case 2: // Опции для мага
                    {
                        if(farmCommand.equalsIgnoreCase("farmLeaderAssist"))
                        {
                            boolean isLeaderAssist = autoFarmSystem.isLeaderAssist() ? false : true;
                            autoFarmSystem.setLeaderAssist(isLeaderAssist, false);
                            successSetVarriable = true;
                        }
                        else if(farmCommand.equalsIgnoreCase("farmAssistMonsterAttack"))
                        {
                            boolean isAssistMonsterAttack = autoFarmSystem.isAssistMonsterAttack() ? false : true;
                            autoFarmSystem.setAssistMonsterAttack(isAssistMonsterAttack, false);
                            successSetVarriable = true;
                        }
                        else if(farmCommand.equalsIgnoreCase("farmKeepLocation"))
                        {
                            boolean isKeepLocation = autoFarmSystem.isKeepLocation() ? false : true;
                            autoFarmSystem.setKeepLocation(player.getLoc(), isKeepLocation, false);
                            successSetVarriable = true;
                        }
                        else if(farmCommand.equalsIgnoreCase("farmRunTargetCloseUp"))
                        {
                            boolean isRunTargetCloseUp = autoFarmSystem.isRunTargetCloseUp() ? false : true;
                            autoFarmSystem.setRunTargetCloseUp(isRunTargetCloseUp, false);
                            successSetVarriable = true;
                        }
                        break;
                    }
                    case 3: // Опции для хиллера
                    {
                        if(farmCommand.equalsIgnoreCase("farmLeaderAssist"))
                        {
                            boolean isLeaderAssist = autoFarmSystem.isLeaderAssist() ? false : true;
                            autoFarmSystem.setLeaderAssist(isLeaderAssist, false);
                            successSetVarriable = true;
                        }
                        else if(farmCommand.equalsIgnoreCase("farmAssistMonsterAttack"))
                        {
                            boolean isAssistMonsterAttack = autoFarmSystem.isAssistMonsterAttack() ? false : true;
                            autoFarmSystem.setAssistMonsterAttack(isAssistMonsterAttack, false);
                            successSetVarriable = true;
                        }
                        else if(farmCommand.equalsIgnoreCase("farmTargetRestoreMp"))
                        {
                            boolean isTargetRestoreMp = autoFarmSystem.isTargetRestoreMp() ? false : true;
                            autoFarmSystem.setTargetRestoreMp(isTargetRestoreMp, false);
                            successSetVarriable = true;
                        }
                        break;
                    }
                    case 4: // Опции для суммона
                    {
                        if(farmCommand.equalsIgnoreCase("farmLeaderAssist"))
                        {
                            boolean isLeaderAssist = autoFarmSystem.isLeaderAssist() ? false : true;
                            autoFarmSystem.setLeaderAssist(isLeaderAssist, false);
                            successSetVarriable = true;
                        }
                        else if(farmCommand.equalsIgnoreCase("farmAssistMonsterAttack"))
                        {
                            boolean isAssistMonsterAttack = autoFarmSystem.isAssistMonsterAttack() ? false : true;
                            autoFarmSystem.setAssistMonsterAttack(isAssistMonsterAttack, false);
                            successSetVarriable = true;
                        }
                        else if(farmCommand.equalsIgnoreCase("farmKeepLocation"))
                        {
                            boolean isKeepLocation = autoFarmSystem.isKeepLocation() ? false : true;
                            autoFarmSystem.setKeepLocation(player.getLoc(), isKeepLocation, false);
                            successSetVarriable = true;
                        }
                        else if(farmCommand.equalsIgnoreCase("farmUseSummonSkills"))
                        {
                            if(player.getPet() == null)
                            {
                                player.sendMessage(new CustomMessage("YOU_CANT_USE_THIS_OPTION", player));
                                sendMainHtml(player, autoFarmSystem, null, farmType, spellType);
                                return true;
                            }
                            boolean isUseSummonSkills = autoFarmSystem.isUseSummonSkills() ? false : true;
                            autoFarmSystem.setUseSummonSkills(isUseSummonSkills, false);
                            successSetVarriable = true;
                        }
                        else if(farmCommand.equalsIgnoreCase("farmDelaySkills"))
                        {
                            boolean isExtraDelaySkill = autoFarmSystem.isExtraDelaySkill() ? false : true;
                            autoFarmSystem.setExDelaySkill(isExtraDelaySkill, false);
                            successSetVarriable = true;
                        }
                        break;
                    }
                }

                if(successSetVarriable)
                {
                    sendMainHtml(player, autoFarmSystem, null, farmType, spellType);
                    return true;
                }

                switch(spellType)
                {
                    case "attack": // Опции для атакующих скилов
                    {
                        if(farmCommand.equalsIgnoreCase("farmRndAttackSkills"))
                        {
                            boolean isRndAttackSkills = autoFarmSystem.isRndAttackSkills() ? false : true;
                            autoFarmSystem.setRndAttackSkills(isRndAttackSkills, false);
                        }
                        sendMainHtml(player, autoFarmSystem, null, farmType, spellType);
                        return true;
                    }
                    case "chance": // Опции для шансовых скилов
                    {
                        if(farmCommand.equalsIgnoreCase("farmRndChanceSkills"))
                        {
                            boolean isRndChanceSkills = autoFarmSystem.isRndChanceSkills() ? false : true;
                            autoFarmSystem.setRndChanceSkills(isRndChanceSkills, false);
                        }
                        sendMainHtml(player, autoFarmSystem, null, farmType, spellType);
                        return true;
                    }
                    case "self": // Опции для скилов на себя
                    {
                        if(farmCommand.equalsIgnoreCase("farmRndSelfSkills"))
                        {
                            boolean isRndSelfSkills = autoFarmSystem.isRndSelfSkills() ? false : true;
                            autoFarmSystem.setRndSelfSkills(isRndSelfSkills, false);
                        }
                        sendMainHtml(player, autoFarmSystem, null, farmType, spellType);
                        return true;
                    }
                    case "heal": // Опции для хил скилов
                    {
                        if(farmCommand.equalsIgnoreCase("farmRndLifeSkills"))
                        {
                            boolean isRndLifeSkills = autoFarmSystem.isRndLifeSkills() ? false : true;
                            autoFarmSystem.setRndLifeSkills(isRndLifeSkills, false);
                        }
                        sendMainHtml(player, autoFarmSystem, null, farmType, spellType);
                        return true;
                    }
                }
            }
            sendMainHtml(player, autoFarmSystem, null, farmType, spellType);
            return true;
        }
        else if(command.startsWith("editSummonFarmOption")) // Настройка опций сумона для фарма
        {
            String[] param = args.split(" ");
            String summonSpellType = "attack";
            String spellType = "attack";
            String farmCommand = null;
            try
            {
                summonSpellType = param[0];
            }
            catch(Exception e)
            {}

            try
            {
                farmCommand = param[1];
            }
            catch(Exception e)
            {}

            try
            {
                spellType = param[2];
            }
            catch(Exception e)
            {}

            if(summonSpellType != null && farmCommand != null)
            {
                if(farmCommand.equalsIgnoreCase("farmSummonDelaySkills")) // опции для всех типов скилов сумона
                {
                    boolean isExtraSummonDelay = autoFarmSystem.isExtraSummonDelaySkill() ? false : true;
                    autoFarmSystem.setExSummonDelaySkill(isExtraSummonDelay, false);
                    sendSummonHtml(player, autoFarmSystem, null, farmType, summonSpellType, spellType);
                    return true;
                }

                switch(summonSpellType)
                {
                    case "attack": // Опции для атакующих скилов сумона
                    {
                        if(farmCommand.equalsIgnoreCase("farmRndSummonAttackSkills"))
                        {
                            boolean isRndSummonAttackSkills = autoFarmSystem.isRndSummonAttackSkills() ? false : true;
                            autoFarmSystem.setRndSummonAttackSkills(isRndSummonAttackSkills, false);
                        }
                        sendSummonHtml(player, autoFarmSystem, null, farmType, summonSpellType, spellType);
                        return true;
                    }
                    case "self": // Опции для скилов на себя у сумона
                    {
                        if(farmCommand.equalsIgnoreCase("farmRndSummonSelfSkills"))
                        {
                            boolean isRndSummonSelfSkills = autoFarmSystem.isRndSummonSelfSkills() ? false : true;
                            autoFarmSystem.setRndSummonSelfSkills(isRndSummonSelfSkills, false);
                        }
                        sendSummonHtml(player, autoFarmSystem, null, farmType, summonSpellType, spellType);
                        return true;
                    }
                    case "heal": // Опции для хил скилов сумона
                    {
                        if(farmCommand.equalsIgnoreCase("farmRndSummonLifeSkills"))
                        {
                            boolean isRndSummonLifeSkills = autoFarmSystem.isRndSummonLifeSkills() ? false : true;
                            autoFarmSystem.setRndSummonLifeSkills(isRndSummonLifeSkills, false);
                        }
                        sendSummonHtml(player, autoFarmSystem, null, farmType, summonSpellType, spellType);
                        return true;
                    }
                }
            }

            sendSummonHtml(player, autoFarmSystem, null, farmType, summonSpellType, spellType);
            return true;
        }
        else if(command.equalsIgnoreCase("farmstop")) // Остановка фарма
        {
            autoFarmSystem.stopFarmTask(false);
            sendMainHtml(player, autoFarmSystem, null, farmType, "attack");
        }
        else if(command.equalsIgnoreCase("expendLimit")) // Лимит использований
        {
            if(autoFarmSystem.isAutofarming())
            {
                sendMainHtml(player, autoFarmSystem, null, farmType, "attack");
                return false;
            }

            if(AutoFarmManager.getInstance().isNonCheckPlayer(player.getObjectId()))
            {
                player.sendMessage(new CustomMessage("YOU_HAVE_ALREADY_USED_THIS_SERVICE", player));
                sendMainHtml(player, autoFarmSystem, null, farmType, "attack");
                return false;
            }

            if(Config.FARM_EXPEND_LIMIT_PRICE[0] != 0)
            {
                if(player.getInventory().getItemByItemId(Config.FARM_EXPEND_LIMIT_PRICE[0]) == null || player.getInventory().getItemByItemId(Config.FARM_EXPEND_LIMIT_PRICE[0]).getCount() < Config.FARM_EXPEND_LIMIT_PRICE[1])
                {
                    L2Item itemTemplate = ItemTable.getInstance().getTemplate(Config.FARM_EXPEND_LIMIT_PRICE[0]);
                    if(itemTemplate != null)
                        player.sendMessage(new CustomMessage("TO_USE_THE_SERVICE_YOU_MUST_HAVE", player).addNumber(Config.FARM_EXPEND_LIMIT_PRICE[1]).addString(itemTemplate.getName()));
                    sendMainHtml(player, autoFarmSystem, null, farmType, "attack");
                    return false;
                }

                Functions.removeItem(player, Config.FARM_EXPEND_LIMIT_PRICE[0], Config.FARM_EXPEND_LIMIT_PRICE[1]);
                Log.service("AutoFarm", player, " bought expend period " + Config.FARM_EXPEND_LIMIT_PRICE[0] + " amount " + Config.FARM_EXPEND_LIMIT_PRICE[1]);
            }

            AutoFarmManager.getInstance().addNonCheckPlayer(player.getObjectId());
            player.sendMessage(new CustomMessage("YOUR_CHARACTER_IS_EXCLUDED_FROM_LIMITS_LIST", player));
            sendMainHtml(player, autoFarmSystem, null, farmType, "attack");
        }
        else if(command.equalsIgnoreCase("buyfarm")) // Покупка фарма
        {
            if(autoFarmSystem.isActiveAutofarm())
            {
                player.sendMessage(new CustomMessage("YOU_ALREADY_HAVE_ACTIVE_AUTO_FARM_TIME", player));
                sendMainHtml(player, autoFarmSystem, null, farmType, "attack");
                return false;
            }

            sendBuyHtml(player, autoFarmSystem);
            return true;
        }
        else if(command.startsWith("tryFreeTime")) // Получить бесплатный пробный авто фарм
        {
            String[] param = args.split(" ");
            String spellType = "attack";
            try
            {
                spellType = param[0];
            }
            catch(Exception e)
            {}

            if(spellType == null || spellType.isEmpty())
                spellType = "attack";

            long farmFreeTimeVar = player.getVarLong("farmFreeTime", 0);
            if(!Config.ALLOW_FARM_FREE_TIME || farmFreeTimeVar > 0L)
            {
                player.sendMessage(new CustomMessage("FUNCTION_IS_NOT_AVAILABLE", player));
                sendBuyHtml(player, autoFarmSystem);
                return false;
            }

            long farmFreeTimeAdd = System.currentTimeMillis() + Config.FARM_FREE_TIME * 3600000L;
            if(Config.FARM_ONLINE_TYPE)
            {
                player.setVar("activeFarmOnlineTask", String.valueOf(farmFreeTimeAdd - System.currentTimeMillis()), -1);
                player.setVar("activeFarmOnlineTime", String.valueOf(0), -1);
                autoFarmSystem.refreshFarmOnlineTime();
            }
            else
            {
                player.setVar("activeFarmTask", String.valueOf(farmFreeTimeAdd), -1);
                autoFarmSystem.setAutoFarmEndTask(farmFreeTimeAdd);
            }

            player.setVar("farmFreeTime", String.valueOf(farmFreeTimeAdd), -1);
            autoFarmSystem.checkFarmTask();
            sendMainHtml(player, autoFarmSystem, null, farmType, spellType);
            player.sendMessage(new CustomMessage("YOU_HAVE_SUCCESSFULLY_ACTIVATE_AUTO_FARM_FREE_TIME_SERVICE", player));
        }
        else if(command.startsWith("buyfarmTime")) // Покупка времени фарма
        {
            String[] param = args.split(" ");
            String hourParam = null;
            String spellType = "attack";
            try
            {
                hourParam = param[0];
            }
            catch(Exception e)
            {}

            try
            {
                spellType = param[1];
            }
            catch(Exception e)
            {}

            if(hourParam != null)
            {
                int hour = Integer.parseInt(hourParam);
                boolean success = false;
                int itemId = 0;
                long itemCount = 0;
                long farmTime = System.currentTimeMillis() + hour * 3600000L;
                for(int hourPrice : Config.AUTO_FARM_PRICES.keySet())
                {
                    if(hourPrice == hour)
                    {
                        success = true;
                        String[] autoFarmPrices = Config.AUTO_FARM_PRICES.get(hourPrice).split(":");
                        if(autoFarmPrices != null && autoFarmPrices.length == 2)
                        {
                            itemId = Integer.parseInt(autoFarmPrices[0]);
                            itemCount = Long.parseLong(autoFarmPrices[1]);
                        }
                        break;
                    }
                }

                if(success)
                {
                    if(itemId != 0)
                    {
                        if(player.getInventory().getItemByItemId(itemId) == null || player.getInventory().getItemByItemId(itemId).getCount() < itemCount)
                        {
                            L2Item itemTemplate = ItemTable.getInstance().getTemplate(itemId);
                            if(itemTemplate != null)
                                player.sendMessage((new CustomMessage("TO_USE_THE_SERVICE_YOU_MUST_HAVE", player)).addNumber(itemCount).addString(itemTemplate.getName()));
                            sendBuyHtml(player, autoFarmSystem);
                            return false;
                        }
                        Functions.removeItem(player, itemId, itemCount);
                        Log.service("AutoFarm", player, "bought farm period " + itemId + " amount " + itemCount);
                    }

                    if(Config.FARM_ONLINE_TYPE)
                    {
                        player.setVar("activeFarmOnlineTask", String.valueOf(farmTime - System.currentTimeMillis()), -1);
                        player.setVar("activeFarmOnlineTime", String.valueOf(0), -1);
                        autoFarmSystem.refreshFarmOnlineTime();
                    }
                    else
                    {
                        player.setVar("activeFarmTask", String.valueOf(farmTime), -1);
                        autoFarmSystem.setAutoFarmEndTask(farmTime);
                    }

                    autoFarmSystem.checkFarmTask();
                    sendMainHtml(player, autoFarmSystem, null, farmType, spellType);
                    player.sendMessage(new CustomMessage("YOU_HAVE_SUCCESSFULLY_PURCHASED_THE_AUTO_FARM_SERVICE", player));
                }
            }

            return true;
        }
        else if(command.startsWith("autofarm")) // Автофарм
        {
            String[] param = args.split(" ");
            String spellType = "attack";
            if(param.length >= 2 && param[0].equalsIgnoreCase("edit_farm"))
            {
                try
                {
                    spellType = param[2];
                }
                catch(Exception e)
                {}
                sendMainHtml(player, autoFarmSystem, param[1], farmType, spellType);
            }
            else if(param.length >= 2 && param[0].equals("edit_farmType"))
            {
                String idFarmType = null;
                try
                {
                    idFarmType = param[1];
                }
                catch(Exception e)
                {}
                try
                {
                    spellType = param[2];
                }
                catch(Exception e)
                {}

                if(idFarmType != null)
                {
                    int idType = Integer.parseInt(idFarmType);
                    if(idType > 4)
                        idType = 4;
                    else if(idType < 0)
                        idType = 0;

                    player.setVar("farmType", String.valueOf(idType), -1);
                    autoFarmSystem.setFarmTypeValue(idType);
                    sendMainHtml(player, autoFarmSystem, null, idType, spellType);
                    return true;
                }

                sendMainHtml(player, autoFarmSystem, null, farmType, spellType);
            }
            else if(param.length >= 2)
            {
                try
                {
                    spellType = param[2];
                }
                catch(Exception e)
                {}

                if(param[0].equals("set_attackSkills"))
                    setFarmOption(player, autoFarmSystem, param, 100, 1);
                else if(param[0].equals("set_chanceSkills"))
                    setFarmOption(player, autoFarmSystem, param, 100, 1);
                else if(param[0].equals("set_selfSkills"))
                    setFarmOption(player, autoFarmSystem, param, 100, 1);
                else if(param[0].equals("set_healSkills"))
                    setFarmOption(player, autoFarmSystem, param, 100, 1);
                else if(param[0].equals("set_attackSkillsPercent"))
                    setFarmOption(player, autoFarmSystem, param, 100, 1);
                else if(param[0].equals("set_chanceSkillsPercent"))
                    setFarmOption(player, autoFarmSystem, param, 100, 1);
                else if(param[0].equals("set_selfSkillsPercent"))
                    setFarmOption(player, autoFarmSystem, param, 100, 1);
                else if(param[0].equals("set_healSkillsPercent"))
                    setFarmOption(player, autoFarmSystem, param, 100, 1);
                else if(param[0].equals("set_distance"))
                    setFarmOption(player, autoFarmSystem, param, Config.SEARCH_DISTANCE, 1);
                else if(param[0].equals("set_shortcutPage"))
                    setFarmOption(player, autoFarmSystem, param, Config.SHORTCUT_PAGE, 1);

                sendMainHtml(player, autoFarmSystem, null, farmType, spellType);
            }
            else
            {
                try
                {
                    spellType = param[2];
                }
                catch(Exception e)
                {}

                sendMainHtml(player, autoFarmSystem, null, farmType, spellType);
            }
            return true;
        }
        else if(command.startsWith("autosummonfarm")) // Настройка автофарма сумону
        {
            String[] param = args.split(" ");
            String summonSpellType = "attack";
            if(param.length >= 2 && param[0].equalsIgnoreCase("edit_farm"))
            {
                try
                {
                    summonSpellType = param[2];
                }
                catch(Exception e)
                {}

                sendSummonHtml(player, autoFarmSystem, param[1], farmType, summonSpellType, "attack");
            }
            else if(param.length >= 2)
            {
                try
                {
                    summonSpellType = param[2];
                }
                catch(Exception e)
                {}

                if(param[0].equals("set_attackSummonSkills"))
                    setFarmOption(player, autoFarmSystem, param, 100, 1);
                else if(param[0].equals("set_selfSummonSkills"))
                    setFarmOption(player, autoFarmSystem, param, 100, 1);
                else if(param[0].equals("set_healSummonSkills"))
                    setFarmOption(player, autoFarmSystem, param, 100, 1);
                else if(param[0].equals("set_attackSummonSkillsPercent"))
                    setFarmOption(player, autoFarmSystem, param, 100, 1);
                else if(param[0].equals("set_selfSummonSkillsPercent"))
                    setFarmOption(player, autoFarmSystem, param, 100, 1);
                else if(param[0].equals("set_healSummonSkillsPercent"))
                    setFarmOption(player, autoFarmSystem, param, 100, 1);

                sendSummonHtml(player, autoFarmSystem, null, farmType, summonSpellType, "attack");
            }
            else
            {
                try
                {
                    summonSpellType = param[2];
                }
                catch(Exception e)
                {}

                sendSummonHtml(player, autoFarmSystem, null, farmType, summonSpellType, "attack");
            }
            return true;
        }
        return true;
    }

    private void setFarmOption(L2Player player, AutoFarmContext farmSystem, String[] param, int maxValue, int minValue)
    {
        String str = null;
        try
        {
            str = param[1];
        }
        catch(Exception e)
        {}
        if(str != null)
        {
            int value = 0;
            try
            {
                value = Integer.parseInt(str);
                if(value > maxValue)
                    value = maxValue;
                if(value < minValue)
                    value = minValue;
            }
            catch(NumberFormatException e)
            {
                if(param[0].equals("set_attackSkills"))
                    value = player.getVarInt("attackChanceSkills", Config.ATTACK_SKILL_CHANCE);
                else if(param[0].equals("set_chanceSkills"))
                    value = player.getVarInt("chanceChanceSkills", Config.CHANCE_SKILL_CHANCE);
                else if(param[0].equals("set_selfSkills"))
                    value = player.getVarInt("selfChanceSkills", Config.SELF_SKILL_CHANCE);
                else if(param[0].equals("set_healSkills"))
                    value = player.getVarInt("healChanceSkills", Config.HEAL_SKILL_CHANCE);
                else if(param[0].equals("set_attackSkillsPercent"))
                    value = player.getVarInt("attackSkillsPercent", Config.ATTACK_SKILL_PERCENT);
                else if(param[0].equals("set_chanceSkillsPercent"))
                    value = player.getVarInt("chanceSkillsPercent", Config.CHANCE_SKILL_PERCENT);
                else if(param[0].equals("set_selfSkillsPercent"))
                    value = player.getVarInt("selfSkillsPercent", Config.SELF_SKILL_PERCENT);
                else if(param[0].equals("set_healSkillsPercent"))
                    value = player.getVarInt("healSkillsPercent", Config.HEAL_SKILL_PERCENT);
                else if(param[0].equals("set_distance"))
                    value = player.getVarInt("farmDistance", Config.SEARCH_DISTANCE);
                else if(param[0].equals("set_shortcutPage"))
                    value = player.getVarInt("shortcutPage", Config.SHORTCUT_PAGE);
                else if(param[0].equals("set_attackSummonSkills"))
                    value = player.getVarInt("attackSummonChanceSkills", Config.SUMMON_ATTACK_SKILL_CHANCE);
                else if(param[0].equals("set_selfSummonSkills"))
                    value = player.getVarInt("selfSummonChanceSkills", Config.SUMMON_SELF_SKILL_CHANCE);
                else if(param[0].equals("set_healSummonSkills"))
                    value = player.getVarInt("healSummonChanceSkills", Config.SUMMON_HEAL_SKILL_CHANCE);
                else if(param[0].equals("set_attackSummonSkillsPercent"))
                    value = player.getVarInt("attackSummonSkillsPercent", Config.SUMMON_ATTACK_SKILL_PERCENT);
                else if(param[0].equals("set_selfSummonSkillsPercent"))
                    value = player.getVarInt("selfSummonSkillsPercent", Config.SUMMON_SELF_SKILL_PERCENT);
                else if(param[0].equals("set_healSummonSkillsPercent"))
                    value = player.getVarInt("healSummonSkillsPercent", Config.SUMMON_HEAL_SKILL_PERCENT);
            }

            if(param[0].equals("set_attackSkills"))
            {
                player.setVar("attackChanceSkills", String.valueOf(value), -1);
                farmSystem.setAttackSkillValue(false, value);
            }
            else if(param[0].equals("set_chanceSkills"))
            {
                player.setVar("chanceChanceSkills", String.valueOf(value), -1);
                farmSystem.setChanceSkillValue(false, value);
            }
            else if(param[0].equals("set_selfSkills"))
            {
                player.setVar("selfChanceSkills", String.valueOf(value), -1);
                farmSystem.setSelfSkillValue(false, value);
            }
            else if(param[0].equals("set_healSkills"))
            {
                player.setVar("healChanceSkills", String.valueOf(value), -1);
                farmSystem.setLifeSkillValue(false, value);
            }
            else if(param[0].equals("set_attackSkillsPercent"))
            {
                player.setVar("attackSkillsPercent", String.valueOf(value), -1);
                farmSystem.setAttackSkillValue(true, value);
            }
            else if(param[0].equals("set_chanceSkillsPercent"))
            {
                player.setVar("chanceSkillsPercent", String.valueOf(value), -1);
                farmSystem.setChanceSkillValue(true, value);
            }
            else if(param[0].equals("set_selfSkillsPercent"))
            {
                player.setVar("selfSkillsPercent", String.valueOf(value), -1);
                farmSystem.setSelfSkillValue(true, value);
            }
            else if(param[0].equals("set_healSkillsPercent"))
            {
                player.setVar("healSkillsPercent", String.valueOf(value), -1);
                farmSystem.setLifeSkillValue(true, value);
            }
            else if(param[0].equals("set_distance"))
            {
                player.setVar("farmDistance", String.valueOf(value), -1);
                farmSystem.setRadiusValue(value);
            }
            else if(param[0].equals("set_shortcutPage"))
            {
                player.setVar("shortcutPage", String.valueOf(value), -1);
                farmSystem.setShortcutPageValue(value);
            }
            else if(param[0].equals("set_attackSummonSkills"))
            {
                player.setVar("attackSummonChanceSkills", String.valueOf(value), -1);
                farmSystem.setSummonAttackSkillValue(false, value);
            }
            else if(param[0].equals("set_selfSummonSkills"))
            {
                player.setVar("selfSummonChanceSkills", String.valueOf(value), -1);
                farmSystem.setSummonSelfSkillValue(false, value);
            }
            else if(param[0].equals("set_healSummonSkills"))
            {
                player.setVar("healSummonChanceSkills", String.valueOf(value), -1);
                farmSystem.setSummonLifeSkillValue(false, value);
            }
            else if(param[0].equals("set_attackSummonSkillsPercent"))
            {
                player.setVar("attackSummonSkillsPercent", String.valueOf(value), -1);
                farmSystem.setSummonAttackSkillValue(true, value);
            }
            else if(param[0].equals("set_selfSummonSkillsPercent"))
            {
                player.setVar("selfSummonSkillsPercent", String.valueOf(value), -1);
                farmSystem.setSummonSelfSkillValue(true, value);
            }
            else if(param[0].equals("set_healSummonSkillsPercent"))
            {
                player.setVar("healSummonSkillsPercent", String.valueOf(value), -1);
                farmSystem.setSummonLifeSkillValue(true, value);
            }
        }
    }

    private void sendMainHtml(L2Player player, AutoFarmContext farmSystem, String editCommand, int farmType, String spellType)
    {
        String html = null;
        switch(farmType)
        {
            case 0:
            {
                html = HtmCache.getInstance().getNotNull("command/autofarm/index-fighter.htm", player);
                if(farmSystem.isLeaderAssist())
                    html = html.replace("%assist_img%", new CustomMessage("services.autofarm.checkbox.checked", player).toString());
                else
                    html = html.replace("%assist_img%", new CustomMessage("services.autofarm.checkbox", player).toString());

                html = html.replace("%assist_bypass%", "bypass -h user_editFarmOption " + spellType + " farmLeaderAssist");

                if(farmSystem.isAssistMonsterAttack())
                    html = html.replace("%assistMAttack_img%", new CustomMessage("services.autofarm.checkbox.checked", player).toString());
                else
                    html = html.replace("%assistMAttack_img%", new CustomMessage("services.autofarm.checkbox", player).toString());
                html = html.replace("%assistMAttack_bypass%", "bypass -h user_editFarmOption " + spellType + " farmAssistMonsterAttack");

                if(farmSystem.isKeepLocation())
                    html = html.replace("%keepLoc_img%", new CustomMessage("services.autofarm.checkbox.checked", player).toString());
                else
                    html = html.replace("%keepLoc_img%", new CustomMessage("services.autofarm.checkbox", player).toString());

                html = html.replace("%keepLoc_bypass%", "bypass -h user_editFarmOption " + spellType + " farmKeepLocation");

                if(farmSystem.isExtraDelaySkill())
                    html = html.replace("%delaySk_img%", new CustomMessage("services.autofarm.checkbox.checked", player).toString());
                else
                    html = html.replace("%delaySk_img%", new CustomMessage("services.autofarm.checkbox", player).toString());

                html = html.replace("%delaySk_bypass%", "bypass -h user_editFarmOption " + spellType + " farmDelaySkills");
                break;
            }
            case 1:
            {
                html = HtmCache.getInstance().getNotNull("command/autofarm/index-archer.htm", player);
                if(farmSystem.isLeaderAssist())
                    html = html.replace("%assist_img%", new CustomMessage("services.autofarm.checkbox.checked", player).toString());
                else
                    html = html.replace("%assist_img%", new CustomMessage("services.autofarm.checkbox", player).toString());

                html = html.replace("%assist_bypass%", "bypass -h user_editFarmOption " + spellType + " farmLeaderAssist");

                if(farmSystem.isAssistMonsterAttack())
                    html = html.replace("%assistMAttack_img%", new CustomMessage("services.autofarm.checkbox.checked", player).toString());
                else
                    html = html.replace("%assistMAttack_img%", new CustomMessage("services.autofarm.checkbox", player).toString());
                html = html.replace("%assistMAttack_bypass%", "bypass -h user_editFarmOption " + spellType + " farmAssistMonsterAttack");

                if(farmSystem.isKeepLocation())
                    html = html.replace("%keepLoc_img%", new CustomMessage("services.autofarm.checkbox.checked", player).toString());
                else
                    html = html.replace("%keepLoc_img%", new CustomMessage("services.autofarm.checkbox", player).toString());

                html = html.replace("%keepLoc_bypass%", "bypass -h user_editFarmOption " + spellType + " farmKeepLocation");

                if(farmSystem.isExtraDelaySkill())
                    html = html.replace("%delaySk_img%", new CustomMessage("services.autofarm.checkbox.checked", player).toString());
                else
                    html = html.replace("%delaySk_img%", new CustomMessage("services.autofarm.checkbox", player).toString());

                html = html.replace("%delaySk_bypass%", "bypass -h user_editFarmOption " + spellType + " farmDelaySkills");
                if(farmSystem.isRunTargetCloseUp())
                    html = html.replace("%runCloseUp_img%", new CustomMessage("services.autofarm.checkbox.checked", player).toString());
                else
                    html = html.replace("%runCloseUp_img%", new CustomMessage("services.autofarm.checkbox", player).toString());

                html = html.replace("%runCloseUp_bypass%", "bypass -h user_editFarmOption " + spellType + " farmRunTargetCloseUp");
                break;
            }
            case 2:
            {
                html = HtmCache.getInstance().getNotNull("command/autofarm/index-mage.htm", player);
                if(farmSystem.isLeaderAssist())
                    html = html.replace("%assist_img%", new CustomMessage("services.autofarm.checkbox.checked", player).toString());
                else
                    html = html.replace("%assist_img%", new CustomMessage("services.autofarm.checkbox", player).toString());

                html = html.replace("%assist_bypass%", "bypass -h user_editFarmOption " + spellType + " farmLeaderAssist");

                if(farmSystem.isAssistMonsterAttack())
                    html = html.replace("%assistMAttack_img%", new CustomMessage("services.autofarm.checkbox.checked", player).toString());
                else
                    html = html.replace("%assistMAttack_img%", new CustomMessage("services.autofarm.checkbox", player).toString());
                html = html.replace("%assistMAttack_bypass%", "bypass -h user_editFarmOption " + spellType + " farmAssistMonsterAttack");

                if(farmSystem.isKeepLocation())
                    html = html.replace("%keepLoc_img%", new CustomMessage("services.autofarm.checkbox.checked", player).toString());
                else
                    html = html.replace("%keepLoc_img%", new CustomMessage("services.autofarm.checkbox", player).toString());

                html = html.replace("%keepLoc_bypass%", "bypass -h user_editFarmOption " + spellType + " farmKeepLocation");

                if(farmSystem.isRunTargetCloseUp())
                    html = html.replace("%runCloseUp_img%", new CustomMessage("services.autofarm.checkbox.checked", player).toString());
                else
                    html = html.replace("%runCloseUp_img%", new CustomMessage("services.autofarm.checkbox", player).toString());

                html = html.replace("%runCloseUp_bypass%", "bypass -h user_editFarmOption " + spellType + " farmRunTargetCloseUp");
                break;
            }
            case 3:
            {
                html = HtmCache.getInstance().getNotNull("command/autofarm/index-heal.htm", player);
                if(farmSystem.isLeaderAssist())
                    html = html.replace("%assist_img%", new CustomMessage("services.autofarm.checkbox.checked", player).toString());
                else
                    html = html.replace("%assist_img%", new CustomMessage("services.autofarm.checkbox", player).toString());

                html = html.replace("%assist_bypass%", "bypass -h user_editFarmOption " + spellType + " farmLeaderAssist");

                if(farmSystem.isAssistMonsterAttack())
                    html = html.replace("%assistMAttack_img%", new CustomMessage("services.autofarm.checkbox.checked", player).toString());
                else
                    html = html.replace("%assistMAttack_img%", new CustomMessage("services.autofarm.checkbox", player).toString());

                html = html.replace("%assistMAttack_bypass%", "bypass -h user_editFarmOption " + spellType + " farmAssistMonsterAttack");

                if(farmSystem.isTargetRestoreMp())
                    html = html.replace("%tgRestoreMp_img%", new CustomMessage("services.autofarm.checkbox.checked", player).toString());
                else
                    html = html.replace("%tgRestoreMp_img%", new CustomMessage("services.autofarm.checkbox", player).toString());

                html = html.replace("%tgRestoreMp_bypass%", "bypass -h user_editFarmOption " + spellType + " farmTargetRestoreMp");
                break;
            }
            case 4:
            {
                html = HtmCache.getInstance().getNotNull("command/autofarm/index-summon.htm", player);

                if(farmSystem.isLeaderAssist())
                    html = html.replace("%assist_img%", new CustomMessage("services.autofarm.checkbox.checked", player).toString());
                else
                    html = html.replace("%assist_img%", new CustomMessage("services.autofarm.checkbox", player).toString());

                html = html.replace("%assist_bypass%", "bypass -h user_editFarmOption " + spellType + " farmLeaderAssist");

                if(farmSystem.isAssistMonsterAttack())
                    html = html.replace("%assistMAttack_img%", new CustomMessage("services.autofarm.checkbox.checked", player).toString());
                else
                    html = html.replace("%assistMAttack_img%", new CustomMessage("services.autofarm.checkbox", player).toString());
                html = html.replace("%assistMAttack_bypass%", "bypass -h user_editFarmOption " + spellType + " farmAssistMonsterAttack");

                if(farmSystem.isKeepLocation())
                    html = html.replace("%keepLoc_img%", new CustomMessage("services.autofarm.checkbox.checked", player).toString());
                else
                    html = html.replace("%keepLoc_img%", new CustomMessage("services.autofarm.checkbox", player).toString());

                html = html.replace("%keepLoc_bypass%", "bypass -h user_editFarmOption " + spellType + " farmKeepLocation");

                if(farmSystem.isUseSummonSkills())
                    html = html.replace("%useSummonSk_img%", new CustomMessage("services.autofarm.checkbox.checked", player).toString());
                else
                    html = html.replace("%useSummonSk_img%", new CustomMessage("services.autofarm.checkbox", player).toString());

                html = html.replace("%useSummonSk_bypass%", "bypass -h user_editFarmOption " + spellType + " farmUseSummonSkills");

                if(farmSystem.isExtraDelaySkill())
                    html = html.replace("%delaySk_img%", new CustomMessage("services.autofarm.checkbox.checked", player).toString());
                else
                    html = html.replace("%delaySk_img%", new CustomMessage("services.autofarm.checkbox", player).toString());

                html = html.replace("%delaySk_bypass%", "bypass -h user_editFarmOption " + spellType + " farmDelaySkills");
                break;
            }
        }

        String chanceAttack = "";
        String percentAttack = "";
        String chanceChance = "";
        String percentChance = "";
        String chanceSelf = "";
        String percentSelf = "";
        String chanceLowHeal = "";
        String percentLowHeal = "";
        List<Integer> skillIds = null;
        String skillsHtml = null;
        switch(spellType)
        {
            case "attack":
                skillIds = farmSystem.getAttackSpells();
                skillsHtml = HtmCache.getInstance().getNotNull("command/autofarm/index-skill_attack.htm", player);
                chanceAttack = getShortCutPage(player, (editCommand != null && editCommand.equals("editAttackSkills")) ? editCommand : null, "editAttackSkills", "attackChanceSkills", Config.ATTACK_SKILL_CHANCE, spellType);
                percentAttack = getShortCutPage(player, (editCommand != null && editCommand.equals("editAttackPercent")) ? editCommand : null, "editAttackPercent", "attackSkillsPercent", Config.ATTACK_SKILL_PERCENT, spellType);
                if(farmSystem.isRndAttackSkills())
                    skillsHtml = skillsHtml.replace("%rndAttackSk_img%", new CustomMessage("services.autofarm.checkbox.checked", player).toString());
                else
                    skillsHtml = skillsHtml.replace("%rndAttackSk_img%", new CustomMessage("services.autofarm.checkbox", player).toString());
                skillsHtml = skillsHtml.replace("%rndAttackSk_bypass%", "bypass -h user_editFarmOption " + spellType + " farmRndAttackSkills");
                break;
            case "chance":
                skillIds = farmSystem.getChanceSpells();
                skillsHtml = HtmCache.getInstance().getNotNull("command/autofarm/index-skill_chance.htm", player);
                chanceChance = getShortCutPage(player, (editCommand != null && editCommand.equals("editChanceSkills")) ? editCommand : null, "editChanceSkills", "chanceChanceSkills", Config.CHANCE_SKILL_CHANCE, spellType);
                percentChance = getShortCutPage(player, (editCommand != null && editCommand.equals("editChancePercent")) ? editCommand : null, "editChancePercent", "chanceSkillsPercent", Config.CHANCE_SKILL_PERCENT, spellType);
                if(farmSystem.isRndChanceSkills())
                    skillsHtml = skillsHtml.replace("%rndChanceSk_img%", new CustomMessage("services.autofarm.checkbox.checked", player).toString());
                else
                    skillsHtml = skillsHtml.replace("%rndChanceSk_img%", new CustomMessage("services.autofarm.checkbox", player).toString());
                skillsHtml = skillsHtml.replace("%rndChanceSk_bypass%", "bypass -h user_editFarmOption " + spellType + " farmRndChanceSkills");
                break;
            case "self":
                skillIds = farmSystem.getSelfSpells();
                skillsHtml = HtmCache.getInstance().getNotNull("command/autofarm/index-skill_self.htm", player);
                chanceSelf = getShortCutPage(player, (editCommand != null && editCommand.equals("editSelfSkills")) ? editCommand : null, "editSelfSkills", "selfChanceSkills", Config.SELF_SKILL_CHANCE, spellType);
                percentSelf = getShortCutPage(player, (editCommand != null && editCommand.equals("editSelfPercent")) ? editCommand : null, "editSelfPercent", "selfSkillsPercent", Config.SELF_SKILL_PERCENT, spellType);
                if(farmSystem.isRndSelfSkills())
                    skillsHtml = skillsHtml.replace("%rndSelfSk_img%", new CustomMessage("services.autofarm.checkbox.checked", player).toString());
                else
                    skillsHtml = skillsHtml.replace("%rndSelfSk_img%", new CustomMessage("services.autofarm.checkbox", player).toString());

                skillsHtml = skillsHtml.replace("%rndSelfSk_bypass%", "bypass -h user_editFarmOption " + spellType + " farmRndSelfSkills");
                break;
            case "heal":
                skillIds = farmSystem.getLowLifeSpells();
                skillsHtml = HtmCache.getInstance().getNotNull("command/autofarm/index-skill_heal.htm", player);
                chanceLowHeal = getShortCutPage(player, (editCommand != null && editCommand.equals("editHealSkills")) ? editCommand : null, "editHealSkills", "healChanceSkills", Config.HEAL_SKILL_CHANCE, spellType);
                percentLowHeal = getShortCutPage(player, (editCommand != null && editCommand.equals("editLowHealPercent")) ? editCommand : null, "editLowHealPercent", "healSkillsPercent", Config.HEAL_SKILL_PERCENT, spellType);
                if(farmSystem.isRndLifeSkills())
                    skillsHtml = skillsHtml.replace("%rndLifeSk_img%", new CustomMessage("services.autofarm.checkbox.checked", player).toString());
                else
                    skillsHtml = skillsHtml.replace("%rndLifeSk_img%", new CustomMessage("services.autofarm.checkbox", player).toString());

                skillsHtml = skillsHtml.replace("%rndLifeSk_bypass%", "bypass -h user_editFarmOption " + spellType + " farmRndLifeSkills");
                break;
        }

        String skillList = "";
        if(skillIds != null)
        {
            List<Integer> arrayList = new ArrayList<Integer>();

            for(final int id : skillIds)
            {
                if(id > 0)
                {
                    L2Skill skill = player.getKnownSkill(id);
                    if(skill == null)
                        arrayList.add(id);
                }
            }

            if(!arrayList.isEmpty())
            {
                for(final int id : arrayList)
                {
                    skillIds.remove((Object) id);
                }
                arrayList.clear();
            }
            String skillTemplateText = HtmCache.getInstance().getNotNull("command/autofarm/skill-template.htm", player);
            for(int i = 0; i < 8; i++)
            {
                String text = skillTemplateText;
                if(skillIds.size() > 0 && i < skillIds.size())
                {
                    int skillId = skillIds.get(i);
                    if(skillId > 0)
                    {
                        L2Skill skill = player.getKnownSkill(skillId);
                        if(skill != null)
                        {
                            text = text.replace("%icon%", skill.getIcon());
                            text = text.replace("%background%", "");
                            text = text.replace("%bypass%", "bypass -h user_removeSkill " + spellType + " " + skill.getId() + "");
                        }
                        else
                        {
                            text = text.replace("%icon%", new CustomMessage("services.autofarm.empty.icon", player).toString());
                            text = text.replace("%bypass%", "bypass -h user_addSkill " + spellType + "");
                        }
                    }
                    else
                    {
                        text = text.replace("%icon%", new CustomMessage("services.autofarm.empty.icon", player).toString());
                        text = text.replace("%bypass%", "bypass -h user_addSkill " + spellType + "");
                    }
                }
                else
                {
                    text = text.replace("%icon%", new CustomMessage("services.autofarm.empty.icon", player).toString());
                    text = text.replace("%bypass%", "bypass -h user_addSkill " + spellType + "");
                }
                skillList += text;
            }
        }

        String distance = getFarmDistance(player, (editCommand != null && editCommand.equals("editDistance")) ? editCommand : null, "editDistance", "farmDistance", Config.SEARCH_DISTANCE, spellType);
        String shortcutPage = getShortCutPage(player, (editCommand != null && editCommand.equals("editShortcutPage")) ? editCommand : null, "editShortcutPage", "shortcutPage", Config.SHORTCUT_PAGE, spellType);

        html = html.replace("%activate%", getActivate(player, farmSystem));
        html = html.replace("%skillType%", spellType);
        html = html.replace("%skillList%", skillList);
        html = html.replace("%skillsParam%", skillsHtml);
        html = html.replace("%activeHwids%", getActivateHwids(player));
        html = html.replace("%refreshSkills%", "bypass -h user_refreshSkills " + spellType + "");
        html = html.replace("%status%", farmSystem.isAutofarming() ? new CustomMessage("services.autofarm.on", player).toString() : new CustomMessage("services.autofarm.off", player).toString());
        html = html.replace("%button%", farmSystem.isAutofarming() ? new CustomMessage("services.autofarm.on_button", player).toString() : new CustomMessage("services.autofarm.off_button", player).toString());
        html = html.replace("%chanceAttack%", chanceAttack.equals("") ? "" : chanceAttack);
        html = html.replace("%chanceChance%", chanceChance.equals("") ? "" : chanceChance);
        html = html.replace("%chanceSelf%", chanceSelf.equals("") ? "" : chanceSelf);
        html = html.replace("%chanceLowHeal%", chanceLowHeal.equals("") ? "" : chanceLowHeal);
        html = html.replace("%percentAttack%", percentAttack.equals("") ? "" : percentAttack);
        html = html.replace("%percentChance%", percentChance.equals("") ? "" : percentChance);
        html = html.replace("%percentSelf%", percentSelf.equals("") ? "" : percentSelf);
        html = html.replace("%percentLowHeal%", percentLowHeal.equals("") ? "" : percentLowHeal);
        html = html.replace("%distance%", distance.equals("") ? "" : distance);
        html = html.replace("%shortcutPage%", shortcutPage.equals("") ? "" : shortcutPage);
        html = html.replace("%farmType%", getFarmTypeText(player, farmType, spellType));
        Functions.show(html, player, null);
    }

    public String getActivateHwids(L2Player player)
    {
        if(Config.FARM_ACTIVE_LIMITS < 0)
            return "<font color=\"LEVEL\">-<font>";
        int activeFarms = AutoFarmManager.getInstance().getActiveFarms(Config.ALLOW_CHECK_HWID_LIMIT ? player.getHWID() : player.getIP());
        if(activeFarms > 0)
            return "<font color=\"LEVEL\">" + activeFarms + "<font>";
        else if(activeFarms <= 0 && AutoFarmManager.getInstance().isNonCheckPlayer(player.getObjectId()))
            return "<font color=\"00FF00\">" + activeFarms + "<font>";
        else
            return "<a action=\"bypass -h user_expendLimit\"><font color=\"FF0000\">" + activeFarms + "<font></a>";
    }

    private static String getActivate(L2Player player, AutoFarmContext farmSystem)
    {
        String autoFarmNoLimitTime = new CustomMessage("AUTO_FARM_NO_LIMIT_TIME", player).toString();
        String autoFarmBuyTime = new CustomMessage("AUTO_FARM_BUY_TIME", player).toString();
        if(Config.AUTO_FARM_FREE || (Config.PREMIUM_FARM_FREE && player.isPremium()) || (Config.SERVICES_OFFLINE_AUTOFARM_ALLOW && player.isInOfflineAutoFarm()))
        {
            return autoFarmNoLimitTime;
        }
        if(!farmSystem.isActiveAutofarm())
        {
            return autoFarmBuyTime;
        }
        if(Config.FARM_ONLINE_TYPE)
        {
            long autoFarmOnlineTime;
            if(!farmSystem.isActiveFarmTask())
            {
                autoFarmOnlineTime = (player.getVarLong("activeFarmOnlineTask", 0L) - farmSystem.getLastFarmOnlineTime()) / 1000L;
            }
            else
            {
                autoFarmOnlineTime = (player.getVarLong("activeFarmOnlineTask", 0L) - (farmSystem.getLastFarmOnlineTime() + (System.currentTimeMillis() - farmSystem.getFarmOnlineTime()))) / 1000L;
            }
            return "<font color=\"E6D0AE\">" + Util.formatTime((int) autoFarmOnlineTime, false) + "</font>";
        }
        return "<font color=\"E6D0AE\">" + Util.formatTime((int) ((farmSystem.getAutoFarmEnd() - System.currentTimeMillis()) / 1000L), false) + "</font>";
    }

    private void sendBuyHtml(L2Player player, AutoFarmContext farmSystem)
    {
        String html = HtmCache.getInstance().getNotNull("command/autofarm/buy.htm", player);
        String time = "";
        List<Integer> hoursList = new ArrayList<Integer>();
        for(int id : Config.AUTO_FARM_PRICES.keySet())
        {
            hoursList.add(id);
        }

        SortTimeInfo sortTimeInfo = new SortTimeInfo();
        Collections.sort(hoursList, sortTimeInfo);

        int i = 0;
        for(final int id : hoursList)
        {
            if(i > 0)
                time += ";";
            time += "" + id + "";
            i++;
        }
        html = html.replace("%time%", time);
        html = html.replace("%freeUse%", getFreeUseButton(player, farmSystem));
        hoursList.clear();
        Functions.show(html, player, null);
    }

    private String getFreeUseButton(L2Player player, AutoFarmContext farmSystem)
    {
        if(farmSystem.isActiveAutofarm() || !Config.ALLOW_FARM_FREE_TIME)
            return "";
        long farm = player.getVarLong("farmFreeTime", 0);
        if(farm <= 0)
            return new CustomMessage("services.autofarm.try_free", player, Config.FARM_FREE_TIME).toString();
        else
            return "";
    }

    private String getFarmTypeText(L2Player player, int farmType, String spellType)
    {
        String text = "<td aling=center width=20>";
        int idFarmType = 0;
        String typeName = "";
        switch(farmType)
        {
            case 0:
                text += new CustomMessage("services.autofarm.fightericon", player).toString();
                typeName = "Fighter";
                idFarmType++;
                break;
            case 1:
                text += new CustomMessage("services.autofarm.archericon", player).toString();
                typeName = "Archer";
                idFarmType = 2;
                break;
            case 2:
                text += new CustomMessage("services.autofarm.magicicon", player).toString();
                typeName = "Magic";
                idFarmType = 3;
                break;
            case 3:
                text += new CustomMessage("services.autofarm.healicon", player).toString();
                typeName = "Healer";
                idFarmType = 4;
                break;
            case 4:
                text += new CustomMessage("services.autofarm.summonicon", player).toString();
                typeName = "Summon";
                break;
        }
        return text + "<td width=90>" + typeName + "</td><td width=60><button width=40 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\" action=\"bypass -h user_autofarm edit_farmType " + idFarmType + " " + spellType + "\" value=\"Switch\"></td>";
    }

    private static String getFarmDistance(L2Player player, String secondCommand, String editCommand, String nameFarmDistanceVar, int searchDistance, String spellType)
    {
        String text = "";
        if(secondCommand != null && !secondCommand.isEmpty())
        {
            if(secondCommand.equals("editDistance"))
            {
                text += "<td width=50><edit var=\"" + secondCommand + "\" width=40 height=12></td>";
                text += "<td width=40><button width=40 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\" action=\"bypass -h user_autofarm set_distance $editDistance " + spellType + "\" value=\"Save\"></td>";
            }
        }
        else
        {
            text += "<td aling=center width=50><font color=c1b33a>" + player.getVarInt(nameFarmDistanceVar, searchDistance) + "</font></td>";
            text += "<td width=40><button width=40 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\" action=\"bypass -h user_autofarm edit_farm " + editCommand + " " + spellType + "\" value=\"Edit\"></td>";
        }
        return text;
    }

    private static String getShortCutPage(L2Player player, String secondCommand, String editCommand, String nameShortCutPageVar, int value, String spellType)
    {
        String text = "";
        if(secondCommand != null && !secondCommand.isEmpty())
        {
            if(secondCommand.equals("editAttackSkills"))
            {
                text += "<td width=43><edit var=\"" + secondCommand + "\" width=34 height=12></td>";
                text += "<td width=40><button width=40 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\" action=\"bypass -h user_autofarm set_attackSkills $editAttackSkills " + spellType + "\" value=\"Save\"></td>";
            }
            else if(secondCommand.equals("editChanceSkills"))
            {
                text += "<td width=43><edit var=\"" + secondCommand + "\" width=34 height=12></td>";
                text += "<td width=40><button width=40 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\" action=\"bypass -h user_autofarm set_chanceSkills $editChanceSkills " + spellType + "\" value=\"Save\"></td>";
            }
            else if(secondCommand.equals("editSelfSkills"))
            {
                text += "<td width=43><edit var=\"" + secondCommand + "\" width=34 height=12></td>";
                text += "<td width=40><button width=40 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\" action=\"bypass -h user_autofarm set_selfSkills $editSelfSkills " + spellType + "\" value=\"Save\"></td>";
            }
            else if(secondCommand.equals("editHealSkills"))
            {
                text += "<td width=43><edit var=\"" + secondCommand + "\" width=34 height=12></td>";
                text += "<td width=40><button width=40 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\" action=\"bypass -h user_autofarm set_healSkills $editHealSkills " + spellType + "\" value=\"Save\"></td>";
            }
            else if(secondCommand.equals("editAttackPercent"))
            {
                text += "<td width=43><edit var=\"" + secondCommand + "\" width=34 height=12></td>";
                text += "<td width=40><button width=40 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\" action=\"bypass -h user_autofarm set_attackSkillsPercent $editAttackPercent " + spellType + "\" value=\"Save\"></td>";
            }
            else if(secondCommand.equals("editChancePercent"))
            {
                text += "<td width=43><edit var=\"" + secondCommand + "\" width=34 height=12></td>";
                text += "<td width=40><button width=40 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\" action=\"bypass -h user_autofarm set_chanceSkillsPercent $editChancePercent " + spellType + "\" value=\"Save\"></td>";
            }
            else if(secondCommand.equals("editSelfPercent"))
            {
                text += "<td width=43><edit var=\"" + secondCommand + "\" width=34 height=12></td>";
                text += "<td width=40><button width=40 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\" action=\"bypass -h user_autofarm set_selfSkillsPercent $editSelfPercent " + spellType + "\" value=\"Save\"></td>";
            }
            else if(secondCommand.equals("editLowHealPercent"))
            {
                text += "<td width=43><edit var=\"" + secondCommand + "\" width=34 height=12></td>";
                text += "<td width=40><button width=40 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\" action=\"bypass -h user_autofarm set_healSkillsPercent $editLowHealPercent " + spellType + "\" value=\"Save\"></td>";
            }
            else if(secondCommand.equals("editShortcutPage"))
            {
                text += "<td width=43><edit var=\"" + secondCommand + "\" width=34 height=12></td>";
                text += "<td width=40><button width=40 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\" action=\"bypass -h user_autofarm set_shortcutPage $editShortcutPage " + spellType + "\" value=\"Save\"></td>";
            }
        }
        else
        {
            if(nameShortCutPageVar.equals("shortcutPage"))
            {
                text += "<td aling=center width=45><font color=c1b33a>" + player.getVarInt(nameShortCutPageVar, value) + "</font></td>";
            }
            else
            {
                text += "<td aling=center width=45><font color=c1b33a>" + player.getVarInt(nameShortCutPageVar, value) + "%</font></td>";
            }
            text += "<td width=40><button width=40 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\" action=\"bypass -h user_autofarm edit_farm " + editCommand + " " + spellType + "\" value=\"Edit\"></td>";
        }
        return text;
    }

    private static String getSummonEditTab(L2Player player, String secondCommand, String editCommand, String varName, int value, String summonSpellType)
    {
        String text = "";
        if(secondCommand != null && !secondCommand.isEmpty())
        {
            if(secondCommand.equals("editSummonAttackSkills"))
            {
                text += "<td width=43><edit var=\"" + secondCommand + "\" width=34 height=12></td>";
                text += "<td width=40><button width=40 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\" action=\"bypass -h user_autosummonfarm set_attackSummonSkills $editSummonAttackSkills " + summonSpellType + "\" value=\"Save\"></td>";
            }
            else if(secondCommand.equals("editSummonSelfSkills"))
            {
                text += "<td width=43><edit var=\"" + secondCommand + "\" width=34 height=12></td>";
                text += "<td width=40><button width=40 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\" action=\"bypass -h user_autosummonfarm set_selfSummonSkills $editSummonSelfSkills " + summonSpellType + "\" value=\"Save\"></td>";
            }
            else if(secondCommand.equals("editSummonHealSkills"))
            {
                text += "<td width=43><edit var=\"" + secondCommand + "\" width=34 height=12></td>";
                text += "<td width=40><button width=40 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\" action=\"bypass -h user_autosummonfarm set_healSummonSkills $editSummonHealSkills " + summonSpellType + "\" value=\"Save\"></td>";
            }
            else if(secondCommand.equals("editSummonAttackPercent"))
            {
                text += "<td width=43><edit var=\"" + secondCommand + "\" width=34 height=12></td>";
                text += "<td width=40><button width=40 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\" action=\"bypass -h user_autosummonfarm set_attackSummonSkillsPercent $editSummonAttackPercent " + summonSpellType + "\" value=\"Save\"></td>";
            }
            else if(secondCommand.equals("editSummonSelfPercent"))
            {
                text += "<td width=43><edit var=\"" + secondCommand + "\" width=34 height=12></td>";
                text += "<td width=40><button width=40 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\" action=\"bypass -h user_autosummonfarm set_selfSummonSkillsPercent $editSummonSelfPercent " + summonSpellType + "\" value=\"Save\"></td>";
            }
            else if(secondCommand.equals("editSummonLowHealPercent"))
            {
                text += "<td width=43><edit var=\"" + secondCommand + "\" width=34 height=12></td>";
                text += "<td width=40><button width=40 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\" action=\"bypass -h user_autosummonfarm set_healSummonSkillsPercent $editSummonLowHealPercent " + summonSpellType + "\" value=\"Save\"></td>";
            }
        }
        else
        {
            text += "<td aling=center width=45><font color=c1b33a>" + player.getVarInt(varName, value) + "%</font></td>";
            text += "<td width=40><button width=40 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\" action=\"bypass -h user_autosummonfarm edit_farm " + editCommand + " " + summonSpellType + "\" value=\"Edit\"></td>";
        }
        return text;
    }

    private void sendAddSkillHtml(L2Player player, AutoFarmContext farmSystem, String spellType, int page)
    {
        List<Integer> skillIds = null;
        List<L2Skill> skillList = new ArrayList<L2Skill>();
        switch(spellType)
        {
            case "attack":
                skillIds = farmSystem.getAttackSpells();
                for(L2Skill skill : player.getAllSkills())
                {
                    if(skill != null && !skill.isPassive()
                            && !skill.isSpoilSkill()
                            && !skill.isSweepSkill()
                            && skill.getId() != 1263
                            && (skill.getSkillType() == SkillType.AGGRESSION
                            || skill.getSkillType() == SkillType.PDAM
                            || skill.getSkillType() == SkillType.MANADAM
                            || skill.getSkillType() == SkillType.MDAM
                            || skill.getSkillType() == SkillType.DRAIN
                            || skill.getSkillType() == SkillType.CPDAM
                            || skill.getSkillType() == SkillType.STUN))
                        skillList.add(skill);
                }
                break;
            case "chance":
                skillIds = farmSystem.getChanceSpells();
                for(L2Skill skill : player.getAllSkills())
                {
                    if(skill != null && !skill.isPassive() && (skill.getSkillType() == SkillType.DOT || skill.getSkillType() == SkillType.MDOT || skill.getSkillType() == SkillType.POISON || skill.getSkillType() == SkillType.BLEED || skill.getSkillType() == SkillType.DEBUFF || skill.getSkillType() == SkillType.SLEEP || skill.getSkillType() == SkillType.ROOT || skill.getSkillType() == SkillType.PARALYZE || skill.getSkillType() == SkillType.MUTE || skill.isSpoilSkill() || skill.isSweepSkill() || skill.getId() == 1263))
                        skillList.add(skill);
                }
                break;
            case "self":
                skillIds = farmSystem.getSelfSpells();
                for(L2Skill skill : player.getAllSkills())
                {
                    if(skill != null && !skill.isPassive() && (skill.isToggle() || skill.isMusic() || skill.getSkillType() == SkillType.BUFF || skill.isCubicSkill()))
                        skillList.add(skill);
                }
                break;
            case "heal":
                skillIds = farmSystem.getLowLifeSpells();
                for(L2Skill skill : player.getAllSkills())
                {
                    if(skill != null && !skill.isPassive() && (skill.getSkillType() == SkillType.DRAIN || skill.getSkillType() == SkillType.HEAL || skill.getSkillType() == SkillType.HEAL_PERCENT || skill.getSkillType() == SkillType.MANAHEAL || skill.getSkillType() == SkillType.MANAHEAL_PERCENT))
                        skillList.add(skill);
                }
                break;
        }

        if(skillList.isEmpty())
        {
            player.sendMessage("You have no valid skills!");
            sendMainHtml(player, farmSystem, null, player.getVarInt("farmType", Config.FARM_TYPE), spellType);
            return;
        }

        if(skillIds.size() > 0)
        {
            List<L2Skill> availableSkills = new ArrayList<L2Skill>();
            for(L2Skill skill : skillList)
            {
                if(skillIds.contains(skill.getId()))
                    availableSkills.add(skill);
            }
            if(!availableSkills.isEmpty())
            {
                for(L2Skill skill : availableSkills)
                    skillList.remove(skill);
                availableSkills.clear();
            }
        }

        String skillHtml = HtmCache.getInstance().getNotNull("command/autofarm/player_skills.htm", player);
        String contentTemplate = HtmCache.getInstance().getNotNull("command/autofarm/player_skills_template.htm", player);
        String template = "";
        String skillContent = "";
        int count = 0;
        int countSkills = skillList.size();
        boolean somePage = countSkills > 5;
        for(int i = (page - 1) * 5; i < countSkills; i++)
        {
            L2Skill skill = skillList.get(i);
            if(skill != null)
            {
                template = contentTemplate;
                template = template.replace("%name%", skill.getName());
                template = template.replace("%icon%", skill.getIcon());
                template = template.replace("%bypass%", "bypass -h user_addNewSkill " + skill.getId() + " " + spellType + "");
                skillContent += template;
            }
            if(++count >= 5)
                break;
        }
        double pageFor = countSkills / 5.;
        int maxPage = (int) Math.ceil(pageFor);
        skillHtml = skillHtml.replace("%list%", skillContent);
        skillHtml = skillHtml.replace("%page%", String.valueOf(page));
        skillHtml = skillHtml.replace("%skillType%", spellType);
        skillHtml = skillHtml.replace("%navigation%", Util.getNavigationBlock(maxPage, page, countSkills, 5, somePage, "user_addSkill " + spellType + " %s"));
        Functions.show(skillHtml, player, null);
        skillList.clear();
    }

    private void sendSummonHtml(L2Player player, AutoFarmContext farmSystem, String editCommand, int farmType, String summonSpellType, String spellType)
    {
        if(player.getPet() == null)
        {
            player.sendMessage(new CustomMessage("YOU_CANT_USE_THIS_OPTION", player));
            sendSummonHtml(player, farmSystem, null, farmType, summonSpellType, spellType);
            return;
        }
        L2Summon summon = player.getPet();
        if(summon.isPet() && summon.getLevel() - player.getLevel() > 20)
        {
            player.sendPacket(Msg.THE_PET_IS_TOO_HIGH_LEVEL_TO_CONTROL);
            sendSummonHtml(player, farmSystem, null, farmType, summonSpellType, spellType);
            return;
        }

        String html = HtmCache.getInstance().getNotNull("command/autofarm/index-summonSkills.htm", player);
        List<Integer> skillIds = null;
        String skillsParam = null;
        String chanceAttack = "";
        String percentAttack = "";
        String chanceSelf = "";
        String percentSelf = "";
        String chanceLowHeal = "";
        String percentLowHeal = "";
        switch(summonSpellType)
        {
            case "attack":
            {
                skillIds = farmSystem.getSummonAttackSpells();
                skillsParam = HtmCache.getInstance().getNotNull("command/autofarm/index-summon_skill_attack.htm", player);
                chanceAttack = getSummonEditTab(player, (editCommand != null && editCommand.equals("editSummonAttackSkills")) ? editCommand : null, "editSummonAttackSkills", "attackSummonChanceSkills", Config.SUMMON_ATTACK_SKILL_CHANCE, summonSpellType);
                percentAttack = getSummonEditTab(player, (editCommand != null && editCommand.equals("editSummonAttackPercent")) ? editCommand : null, "editSummonAttackPercent", "attackSummonSkillsPercent", Config.SUMMON_ATTACK_SKILL_PERCENT, summonSpellType);

                if(farmSystem.isRndSummonAttackSkills())
                    skillsParam = skillsParam.replace("%rndAttackSk_img%", new CustomMessage("services.autofarm.checkbox.checked", player).toString());
                else
                    skillsParam = skillsParam.replace("%rndAttackSk_img%", new CustomMessage("services.autofarm.checkbox", player).toString());

                skillsParam = skillsParam.replace("%rndAttackSk_bypass%", "bypass -h user_editSummonFarmOption " + summonSpellType + " farmRndSummonAttackSkills " + spellType);
                break;
            }
            case "self":
            {
                skillIds = farmSystem.getSummonSelfSpells();
                skillsParam = HtmCache.getInstance().getNotNull("command/autofarm/index-summon_skill_self.htm", player);
                chanceSelf = getSummonEditTab(player, (editCommand != null && editCommand.equals("editSummonSelfSkills")) ? editCommand : null, "editSummonSelfSkills", "selfSummonChanceSkills", Config.SUMMON_SELF_SKILL_CHANCE, summonSpellType);
                percentSelf = getSummonEditTab(player, (editCommand != null && editCommand.equals("editSummonSelfPercent")) ? editCommand : null, "editSummonSelfPercent", "selfSummonSkillsPercent", Config.SUMMON_SELF_SKILL_PERCENT, summonSpellType);

                if(farmSystem.isRndSummonSelfSkills())
                    skillsParam = skillsParam.replace("%rndSelfSk_img%", new CustomMessage("services.autofarm.checkbox.checked", player).toString());
                else
                    skillsParam = skillsParam.replace("%rndSelfSk_img%", new CustomMessage("services.autofarm.checkbox", player).toString());

                skillsParam = skillsParam.replace("%rndSelfSk_bypass%", "bypass -h user_editSummonFarmOption " + summonSpellType + " farmRndSummonSelfSkills " + spellType);
                break;
            }
            case "heal":
                skillIds = farmSystem.getSummonHealSpells();
                skillsParam = HtmCache.getInstance().getNotNull("command/autofarm/index-summon_skill_heal.htm", player);
                chanceLowHeal = getSummonEditTab(player, (editCommand != null && editCommand.equals("editSummonHealSkills")) ? editCommand : null, "editSummonHealSkills", "healSummonChanceSkills", Config.SUMMON_HEAL_SKILL_CHANCE, summonSpellType);
                percentLowHeal = getSummonEditTab(player, (editCommand != null && editCommand.equals("editSummonLowHealPercent")) ? editCommand : null, "editSummonLowHealPercent", "healSummonSkillsPercent", Config.SUMMON_HEAL_SKILL_PERCENT, summonSpellType);

                if(farmSystem.isRndSummonLifeSkills())
                    skillsParam = skillsParam.replace("%rndLifeSk_img%", new CustomMessage("services.autofarm.checkbox.checked", player).toString());
                else
                    skillsParam = skillsParam.replace("%rndLifeSk_img%", new CustomMessage("services.autofarm.checkbox", player).toString());

                skillsParam = skillsParam.replace("%rndLifeSk_bypass%", "bypass -h user_editSummonFarmOption " + summonSpellType + " farmRndSummonLifeSkills " + spellType);
                break;
        }

        String template = "";
        String skillList = "";
        if(skillIds != null && !skillIds.isEmpty())
        {
            List<Integer> availableIds = new ArrayList<Integer>();
            for(int id : skillIds)
            {
                if(id > 0)
                {
                    int availableLevel = summon.getTemplate().getAvailableLevel(summon, id);
                    if(availableLevel == 0)
                        availableIds.add(id);
                }
            }

            if(!availableIds.isEmpty())
            {
                for(int id : availableIds)
                {
                    skillIds.remove((Integer) id);
                }
                availableIds.clear();
            }
        }

        String templateHtml = HtmCache.getInstance().getNotNull("command/autofarm/summon_skill-template.htm", player);
        for(int i = 0; i < 8; i++)
        {
            template = templateHtml;
            if(skillIds.size() > 0 && i < skillIds.size())
            {
                int id = skillIds.get(i);
                if(id > 0)
                {
                    int availableSkillLevel = summon.getTemplate().getAvailableLevel(summon, id);
                    if(availableSkillLevel > 0)
                    {
                        template = template.replace("%icon%", SkillTable.getInstance().getInfo(id, availableSkillLevel).getIcon());
                        template = template.replace("%background%", "");
                        template = template.replace("%bypass%", "bypass -h user_removeSummonSkill " + summonSpellType + " " + id + " " + spellType);
                    }
                    else
                    {
                        template = template.replace("%icon%", "icon.high_tab");
                        template = template.replace("%background%", "background=\"l2ui_ch3.multisell_plusicon\"");
                        template = template.replace("%bypass%", "bypass -h user_addSummonSkill " + summonSpellType + " " + spellType);
                    }
                }
                else
                {
                    template = template.replace("%icon%", "icon.high_tab");
                    template = template.replace("%background%", "background=\"l2ui_ch3.multisell_plusicon\"");
                    template = template.replace("%bypass%", "bypass -h user_addSummonSkill " + summonSpellType + " " + spellType);
                }
            }
            else
            {
                template = template.replace("%icon%", "icon.high_tab");
                template = template.replace("%background%", "background=\"l2ui_ch3.multisell_plusicon\"");
                template = template.replace("%bypass%", "bypass -h user_addSummonSkill " + summonSpellType + " " + spellType);
            }
            skillList += template;
        }

        if(farmSystem.isExtraSummonDelaySkill())
            html = html.replace("%delaySk_img%", new CustomMessage("services.autofarm.checkbox.checked", player).toString());
        else
            html = html.replace("%delaySk_img%", new CustomMessage("services.autofarm.checkbox", player).toString());

        html = html.replace("%delaySk_bypass%", "bypass -h user_editSummonFarmOption " + summonSpellType + " farmSummonDelaySkills " + spellType);
        html = html.replace("%skillType%", spellType);
        html = html.replace("%skillsParam%", skillsParam);
        html = html.replace("%summonSkillType%", summonSpellType);
        html = html.replace("%skillList%", skillList);
        html = html.replace("%chanceAttack%", chanceAttack.equals("") ? "" : chanceAttack);
        html = html.replace("%chanceSelf%", chanceSelf.equals("") ? "" : chanceSelf);
        html = html.replace("%chanceLowHeal%", chanceLowHeal.equals("") ? "" : chanceLowHeal);
        html = html.replace("%percentAttack%", percentAttack.equals("") ? "" : percentAttack);
        html = html.replace("%percentSelf%", percentSelf.equals("") ? "" : percentSelf);
        html = html.replace("%percentLowHeal%", percentLowHeal.equals("") ? "" : percentLowHeal);
        Functions.show(html, player, null);
    }

    private void sendAddSummonSkillHtml(L2Player player, AutoFarmContext farmSystem, int farmType, String summonSpellType, String spellType, int page)
    {
        if(player.getPet() == null)
        {
            player.sendMessage(new CustomMessage("YOU_CANT_USE_THIS_OPTION", player));
            sendSummonHtml(player, farmSystem, null, farmType, summonSpellType, spellType);
            return;
        }

        L2Summon summon = player.getPet();
        if(summon.isPet() && summon.getLevel() - player.getLevel() > 20)
        {
            player.sendPacket(Msg.THE_PET_IS_TOO_HIGH_LEVEL_TO_CONTROL);
            sendSummonHtml(player, farmSystem, null, farmType, summonSpellType, spellType);
            return;
        }

        List<Integer> skillIds = null;
        List<L2Skill> skillList = new ArrayList<L2Skill>();
        switch(summonSpellType)
        {
            case "attack":
            {
                skillIds = farmSystem.getSummonAttackSpells();
                for(L2Skill skill : summon.getTemplate().getSkills().values())
                {
                    int skillId = skill.getId();
                    int skillLvl = summon.getTemplate().getAvailableLevel(summon, skillId);
                    if(skillLvl > 0)
                    {
                        L2Skill skillInfo = SkillTable.getInstance().getInfo(skillId, skillLvl);
                        if (skillInfo != null
                                && (skillInfo.getSkillType() == SkillType.AGGRESSION
                                || skillInfo.getSkillType() == SkillType.PDAM
                                || skillInfo.getSkillType() == SkillType.MANADAM
                                || skillInfo.getSkillType() == SkillType.MDAM
                                || skillInfo.getSkillType() == SkillType.DRAIN
                                || skillInfo.getSkillType() == SkillType.CPDAM
                                || skillInfo.getSkillType() == SkillType.STUN))
                            skillList.add(skillInfo);
                    }
                }
                break;
            }
            case "self":
            {
                skillIds = farmSystem.getSummonSelfSpells();
                for(L2Skill skill : summon.getTemplate().getSkills().values())
                {
                    int skillId = skill.getId();
                    int skillLvl = summon.getTemplate().getAvailableLevel(summon, skillId);
                    if(skillLvl > 0)
                    {
                        L2Skill skillInfo = SkillTable.getInstance().getInfo(skillId, skillLvl);
                        if(skillInfo != null && (skillInfo.isToggle() || skillInfo.isMusic() || skillInfo.getSkillType() == SkillType.BUFF || skillInfo.isCubicSkill()))
                            skillList.add(skillInfo);
                    }
                }
                break;
            }
            case "heal":
            {
                skillIds = farmSystem.getSummonHealSpells();
                for(L2Skill skill : summon.getTemplate().getSkills().values())
                {
                    int skillId = skill.getId();
                    int skillLevel = summon.getTemplate().getAvailableLevel(summon, skillId);
                    if(skillLevel > 0)
                    {
                        L2Skill skillInfo = SkillTable.getInstance().getInfo(skillId, skillLevel);
                        if(skillInfo != null
                                && (skillInfo.getSkillType() == SkillType.DRAIN
                                || skillInfo.getSkillType() == SkillType.HEAL
                                || skillInfo.getSkillType() == SkillType.HEAL_PERCENT
                                || skillInfo.getSkillType() == SkillType.MANAHEAL
                                || skillInfo.getSkillType() == SkillType.MANAHEAL_PERCENT))
                            skillList.add(skillInfo);
                    }
                }
                break;
            }
        }

        if(skillIds.size() > 0)
        {
            List<L2Skill> availableSkills = new ArrayList<L2Skill>();
            for(L2Skill skill : skillList)
            {
                if(skillIds.contains(skill.getId()))
                    availableSkills.add(skill);
            }
            if(!availableSkills.isEmpty())
            {
                for(L2Skill skill : availableSkills)
                    skillList.remove(skill);
                availableSkills.clear();
            }
        }

        if(skillList.isEmpty())
        {
            player.sendMessage("Your summon has no valid skills!");
            sendSummonHtml(player, farmSystem, null, farmType, summonSpellType, spellType);
            return;
        }

        String skillHtml = HtmCache.getInstance().getNotNull("command/autofarm/summon_skills.htm", player);
        String templateHtml = HtmCache.getInstance().getNotNull("command/autofarm/summon_skills_template.htm", player);
        String textSkill = "";
        String skillContent = "";
        int count = 0;
        int countSkills = skillList.size();
        boolean somePage = countSkills > 5;
        for(int i = (page - 1) * 5; i < countSkills; i++)
        {
            L2Skill skill = skillList.get(i);
            if(skill != null)
            {
                textSkill = templateHtml;
                textSkill = textSkill.replace("%name%", skill.getName());
                textSkill = textSkill.replace("%icon%", skill.getIcon());
                textSkill = textSkill.replace("%bypass%", "bypass -h user_addNewSummonSkill " + skill.getId() + " " + summonSpellType + " " + spellType + "");
                skillContent += textSkill;
            }
            if(++count >= 5)
                break;
        }
        double pageFor = countSkills / 5.;
        int maxPage = (int) Math.ceil(pageFor);
        skillHtml = skillHtml.replace("%list%", skillContent);
        skillHtml = skillHtml.replace("%page%", String.valueOf(page));
        skillHtml = skillHtml.replace("%summonSkillType%", summonSpellType);
        skillHtml = skillHtml.replace("%skillType%", spellType);
        skillHtml = skillHtml.replace("%navigation%", Util.getNavigationBlock(maxPage, page, countSkills, 5, somePage, "user_addSummonSkill " + summonSpellType + " " + spellType + " %s"));
        Functions.show(skillHtml, player, null);
        skillList.clear();
    }

    @Override
    public String[] getVoicedCommandList()
    {
        return _commands;
    }

    @Override
    public void onLoad()
    {
        if(Config.ALLOW_AUTO_FARM)
            VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
    }

    @Override
    public void onReload()
    {

    }

    @Override
    public void onShutdown()
    {

    }

    private static class SortTimeInfo implements Serializable, Comparator<Integer>
    {
        private static final long serialVersionUID = 7691414259610932752L;

        @Override
        public int compare(Integer o1, Integer o2)
        {
            return Double.compare(o1, o2);
        }
    }
}