package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.data.xml.holder.ResidenceHolder;
import l2p.gameserver.instancemanager.CastleManorManager;
import l2p.gameserver.instancemanager.CastleManorManager.CropProcure;
import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2Manor;
import l2p.gameserver.model.L2Object;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.entity.residence.Castle;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.model.instances.L2ManorManagerInstance;
import l2p.gameserver.serverpackets.StatusUpdate;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.tables.ItemTable;
import l2p.gameserver.templates.L2Item;

/**
 * Format: (ch) d [dddd]
 * d: size
 * [
 * d  obj id
 * d  item id
 * d  manor id
 * d  count
 * ]
 */
public class RequestProcureCropList extends L2GameClientPacket
{
	private int _size;
	private int[] _items; // count*4

	@Override
	protected void readImpl()
	{
		_size = readD();
		if(_size * (getClient().isITClient() ? 16 : 20) > _buf.remaining() || _size > Short.MAX_VALUE || _size <= 0)
		{
			_size = 0;
			return;
		}
		_items = new int[_size * 4];
		for(int i = 0; i < _size; i++)
		{
			int objId = readD();
			_items[i * 4 + 0] = objId;
			int itemId = readD();
			_items[i * 4 + 1] = itemId;
			int manorId = readD();
			_items[i * 4 + 2] = manorId;
			// TODO [V] - long
			int count = getClient().isITClient() ? readD() : (int) readQ();
			_items[i * 4 + 3] = count;
		}
	}

	@Override
	protected void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;

		if(_size < 1)
		{
			player.sendActionFailed();
			return;
		}

		if(!Config.ALLOW_MANOR)
		{
			player.sendMessage("Manor disabled.");
			player.sendActionFailed();
			return;
		}

		if(player.isActionsDisabled())
		{
			player.sendActionFailed();
			return;
		}

		if(player.isInStoreMode())
		{
			player.sendPacket(new SystemMessage(SystemMessage.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM));
			return;
		}

		if(player.isInTrade())
		{
			player.sendActionFailed();
			return;
		}

		L2Object target = player.getTarget();

		L2ManorManagerInstance manor = target != null && target instanceof L2ManorManagerInstance ? (L2ManorManagerInstance) target : null;
		if(manor == null || !manor.isInActingRange(player))
		{
			player.sendActionFailed();
			return;
		}

		int currentManorId = manor == null ? 0 : manor.getCastle().getId();

		// Calculate summary values
		int slots = 0;
		int weight = 0;

