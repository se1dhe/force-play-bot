package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2ObjectsStorage;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.serverpackets.ExMPCCShowPartyMemberInfo;

public class RequestExMPCCShowPartyMembersInfo extends L2GameClientPacket
{
	private int _objectId;

	@Override
	public void readImpl()
	{
		_objectId = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();

		if(activeChar == null)
			return;

		if(!activeChar.isInParty() || !activeChar.getParty().isInCommandChannel())
		{
			activeChar.sendActionFailed();
			return;
		}

		L2Player partyLeader = L2ObjectsStorage.getPlayer(_objectId);
		if(partyLeader != null && partyLeader.getParty() != null)
			activeChar.sendPacket(new ExMPCCShowPartyMemberInfo(partyLeader));
	}
}