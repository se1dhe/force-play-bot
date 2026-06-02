package l2p.gameserver.clientpackets;

import l2p.gameserver.handler.IUserCommandHandler;
import l2p.gameserver.handler.UserCommandHandler;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.multilang.CustomMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BypassUserCmd extends L2GameClientPacket
{
	static Logger _log = LoggerFactory.getLogger(BypassUserCmd.class);

	private int _command;

	/**
	 * packet type id 0xB3
	 * format:  cd
	 */
	@Override
	public void readImpl()
	{
		_command = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		IUserCommandHandler handler = UserCommandHandler.getInstance().getUserCommandHandler(_command);

		if(handler == null)
			activeChar.sendMessage(new CustomMessage("common.S1NotImplemented", activeChar).addString(String.valueOf(_command)));
		else
			handler.useUserCommand(_command, activeChar);
	}
}