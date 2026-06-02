package l2p.gameserver.serverpackets;

import l2p.commons.util.GArray;
import l2p.gameserver.model.L2Effect;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.tables.SkillTree;

public class ExAbnormalStatusUpdateFromTarget extends L2GameServerPacket
{
    private int _objectId;

    private static class Effect
    {
        public int effectorObjectId;
        public int skillId;
        public int skillLevel;
        public int clientAbnormalId;
        public int duration;

        public Effect(int effectorObjectId, int skillId, int skillLevel, int clientAbnormalId, int duration)
        {
            this.effectorObjectId = effectorObjectId;
            this.skillId = skillId;
            this.skillLevel = skillLevel;
            this.clientAbnormalId = clientAbnormalId;
            this.duration = duration;
        }
    }

    public static final int INFINITIVE_EFFECT = -1;
    private GArray<Effect> _effects;

    public ExAbnormalStatusUpdateFromTarget(L2Player player)
    {
        _objectId = player.getObjectId();
        _effects = new GArray<Effect>();
    }

    public ExAbnormalStatusUpdateFromTarget(L2Player player, boolean force)
    {
        _objectId = player.getObjectId();
        _effects = new GArray<Effect>();
        if(force)
        {
            for(L2Effect effect : player.getEffectList().getAllFirstEffects())
            {
                if(effect == null || !effect.isInUse() || !effect.isActive() || effect.isHidden() || effect.getSkill().isToggle())
                {
                    continue;
                }
                addEffect(effect);
            }
        }
    }

    public void addEffect(int effectorObjectId, int skillId, int skillLvl, int clientAbnormalId, int duration)
    {
        _effects.add(new Effect(effectorObjectId, skillId, skillLvl, clientAbnormalId, duration));
    }

    public void addEffect(L2Effect effect)
    {
        int level = effect.getDisplayLevel();
        if(level < 100)
        {
            addEffect(effect.getDisplayId(), level, (int) effect.getTimeLeft(), 0, effect.getEffector() != null ? effect.getEffector().getObjectId() : 0);
        }
        else
        {
            addEffect(effect.getDisplayId(), effect.getSkill() != null ? SkillTree.getBaseLevels().get(effect.getSkill().getId()) : 1, (int) effect.getTimeLeft(), 0, effect.getEffector() != null ? effect.getEffector().getObjectId() : 0);
        }
    }

    @Override
    protected final void writeImpl()
    {
        writeD(_objectId);
        writeH(_effects.size());

        for(Effect temp : _effects)
        {
            writeD(temp.skillId);
            writeH(temp.skillLevel);
            writeH(temp.clientAbnormalId);	// Abnormal Type
            writeOptionalD(temp.duration);
            writeD(temp.effectorObjectId); // Buffer OID
        }
    }

    @Override
    protected boolean canWriteIT()
    {
        return false;
    }
}