		for(int i = 0; i < _size; i++)
		{
			int itemId = _items[i * 4 + 1];
			int manorId = _items[i * 4 + 2];
			int count = _items[i * 4 + 3];

			if(itemId == 0 || manorId == 0 || count == 0)
				continue;
			if(count < 1)
				continue;
			if(count > Integer.MAX_VALUE)
			{
				sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED));
				return;
			}

			Castle castle = ResidenceHolder.getInstance().getResidence(Castle.class, manorId);
			if(castle == null)
				return;

			CropProcure crop = castle.getCrop(itemId, CastleManorManager.PERIOD_CURRENT);
			if(crop == null || crop.getId() == 0 || crop.getPrice() == 0)
				return;

			try
			{
				int rewardItemId = L2Manor.getInstance().getRewardItem(itemId, crop.getReward());
				L2Item template = ItemTable.getInstance().getTemplate(rewardItemId);
				weight += count * template.getWeight();

				if(!template.isStackable())
					slots += count;
				else if(player.getInventory().getItemByItemId(itemId) == null)
					slots++;
			}
			catch(NullPointerException e)
			{
				continue;
			}
		}

		if(!player.getInventory().validateWeight(weight))
		{
			sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
			return;
		}

		if(!player.getInventory().validateCapacity(slots))
		{
			sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
			return;
		}

		// Proceed the purchase
		for(int i = 0; i < _size; i++)
		{
			int objId = _items[i * 4 + 0];
			int cropId = _items[i * 4 + 1];
			int manorId = _items[i * 4 + 2];
			int count = _items[i * 4 + 3];

			if(objId == 0 || cropId == 0 || manorId == 0 || count == 0)
				continue;

			if(count < 1)
				continue;

			Castle castle = ResidenceHolder.getInstance().getResidence(Castle.class, manorId);
			if(castle == null)
				continue;

			CropProcure crop = castle.getCrop(cropId, CastleManorManager.PERIOD_CURRENT);
			if(crop == null || crop.getId() == 0 || crop.getPrice() == 0)
				continue;

			long fee = 0; // fee for selling to other manors

			int rewardItem = L2Manor.getInstance().getRewardItem(cropId, crop.getReward());

			if(count > crop.getAmount())
				continue;

			long sellPrice = count * crop.getPrice();
			long rewardPrice = ItemTable.getInstance().getTemplate(rewardItem).getReferencePrice();

			if(rewardPrice == 0)
				continue;

			long rewardItemCount = sellPrice / rewardPrice;
			if(rewardItemCount < 1)
			{
				SystemMessage sm = new SystemMessage(SystemMessage.FAILED_IN_TRADING_S2_OF_CROP_S1);
				sm.addItemName(cropId);
				sm.addNumber(count);
				player.sendPacket(sm);
				continue;
			}

			if(manorId != currentManorId)
				fee = sellPrice * 5 / 100; // 5% fee for selling to other manor

			if(player.getInventory().getAdena() < fee)
			{
				SystemMessage sm = new SystemMessage(SystemMessage.FAILED_IN_TRADING_S2_OF_CROP_S1);
				sm.addItemName(cropId);
				sm.addNumber(count);
				player.sendPacket(sm);
				player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
				continue;
			}

			// Add item to Inventory and adjust update packet
			L2ItemInstance itemDel = null;
			L2ItemInstance itemAdd = null;
			if(player.getInventory().getItemByObjectId(objId) == null)
				continue;

			// check if player have correct items count
			L2ItemInstance item = player.getInventory().getItemByObjectId(objId);
			if(item.getIntegerLimitedCount() < count)
				continue;

			itemDel = player.getInventory().destroyItem(objId, count, true);
			if(itemDel == null)
				continue;

			if(fee > 0)
				player.getInventory().reduceAdena(fee);
			crop.setAmount(crop.getAmount() - count);
			if(Config.MANOR_SAVE_ALL_ACTIONS)
				ResidenceHolder.getInstance().getResidence(Castle.class, manorId).updateCrop(crop.getId(), crop.getAmount(), CastleManorManager.PERIOD_CURRENT);

			itemAdd = player.getInventory().addItem(rewardItem, rewardItemCount, "<CropList>");
			if(itemAdd == null)
				continue;

			// Send System Messages
			SystemMessage sm = new SystemMessage(SystemMessage.TRADED_S2_OF_CROP_S1);
			sm.addItemName(cropId);
			sm.addNumber(count);
			player.sendPacket(sm);

			if(fee > 0)
			{
				sm = new SystemMessage(SystemMessage.S1_ADENA_HAS_BEEN_PAID_FOR_PURCHASING_FEES);
				sm.addNumber(fee);
				player.sendPacket(sm);
			}

			sm = new SystemMessage(SystemMessage.S2_S1_HAS_DISAPPEARED);
			sm.addItemName(cropId);
			sm.addNumber(count);
			player.sendPacket(sm);

			if(fee > 0)
			{
				sm = new SystemMessage(SystemMessage.S1_ADENA_DISAPPEARED);
				sm.addNumber(fee);
				player.sendPacket(sm);
			}

			sm = new SystemMessage(SystemMessage.EARNED_S2_S1_s);
			sm.addItemName(rewardItem);
			sm.addNumber(rewardItemCount);
			player.sendPacket(sm);
		}

		StatusUpdate su = new StatusUpdate(player);
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
	}
}