package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.serverpackets.SystemMessage;

public class RequestChangePartyLeader extends L2GameClientPacket
{
	private String _name;

	@Override
	public void readImpl()
	{
		_name = readS(Config.CNAME_MAXLEN);
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(!activeChar.isInParty())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(!activeChar.getParty().isLeader(activeChar))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.ONLY_A_PARTY_LEADER_CAN_TRANSFER_ONES_RIGHTS_TO_ANOTHER_PLAYER));
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.getName().equalsIgnoreCase(_name))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_TRANSFER_RIGHTS_TO_YOURSELF));
			activeChar.sendActionFailed();
			return;
		}
		activeChar.getParty().changePartyLeader(_name);
	}
}