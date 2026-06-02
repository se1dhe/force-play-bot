package l2p.gameserver.serverpackets;

public class BrowserBypassPacket extends L2GameServerPacket
{
    private String URL;

    public BrowserBypassPacket(String url)
    {
        URL = url;
    }

    @Override
    protected void writeImpl()
    {
        writeC(0xFF);
        writeC(0x03);
        writeS(URL);
    }
}