package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.entity.olympiad.Olympiad;

public class RequestOlympiadMatchList extends L2GameClientPacket
{
	@Override
	public void readImpl()
	{}

	@Override
	public void runImpl()
	{
		L2Player player = getClient().getActiveChar();

		if(!player.isInOlympiadObserverMode() || player.getOlympiadObserveId() > 100)
		{
			player.sendActionFailed();
			return;
		}

		Olympiad.showCompetitionList(player);
	}
}