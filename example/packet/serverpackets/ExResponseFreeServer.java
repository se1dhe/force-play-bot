package l2p.gameserver.serverpackets;

public class ExResponseFreeServer extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		// just trigger
	}

	@Override
	protected boolean canWriteIT()
	{
		return false;
	}
}