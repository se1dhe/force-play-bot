package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2World;
import l2p.gameserver.model.base.Transaction;
import l2p.gameserver.model.base.Transaction.TransactionType;
import l2p.gameserver.serverpackets.FriendAddRequest;
import l2p.gameserver.serverpackets.SystemMessage;

public class RequestFriendInvite extends L2GameClientPacket
{
	private String _name;

	@Override
	protected void readImpl()
	{
		_name = readS(Config.CNAME_MAXLEN);
	}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isInTransaction())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.WAITING_FOR_ANOTHER_REPLY));
			return;
		}
		L2Player target = L2World.getPlayer(this._name);
		if(target == null || target.isInvisible())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.THE_USER_WHO_REQUESTED_TO_BECOME_FRIENDS_IS_NOT_FOUND_IN_THE_GAME));
			return;
		}
		if(target == activeChar)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_ADD_YOURSELF_TO_YOUR_OWN_FRIEND_LIST));
			return;
		}
		if(target.getMessageRefusal())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.THE_PERSON_IS_IN_A_MESSAGE_REFUSAL_MODE));
			return;
		}
		if(target.isBlockAll())
		{
			activeChar.sendMessage("Your target blocks all.");
			return;
		}
		if(target.isInBlockList(activeChar))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_BEEN_BLOCKED_FROM_THE_CONTACT_YOU_SELECTED));
			return;
		}
		if(target.isInTransaction())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.S1_IS_BUSY_PLEASE_TRY_AGAIN_LATER).addName(target));
			return;
		}
		if(activeChar.getFriendList().getList().containsKey(target.getObjectId()))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.S1_IS_ALREADY_ON_YOUR_FRIEND_LIST).addString(target.getName()));
			return;
		}
		if(activeChar.getFriendList().getList().size() >= Config.MAX_FRIENDS_SIZE)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_MAY_REGISTER_UP_TO_64_PEOPLE_ON_YOUR_LIST));
			return;
		}
		if(target.getFriendList().getList().size() >= Config.MAX_FRIENDS_SIZE)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_BE_REGISTERED_BECAUSE_THE_OTHER_PERSON_HAS_ALREADY_REGISTERED_64_PEOPLE_ON_HIS_HER_LIST));
			return;
		}
		if(target.isInOlympiadMode())
		{
			activeChar.sendMessage("A user currently participating in the Olympiad cannot accept party and friend invitations.");
			return;
		}
		new Transaction(TransactionType.FRIEND, activeChar, target, 10000L);
		activeChar.sendMessage("You've requested " + target.getName() + " to be on your Friends List.");
		target.sendPacket(new SystemMessage(SystemMessage.S1_HAS_REQUESTED_TO_BECOME_FRIENDS).addString(activeChar.getName()), new FriendAddRequest(activeChar.getName()));
	}
}