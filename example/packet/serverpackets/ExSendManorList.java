package l2p.gameserver.serverpackets;

public class ExSendManorList extends L2GameServerPacket
{
	public static final ExSendManorList STATIC_PACKET = new ExSendManorList();

	private static final String[] _manorList = { "gludio", "dion", "giran", "oren", "aden", "innadril", "goddard", "rune", "schuttgart" };

	private ExSendManorList()
	{}

	@Override
	protected void writeImpl()
	{
		writeD(_manorList.length);
		for(int i = 0; i < _manorList.length; i++)
		{
			writeD(i + 1);
			writeS(_manorList[i]);
		}
	}

	@Override
	protected void writeImplIT()
	{
		writeD(_manorList.length);
		for(int i = 0; i < _manorList.length; i++)
		{
			writeD(i + 1);
			writeS(_manorList[i]);
		}
	}
}