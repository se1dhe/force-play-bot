package l2p.gameserver.clientpackets;

import l2p.commons.lang.reference.HardReference;
import l2p.gameserver.Config;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.model.*;
import l2p.gameserver.serverpackets.SystemMessage;

public class RequestRecipeShopListSet extends L2GameClientPacket
{
	// format: cdb, b - array of (dd)
	private int _count;
	L2ManufactureList createList = new L2ManufactureList();

	@Override
	public void readImpl()
	{
		_count = readD();
		if(_count * (getClient().isITClient() ? 8 : 12) > _buf.remaining() || _count > Short.MAX_VALUE || _count < 0)
		{
			_count = 0;
			return;
		}
		for(int x = 0; x < _count; x++) // TODO [V] - long
			createList.add(new L2ManufactureItem(readD(), getClient().isITClient() ? readD() : (int) readQ()));
		_count = createList.size();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.getSittingTask())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(!activeChar.checksForShop(true))
		{
			L2TradeList.cancelStore(activeChar);
			return;
		}

		if(activeChar.getNoChannel() != 0)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_ARE_CURRENTLY_BANNED_FROM_ACTIVITIES_RELATED_TO_THE_PRIVATE_STORE_AND_PRIVATE_WORKSHOP));
			L2TradeList.cancelStore(activeChar);
			return;
		}

		if(_count == 0 || activeChar.getCreateList() == null)
		{
			L2TradeList.cancelStore(activeChar);
			return;
		}

		if(_count > Config.MAX_PVTCRAFT_SLOTS)
		{
			sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED));
			L2TradeList.cancelStore(activeChar);
			return;
		}

		createList.setStoreName(activeChar.getCreateList().getStoreName());
		activeChar.setCreateList(createList);
		activeChar.setPrivateStoreType(L2Player.STORE_PRIVATE_MANUFACTURE);
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
				player.broadcastPrivateStoreMsg(3);
				player.broadcastUserInfo(false);
			}
		}, 2500L);
	}
}