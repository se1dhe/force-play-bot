package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Character;

/**
 * @author Bonux
 **/
public class NpcInfoState extends L2GameServerPacket
{
	private static final int IS_DEAD = 1 << 0;
	private static final int IS_IN_COMBAT = 1 << 1;
	private static final int IS_RUNNING = 1 << 2;

	private final int _objectId;
	private int _state;

	public NpcInfoState(L2Character npc)
	{
		_objectId = npc.getObjectId();

		if(npc.isAlikeDead())
			_state |= IS_DEAD;

		if(npc.isInCombat())
			_state |= IS_IN_COMBAT;

		if(npc.isRunning())
			_state |= IS_RUNNING;
	}

	@Override
	protected boolean canWriteIT()
	{
		return false;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_objectId);
		writeC(_state);
	}
}