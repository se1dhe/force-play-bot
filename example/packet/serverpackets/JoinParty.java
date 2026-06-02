package l2p.gameserver.serverpackets;

public class JoinParty extends L2GameServerPacket
{
	private int _response;

	public JoinParty(int response)
	{
		_response = response;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_response);
	}
}