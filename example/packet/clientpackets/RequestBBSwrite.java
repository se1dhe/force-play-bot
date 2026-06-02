package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.communitybbs.CommunityBoard;
import l2p.gameserver.handler.CommunityBoardManager;
import l2p.gameserver.handler.ICommunityBoardHandler;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.utils.BypassStorage.ValidBypass;
import l2p.gameserver.utils.Log;

public class RequestBBSwrite extends L2GameClientPacket
{
	private String _url;
	private String _arg1;
	private String _arg2;
	private String _arg3;
	private String _arg4;
	private String _arg5;

	@Override
	protected void readImpl()
	{
		_url = readS();
		_arg1 = readS();
		_arg2 = readS();
		_arg3 = readS();
		_arg4 = readS();
		_arg5 = readS();
	}

	@Override
	protected void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;

		ValidBypass bp = player.getBypassStorage().validate(_url);
		if(bp == null)
		{
			player.sendActionFailed();
			Log.addLog("BBSwrite direct access to BBS bypass: " + _url + " / Player: " + player, "bypass");
			return;
		}

		ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler(_url, player);
		if(handler != null)
		{
			if(!Config.ALLOW_COMMUNITYBOARD)
				player.sendPacket(new SystemMessage(SystemMessage.THE_COMMUNITY_SERVER_IS_CURRENTLY_OFFLINE));
			else
				handler.onWriteCommand(player, _url, _arg1, _arg2, _arg3, _arg4, _arg5);
		}
		else
		{
			CommunityBoard.getInstance().handleWriteCommands(getClient(), _url, _arg1, _arg2, _arg3, _arg4, _arg5);
		}
	}
}