package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Character;

public class ChangeMoveType extends L2GameServerPacket
{
	public static int WALK = 0;
	public static int RUN = 1;

	private int _chaId;
	private boolean _running;

	public ChangeMoveType(L2Character cha)
	{
		_chaId = cha.getObjectId();
		_running = cha.isRunning();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_chaId);
		writeD(_running ? 1 : 0);
		writeD(0); //c2
	}
}