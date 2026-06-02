package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2TradeList;
import l2p.gameserver.model.TradeItem;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.model.items.ItemInfo;
import l2p.gameserver.templates.L2Item;

import java.util.Collection;

public class PrivateStoreBuyManageList extends AbstractItemListPacket
{
	private int _type;
	private Collection<TradeItem> buylist;
	private int buyer_id, buyer_adena;
	private L2TradeList _list;

	/**
	 * Окно управления личным магазином продажи
	 * @param buyer
	 */
	public PrivateStoreBuyManageList(int type, L2Player buyer)
	{
		_type = type;
		buyer_id = buyer.getObjectId();
		buyer_adena = buyer.getAdena();

		buylist = buyer.getBuyList();

		_list = new L2TradeList(0);
		for(L2ItemInstance item : buyer.getInventory().getItems())
			if(item != null && item.canBeTraded(buyer) && item.getItem().getType2() != L2Item.TYPE2_MONEY)
			{
				for(TradeItem ti : buyer.getBuyList())
					if(ti.getItemId() == item.getItemId() && ti.getEnchantLevel() == item.getEnchantLevel())
						continue;
				_list.addItem(item);
			}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(_type);
		if(_type == 1)
		{
			writeD(buyer_id);
			writeQ(buyer_adena);
			writeD(buylist.size());//count for any items already added for sell
			for(TradeItem bi : buylist)
			{
				writeItemInfo(bi);
				writeQ(bi.getOwnersPrice());
				writeQ(bi.getStorePrice());
				writeQ(bi.getCount());
			}
			writeD(_list.getItems().size());
		}
		else if(_type == 2)
		{
			writeD(_list.getItems().size());
			writeD(_list.getItems().size());//for potential sells
			for(L2ItemInstance temp : _list.getItems())
			{
				writeItemInfo(new ItemInfo(temp, false));
				writeQ(temp.getReferencePrice());
			}
		}
	}

	@Override
	protected boolean canWriteIT()
	{
		return _type == 2;
	}

	@Override
	protected final void writeImplIT()
	{
		//section 1
		writeD(buyer_id);
		writeD(buyer_adena);

		//section2
		writeD(_list.getItems().size());//for potential sells
		for(L2ItemInstance temp : _list.getItems())
		{
			writeD(temp.getItemId());
			writeH(temp.getEnchantLevel()); //show enchant lvl as 0, as you can't buy enchanted weapons
			writeD(temp.getIntegerLimitedCount());
			writeD(temp.getReferencePrice());
			writeH(0);
			writeD(temp.getBodyPart());
			writeH(temp.getItem().getType2());
		}

		//section 3
		writeD(buylist.size());//count for any items already added for sell
		for(TradeItem e : buylist)
		{
			writeD(e.getItemId());
			writeH(e.getEnchantLevel());
			writeD(e.getCount());
			writeD(e.getStorePrice());
			writeH(0);
			writeD(e.getItem().getBodyPart());
			writeH(e.getItem().getType2());
			writeD(e.getOwnersPrice());//your price
			writeD(e.getStorePrice());//fixed store price
		}
	}

	static class BuyItemInfo
	{
		public int _id, count, store_price, body_part, type2, owner_price, enchant;

		public BuyItemInfo(int __id, int _count, int _store_price, int _body_part, int _type2, int _owner_price, int _enchant)
		{
			_id = __id;
			count = _count;
			store_price = _store_price;
			body_part = _body_part;
			type2 = _type2;
			owner_price = _owner_price;
			enchant = _enchant;
		}
	}
}