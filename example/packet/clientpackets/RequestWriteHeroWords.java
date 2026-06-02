package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.entity.Hero;

public class RequestWriteHeroWords extends L2GameClientPacket
{
	private String _heroWords;

	@Override
	public void readImpl()
	{
		_heroWords = readS();
	}

	@Override
	public void runImpl()
	{
		final L2Player player = getClient().getActiveChar();
		if(player == null || !player.isHero())
			return;

		if(_heroWords == null || _heroWords.length() > 300)
			return;

		Hero.getInstance().setHeroMessage(player.getObjectId(), _heroWords);
	}
}