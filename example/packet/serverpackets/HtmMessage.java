package l2p.gameserver.serverpackets;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmMessage extends L2GameServerPacket
{
	private int _npcObjId;
	private String _html;

	public HtmMessage(int npcObjId, String text)
	{
		_npcObjId = npcObjId;
		if(!text.contains("<html>"))
			text = "<html><body>" + text + "</body></html>";
		_html = text;
	}

	private static final Pattern objectId = Pattern.compile("%objectId%");

	@Override
	protected final void writeImpl()
	{
		Matcher m = objectId.matcher(_html);
		if(m != null)
			_html = m.replaceAll(String.valueOf(_npcObjId));

		if(_html.length() > 8192)
			_html = "<html><body><center>Sorry, to long html.</center></body></html>";

		writeC(0x0F);
		writeD(_npcObjId);
		writeS(_html);
		writeD(0);
	}
}
