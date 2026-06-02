package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2ObjectsStorage;
import l2p.gameserver.model.L2Player;

public class SnoopQuit extends L2GameClientPacket
{
	private int _snoopID;

	@Override
	public void readImpl()
	{
		_snoopID = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player player = L2ObjectsStorage.getPlayer(_snoopID);
		if(player == null)
			return;
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		player.removeSnooper(activeChar);
		activeChar.removeSnooped(player);
	}
}