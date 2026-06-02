package l2p.gameserver.serverpackets;

import l2p.commons.util.GArray;
import l2p.gameserver.TradeController.NpcTradeList;
import l2p.gameserver.model.TradeItem;

public final class BuyListSeed extends AbstractItemListPacket
{
	private int _manorId;
	private GArray<TradeItem> _list = new GArray<TradeItem>();
	private int _money;

	public BuyListSeed(NpcTradeList list, int manorId, int currentMoney)
	{
		_money = currentMoney;
		_manorId = manorId;
		_list = list.getItems();
	}

	@Override
	protected final void writeImpl()
	{
		writeQ(_money); // current money
		writeD(0x00); // God UNK
		writeD(_manorId); // manor id

		if(_list != null && !_list.isEmpty())
		{
			writeH(_list.size()); // list length
			for(TradeItem item : _list)
			{
				writeItemInfo(item);
				writeQ(item.getOwnersPrice());
			}
		}
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_money); // current money
		writeD(_manorId); // manor id

		if(_list != null && !_list.isEmpty())
		{
			writeH(_list.size()); // list length
			for(TradeItem item : _list)
			{
				writeH(0x04); // item->type1
				writeD(0x00); // objectId
				writeD(item.getItemId()); // item id
				writeD(item.getCount()); // item count
				writeH(0x04); // item->type2
				writeH(0x00); // unknown :)
				writeD(item.getOwnersPrice()); // price
			}
		}
	}
}
