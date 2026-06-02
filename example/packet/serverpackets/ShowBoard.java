package l2p.gameserver.serverpackets;

import l2p.gameserver.utils.Strings;

import java.util.List;

public class ShowBoard extends L2GameServerPacket
{
	public static L2GameServerPacket CLOSE = new ShowBoard();
	public static final ShowBoard STATIC_SHOWBOARD_102 = new ShowBoard(null, "102");
	public static final ShowBoard STATIC_SHOWBOARD_103 = new ShowBoard(null, "103");

	private static final String TOP = "bypass _bbshome";
	private static final String FAV = "bypass _bbsgetfav";
	private static final String REGION = "bypass _bbsloc";
	private static final String CLAN = "bypass _bbsclan";
	private static final String MEMO = "bypass _bbsmemo";
	private static final String MAIL = "bypass _maillist_0_1_0_";
	private static final String FRIENDS = "bypass _friendlist_0_";
	private static final String ADDFAV = "bypass bbs_add_fav";
	private final StringBuilder _htmlCode = new StringBuilder();
	private int _open = 1;

	public ShowBoard(String htmlCode, String id)
	{
		Strings.append(_htmlCode, id, "\b", htmlCode);
	}

	public ShowBoard(List<String> arg)
	{
		_htmlCode.append("1002\b");
		for(String str : arg)
			Strings.append(_htmlCode, str, " \b");
	}

	public ShowBoard()
	{
		_open = 0;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(_open); //c4 1 to show community 00 to hide
		if(_open == 1)
		{
			writeS(TOP);
			writeS(FAV);
			writeS(REGION);
			writeS(CLAN);
			writeS(MEMO);
			writeS(MAIL);
			writeS(FRIENDS);
			writeS(ADDFAV);
			writeS(_htmlCode.toString());
		}
	}
}