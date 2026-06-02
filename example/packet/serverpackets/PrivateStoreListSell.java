package l2p.gameserver.serverpackets;

import java.util.concurrent.ConcurrentLinkedQueue;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.TradeItem;
import l2p.gameserver.tables.ItemTable;
import l2p.gameserver.templates.L2Item;

public class PrivateStoreListSell extends AbstractItemListPacket
{
	private int seller_id;
	private int buyer_adena;
	private final boolean _package;
	private ConcurrentLinkedQueue<TradeItem> _sellList;

	/**
	 * Список вещей в личном магазине продажи, показываемый покупателю
	 * @param buyer
	 * @param seller
	 */
	public PrivateStoreListSell(L2Player buyer, L2Player seller)
	{
		seller_id = seller.getObjectId();
		buyer_adena = buyer.getAdena();
		_package = seller.getPrivateStoreType() == L2Player.STORE_PRIVATE_SELL_PACKAGE;
		_sellList = seller.getSellList();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(seller_id);
		writeD(_package ? 1 : 0);
		writeQ(buyer_adena);
		writeD(0x00); //TODO: [Bonux] Количество свободных ячеек в инвентаре.
		writeD(_sellList.size());
		for(TradeItem si : _sellList)
		{
			writeItemInfo(si);
			writeQ(si.getOwnersPrice());
			writeQ(si.getStorePrice());
		}
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(seller_id);
		writeD(_package ? 1 : 0);
		writeD(buyer_adena);

		writeD(_sellList.size());
		for(TradeItem ti : _sellList)
		{
			L2Item tempItem = ItemTable.getInstance().getTemplate(ti.getItemId());
			writeD(tempItem.getType2ForPackets());
			writeD(ti.getObjectId());
			writeD(ti.getItemId());
			writeD(ti.getCount());
			writeH(0);
			writeH(ti.getEnchantLevel());
			writeH(0x00);
			writeD(tempItem.getBodyPart());
			writeD(ti.getOwnersPrice());
			writeD(ti.getStorePrice());
		}
	}
}