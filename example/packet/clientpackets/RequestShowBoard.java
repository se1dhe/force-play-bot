package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.communitybbs.CommunityBoard;
import l2p.gameserver.handler.CommunityBoardManager;
import l2p.gameserver.handler.ICommunityBoardHandler;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.network.L2GameClient;
import l2p.gameserver.serverpackets.SystemMessage;

public class RequestShowBoard extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int _unknown;

	@Override
	public void readImpl()
	{
		_unknown = readD(); //always 1
	}

	@Override
	public void runImpl()
	{
		L2GameClient client = getClient();
		if(client == null)
			return;
		L2Player player = client.getActiveChar();
		if(player == null)
			return;
		if(Config.ALLOW_COMMUNITYBOARD)
		{
			ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler(Config.BBS_DEFAULT, player);
			if(handler != null)
				handler.onBypassCommand(player, Config.BBS_DEFAULT);
			else
				CommunityBoard.getInstance().handleCommands(getClient(), Config.BBS_DEFAULT);
		}
		else
			player.sendPacket(new SystemMessage(SystemMessage.THE_COMMUNITY_SERVER_IS_CURRENTLY_OFFLINE));
	}
}