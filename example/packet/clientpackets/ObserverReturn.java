package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;

public class ObserverReturn extends L2GameClientPacket
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
		if(activeChar.getObserverMode() == 3)
			if(activeChar.getOlympiadObserveId() != -1)
				activeChar.leaveOlympiadObserverMode();
			else
				activeChar.leaveObserverMode();
	}
}