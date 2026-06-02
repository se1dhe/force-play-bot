package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2World;
import l2p.gameserver.model.PartyRoom;
import l2p.gameserver.model.base.Transaction;
import l2p.gameserver.model.base.Transaction.TransactionType;
import l2p.gameserver.serverpackets.ExAskJoinPartyRoom;
import l2p.gameserver.serverpackets.SystemMessage;

/**
 * format: (ch)S
 */
public class RequestAskJoinPartyRoom extends L2GameClientPacket
{
	private String _name; // not tested, just guessed

	@Override
	protected void readImpl()
	{
		_name = readS();
	}

	@Override
	protected void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;
		L2Player targetPlayer = L2World.getPlayer(_name);

		if(targetPlayer == null || targetPlayer == player)
		{
			player.sendActionFailed();
			return;
		}

		if(player.isInTransaction())
		{
			player.sendPacket(new SystemMessage(SystemMessage.WAITING_FOR_ANOTHER_REPLY));
			return;
		}

		if(targetPlayer.isInTransaction())
		{
			player.sendPacket(new SystemMessage(SystemMessage.S1_IS_BUSY_PLEASE_TRY_AGAIN_LATER).addName(targetPlayer));
			return;
		}

		if(targetPlayer.getPartyRoom() != null)
			return;

		PartyRoom room = player.getPartyRoom();
		if(room == null)
			return;

		if(room.getLeader() != player)
		{
			player.sendPacket(new SystemMessage(SystemMessage.ONLY_A_ROOM_LEADER_MAY_INVITE_OTHERS_TO_A_PARTY_ROOM));
			return;
		}

		if(room.getPlayers().size() >= room.getMaxMembersSize())
		{
			player.sendPacket(new SystemMessage(SystemMessage.THE_PARTY_ROOM_IS_FULL));
			return;
		}

		new Transaction(TransactionType.PARTY_ROOM, player, targetPlayer, 10000L);

		targetPlayer.sendPacket(new ExAskJoinPartyRoom(player.getName(), room.getTopic()));
		player.sendPacket(new SystemMessage(SystemMessage.S1_HAS_INVITED_YOU_TO_ENTER_THE_PARTY_ROOM).addString(targetPlayer.getName()));
	}
}