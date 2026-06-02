package l2p.gameserver.serverpackets;

/**
 * @author monithly
 */
public class ExMagicAttackInfo extends L2GameServerPacket
{
    public static final int CRITICAL = 1;
    public static final int CRITICAL_HEAL = 2;
    public static final int OVERHIT = 3;
    public static final int EVADED = 4;
    public static final int BLOCKED = 5;
    public static final int RESISTED = 6;
    public static final int IMMUNE = 7;
    public static final int IMMUNE2 = 8;

    private final int _attackerId, _targetId, _type;

    public ExMagicAttackInfo(int attackerId, int targetId, int type)
    {
        _attackerId = attackerId;
        _targetId = targetId;
        _type = type;
    }

    @Override
    protected void writeImpl()
    {
        writeD(_attackerId);
        writeD(_targetId);
        writeD(_type);
    }

    @Override
    protected boolean canWriteIT()
    {
        return false;
    }
}