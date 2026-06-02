package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.serverpackets.SSQStatus;

public class RequestSSQStatus extends L2GameClientPacket
{
	private int _page;

	@Override
	public void readImpl()
	{
		_page = readC();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		sendPacket(new SSQStatus(activeChar, _page));
	}
}
