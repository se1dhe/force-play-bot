package l2p.gameserver.serverpackets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import l2p.commons.util.GArray;
import l2p.gameserver.Config;
import l2p.gameserver.TradeController;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.TradeItem;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.network.ServerPacketOpcodes;

public abstract class ExBuySellList extends AbstractItemListPacket
{
	@Override
	protected ServerPacketOpcodes getOpcodes()
	{
		return ServerPacketOpcodes.ExBuySellList;
	}

	public static class BuyList extends ExBuySellList
	{
		private final int _listId;
		private final GArray<TradeItem> _buyList;
		private final long _adena;
		private final double _taxRate;
		private final int _inventoryUsedSlots;

		public BuyList(TradeController.NpcTradeList tradeList, L2Player activeChar, double taxRate)
		{
			_adena = activeChar.getAdena();
			_taxRate = taxRate;
			_inventoryUsedSlots = activeChar.getInventory().getSize();

			if(tradeList != null)
			{
				_listId = tradeList.getListId();
				_buyList = tradeList.getItems();
				activeChar.setBuyListId(_listId);
			}
			else
			{
				_listId = 0;
				_buyList = TradeController.NpcTradeList.emptyList();
				activeChar.setBuyListId(0);
			}
		}

		@Override
		protected void writeImpl()
		{
			writeD(0x00); // BUY LIST TYPE
			writeQ(_adena); // current money
			writeD(_listId);
			writeD(_inventoryUsedSlots); //TODO [Bonux] Awakening
			writeH(_buyList.size());
			for(TradeItem item : _buyList)
			{
				writeItemInfo(item, item.getCurrentValue());
				writeQ((long) (item.getOwnersPrice() * (1. + _taxRate)));
			}
		}

		@Override
		protected boolean canWriteIT()
		{
			return false;
		}
	}

	public static class SellRefundList extends ExBuySellList
	{
		private final List<TradeItem> _sellList;
		private final List<TradeItem> _refundList;
		private final int _inventoryUsedSlots;
		private int _done;

		public SellRefundList(L2Player activeChar, boolean done)
		{
			_done = done ? 1 : 0;
			_inventoryUsedSlots = activeChar.getInventory().getSize();

			if(done)
			{
				_refundList = Collections.emptyList();
				_sellList = Collections.emptyList();
			}
			else
			{
				L2ItemInstance[] items = activeChar.getInventory().getRefundItemsList();
				_refundList = new ArrayList<TradeItem>(items.length);
				for(L2ItemInstance item : items)
					_refundList.add(new TradeItem(item));

				items = activeChar.getInventory().getItems();
				_sellList = new ArrayList<TradeItem>(items.length);
				for(L2ItemInstance item : items)
					//if(item.canBeSold(activeChar) && !item.isEquipped()) // TODO [V] - так?
					if(item.getItem().isSellable() && item.canBeTraded(activeChar))
						_sellList.add(new TradeItem(item));
			}
		}

		@Override
		protected void writeImpl()
		{
			writeD(0x01); // SELL/REFUND LIST TYPE
			writeD(_inventoryUsedSlots); //TODO [Bonux] Awakening
			writeH(_sellList.size());
			for(TradeItem item : _sellList)
			{
				writeItemInfo(item);
				writeQ((int)(item.getItem().getReferencePrice() * Config.SELL_MOD));
			}
			writeH(_refundList.size());
			for(TradeItem item : _refundList)
			{
				writeItemInfo(item);
				writeD(item.getObjectId());
				writeQ((int) (item.getCount() * item.getItem().getReferencePrice() * Config.SELL_MOD));
			}
			writeC(_done);
		}

		@Override
		protected boolean canWriteIT()
		{
			return false;
		}
	}
}