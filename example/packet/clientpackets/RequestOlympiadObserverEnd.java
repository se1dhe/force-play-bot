package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;

public class RequestOlympiadObserverEnd extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(activeChar.isInOlympiadObserverMode() && activeChar.getObserverMode() == 3)
			activeChar.leaveOlympiadObserverMode();
	}
}