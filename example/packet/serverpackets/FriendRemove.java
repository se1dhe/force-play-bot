package l2p.gameserver.serverpackets;

// TODO [V] - узнать, нужно ли для it
public class FriendRemove extends L2GameServerPacket
{
	private final int _responce;
	private final String _friendName;

	public FriendRemove(String name, int responce)
	{
		_friendName = name;
		_responce = responce;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_responce); //responce
		writeS(_friendName); //FriendName
	}

	@Override
	protected boolean canWriteIT()
	{
		return false;
	}
}