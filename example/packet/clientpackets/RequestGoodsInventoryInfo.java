package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;

/**
 * @author VISTALL
 * @date 23:33/23.03.2011
 */
public class RequestGoodsInventoryInfo extends L2GameClientPacket
{
	@Override
	protected void readImpl() throws Exception
	{

	}

	@Override
	protected void runImpl() throws Exception
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;
	}
}