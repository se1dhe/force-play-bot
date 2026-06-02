package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Player;

// TODO [V] - пересмотреть?
public class FriendStatus extends L2GameServerPacket
{
	public static final int MODE_OFFLINE = 0;
	public static final int MODE_ONLINE = 1;
	public static final int MODE_LEVEL = 2;
	public static final int MODE_CLASS = 3;

	private String _name;
	private int _type;
	private int _objectId;

	public FriendStatus(L2Player player, int type, int objectId)
	{
		_name = player.getName();
		_type = type;
		_objectId = objectId;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_type);
		writeS(_name);
		switch(_type)
		{
			case MODE_OFFLINE:
			case MODE_LEVEL:
			case MODE_CLASS:
			{
				writeD(_objectId);
				break;
			}
		}
	}

	@Override
	protected boolean canWriteIT()
	{
		return false;
	}
}