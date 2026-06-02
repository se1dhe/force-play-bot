package l2p.gameserver.serverpackets;

public class SendTradeRequest extends L2GameServerPacket
{
	private int _senderID;

	public SendTradeRequest(int senderID)
	{
		_senderID = senderID;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_senderID);
	}
}