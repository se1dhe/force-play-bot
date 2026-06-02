package l2p.gameserver.clientpackets;

import l2p.commons.threading.RunnableImpl;
import l2p.gameserver.Config;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.TradeController;
import l2p.gameserver.TradeController.NpcTradeList;
import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.*;
import l2p.gameserver.model.items.Inventory;
import l2p.gameserver.serverpackets.ShopPreviewInfo;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.tables.ItemTable;
import l2p.gameserver.templates.L2Item;

import java.util.HashMap;
import java.util.Map;

public class RequestPreviewItem extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int _unknow;
	private int _listId;
	private int _count;
	private int[] _items;

	@Override
	public void readImpl()
	{
		_unknow = readD();
		_listId = readD();
		_count = readD();
		if(_count * 4 > _buf.remaining() || _count > Short.MAX_VALUE || _count < 1)
		{
			_count = 0;
			return;
		}
		_items = new int[_count];
		for(int i = 0; i < _count; i++)
			_items[i] = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(!Config.WEAR_TEST_ENABLED || _count < 1)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && activeChar.getKarma() > 0)
		{
			activeChar.sendActionFailed();
			return;
		}

		L2NpcInstance npc = activeChar.getLastNpc();

		boolean isValidMerchant = npc instanceof L2ClanHallManagerInstance || npc instanceof L2MerchantInstance || npc instanceof L2MercManagerInstance || npc instanceof L2CastleChamberlainInstance;

		if(npc == null || !isValidMerchant || !npc.isInActingRange(activeChar))
		{
			activeChar.sendActionFailed();
			return;
		}
		NpcTradeList list = TradeController.getInstance().getBuyList(_listId);
		if(list == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		long neededMoney = 0L;
		Map<Integer, Integer> items = new HashMap<Integer, Integer>();
		boolean lrhand = false;
		try
		{
			for(int i = 0; i < _count; i++)
			{
				int itemId = _items[i];
				if(list.getItemByItemId(itemId) == null)
				{
					activeChar.sendActionFailed();
					return;
				}
				L2Item item = ItemTable.getInstance().getTemplate(itemId);
				if(item == null)
					continue;

				if(!item.isEquipable())
					continue;

				int paperdoll = Inventory.getPaperdollIndex(activeChar, item.getBodyPart());
				if(paperdoll < 0)
					continue;
				if(item.getBodyPart() == L2Item.SLOT_LR_HAND) // TODO [V] - что тут
				{
					if(items.containsKey(Inventory.PAPERDOLL_LHAND))
						continue;
					lrhand = true;
				}
				else if(item.getBodyPart() == L2Item.SLOT_L_HAND && lrhand)
					continue;

				if(items.containsKey(paperdoll))
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.THOSE_ITEMS_MAY_NOT_BE_TRIED_ON_SIMULTANEOUSLY));
					activeChar.sendActionFailed();
					return;
				}
				else
					items.put(paperdoll, itemId);

				neededMoney += Config.WEAR_PRICE;
				if(neededMoney > Integer.MAX_VALUE)
				{
					activeChar.sendActionFailed();
					return;
				}
			}
			if(items.isEmpty())
			{
				activeChar.sendActionFailed();
				return;
			}
			int currentMoney = activeChar.getAdena();
			if(neededMoney > currentMoney || neededMoney < 0 || currentMoney <= 0)
			{
				sendPacket(new SystemMessage(SystemMessage.YOU_DO_NOT_HAVE_ENOUGH_ADENA));
				activeChar.sendActionFailed();
				return;
			}
		}
		catch(ArithmeticException ae)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED));
			return;
		}

		activeChar.reduceAdena(neededMoney, true);
		activeChar.sendPacket(new ShopPreviewInfo(items));
		ThreadPoolManager.getInstance().schedule(new RemoveWearItemsTask(activeChar), Config.WEAR_DELAY * 1000);
	}

	private static class RemoveWearItemsTask extends RunnableImpl
	{
		private L2Player player;

		public RemoveWearItemsTask(L2Player activeChar)
		{
			player = activeChar;
		}

		@Override
		public void runImpl() throws Exception
		{
			if(player == null || player.isLogoutStarted())
				return;

			player.sendPacket(new SystemMessage(SystemMessage.TRYING_ON_MODE_HAS_ENDED));
			player.sendUserInfo(true);
		}
	}
}