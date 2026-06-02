package l2p.gameserver.clientpackets;

import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.FriendList;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.base.Transaction;
import l2p.gameserver.model.base.Transaction.TransactionType;
import l2p.gameserver.serverpackets.FriendAddRequestResult;
import l2p.gameserver.serverpackets.L2Friend;
import l2p.gameserver.serverpackets.SystemMessage;

public class RequestFriendAddReply extends L2GameClientPacket
{
	private int _response;

	@Override
	public void readImpl()
	{
		_response = _buf.hasRemaining() ? readD() : 0;
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		Transaction transaction = activeChar.getTransaction();
		if(transaction == null || !transaction.isTypeOf(TransactionType.FRIEND))
			return;

		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(!transaction.isValid())
		{
			transaction.cancel();
			activeChar.sendPacket(Msg.TIME_EXPIRED, Msg.ActionFail);
			return;
		}
		L2Player requestor = transaction.getOtherPlayer(activeChar);
		if(requestor == null)
		{
			transaction.cancel();
			activeChar.sendPacket(Msg.THE_USER_WHO_REQUESTED_TO_BECOME_FRIENDS_IS_NOT_FOUND_IN_THE_GAME);
			activeChar.sendActionFailed();
			return;
		}
		transaction.cancel();
		if(activeChar.getFriendList().getList().containsKey(requestor.getObjectId()))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.S1_IS_ALREADY_ON_YOUR_FRIEND_LIST).addString(requestor.getName()));
			return;
		}
		if(_response == 1)
		{
			requestor.getFriendList().addFriend(activeChar);
			activeChar.getFriendList().addFriend(requestor);
			requestor.sendPacket(Msg.YOU_HAVE_SUCCEEDED_IN_INVITING_A_FRIEND, new SystemMessage(SystemMessage.S1_HAS_BEEN_ADDED_TO_YOUR_FRIEND_LIST).addString(activeChar.getName()), new L2Friend(activeChar, true));
			activeChar.sendPacket(new SystemMessage(SystemMessage.S1_HAS_JOINED_AS_A_FRIEND).addString(requestor.getName()), new L2Friend(requestor, true));

			activeChar.sendPacket(new FriendAddRequestResult(requestor, 1));
			requestor.sendPacket(new FriendAddRequestResult(activeChar, 1));
		}
		else
			requestor.sendPacket(Msg.YOU_HAVE_FAILED_TO_INVITE_A_FRIEND);
	}
}