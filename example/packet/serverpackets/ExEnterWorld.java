package l2p.gameserver.serverpackets;

public class ExEnterWorld extends L2GameServerPacket
{
    private final int _serverTime;

    public ExEnterWorld()
    {
        _serverTime = (int) (System.currentTimeMillis() / 1000L);
    }

    @Override
    protected final void writeImpl()
    {
        writeD(_serverTime);
    }

    @Override
    protected boolean canWriteIT()
    {
        return false;
    }
}