package l2p.gameserver.serverpackets;

public class TutorialShowHtml extends L2GameServerPacket
{
	private String _html;
	private final int _type;

	public TutorialShowHtml(String html)
	{
		_html = html;
		_type = 1;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_type);
		writeS(_html);
	}

	@Override
	protected final void writeImplIT()
	{
		writeS(_html);
	}
}