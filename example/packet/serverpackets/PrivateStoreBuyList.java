package l2p.gameserver.serverpackets;

import java.util.concurrent.ConcurrentLinkedQueue;

import l2p.commons.util.GArray;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.TradeItem;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.tables.ItemTable;
import l2p.gameserver.templates.L2Item;

public class PrivateStoreBuyList extends AbstractItemListPacket
{
	private int buyer_id, seller_adena;
	private ConcurrentLinkedQueue<TradeItem> _buyerslist;

	/**
	 * Список вещей в личном магазине покупки, показываемый продающему
	 * @param seller
	 * @param storePlayer
	 */
	@SuppressWarnings("unchecked")
	public PrivateStoreBuyList(L2Player seller, L2Player storePlayer)
	{
		seller_adena = seller.getAdena();
		buyer_id = storePlayer.getObjectId();

		ConcurrentLinkedQueue<L2ItemInstance> sellerItems = seller.getInventory().getItemsList();
		_buyerslist = new ConcurrentLinkedQueue<TradeItem>();
		_buyerslist.addAll(storePlayer.getBuyList());

		GArray<Integer> ids = new GArray<Integer>();
		for(TradeItem buyListItem : _buyerslist)
		{
			buyListItem.setCurrentValue(0);
			for(L2ItemInstance sellerItem : sellerItems)
				if(sellerItem.getItemId() == buyListItem.getItemId() && !ids.contains(sellerItem.getObjectId()) && sellerItem.canBeTraded(seller))
				{
					buyListItem.setCurrentValue(Math.min(buyListItem.getCount(), sellerItem.getIntegerLimitedCount()));
					ids.add(sellerItem.getObjectId());
					break;
				}
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeD(buyer_id);
		writeQ(seller_adena);
		writeD(0);

		writeD(_buyerslist.size());

		for(TradeItem buyersitem : _buyerslist)
		{
			writeItemInfo(buyersitem, buyersitem.getCurrentValue());
			writeD(buyersitem.getObjectId());
			writeQ(buyersitem.getOwnersPrice());
			writeQ(buyersitem.getStorePrice());
			writeQ(buyersitem.getCount()); // maximum possible tradecount
		}
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(buyer_id);
		writeD(seller_adena);

		writeD(_buyerslist.size());

		for(TradeItem buyersitem : _buyerslist)
		{
			L2Item tmp = ItemTable.getInstance().getTemplate(buyersitem.getItemId());
			writeD(buyersitem.getObjectId());
			writeD(buyersitem.getItemId());
			writeH(buyersitem.getEnchantLevel());
			writeD(buyersitem.getCurrentValue());

			writeD(tmp.getReferencePrice());
			writeH(0);

			writeD(tmp.getBodyPart());
			writeH(tmp.getType2ForPackets());
			writeD(buyersitem.getOwnersPrice());

			writeD(buyersitem.getCount());
		}
	}
}