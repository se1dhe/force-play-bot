package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Player;

public class ExUISetting extends L2GameServerPacket
{
	private final byte[] data;

	public ExUISetting(L2Player player)
	{
		data = player.getKeyBindings();
	}

	@Override
	protected void writeImpl()
	{
		writeD(data.length);
		writeB(data);
	}

	@Override
	protected boolean canWrite()
	{
		return true;
	}

	@Override
	protected boolean canWriteIT()
	{
		return false;
	}
}