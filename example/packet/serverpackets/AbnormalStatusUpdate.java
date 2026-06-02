package l2p.gameserver.serverpackets;

import l2p.commons.util.GArray;

public class AbnormalStatusUpdate extends L2GameServerPacket
{
    class Effect
    {
        int skillId;
        int skillLevel;
        int clientAbnormalId;
        int duration;

        public Effect(int skillId, int skillLevel, int clientAbnormalId, int duration)
        {
            this.skillId = skillId;
            this.skillLevel = skillLevel;
            this.clientAbnormalId = clientAbnormalId;
            this.duration = duration;
        }
    }

    public static final int INFINITIVE_EFFECT = -1;
    private GArray<Effect> _effects;

    public AbnormalStatusUpdate()
    {
        _effects = new GArray<Effect>();
    }

    public void addEffect(int skillId, int dat, int clientAbnormalId, int duration)
    {
        _effects.add(new Effect(skillId, dat, clientAbnormalId, duration));
    }

    @Override
    protected final void writeImpl()
    {
        writeH(_effects.size());

        for(Effect temp : _effects)
        {
            writeD(temp.skillId);
            writeH(temp.skillLevel);
            writeD(temp.clientAbnormalId);	// Abnormal Type
            writeOptionalD(temp.duration);
        }
    }

    @Override
    protected final void writeImplIT()
    {
        writeH(_effects.size());

        for(Effect temp : _effects)
        {
            writeD(temp.skillId);
            writeH(temp.skillLevel);
            writeD(temp.duration);
        }
    }
}