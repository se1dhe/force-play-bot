package l2p.gameserver.clientpackets;

import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.PartyRoom;
import l2p.gameserver.model.base.Transaction;
import l2p.gameserver.model.base.Transaction.TransactionType;

public class AnswerJoinPartyRoom extends L2GameClientPacket
{
	private int _response;

	@Override
	protected void readImpl()
	{
		if(_buf.hasRemaining())
			_response = readD();
		else
			_response = 0;
	}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		Transaction transaction = activeChar.getTransaction();

		if(transaction == null)
			return;

		if(!transaction.isValid() || !transaction.isTypeOf(TransactionType.PARTY_ROOM))
		{
			transaction.cancel();
			activeChar.sendPacket(Msg.TIME_EXPIRED, Msg.ActionFail);
			return;
		}

		if(!transaction.isInProgress())
		{
			transaction.cancel();
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isOutOfControl())
		{
			transaction.cancel();
			activeChar.sendActionFailed();
			return;
		}

		L2Player requestor = transaction.getOtherPlayer(activeChar);

		if(requestor == null)
		{
			transaction.cancel();
			activeChar.sendPacket(Msg.TARGET_IS_NOT_FOUND_IN_THE_GAME);
			activeChar.sendActionFailed();
			return;
		}

		if(_response == 0)
		{
			transaction.cancel();
			requestor.sendPacket(Msg.THE_PLAYER_DECLINED_TO_JOIN_YOUR_PARTY);
			return;
		}

		if(activeChar.getPartyRoom() != null)
		{
			transaction.cancel();
			activeChar.sendActionFailed();
			return;
		}

		try
		{
			PartyRoom room = requestor.getPartyRoom();
			if(room == null)
				return;

			room.addMember(activeChar);
		}
		finally
		{
			transaction.cancel();
		}
	}
}