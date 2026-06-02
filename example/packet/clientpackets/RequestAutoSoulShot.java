package l2p.gameserver.clientpackets;

import l2p.gameserver.handler.IItemHandler;

import l2p.gameserver.handler.ItemHandler;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.serverpackets.ExAutoSoulShot;
import l2p.gameserver.serverpackets.SystemMessage;

public class RequestAutoSoulShot extends L2GameClientPacket
{
	private int _itemId;
	private boolean _enabled; // 1 = on : 0 = off;
	private int _type;

	@Override
	public void readImpl()
	{
		_itemId = readD();
		_enabled = readD() == 1;
		if(!getClient().isITClient())
			_type = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player player = getClient().getActiveChar();

		if(player == null)
			return;

		if(player.getPrivateStoreType() != L2Player.STORE_PRIVATE_NONE || player.isDead())
			return;

		L2ItemInstance item = player.getInventory().findItemByItemId(_itemId);

		if(item == null)
			return;

		if(_enabled)
		{
			if(player.isInTrade())
			{
				player.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_PICK_UP_OR_USE_ITEMS_WHILE_TRADING));
				return;
			}
			if(_itemId >= 6645 && _itemId <= 6647 && player.getPet() == null)
			{
				player.sendPacket(new SystemMessage(SystemMessage.SINCE_A_SERVITOR_OR_A_PET_DOES_NOT_EXIST_AUTOMATIC_USE_IS_NOT_APPLICABLE));
				return;
			}
			IItemHandler handler = ItemHandler.getInstance().getItemHandler(_itemId);
			if(handler == null)
				return;
			if(handler.useItem(player, item, false))
			{
				player.addAutoSoulShot(_itemId);
				player.sendPacket(new ExAutoSoulShot(_itemId, true, _type));
				player.sendPacket(new SystemMessage(SystemMessage.THE_USE_OF_S1_WILL_NOW_BE_AUTOMATED).addItemName(_itemId));
				if(player.recording)
					player.recBot(10, 1, _itemId, 0, 0, 0, 0);
			}
			return;
		}

		player.removeAutoSoulShot(_itemId);
		player.sendPacket(new ExAutoSoulShot(_itemId, false, _type));
		player.sendPacket(new SystemMessage(SystemMessage.THE_AUTOMATIC_USE_OF_S1_WILL_NOW_BE_CANCELLED).addString(item.getItem().getName()));
		if(player.recording)
			player.recBot(10, 0, _itemId, 0, 0, 0, 0);
	}
}