package l2p.gameserver.serverpackets;

public class ExTutorialList extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeS("");
		writeD(0x00);
		writeD(0x00);
		writeD(0x00);
	}

	@Override
	protected boolean canWriteIT()
	{
		return false;
	}
}