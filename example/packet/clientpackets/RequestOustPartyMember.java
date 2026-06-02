package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Party;
import l2p.gameserver.model.L2Player;

public class RequestOustPartyMember extends L2GameClientPacket
{
	private String _name;

	@Override
	protected void readImpl()
	{
		_name = readS(16);
	}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		L2Party party = activeChar.getParty();
		if(party == null || !party.isLeader(activeChar))
		{
			activeChar.sendActionFailed();
			return;
		}
		L2Player member = party.getPlayerByName(_name);
		if(member == null)
		{
			activeChar.sendMessage("Player " + _name + " not in your party.");
			activeChar.sendActionFailed();
			return;
		}
		if(member.getObjectId() == activeChar.getObjectId())
		{
			activeChar.sendMessage("You can't dismiss yourself.");
			activeChar.sendActionFailed();
			return;
		}

		party.removePartyMember(member, true, false);
	}
}