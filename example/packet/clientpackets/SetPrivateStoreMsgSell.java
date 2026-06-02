package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2TradeList;

public class SetPrivateStoreMsgSell extends L2GameClientPacket
{
	private String _storename;

	@Override
	public void readImpl()
	{
		_storename = readS(32);
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		L2TradeList tradeList = activeChar.getTradeList();
		if(tradeList != null)
			tradeList.setSellStoreName(_storename);
	}
}