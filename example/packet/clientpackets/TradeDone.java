package l2p.gameserver.clientpackets;

import l2p.gameserver.cache.Msg;
import l2p.gameserver.geodata.GeoEngine;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2TradeList;
import l2p.gameserver.model.base.Transaction;
import l2p.gameserver.model.base.Transaction.TransactionType;
import l2p.gameserver.serverpackets.SendTradeDone;
import l2p.gameserver.serverpackets.SystemMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TradeDone extends L2GameClientPacket
{
	private static Logger _log = LoggerFactory.getLogger(TradeDone.class);

	private int _response;

	@Override
	public void readImpl()
	{
		_response = readD();
	}

	@Override
	public void runImpl()
	{
		synchronized (getClient())
		{
			L2Player activeChar = getClient().getActiveChar();
			if(activeChar == null)
				return;

			Transaction transaction = activeChar.getTransaction();

			L2Player requestor;
			if(transaction == null || (requestor = transaction.getOtherPlayer(activeChar)) == null)
			{
				if(transaction != null)
					transaction.cancel();
				activeChar.sendPacket(SendTradeDone.Fail, Msg.ActionFail);
				return;
			}

			if(activeChar.isOutOfControl())
			{
				transaction.cancel();
				activeChar.sendPacket(SendTradeDone.Fail, Msg.ActionFail);
				return;
			}

			if(activeChar.isInFightClub())
			{
				activeChar.sendActionFailed();
				return;
			}

			if(activeChar.isInStoreMode() || requestor.isInStoreMode())
			{
				transaction.cancel();
				activeChar.sendPacket(SendTradeDone.Fail, Msg.ActionFail);
				activeChar.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
				requestor.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
				return;
			}

			if(GeoEngine.noPath(activeChar, requestor))
			{
				transaction.cancel();
				activeChar.sendPacket(SendTradeDone.Fail, Msg.ActionFail);
				activeChar.sendPacket(Msg.CANNOT_SEE_TARGET);
				requestor.sendPacket(Msg.CANNOT_SEE_TARGET);
				return;
			}

			if(!transaction.isTypeOf(TransactionType.TRADE))
			{
				transaction.cancel();
				activeChar.sendPacket(SendTradeDone.Fail, Msg.ActionFail, new SystemMessage("Something wrong. Maybe, cheater?"));
				requestor.sendPacket(SendTradeDone.Fail, Msg.ActionFail, new SystemMessage("Something wrong. Maybe, cheater?"));
				return;
			}

			if(_response == 1)
			{
				if(!requestor.isInActingRange(activeChar))
				{
					activeChar.sendPacket(Msg.YOUR_TARGET_IS_OUT_OF_RANGE);
					return;
				}
				// first party accepted the trade
				// notify clients that "OK" button has been pressed.
				transaction.confirm(activeChar);
				requestor.sendPacket(new SystemMessage(SystemMessage.S1_CONFIRMED_TRADE).addString(activeChar.getName()), Msg.TradePressOtherOk);

				if(!transaction.isConfirmed(activeChar) || !transaction.isConfirmed(requestor))
				{
					activeChar.sendActionFailed();
					return;
				}

				activeChar.getInventory().writeInvLock();
				requestor.getInventory().writeInvLock();
				try
				{
					boolean trade1Valid = L2TradeList.validateTrade(activeChar, transaction.getExchangeList(activeChar), requestor);
					boolean trade2Valid = L2TradeList.validateTrade(requestor, transaction.getExchangeList(requestor), activeChar);

					if(trade1Valid && trade2Valid)
					{
						transaction.tradeItems();
						requestor.sendPacket(Msg.TRADE_HAS_BEEN_SUCCESSFUL, SendTradeDone.Success);
						activeChar.sendPacket(Msg.TRADE_HAS_BEEN_SUCCESSFUL, SendTradeDone.Success);
					}
					else
					{
						activeChar.sendPacket(Msg.THE_ATTEMPT_TO_TRADE_HAS_FAILED, SendTradeDone.Fail);
						requestor.sendPacket(Msg.THE_ATTEMPT_TO_TRADE_HAS_FAILED, SendTradeDone.Fail);
					}
				}
				finally
				{
					requestor.getInventory().writeInvUnlock();
					activeChar.getInventory().writeInvUnlock();
				}
			}
			else
			{
				activeChar.sendPacket(SendTradeDone.Fail);
				requestor.sendPacket(SendTradeDone.Fail, new SystemMessage(SystemMessage.S1_CANCELED_THE_TRADE).addString(activeChar.getName()));
			}

			transaction.cancel();
		}
	}
}