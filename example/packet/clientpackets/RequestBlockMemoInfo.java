package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.serverpackets.ExBlockDetailInfo;

public class RequestBlockMemoInfo extends L2GameClientPacket
{
	private String _name;

	@Override
	protected void readImpl() throws Exception
	{
		_name = readS(Config.CNAME_MAXLEN);
	}

	@Override
	protected void runImpl() throws Exception
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;

		if(player.getBlockList().contains(_name))
			player.sendPacket(new ExBlockDetailInfo(_name, "test"));
	}
}
