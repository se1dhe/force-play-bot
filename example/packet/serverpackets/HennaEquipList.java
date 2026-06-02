package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2HennaInstance;
import l2p.gameserver.tables.ItemTable;
import l2p.gameserver.templates.L2Item;

import java.util.HashSet;

public class HennaEquipList extends L2GameServerPacket
{
	private int char_adena, HennaEmptySlots;
	private HashSet<L2HennaInstance> availHenna = new HashSet<L2HennaInstance>();
	private boolean _free = false;

	public HennaEquipList(L2Player player, L2HennaInstance[] hennaEquipList)
	{
		char_adena = player.getAdena();
		HennaEmptySlots = player.getHennaEmptySlots();
		if(player.hennaSet)
		{
			_free = true;
			for(L2HennaInstance e : hennaEquipList)
			{
				L2Item item = ItemTable.getInstance().getTemplate(e.getItemIdDye());
				if(item != null && item.getIcon().endsWith("i02"))
					availHenna.add(e);
			}
		}
		else
		{
			for(L2HennaInstance e : hennaEquipList)
				if(player.getInventory().findItemByItemId(e.getItemIdDye()) != null)
					availHenna.add(e);
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeQ(char_adena);
		writeD(HennaEmptySlots);
		if(availHenna.size() != 0)
		{
			writeD(availHenna.size());
			for(L2HennaInstance henna : availHenna)
			{
				writeD(henna.getSymbolId()); //symbolid
				writeD(henna.getItemIdDye()); //itemid of dye
				writeQ(_free ? 0 : henna.getAmountDyeRequire()); //amount of dye require
				writeQ(_free ? 0 : henna.getPrice()); //amount of aden require
				writeD(1); //meet the requirement or not
				//writeD(0x00); //period
			}
		}
		else
		{
			writeD(0x01);
			writeD(0x00);
			writeD(0x00);
			writeQ(0x00);
			writeQ(0x00);
			writeD(0x00);
			//writeD(0x00); //period
		}
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(char_adena);
		writeD(HennaEmptySlots);
		if(availHenna.size() != 0)
		{
			writeD(availHenna.size());
			for(L2HennaInstance henna : availHenna)
			{
				writeD(henna.getSymbolId()); //symbolid
				writeD(henna.getItemIdDye()); //itemid of dye
				writeD(_free ? 0 : henna.getAmountDyeRequire()); //amount of dye require
				writeD(_free ? 0 : henna.getPrice()); //amount of aden require
				writeD(1); //meet the requirement or not
			}
		}
		else
		{
			writeD(0x01);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
		}
	}
}