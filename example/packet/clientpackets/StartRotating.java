package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;

public class StartRotating extends L2GameClientPacket
{
	private int _degree;
	private int _side;

	@Override
	public void readImpl()
	{
		_degree = readD();
		_side = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		activeChar.setHeading(_degree);
		activeChar.broadcastPacket(new l2p.gameserver.serverpackets.StartRotating(activeChar.getObjectId(), _degree, _side, 0));
	}
}