package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2TradeList;

public class SetPrivateStoreWholeMsg extends L2GameClientPacket
{
	private String _storename;

	@Override
	protected void readImpl()
	{
		_storename = readS(32);
	}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		L2TradeList tradeList = activeChar.getTradeList();
		if(tradeList != null)
			tradeList.setSellStoreName(_storename);
	}
}