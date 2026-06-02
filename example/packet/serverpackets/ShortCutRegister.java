package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2ShortCut;
import l2p.gameserver.tables.SkillTree;

public class ShortCutRegister extends L2GameServerPacket
{
	private L2ShortCut sc;

	public ShortCutRegister(L2ShortCut _sc)
	{
		sc = _sc;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(sc.type);
		writeD(sc.slot + sc.page * 12); // номер слота

		switch(sc.type)
		{
			case L2ShortCut.TYPE_ITEM:
				writeD(sc.id); // id скилла или object id вещи
				writeD(1); // неизвестно, если не 1 то черный квадрат
				writeD(-1); // если больше чем -1 то показывать реюз
				writeD(0x00); // unknown
				writeD(0x00); // unknown
				writeD(0x00); // item augment id
				writeD(0x00); // item visual id ?
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
				writeD(-1); // если больше чем -1 то показывать реюз
				writeC(0);
				writeD(1);
				writeD(0x00);
				writeD(0x00);
				break;
			default:
				writeD(sc.id); // id скилла или object id вещи
				writeD(1); // неизвестно, на изменение клиент не реагирует
				break;
		}
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(sc.type);
		writeD(sc.slot + sc.page * 12); // номер слота

		switch(sc.type)
		{
			case L2ShortCut.TYPE_ITEM:
				writeD(sc.id); // id скилла или object id вещи
				writeD(1); // неизвестно, если не 1 то черный квадрат
				writeD(-1); // если больше чем -1 то показывать реюз
				break;
			case L2ShortCut.TYPE_SKILL:
				writeD(sc.id); // id скилла или object id вещи
				writeD(sc.level);
				writeC(0);
				writeD(1);
				break;
			default:
				writeD(sc.id); // id скилла или object id вещи
				writeD(1); // неизвестно, на изменение клиент не реагирует
				break;
		}
	}
}