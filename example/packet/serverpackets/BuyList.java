package l2p.gameserver.serverpackets;

import l2p.commons.util.GArray;
import l2p.gameserver.TradeController.NpcTradeList;
import l2p.gameserver.model.TradeItem;

public class BuyList extends L2GameServerPacket
{
	private int _listId;
	private GArray<TradeItem> _list;
	private int _money;
	private double _TaxRate = 0;

	public BuyList(NpcTradeList list, int currentMoney)
	{
		_listId = list.getListId();
		_list = list.getItems();
		_money = currentMoney;
	}

	public BuyList(NpcTradeList list, int currentMoney, double taxRate)
	{
		_listId = list.getListId();
		_list = list.getItems();
		_money = currentMoney;
		_TaxRate = taxRate;
	}

	@Override
	protected boolean canWrite()
	{
		return false;
	}

	@Override
	protected boolean canWriteIT()
	{
		return true;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_money); // current money
		writeD(_listId);
		if(_list == null)
			writeH(0);
		else
		{
			writeH(_list.size());
			for(TradeItem item : _list)
			{
				writeH(item.getItem().getType1()); // item type1
				writeD(item.getObjectId());
				writeD(item.getItemId());
				writeD(item.getCurrentValue()); // max amount of items that a player can buy at a time (with this itemid)
				writeH(item.getItem().getType2()); // item type2
				writeH(0); // getCustomType1?
				writeD(item.getItem().getBodyPart());
				writeH(item.getEnchantLevel()); // enchant level
				writeH(0); // getCustomType1?
				writeH(0); // unknown
				writeD((int) (item.getOwnersPrice() * (1 + _TaxRate)));
			}
		}
	}
}
