package l2p.gameserver.serverpackets;

import java.util.Collection;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2ShortCut;
import l2p.gameserver.tables.SkillTree;

public class ShortCutInit extends L2GameServerPacket
{
	private Collection<L2ShortCut> _shortCuts;

	public ShortCutInit(L2Player pl)
	{
		_shortCuts = pl.getAllShortCuts();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_shortCuts.size());

		for(final L2ShortCut sc : _shortCuts)
		{
			writeD(sc.type);
			writeD(sc.slot + sc.page * 12);

			switch(sc.type)
			{
				case L2ShortCut.TYPE_ITEM:
					writeD(sc.id);
					writeD(1); // неизвестно, если не 1 то черный квадрат
					writeD(-1); // если больше чем -1 то показывать реюз 
					writeD(0); // оставшееся время реюза в секундах
					writeD(0); // реюз в секундах
					writeD(0); // неизвестно, на изменение клиент не реагирует
					writeD(0); // неизвестно, на изменение клиент не реагирует
					writeD(0x00); // Visual id
					break;
				case L2ShortCut.TYPE_SKILL:
					if(sc.level < 100)
					{
						writeD(sc.id); // id скилла или object id вещи
						writeH(sc.level);
						writeH(sc.subLevel);
					}
					else
					{
						int baseLevel = SkillTree.getBaseLevels().get(sc.id);
						int step = sc.level % 100;
						int subLevel = (1 + step / 40) * 1000 + step % 40;

						writeD(sc.id); // id скилла или object id вещи
						writeH(baseLevel);
						writeH(subLevel);
					}
					writeD(-1);
					writeC(0); // неизвестно, на изменение клиент не реагирует
					writeD(1); // неизвестно, на изменение клиент не реагирует
					break;
				default:
					writeD(sc.id);
					writeD(1); // неизвестно, на изменение клиент не реагирует
					break;
			}
		}
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_shortCuts.size());

		for(final L2ShortCut sc : _shortCuts)
		{
			writeD(sc.type);
			writeD(sc.slot + sc.page * 12);

			switch(sc.type)
			{
				case L2ShortCut.TYPE_ITEM:
					writeD(sc.id);
					writeD(1); // неизвестно, если не 1 то черный квадрат
					writeD(-1); // если больше чем -1 то показывать реюз
					writeD(0); // оставшееся время реюза в секундах
					writeD(0); // реюз в секундах
					writeH(0); // неизвестно, на изменение клиент не реагирует
					writeH(0); // неизвестно, на изменение клиент не реагирует
					break;
				case L2ShortCut.TYPE_SKILL:
					writeD(sc.id);
					writeD(sc.level);
					writeC(0); // неизвестно, на изменение клиент не реагирует
					writeD(1); // неизвестно, на изменение клиент не реагирует
					break;
				default:
					writeD(sc.id);
					writeD(1); // неизвестно, на изменение клиент не реагирует
					break;
			}
		}
	}
}