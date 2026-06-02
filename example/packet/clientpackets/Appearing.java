package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;

public class Appearing extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		final L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.isLogoutStarted())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.getObserverMode() == 1)
		{
			activeChar.appearObserverMode();
			return;
		}

		if(activeChar.getObserverMode() == 2)
		{
			activeChar.returnFromObserverMode();
			return;
		}

		if(!activeChar.isTeleporting())
		{
			activeChar.sendActionFailed();
			return;
		}

		boolean inGvg = activeChar.inGvG;
		activeChar.inGvG = false;
		activeChar.onTeleported();
		activeChar.inGvG = inGvg;
	}
}