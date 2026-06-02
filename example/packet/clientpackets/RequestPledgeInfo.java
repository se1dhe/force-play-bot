package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Clan;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.serverpackets.PledgeInfo;
import l2p.gameserver.tables.ClanTable;

public class RequestPledgeInfo extends L2GameClientPacket
{
	private int _clanId;

	@Override
	public void readImpl()
	{
		_clanId = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(_clanId < 10000000)
		{
			activeChar.sendActionFailed();
			return;
		}
		L2Clan clan = ClanTable.getInstance().getClan(_clanId);
		if(clan == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		activeChar.sendPacket(new PledgeInfo(clan));
	}

	@Override
	public boolean isFilter()
	{
		return false;
	}
}