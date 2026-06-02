package l2p.gameserver.clientpackets;

import l2p.gameserver.cache.Msg;
import l2p.gameserver.instancemanager.PartyRoomManager;
import l2p.gameserver.model.L2CommandChannel;
import l2p.gameserver.model.L2Party;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.serverpackets.ListPartyWaiting;

public class RequestPartyMatchConfig extends L2GameClientPacket
{
	private int _page;
	private int _region;
	private int _allLevels;

	@Override
	protected void readImpl()
	{
		_page = readD();
		_region = readD(); // 0 to 15, or -1
		_allLevels = readD(); // 1 -> all levels, 0 -> only levels matching my level
	}

	@Override
	protected void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;

		L2Party party = player.getParty();
		L2CommandChannel channel = party != null ? party.getCommandChannel() : null;

		if(channel != null && !channel.getParties().contains(party))
			player.sendMessage("The Command Channel affiliated party's party member cannot use the matching screen.");
		else if(party != null && !party.isLeader(player))
			player.sendPacket(Msg.THE_LIST_OF_PARTY_ROOMS_CAN_BE_VIEWED_BY_A_PERSON_WHO_HAS_NOT_JOINED_A_PARTY_OR_WHO_IS_A_PARTY_LEADER);
		else
		{
			PartyRoomManager.getInstance().addToWaitingList(player);
			player.sendPacket(new ListPartyWaiting(_region, _allLevels == 1, _page, player));
		}
	}
}