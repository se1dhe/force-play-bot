package l2p.gameserver.serverpackets;

public class ExAskJoinPartyRoom extends L2GameServerPacket
{
	private String _charName;
	private String _roomName;

	public ExAskJoinPartyRoom(String charName, String roomName)
	{
		_charName = charName;
		_roomName = roomName;
	}

	@Override
	protected final void writeImpl()
	{
		writeS(_charName);
		writeS(_roomName);
	}

	@Override
	protected final void writeImplIT()
	{
		writeS(_charName);
	}
}