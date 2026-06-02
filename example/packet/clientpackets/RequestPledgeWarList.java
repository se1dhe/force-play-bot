package l2p.gameserver.clientpackets;

import l2p.commons.util.GArray;
import l2p.gameserver.model.L2Clan;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.serverpackets.PledgeReceiveWarList;

public final class RequestPledgeWarList extends L2GameClientPacket
{
	// format: (ch)dd
	private int _page;
	private int _tab;

	@Override
	protected void readImpl()
	{
		_page = readD();
		_tab = readD();
	}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		L2Clan clan = activeChar.getClan();
		if(clan == null)
			return;

		GArray<String> list = new GArray<String>();
		for(L2Clan _clan : _tab == 0 ? clan.getEnemyClans() : clan.getAttackerClans())
		{
			if(_clan != null)
				list.add(_clan.getName());
		}

		if(_tab != 0)
			_page = Math.max(0, _page > list.size() / 13 ? 0 : _page);

		activeChar.sendPacket(new PledgeReceiveWarList(list, clan.getAttackerClans(), clan.getEnemyClans(), _tab, _page));
	}
}