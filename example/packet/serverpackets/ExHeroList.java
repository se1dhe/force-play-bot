package l2p.gameserver.serverpackets;

import java.util.Map;

import l2p.gameserver.Config;
import l2p.gameserver.model.entity.Hero;
import l2p.gameserver.model.entity.olympiad.Olympiad;
import l2p.gameserver.templates.StatsSet;
import org.apache.commons.lang3.StringUtils;

public class ExHeroList extends L2GameServerPacket
{
	private Map<Integer, StatsSet> _heroList;

	public ExHeroList()
	{
		_heroList = Hero.getInstance().getHeroes();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_heroList.size());
		for(StatsSet hero : _heroList.values())
		{
			writeS(hero.getString(Olympiad.CHAR_NAME));
			writeD(hero.getInteger(Olympiad.CLASS_ID));
			writeS(hero.getString(Hero.CLAN_NAME, StringUtils.EMPTY));
			writeD(hero.getInteger(Hero.CLAN_CREST, 0));
			writeS(hero.getString(Hero.ALLY_NAME, StringUtils.EMPTY));
			writeD(hero.getInteger(Hero.ALLY_CREST, 0));
			writeD(hero.getInteger(Hero.COUNT));
			writeD(Config.REQUEST_ID);
		}
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_heroList.size());
		for(StatsSet hero : _heroList.values())
		{
			writeS(hero.getString(Olympiad.CHAR_NAME));
			writeD(hero.getInteger(Olympiad.CLASS_ID));
			writeS(hero.getString(Hero.CLAN_NAME, StringUtils.EMPTY));
			writeD(hero.getInteger(Hero.CLAN_CREST, 0));
			writeS(hero.getString(Hero.ALLY_NAME, StringUtils.EMPTY));
			writeD(hero.getInteger(Hero.ALLY_CREST, 0));
			writeD(hero.getInteger(Hero.COUNT));
		}
	}
}