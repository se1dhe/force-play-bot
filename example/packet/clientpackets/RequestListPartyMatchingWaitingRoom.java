package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.serverpackets.ExListPartyMatchingWaitingRoom;

public class RequestListPartyMatchingWaitingRoom extends L2GameClientPacket
{
	private int _minLevel, _maxLevel, _page;
	private boolean _showAll;

	@Override
	protected void readImpl()
	{
		_page = readD();
		_minLevel = readD();
		_maxLevel = readD();

		_showAll = readD() == 1;
	}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		activeChar.sendPacket(new ExListPartyMatchingWaitingRoom(activeChar, _minLevel, _maxLevel, _page, _showAll));
	}
}