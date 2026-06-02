package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;

public class FinishRotating extends L2GameClientPacket
{
	private int _degree;
	@SuppressWarnings("unused")
	private int _unknown;

	public void readImpl()
	{
		_degree = readD();
		_unknown = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		activeChar.broadcastPacket(new l2p.gameserver.serverpackets.FinishRotating(activeChar.getObjectId(), _degree, 0));
	}
}