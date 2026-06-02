package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;

public class RequestExBuySellUIClose extends L2GameClientPacket
{
	@Override
	protected void runImpl()
	{
		// trigger
	}

	@Override
	protected void readImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		activeChar.setBuyListId(0);
		activeChar.sendItemList(true);
	}
}