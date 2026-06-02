package l2p.gameserver.clientpackets;

import java.util.concurrent.ConcurrentLinkedQueue;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2TradeList;
import l2p.gameserver.model.TradeItem;
import l2p.gameserver.serverpackets.PrivateStoreManageList;

public class RequestPrivateStoreManageSell extends L2GameClientPacket
{
 	@Override
	public void readImpl()
	{
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

		if(!activeChar.checksForShop(false))
		{
			activeChar.sendActionFailed();
			return;
		}

		switch (activeChar.getPrivateStoreType())
		{
			case L2Player.STORE_PRIVATE_NONE:
			{
				if(activeChar.isSitting())
					activeChar.standUp();
				if(activeChar.getTradeList() == null)
					activeChar.setTradeList(new L2TradeList(0));
				if(activeChar.getSellList() == null)
					activeChar.setSellList(new ConcurrentLinkedQueue<TradeItem>());

				activeChar.getTradeList().updateSellList(activeChar, activeChar.getSellList());
				activeChar.setPrivateStoreType(L2Player.STORE_PRIVATE_NONE);
				activeChar.sendPacket(new PrivateStoreManageList(1, activeChar, false), new PrivateStoreManageList(2, activeChar, false));
				break;
			}
			case L2Player.STORE_PRIVATE_SELL_PACKAGE:
			{
				activeChar.setPrivateStoreType(L2Player.STORE_PRIVATE_NONE);
				activeChar.standUp();
				activeChar.broadcastUserInfo(false);
				activeChar.sendPacket(new PrivateStoreManageList(1, activeChar, true), new PrivateStoreManageList(2, activeChar, true));
				break;
			}
			case L2Player.STORE_PRIVATE_SELL:
			case L2Player.STORE_PRIVATE_BUY:
			{
				activeChar.setPrivateStoreType(L2Player.STORE_PRIVATE_NONE);
				activeChar.standUp();
				activeChar.broadcastUserInfo(false);
				activeChar.sendPacket(new PrivateStoreManageList(1, activeChar, false), new PrivateStoreManageList(2, activeChar, false));
				break;
			}
		}
	}
}