package l2p.gameserver.serverpackets;

public class NetPing extends L2GameServerPacket
{
	private final int timestamp;

	public NetPing(int timestamp)
	{
		this.timestamp = timestamp;
	}

	@Override
	protected void writeImpl()
	{
		writeD(timestamp);
	}
}