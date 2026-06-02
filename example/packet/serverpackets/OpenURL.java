package l2p.gameserver.serverpackets;

public class OpenURL extends L2GameServerPacket
{
	private final String _url;

	public OpenURL(String url)
	{
		_url = url;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0x03);
		writeS(_url);
	}
}