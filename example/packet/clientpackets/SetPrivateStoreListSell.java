package l2p.gameserver.clientpackets;

import java.util.concurrent.ConcurrentLinkedQueue;

import l2p.commons.lang.reference.HardReference;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.model.L2ObjectsStorage;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2TradeList;
import l2p.gameserver.model.TradeItem;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.serverpackets.SystemMessage;

public class SetPrivateStoreListSell extends L2GameClientPacket
{
	private int _count;
	private boolean _package;
	private int[] _items;

	@Override
	public void readImpl()
	{
		_package = readD() == 1;
		_count = readD();

		if(getClient().isITClient())
		{
			if(_count * 12 > _buf.remaining() || _count > Short.MAX_VALUE || _count <= 0)
			{
				_items = null;
				return;
			}
			_items = new int[_count * 3];
			for(int i = 0; i < _count; i++)
			{
				_items[i * 3] = readD();
				_items[i * 3 + 1] = readD();
				_items[i * 3 + 2] = readD();
				if(_items[i * 3 + 1] < 0)
				{
					_items = null;
					break;
				}
			}
		}
		else
		{
			if(_count * 20 > _buf.remaining() || _count > Short.MAX_VALUE || _count <= 0)
			{
				_items = null;
				return;
			}
			_items = new int[_count * 3];
			for(int i = 0; i < _count; i++)
			{
				_items[i * 3] = readD();
				// TODO [V] - long
				_items[i * 3 + 1] = (int) readQ();
				_items[i * 3 + 2] = (int) readQ();
				if(_items[i * 3 + 1] < 0)
				{
					_items = null;
					break;
				}
			}
		}
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(_items == null || _count <= 0 || activeChar.isMounted())
		{
			L2TradeList.cancelStore(activeChar);
			return;
		}

		if(!activeChar.checksForShop(false))
		{
			L2TradeList.cancelStore(activeChar);
			return;
		}

		int maxSlots = activeChar.getPrivateSellLimit();
		if(_count > maxSlots)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED));
			L2TradeList.cancelStore(activeChar);
			return;
		}

		TradeItem temp;
		ConcurrentLinkedQueue<TradeItem> listsell = new ConcurrentLinkedQueue<TradeItem>();

		activeChar.getInventory().writeInvLock();
		long total = 0;
		try
		{
			for(int x = 0; x < _count; x++)
			{
				int objectId = _items[x * 3];
				long cnt = _items[x * 3 + 1];
				long price = _items[x * 3 + 2];

				L2ItemInstance itemToSell = activeChar.getInventory().getItemByObjectId(objectId);

				if(cnt < 1)
				{
					activeChar.sendMessage("Count of item must be > 0");
					return;
				}
				if(itemToSell == null)
				{
					activeChar.sendMessage("Item in your inventory not found.");
					return;
				}
				if(!itemToSell.canBeTraded(activeChar))
				{
					activeChar.sendMessage("[" + itemToSell.getName() + "] can't be traded.");
					return;
				}
				if(_package)
				{
					total += cnt * price;
					if(total + activeChar.getInventory().getAdena() > Integer.MAX_VALUE)
					{
						activeChar.sendMessage("Adena in your inventory + items price are too high.");
						return;
					}
				}
				else if(cnt * price + activeChar.getInventory().getAdena() > Integer.MAX_VALUE)
				{
					activeChar.sendMessage("Adena in your inventory + [" + itemToSell.getName() + "] price are too high.");
					return;
				}

				// If player sells the enchant scroll he is using, deactivate it
				if(activeChar.getEnchantScroll() != null && itemToSell.getObjectId() == activeChar.getEnchantScroll().getObjectId())
					activeChar.setEnchantScroll(null);

				if(cnt > itemToSell.getIntegerLimitedCount())
					cnt = itemToSell.getIntegerLimitedCount();

				temp = new TradeItem();
				temp.setObjectId(objectId);
				temp.setCount((int) cnt);
				temp.setOwnersPrice((int) price);
				temp.setItemId(itemToSell.getItemId());
				temp.setEnchantLevel(itemToSell.getEnchantLevel());
				listsell.add(temp);
			}
		}
		catch (Exception e)
		{}
		finally
		{
			activeChar.getInventory().writeInvUnlock();
		}

		activeChar.setSellList(listsell);
		activeChar.setPrivateStoreType(_package ? L2Player.STORE_PRIVATE_SELL_PACKAGE : L2Player.STORE_PRIVATE_SELL);
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
				player.broadcastPrivateStoreMsg(2);
				player.broadcastUserInfo(false);
			}
		}, 2500L);
	}
}
