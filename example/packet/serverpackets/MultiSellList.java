package l2p.gameserver.serverpackets;

import l2p.gameserver.Config;
import l2p.gameserver.data.xml.holder.MultiSellHolder.MultiSellListContainer;
import l2p.gameserver.model.base.MultiSellEntry;
import l2p.gameserver.model.base.MultiSellIngredient;
import l2p.gameserver.tables.ItemTable;
import l2p.gameserver.templates.L2Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MultiSellList extends L2GameServerPacket
{
	private static final Logger _log = LoggerFactory.getLogger(MultiSellList.class);

	private final int _page;
	private final int _finished;
	private final int _listId;
	private final List<MultiSellEntry> _list;

	public MultiSellList(MultiSellListContainer list, int page, int finished)
	{
		_list = list.getEntries();
		_listId = list.getListId();
		_page = page;
		_finished = finished;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x00); // UNK
		writeD(_listId); // list id
		writeC(0x00); // UNK
		writeD(_page); // page
		writeD(_finished); // finished
		writeD(40); // size of pages
		writeD( _list.size()); //list length
		writeC(0x00); // [TODO]: Grand Crusade
		writeC(0x00);//Type (0x00 - Нормальный, 0xD0 - с шансом)
		writeD(0x00); // UNK
		List<MultiSellIngredient> ingredients;

		int itemId = 0;
		try
		{
			for(MultiSellEntry ent : _list)
			{
				itemId = ent.getProduction().get(0).getItemId();
				ingredients = ent.getIngredients();
				writeD(ent.getEntryId());
				writeC(Config.MULTISELL_PTS && (ent.getProduction().isEmpty() || !ent.getProduction().get(0).isStackable()) ? 0x00 : 0x01);
				writeH(0x00); // enchant level
				writeD(0x00); // инкрустация
				writeD(0x00); // инкрустация

				writeItemElements();

				int saCount = 0;
				writeC(0x00); // SA 1 count
				for(int i = 0; i < saCount; i++)
					writeD(0x00); // SA 1 effect

				writeC(0x00); // SA 2 count
				for(int i = 0; i < saCount; i++)
					writeD(0x00); // SA 2 effect

				writeH(ent.getProduction().size());
				writeH(ingredients.size());

				for(MultiSellIngredient prod : ent.getProduction())
				{
					itemId = prod.getItemId();
					L2Item template = itemId > 0 && itemId < 65336 ? ItemTable.getInstance().getTemplate(itemId) : null;
					writeD(itemId);
					writeQ(template != null ? template.getBodyPart() : 0);
					writeH(template != null ? template.getType2() : 0);
					writeQ(prod.getItemCount());
					writeH(prod.getItemEnchant());
					writeD(100);	// Chance
					writeD(0x00); // augment id 1
					writeD(0x00); // augment id 2
					writeItemElements();

					writeC(0x00); // SA 1 count
					for(int i = 0; i < saCount; i++)
						writeD(0x00); // SA 1 effect

					writeC(0x00); // SA 2 count
					for(int i = 0; i < saCount; i++)
						writeD(0x00); // SA 2 effect
				}

				for(MultiSellIngredient i : ingredients)
				{
					itemId = i.getItemId();
					final L2Item item = itemId > 0 && itemId < 65336 ? ItemTable.getInstance().getTemplate(itemId) : null;
					writeD(itemId); //ID
					writeH(item != null ? item.getType2() : 0xffff);
					writeQ(i.getItemCount()); //Count
					writeH(i.getItemEnchant()); //Enchant Level
					writeD(0x00); // инкрустация
					writeD(0x00); // инкрустация
					writeItemElements();

					writeC(0x00); // SA 1 count
					for(int s = 0; s < saCount; s++)
						writeD(0x00); // SA 1 effect

					writeC(0x00); // SA 2 count
					for(int s = 0; s < saCount; s++)
						writeD(0x00); // SA 2 effect
				}
			}
		}
		catch (Exception e)
		{
			_log.error("Multisell " + _listId + " itemId " + itemId, e);
		}
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_listId); // list id
		writeD(_page); // page
		writeD(_finished); // finished
		writeD(40); // size of pages
		writeD( _list.size()); //list length
		List<MultiSellIngredient> ingredients;

		int itemId = 0;
		try
		{
			for(MultiSellEntry ent : _list)
			{
				itemId = ent.getProduction().get(0).getItemId();
				ingredients = ent.getIngredients();
				writeD(ent.getEntryId());
				writeD(0x00); // инкрустация
				writeD(0x00); // инкрустация
				writeC(Config.MULTISELL_PTS && (ent.getProduction().isEmpty() || !ent.getProduction().get(0).isStackable()) ? 0x00 : 0x01);
				writeH(ent.getProduction().size());
				writeH(ingredients.size());

				for(MultiSellIngredient prod : ent.getProduction())
				{
					itemId = prod.getItemId();
					L2Item template = itemId > 0 && itemId < 65336 ? ItemTable.getInstance().getTemplate(itemId) : null;
					writeH(itemId);
					//System.out.println("Product entryId=" + ent.getEntryId() + " id=" + itemId + " name=" + template.getName() + " enchant=" + prod.getItemEnchant());
					writeD(template != null ? template.getBodyPart() : 0);
					writeH(template != null ? template.getType2ForPackets() : 0);
					writeD(prod.getItemCount());
					writeH(prod.getItemEnchant());
					writeD(0x00); // инкрустация
					writeD(0x00); // инкрустация
				}

				for(MultiSellIngredient i : ingredients)
				{
					itemId = i.getItemId();
					final L2Item item = itemId > 0 && itemId < 65336 ? ItemTable.getInstance().getTemplate(itemId) : null;
					writeH(itemId); //ID
					writeH(item != null ? item.getType2() : 0xffff);
					writeD(i.getItemCount()); //Count
					writeH(i.getItemEnchant()); //Enchant Level
					writeD(0x00); // инкрустация
					writeD(0x00); // инкрустация
				}
			}
		}
		catch (Exception e)
		{
			_log.error("Multisell " + _listId + " itemId " + itemId, e);
		}
	}

	protected void writeItemElements()
	{
		writeH(-1); // attack element (-1 - none)
		writeH(0x00); // attack element value
		writeH(0x00); // водная стихия (fire pdef)
		writeH(0x00); // огненная стихия (water pdef)
		writeH(0x00); // земляная стихия (wind pdef)
		writeH(0x00); // воздушная стихия (earth pdef)
		writeH(0x00); // темная стихия (holy pdef)
		writeH(0x00); // светлая стихия (dark pdef)
	}
}