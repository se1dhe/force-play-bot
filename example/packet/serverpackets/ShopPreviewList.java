package l2p.gameserver.serverpackets;

import l2p.commons.util.GArray;
import l2p.gameserver.Config;
import l2p.gameserver.TradeController.NpcTradeList;
import l2p.gameserver.model.TradeItem;
import l2p.gameserver.templates.L2Item;

public class ShopPreviewList extends L2GameServerPacket
{
	private int _listId;
	private TradeItem[] _list;
	private int _money;
	private int _expertise;

	public ShopPreviewList(NpcTradeList list, int currentMoney, int expertiseIndex)
	{
		_listId = list.getListId();
		GArray<TradeItem> lst = list.getItems();
		_list = lst.toArray(new TradeItem[lst.size()]);
		_money = currentMoney;
		_expertise = expertiseIndex;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(0x13c0); //?
		writeQ(_money);
		writeD(_listId);

		int newlength = 0;
		for(TradeItem item : _list)
			if(item.getItem().getCrystalType() <= _expertise && item.getItem().isEquipable())
				newlength++;
		writeH(newlength);

		for(TradeItem item : _list)
			if(item.getItem().getCrystalType() <= _expertise && item.getItem().isEquipable())
			{
				writeD(item.getItemId());
				writeH(item.getItem().getType2()); // item type2
				writeQ(item.getItem().getType1() != L2Item.TYPE1_ITEM_QUESTITEM_ADENA ? item.getItem().getBodyPart() : 0x00); // rev 415  slot    0006-lr.ear  0008-neck  0030-lr.finger  0040-head  0080-??  0100-l.hand  0200-gloves  0400-chest  0800-pants  1000-feet  2000-??  4000-r.hand  8000-r.hand
				writeQ(Config.WEAR_PRICE);
			}
	}

	@Override
	protected final void writeImplIT()
	{
		writeC(0xC0);
		writeC(0x13);
		writeC(0x00);
		writeC(0x00);
		writeD(_money);
		writeD(_listId);

		int newlength = 0;
		for(TradeItem item : _list)
			if(item.getItem().getCrystalType() <= _expertise && item.getItem().isEquipable())
				newlength++;
		writeH(newlength);

		for(TradeItem item : _list)
			if(item.getItem().getCrystalType() <= _expertise && item.getItem().isEquipable())
			{
				writeD(item.getItemId());
				writeH(item.getItem().getType2()); // item type2
				writeH(item.getItem().getType1() != L2Item.TYPE1_ITEM_QUESTITEM_ADENA ? item.getItem().getBodyPart() : 0x00); // rev 415  slot    0006-lr.ear  0008-neck  0030-lr.finger  0040-head  0080-??  0100-l.hand  0200-gloves  0400-chest  0800-pants  1000-feet  2000-??  4000-r.hand  8000-r.hand
				writeD(Config.WEAR_PRICE);
			}
	}
}