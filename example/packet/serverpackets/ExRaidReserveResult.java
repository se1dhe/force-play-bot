package l2p.gameserver.serverpackets;

public class ExRaidReserveResult extends L2GameServerPacket
{
    @Override
    protected void writeImpl()
    {
        // TODO dx[dddd]
    }

    @Override
    protected boolean canWriteIT()
    {
        return false;
    }
}