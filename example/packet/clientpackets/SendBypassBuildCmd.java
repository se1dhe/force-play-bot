package l2p.gameserver.clientpackets;

import l2p.gameserver.handler.AdminCommandHandler;
import l2p.gameserver.model.L2Player;

public class SendBypassBuildCmd extends L2GameClientPacket
{
	public static final int GM_MESSAGE = 9;
	public static final int ANNOUNCEMENT = 10;

	private String _command;

	@Override
	public void readImpl()
	{
		_command = readS();

		if(_command != null)
			_command = _command.trim();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();

		if(activeChar == null)
			return;

		if(activeChar.isKeyBlocked())
		{
			activeChar.sendActionFailed();
			return;
		}

		String cmd = _command;
		if(!cmd.contains("admin_"))
			cmd = "admin_" + cmd;

		AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, cmd);
	}
}