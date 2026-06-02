package l2p.gameserver.clientpackets;

import java.util.concurrent.ConcurrentLinkedQueue;

import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2TradeList;
import l2p.gameserver.model.TradeItem;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.serverpackets.ExPrivateStoreSellingResult;

public class RequestPrivateStoreBuy extends L2GameClientPacket
{
	private int _sellerID;
	private int _count;
	private int[] _items; // count * 3

	@Override
	public void readImpl()
	{
		_sellerID = readD();
		_count = readD();
		if(_count * (getClient().isITClient() ? 12 : 20) > _buf.remaining() || _count > Short.MAX_VALUE || _count <= 0)
		{
			_items = null;
			return;
		}
		_items = new int[_count * 3];
		for(int i = 0; i < _count; i++)
		{
			_items[i * 3 + 0] = readD(); //object id
			// TODO [V] - long
			_items[i * 3 + 1] = getClient().isITClient() ? readD() : (int) readQ(); //count
			_items[i * 3 + 2] = getClient().isITClient() ? readD() : (int) readQ(); //price
			if(_items[i * 3 + 1] < 0)
			{
				_items = null;
				break;
			}
		}
	}

	@Override
	public void runImpl()
	{
		if(_items == null)
			return;

		L2Player buyer = getClient().getActiveChar();
		if(buyer == null)
			return;

		if(buyer.isActionsDisabled())
		{
			buyer.sendActionFailed();
			return;
		}

		if(!Config.ALLOW_PRIVATE_STORE)
		{
			buyer.sendMessage(buyer.isLangRus() ? "Приватная торговля отключена." : "Private store disabled.");
			return;
		}

		if(buyer.isTradeBannedByGM())
		{
			buyer.sendMessage("You can't use private store.");
			return;
		}

		if(!buyer.getPlayerAccess().UseTrade)
		{
			buyer.sendMessage("You can't use private store.");
			return;
		}

		if(Config.SERVICES_DISABLE_PRIVATE_STORE_BUY && buyer.isTradeKeyBlocked())
		{
			buyer.sendMessage(buyer.isLangRus() ? "Предмет нельзя купить, отключите Lock." : "The item cannot be bought, turn off Lock.");
			buyer.sendActionFailed();
			return;
		}

		ConcurrentLinkedQueue<TradeItem> buyerlist = new ConcurrentLinkedQueue<TradeItem>();

		L2Player seller = (L2Player) buyer.getVisibleObject(_sellerID);

		if(seller == null || seller.getPrivateStoreType() != L2Player.STORE_PRIVATE_SELL && seller.getPrivateStoreType() != L2Player.STORE_PRIVATE_SELL_PACKAGE || !seller.isInActingRange(buyer))
		{
			buyer.sendActionFailed();
			return;
		}

		if(seller.getTradeList() == null)
		{
			L2TradeList.cancelStore(seller);
			return;
		}

		buyer.getInventory().writeInvLock();
		seller.getInventory().writeInvLock();
		try
		{
			ConcurrentLinkedQueue<TradeItem> sellerlist = seller.getSellList();
			int cost = 0;

			if(seller.getPrivateStoreType() == L2Player.STORE_PRIVATE_SELL_PACKAGE)
			{
				buyerlist = new ConcurrentLinkedQueue<TradeItem>();
				buyerlist.addAll(sellerlist);
				for(TradeItem ti : buyerlist)
					cost += ti.getOwnersPrice() * ti.getCount();
			}
			else
				for(int i = 0; i < _count; i++)
				{
					int objectId = _items[i * 3 + 0];
					int count = _items[i * 3 + 1];
					int price = _items[i * 3 + 2];

					for(TradeItem si : sellerlist)
						if(si.getObjectId() == objectId)
						{
							if(count > si.getCount() || price != si.getOwnersPrice())
							{
								buyer.sendActionFailed();
								return;
							}

							L2ItemInstance sellerItem = seller.getInventory().getItemByObjectId(objectId);
							if(sellerItem == null || sellerItem.getIntegerLimitedCount() < count)
							{
								buyer.sendActionFailed();
								return;
							}

							TradeItem temp = new TradeItem();
							temp.setObjectId(si.getObjectId());
							temp.setItemId(sellerItem.getItemId());
							temp.setCount(count);
							temp.setOwnersPrice(si.getOwnersPrice());

							cost += temp.getOwnersPrice() * temp.getCount();
							buyerlist.add(temp);
							seller.sendPacket(new ExPrivateStoreSellingResult(temp.getObjectId(), temp.getCount(), buyer.getName()));
						}
				}

			if(buyer.getAdena() < cost || cost > Integer.MAX_VALUE || cost < 0)
			{
				buyer.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
				buyer.sendActionFailed();
				return;
			}

			seller.getTradeList().buySellItems(buyer, buyerlist, seller, sellerlist);
			buyer.sendChanges();

			seller.saveTradeList();
		}
		finally
		{
			seller.getInventory().writeInvUnlock();
			buyer.getInventory().writeInvUnlock();
		}

		if(seller.getSellList().isEmpty())
			L2TradeList.cancelStore(seller);

		seller.sendChanges();
		buyer.sendActionFailed();
	}
}