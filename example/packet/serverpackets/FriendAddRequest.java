package l2p.gameserver.serverpackets;

public class FriendAddRequest extends L2GameServerPacket
{
	private String _requestorName;

	public FriendAddRequest(String requestorName)
	{
		_requestorName = requestorName;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0); // 0
		writeS(_requestorName);
	}

	@Override
	protected final void writeImplIT()
	{
		writeS(_requestorName);
		writeD(0);
	}
}