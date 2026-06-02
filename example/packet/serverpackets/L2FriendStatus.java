package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Player;

public class L2FriendStatus extends L2GameServerPacket
{
	private String _charName;
	private boolean _login;

	public L2FriendStatus(L2Player player, boolean login)
	{
		_login = login;
		_charName = player.getName();
	}

	@Override
	protected boolean canWrite()
	{
		return false;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_login ? 1 : 0); //Logged in 1 logged off 0
		writeS(_charName);
		writeD(0); //id персонажа с базы оффа, не object_id
	}

	@Override
	protected boolean canWriteIT()
	{
		return true;
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_login ? 1 : 0); //Logged in 1 logged off 0
		writeS(_charName);
		writeD(0); //id персонажа с базы оффа, не object_id
	}
}