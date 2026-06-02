package l2p.gameserver.serverpackets;

public class AskJoinAlliance extends L2GameServerPacket
{
	private String _requestorName;
	private String _requestorAllyName;
	private int _requestorId;

	public AskJoinAlliance(int requestorId, String requestorName, String requestorAllyName)
	{
		_requestorName = requestorName;
		_requestorAllyName = requestorAllyName;
		_requestorId = requestorId;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_requestorId);
		writeS(_requestorName);
		writeS("");
		writeS(_requestorAllyName);
	}
}