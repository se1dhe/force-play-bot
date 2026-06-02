package l2p.gameserver.serverpackets;

public class ExRaidCharSelected extends L2GameServerPacket
{
    @Override
    protected void writeImpl()
    {
        // just a trigger
    }

    @Override
    protected boolean canWriteIT()
    {
        return false;
    }
}