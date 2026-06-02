package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;

public class ChangeMoveType2 extends L2GameClientPacket
{
	private boolean _typeRun;
	@Override
	public void readImpl()
	{
		_typeRun = readD() == 1;
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(_typeRun)
			activeChar.setRunning();
		else
			activeChar.setWalking();
	}
}