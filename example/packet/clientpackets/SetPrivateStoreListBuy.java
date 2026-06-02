package l2p.gameserver.clientpackets;

import java.util.concurrent.ConcurrentLinkedQueue;

import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.model.L2ObjectsStorage;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2TradeList;
import l2p.gameserver.model.TradeItem;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.templates.L2Item;

public class SetPrivateStoreListBuy extends L2GameClientPacket
{
	// format: cdb, b - array of (dhhdd)
	private int _count;
	private int[] _items; // count * 3

	@Override
	public void readImpl()
	{
		_count = readD();
		if(getClient().isITClient())
		{
			if(_count * 12 > _buf.remaining() || _count > Short.MAX_VALUE || _count <= 0)
			{
				_items = null;
				return;
			}
			_items = new int[_count * 4];
			for(int i = 0; i < _count; i++)
			{
				_items[i * 4 + 0] = readD(); //item id
				_items[i * 4 + 3] = readH();
				readH();// don't know what this is
				_items[i * 4 + 1] = readD(); //count
				_items[i * 4 + 2] = readD(); //price
				if(_items[i * 4 + 1] < 0)
				{
					_items = null;
					break;
				}
			}
		}
		else
		{
			if(_count * 54 > _buf.remaining() || _count > Short.MAX_VALUE || _count <= 0)
			{
				_items = null;
				return;
			}
			_items = new int[_count * 4];
			for(int i = 0; i < _count; i++)
			{
				_items[i * 4 + 0] = readD(); //item id
				_items[i * 4 + 3] = readH();
				readH();// don't know what this is
				// TODO [V] - long
				_items[i * 4 + 1] = (int) readQ(); //count
				_items[i * 4 + 2] = (int) readQ(); //price
				if(_items[i * 4 + 1] < 0)
				{
					_items = null;
					break;
				}

				//TODO [V] - нужно это?
				readD(); // option1
				readD(); // option2
				// Attributes
				readH();
				readH();
				readH();
				readH();
				readH();
				readH();
				readH();
				readH();

				readD(); // visualId

				int saCount = readC();
				for(int s = 0; s < saCount; s++)
					readD();

				saCount = readC();
				for(int s = 0; s < saCount; s++)
					readD();
			}
		}
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(_items == null)
		{
			L2TradeList.cancelStore(activeChar);
			return;
		}

		if(!activeChar.checksForShop(false))
		{
			L2TradeList.cancelStore(activeChar);
			return;
		}

		int maxSlots = activeChar.getPrivateBuyLimit();
		if(_count > maxSlots)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED));
			L2TradeList.cancelStore(activeChar);
			return;
		}

		TradeItem temp;
		ConcurrentLinkedQueue<TradeItem> listbuy = new ConcurrentLinkedQueue<TradeItem>();
		int totalCost = 0;
		int slots = 0;
		long weight = 0;

		for(int x = 0; x < _count; x++)
		{
			if(_items[x * 4 + 1] < 1)
			{
				_count--;
				continue;
			}
			temp = new TradeItem();
			temp.setItemId(_items[x * 4 + 0]);
			temp.setCount(_items[x * 4 + 1]);
			temp.setOwnersPrice(_items[x * 4 + 2]);
			temp.setEnchantLevel(_items[x * 4 + 3]);
			totalCost += temp.getOwnersPrice() * temp.getCount();
			if(temp.getOwnersPrice() < 0 || temp.getCount() < 0)
			{
				L2TradeList.cancelStore(activeChar);
				return;
			}
			L2Item item = temp.getItem();
			weight += item.getWeight();
			if(!item.isStackable() || activeChar.getInventory().getItemByItemId(item.getItemId()) == null)
				slots++;
			listbuy.add(temp);
		}

		if(weight < 0)
		{
			activeChar.sendMessage("Wrong weight!");
			activeChar.sendActionFailed();
			L2TradeList.cancelStore(activeChar);
			return;
		}

		if(totalCost < 1)
		{
			activeChar.sendMessage("Wrong price!");
			activeChar.sendActionFailed();
			L2TradeList.cancelStore(activeChar);
			return;
		}

		if(totalCost > activeChar.getAdena())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.THE_PURCHASE_PRICE_IS_HIGHER_THAN_THE_AMOUNT_OF_MONEY_THAT_YOU_HAVE_AND_SO_YOU_CANNOT_OPEN_A_PERSONAL_STORE));
			L2TradeList.cancelStore(activeChar);
			return;
		}

		if(!activeChar.getInventory().validateWeight(weight) || !activeChar.getInventory().validateCapacity(slots))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.THE_WEIGHT_AND_VOLUME_LIMIT_OF_INVENTORY_MUST_NOT_BE_EXCEEDED));
			L2TradeList.cancelStore(activeChar);
			return;
		}

		if(_count > 0)
		{
			activeChar.setBuyList(listbuy);
			activeChar.setPrivateStoreType(L2Player.STORE_PRIVATE_BUY);
			activeChar.sitDown(0);
			final long id = activeChar.getStoredId();
			ThreadPoolManager.getInstance().schedule(new Runnable()
			{
				@Override
				public void run()
				{
					L2Player player = L2ObjectsStorage.getAsPlayer(id);
					if(player == null)
						return;
					player.broadcastPrivateStoreMsg(1);
					player.broadcastUserInfo(false);
				}
			}, 2500L);
		}
		else
			L2TradeList.cancelStore(activeChar);
	}
}