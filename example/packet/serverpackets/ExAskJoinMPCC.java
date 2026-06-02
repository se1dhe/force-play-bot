package l2p.gameserver.serverpackets;

public class ExAskJoinMPCC extends L2GameServerPacket
{
	private String _requestorName;

	public ExAskJoinMPCC(String requestorName)
	{
		_requestorName = requestorName;
	}

	@Override
	protected void writeImpl()
	{
		writeS(_requestorName); // лидер CC
		writeD(0x00);
	}

	@Override
	protected void writeImplIT()
	{
		writeS(_requestorName); // лидер CC
	}
}