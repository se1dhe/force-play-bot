package l2p.gameserver.clientpackets;

import java.util.concurrent.ConcurrentLinkedQueue;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2TradeList;
import l2p.gameserver.model.TradeItem;
import l2p.gameserver.serverpackets.PrivateStoreBuyManageList;

public class RequestPrivateStoreManageBuyIt extends L2GameClientPacket
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
				if(activeChar.getBuyList() == null)
					activeChar.setBuyList(new ConcurrentLinkedQueue<TradeItem>());

				activeChar.getTradeList().updateBuyList(activeChar, activeChar.getBuyList());
				activeChar.setPrivateStoreType(L2Player.STORE_PRIVATE_NONE);
				activeChar.sendPacket(new PrivateStoreBuyManageList(1, activeChar), new PrivateStoreBuyManageList(2, activeChar));
				break;
			}
			case L2Player.STORE_PRIVATE_SELL:
			case L2Player.STORE_PRIVATE_BUY:
			case L2Player.STORE_PRIVATE_SELL_PACKAGE:
			{
				activeChar.setPrivateStoreType(L2Player.STORE_PRIVATE_NONE);
				activeChar.standUp();
				activeChar.broadcastUserInfo(false);
				activeChar.sendPacket(new PrivateStoreBuyManageList(1, activeChar), new PrivateStoreBuyManageList(2, activeChar));
				break;
			}
		}
	}
}