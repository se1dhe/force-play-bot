package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;

public class ChangeWaitType2 extends L2GameClientPacket
{
	private boolean _typeStand;

	@Override
	public void readImpl()
	{
		_typeStand = readD() == 1;
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(_typeStand)
			activeChar.standUp();
		else
			activeChar.sitDown(0);
	}
}