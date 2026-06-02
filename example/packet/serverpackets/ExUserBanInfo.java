package l2p.gameserver.serverpackets;

public class ExUserBanInfo extends L2GameServerPacket
{
    private final int _points;

    public ExUserBanInfo(int points)
    {
        _points = points;
    }

    @Override
    protected final void writeImpl()
    {
        writeD(_points);
    }
}