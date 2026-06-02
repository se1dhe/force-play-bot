package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;

public class RequestNewEnchantPushOne extends L2GameClientPacket
{
	private int _item1ObjectId;

	@Override
	protected void readImpl()
	{
		_item1ObjectId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;
	}
}