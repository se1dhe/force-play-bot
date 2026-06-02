package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.data.xml.holder.ResidenceHolder;
import l2p.gameserver.instancemanager.CastleManorManager;
import l2p.gameserver.instancemanager.CastleManorManager.SeedProduction;
import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2Object;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.entity.residence.Castle;
import l2p.gameserver.model.instances.L2ManorManagerInstance;
import l2p.gameserver.serverpackets.StatusUpdate;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.tables.ItemTable;
import l2p.gameserver.templates.L2Item;

public class RequestBuySeed extends L2GameClientPacket
{
	private int _count;
	private int _manorId;

	private int[] _items; // size _count * 2

	@Override
	protected void readImpl()
	{
		_manorId = readD();
		_count = readD();

		if(_count > Short.MAX_VALUE || _count <= 0 || _count * (getClient().isITClient() ? 8 : 12) < _buf.remaining())
		{
			_count = 0;
			return;
		}

		_items = new int[_count * 2];

		for(int i = 0; i < _count; i++)
		{
			int itemId = readD();
			_items[i * 2 + 0] = itemId;
			long cnt;
			if(getClient().isITClient())
				cnt = readD();
			else
				cnt = readQ();
			if(cnt > Integer.MAX_VALUE || cnt < 1)
			{
				_count = 0;
				_items = null;
				return;
			}
			_items[i * 2 + 1] = (int) cnt;
		}
	}

	@Override
	protected void runImpl()
	{
		long totalPrice = 0;
		int slots = 0;
		int totalWeight = 0;

		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;

		if(_count < 1)
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

		if(player.isFishing())
		{
			player.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_DO_THAT_WHILE_FISHING));
			return;
		}

		L2Object target = player.getTarget();

		L2ManorManagerInstance manor = target != null && (target instanceof L2ManorManagerInstance) ? (L2ManorManagerInstance) target : null;
		if(manor == null || !manor.isInActingRange(player))
		{
			player.sendActionFailed();
			return;
		}

		Castle castle = ResidenceHolder.getInstance().getResidence(Castle.class, _manorId);
		if(castle == null)
			return;

		for(int i = 0; i < _count; i++)
		{
			int seedId = _items[i * 2 + 0];
			long count = _items[i * 2 + 1];
			long price = 0;
			long residual = 0;

			SeedProduction seed = castle.getSeed(seedId, CastleManorManager.PERIOD_CURRENT);
			price = seed.getPrice();
			residual = seed.getCanProduce();

			if(price <= 0)
				return;

			if(residual < count)
				return;

			totalPrice += count * price;

			L2Item template = ItemTable.getInstance().getTemplate(seedId);
			totalWeight = (int)(totalWeight + count * template.getWeight());
			if(!template.isStackable())
				slots = (int)(slots + count);
			else if(player.getInventory().getItemByItemId(seedId) == null)
				slots++;
		}

		if(totalPrice > Integer.MAX_VALUE)
			return;

		if(!player.getInventory().validateWeight(totalWeight))
		{
			sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
			return;
		}

		if(!player.getInventory().validateCapacity(slots))
		{
			sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
			return;
		}

		// Charge buyer
		if(totalPrice < 0 || player.getAdena() < totalPrice)
		{
			sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}

		player.reduceAdena(totalPrice, true);

		// Adding to treasury for Manor Castle
		castle.addToTreasuryNoTax((int) totalPrice, false, true);

		// Proceed the purchase
		for(int i = 0; i < _count; i++)
		{
			int seedId = _items[i * 2 + 0];
			int count = _items[i * 2 + 1];
			if(count < 0)
				count = 0;

			// Update Castle Seeds Amount
			SeedProduction seed = castle.getSeed(seedId, CastleManorManager.PERIOD_CURRENT);
			seed.setCanProduce(seed.getCanProduce() - count);
			if(Config.MANOR_SAVE_ALL_ACTIONS)
				castle.updateSeed(seed.getId(), seed.getCanProduce(), CastleManorManager.PERIOD_CURRENT);

			// Add item to Inventory and adjust update packet
			player.getInventory().addItem(seedId, count, "<BuySeed>");

			// Send Char Buy Messages
			SystemMessage sm = null;
			sm = new SystemMessage(SystemMessage.EARNED_S2_S1_s);
			sm.addItemName(seedId);
			sm.addNumber(count);
			player.sendPacket(sm);
		}

		StatusUpdate su = new StatusUpdate(player);
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
	}
}