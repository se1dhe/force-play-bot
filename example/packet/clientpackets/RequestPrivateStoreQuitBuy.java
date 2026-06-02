package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;

public class RequestPrivateStoreQuitBuy extends L2GameClientPacket
{
	@Override
	public void readImpl()
	{}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(!activeChar.isInStoreMode())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.getTradeList() != null)
			activeChar.getTradeList().removeAll();
		activeChar.setPrivateStoreType(L2Player.STORE_PRIVATE_NONE);
		activeChar.standUp();
		activeChar.broadcastUserInfo(false);
	}
}