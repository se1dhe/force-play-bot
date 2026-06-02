package l2p.gameserver.clientpackets;

import l2p.gameserver.cache.Msg;
import l2p.gameserver.data.xml.holder.EventHolder;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.base.Transaction;
import l2p.gameserver.model.base.Transaction.TransactionType;
import l2p.gameserver.model.entity.events.EventType;
import l2p.gameserver.model.entity.events.impl.DuelEvent;
import l2p.gameserver.serverpackets.SystemMessage;

public class RequestDuelAnswerStart extends L2GameClientPacket
{
	private int _response;
	private int _duelType;

	@Override
	protected void readImpl()
	{
		_duelType = readD();
		readD(); // 1 посылается если ниже  -1(при включеной опции клиента Отменять дуели)
		_response = readD();
	}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		Transaction request = activeChar.getTransaction();
		if(request == null)
			return;

		if(!request.isValid() || !request.isTypeOf(TransactionType.DUEL) || !request.isInProgress())
		{
			request.cancel();
			activeChar.sendPacket(Msg.TIME_EXPIRED, Msg.ActionFail);
			return;
		}

		if(activeChar.isActionsDisabled())
		{
			request.cancel();
			activeChar.sendActionFailed();
			return;
		}

		L2Player requestor = request.getRequestor();
		if(requestor == null)
		{
			request.cancel();
			activeChar.sendPacket(Msg.TARGET_IS_NOT_FOUND_IN_THE_GAME);
			activeChar.sendActionFailed();
			return;
		}
		if(requestor.getObjectId() == activeChar.getObjectId())
		{
			request.cancel();
			activeChar.sendActionFailed();
			return;
		}

		DuelEvent duelEvent = EventHolder.getInstance().getEvent(EventType.PVP_EVENT, _duelType);

		switch(_response)
		{
			case 0:
				request.cancel();
				if(_duelType == 1)
					requestor.sendPacket(Msg.THE_OPPOSING_PARTY_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL);
				else
					requestor.sendPacket(new SystemMessage(SystemMessage.S1_HAS_DECLINED_YOUR_CHALLENGE_TO_A_PARTY_DUEL).addName(activeChar));
				break;
			case -1:
				request.cancel();
				requestor.sendMessage(activeChar.getName() + " is set to refuse duel requests and cannot receive a duel request.");
				break;
			case 1:
				if(!duelEvent.canDuel(requestor, activeChar, false))
				{
					request.cancel();
					return;
				}

				SystemMessage msg1, msg2;
				if(_duelType == 1)
				{
					msg1 = new SystemMessage(SystemMessage.YOU_HAVE_ACCEPTED_S1S_CHALLENGE_TO_A_PARTY_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS);
					msg2 = new SystemMessage(SystemMessage.S1_HAS_ACCEPTED_YOUR_CHALLENGE_TO_DUEL_AGAINST_THEIR_PARTY_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS);
				}
				else
				{
					msg1 = new SystemMessage(SystemMessage.YOU_HAVE_ACCEPTED_S1S_CHALLENGE_TO_A_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS);
					msg2 = new SystemMessage(SystemMessage.S1_HAS_ACCEPTED_YOUR_CHALLENGE_TO_A_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS);
				}

				activeChar.sendPacket(msg1.addName(requestor));
				requestor.sendPacket(msg2.addName(activeChar));

				try
				{
					duelEvent.createDuel(requestor, activeChar);
					requestor.setNotChangeSub(true);
					activeChar.setNotChangeSub(true);
				}
				finally
				{
					request.cancel();
				}
				break;
		}
	}
}