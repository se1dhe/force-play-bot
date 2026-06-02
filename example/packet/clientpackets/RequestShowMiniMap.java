package l2p.gameserver.clientpackets;

import l2p.gameserver.serverpackets.ShowMiniMap;

public class RequestShowMiniMap extends L2GameClientPacket
{
    @Override
    public void readImpl()
    {}

    @Override
    public void runImpl()
    {
        sendPacket(new ShowMiniMap(1665));
    }
}