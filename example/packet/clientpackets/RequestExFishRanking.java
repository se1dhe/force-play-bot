package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.instancemanager.FishingChampionShipManager;
import l2p.gameserver.model.L2Player;

public class RequestExFishRanking extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;
		if(Config.ALT_FISH_CHAMPIONSHIP_ENABLED)
			FishingChampionShipManager.getInstance().showMidResult(player);
		else
			player.sendActionFailed();
	}
}