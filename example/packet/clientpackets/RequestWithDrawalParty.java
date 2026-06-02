package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Party;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.multilang.CustomMessage;

public class RequestWithDrawalParty extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		L2Party party = activeChar.getParty();
		if(party == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		if(party.isInDimensionalRift() && !party.getDimensionalRift().getRevivedAtWaitingRoom().contains(activeChar))
		{
			activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestWithDrawalParty.Rift", activeChar));
			activeChar.sendActionFailed();
			return;
		}
		party.removePartyMember(activeChar, false, true);
	}
}