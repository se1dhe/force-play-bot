package l2p.gameserver.clientpackets;

import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2TradeList;
import l2p.gameserver.model.base.Transaction;
import l2p.gameserver.model.base.Transaction.TransactionType;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.serverpackets.TradeStart;

public class AnswerTradeRequest extends L2GameClientPacket
{
	// Format: cd
	private int _response;

	@Override
	public void readImpl()
	{
		_response = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}

		Transaction transaction = activeChar.getTransaction();

		if(transaction == null)
			return;

		if(!transaction.isValid() || !transaction.isTypeOf(TransactionType.TRADE_REQUEST))
		{
			transaction.cancel();
			if(_response == 1)
				activeChar.sendPacket(Msg.TARGET_IS_NOT_FOUND_IN_THE_GAME, Msg.ActionFail);
			else
				activeChar.sendPacket(Msg.TIME_EXPIRED, Msg.ActionFail);
			return;
		}

		L2Player requestor = transaction.getOtherPlayer(activeChar);

		if(_response != 1 && !requestor.isGM() || activeChar.getPrivateStoreType() != L2Player.STORE_PRIVATE_NONE)
		{
			requestor.sendPacket(new SystemMessage(SystemMessage.S1_DENIED_YOUR_REQUEST_FOR_TRADE).addString(activeChar.getName()), Msg.ActionFail);
			transaction.cancel();
			if(activeChar.getPrivateStoreType() != L2Player.STORE_PRIVATE_NONE)
				activeChar.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}

		transaction.cancel();

		new Transaction(TransactionType.TRADE, activeChar, requestor);

		if(requestor.isITClient())
			requestor.sendPacket(new SystemMessage(SystemMessage.BEGIN_TRADING_WITH_S1).addString(activeChar.getName()), new TradeStart(1, requestor, activeChar));
		else
			requestor.sendPacket(new SystemMessage(SystemMessage.BEGIN_TRADING_WITH_S1).addString(activeChar.getName()), new TradeStart(1, requestor, activeChar), new TradeStart(2, requestor, activeChar));
		if(activeChar.isITClient())
			activeChar.sendPacket(new SystemMessage(SystemMessage.BEGIN_TRADING_WITH_S1).addString(requestor.getName()), new TradeStart(1, activeChar, requestor));
		else
			activeChar.sendPacket(new SystemMessage(SystemMessage.BEGIN_TRADING_WITH_S1).addString(requestor.getName()), new TradeStart(1, activeChar, requestor), new TradeStart(2, activeChar, requestor));
		if(requestor.getTradeList() == null)
			requestor.setTradeList(new L2TradeList(0));
		if(activeChar.getTradeList() == null)
			activeChar.setTradeList(new L2TradeList(0));
	}
}