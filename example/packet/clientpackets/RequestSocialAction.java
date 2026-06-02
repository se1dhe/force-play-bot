package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.serverpackets.SocialAction;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.utils.Util;

public class RequestSocialAction extends L2GameClientPacket
{
	private int _actionId;

	/**
	 * packet type id 0x34
	 * format:		cd
	 */
	@Override
	public void readImpl()
	{
		_actionId = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}

		// You cannot do anything else while fishing
		if(activeChar.isFishing())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_DO_ANYTHING_ELSE_WHILE_FISHING));
			return;
		}

		// internal Social Action check
		if(_actionId < 2 || _actionId > 13)
		{
			Util.handleIllegalPlayerAction(activeChar, "Character " + activeChar.getName() + " at account " + activeChar.getAccountName() + " requested an internal Social Action " + _actionId, 1);
			return;
		}

		if(activeChar.getPrivateStoreType() == L2Player.STORE_PRIVATE_NONE && !activeChar.isInTransaction() && !activeChar.isActionsDisabled() && !activeChar.isSitting())
		{
			activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), _actionId));
			if(Config.ALT_SOCIAL_ACTION_REUSE)
			{
				ThreadPoolManager.getInstance().schedule(new SocialTask(activeChar), 2600);
				activeChar.block();
			}
		}
	}

	class SocialTask implements Runnable
	{
		L2Player _player;

		SocialTask(L2Player player)
		{
			_player = player;
		}

		public void run()
		{
			_player.unblock();
		}
	}
}