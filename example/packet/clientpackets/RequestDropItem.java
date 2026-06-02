package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.geodata.GeoEngine;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.multilang.CustomMessage;
import l2p.gameserver.utils.Location;
import l2p.gameserver.utils.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestDropItem extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(RequestDropItem.class);

	private int _objectId;
	private long _count;
	private Location _loc;

	@Override
	public void readImpl()
	{
		_objectId = readD();
		if(getClient().isITClient())
			_count = readD();
		else
			_count = readQ();
		_loc = new Location(readD(), readD(), readD());
	}

	@Override
	public void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if(player == null || player.isDead())
			return;

		if(player.inObserverMode())
		{
			player.sendActionFailed();
			return;
		}

		if(player.isInFightClub())
		{
			player.sendActionFailed();
			return;
		}

		if(_count < 1 || _loc == null || _loc.isNull())
		{
			player.sendActionFailed();
			return;
		}

		if(!Config.ALLOW_DISCARDITEM && (!Config.ALLOW_DISCARDITEM_GM || !player.isGM()))
		{
			player.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestDropItem.Disallowed", player));
			return;
		}

		if(player.isInOlympiadMode())
		{
			player.sendMessage(player.isLangRus() ? "Вы не можете выбрасывать вещи в Олимпиаде." : "You can't drop items in Olympiad.");
			player.sendActionFailed();
			return;
		}

		if(player.inEvent())
		{
			player.sendMessage(player.isLangRus() ? "Вы не можете выбрасывать вещи на Ивенте." : "You can't drop items in Event.");
			player.sendActionFailed();
			return;
		}

		if(player.getPrivateStoreType() != L2Player.STORE_PRIVATE_NONE)
		{
			player.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}

		if(player.isInTransaction())
		{
			sendPacket(Msg.NOTHING_HAPPENED);
			return;
		}

		if(player.isFishing())
		{
			player.sendPacket(Msg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
			return;
		}

		if(player.isStunned() || player.isSleeping() || player.isParalyzed() || player.isAlikeDead() || player.isOutOfControl() || player.isSitting() || player.isDropDisabled())
		{
			player.sendActionFailed();
			return;
		}

		if(player.isTradeBannedByGM())
		{
			player.sendActionFailed();
			return;
		}

		if(Config.SERVICES_DISABLE_DROP_ITEM && player.isTradeKeyBlocked())
		{
			player.sendMessage(player.isLangRus() ? "Предмет нельзя выбросить, отключите Lock." : "Item cannot be dropped, turn off Lock.");
			player.sendActionFailed();
			return;
		}

		if(!player.isInRangeSq(_loc, 22500) || Math.abs(_loc.z - player.getZ()) > 50 || !GeoEngine.canMoveToCoord(player.getX(), player.getY(), player.getZ(), _loc.getX(), _loc.getY(), player.getZ(), player.getGeoIndex()))
		{
			player.sendPacket(Msg.TOO_FAR_TO_DISCARD);
			return;
		}

		L2ItemInstance dropedItem = null;
		PcInventory inventory = player.getInventory();
		inventory.writeInvLock();
		try
		{
			L2ItemInstance oldItem = inventory.getItemByObjectId(_objectId);
			if(oldItem == null)
			{
				player.logout(true);
				_log.warn(player.toString() + " tried to drop an item that is not in the inventory ?! itemObjectId:" + _objectId);
				return;
			}

			if(!oldItem.canBeDropped(player))
			{
				player.sendPacket(Msg.THAT_ITEM_CANNOT_BE_DISCARDED);
				return;
			}

			int oldCount = oldItem.getIntegerLimitedCount();
			if(oldCount < _count)
			{
				player.sendActionFailed();
				return;
			}

			if(oldItem.isEquipped() && (!oldItem.isArrow() || oldItem.getCount() <= _count))
			{
				if(player.recording)
					player.recBot(3, oldItem.getBodyPart(), 1, 0, 0, 0, 0);
				inventory.unEquipItemInBodySlotAndNotify(oldItem.getBodyPart(), oldItem);
				player.sendUserInfo(true);
			}

			oldItem.setWhFlag(true);
			dropedItem = inventory.dropItem(_objectId, _count, "<RequestDropItem>");
			oldItem.setWhFlag(false);
		}
		finally
		{
			inventory.writeInvUnlock();
		}

		if(dropedItem == null)
		{
			player.sendActionFailed();
			return;
		}

		dropedItem.dropToTheGround(player, _loc);
		player.disableDrop(1000);
		Log.LogItem(player, Log.Drop, dropedItem);
		player.updateStats();
	}
}