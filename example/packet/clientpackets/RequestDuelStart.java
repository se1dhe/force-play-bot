package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.data.xml.holder.EventHolder;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2World;
import l2p.gameserver.model.entity.events.EventType;
import l2p.gameserver.model.entity.events.impl.DuelEvent;
import l2p.gameserver.serverpackets.SystemMessage;

public class RequestDuelStart extends L2GameClientPacket
{
	private String _name;
	private int _duelType;

	@Override
	protected void readImpl()
	{
		_name = readS(Config.CNAME_MAXLEN);
		_duelType = readD();
	}

	@Override
	protected void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;

		if(player.isActionsDisabled())
		{
			player.sendActionFailed();
			return;
		}

		if(player.isInTransaction())
		{
			player.sendPacket(new SystemMessage(SystemMessage.WAITING_FOR_ANOTHER_REPLY));
			return;
		}

		L2Player target = L2World.getPlayer(_name);
		if(target == null || target == player)
		{
			player.sendPacket(new SystemMessage(SystemMessage.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL));
			return;
		}

		DuelEvent duelEvent = EventHolder.getInstance().getEvent(EventType.PVP_EVENT, _duelType);
		if(duelEvent == null)
			return;

		if(!duelEvent.canDuel(player, target, true))
			return;

		if(target.isBusy())
		{
			player.sendPacket(new SystemMessage(SystemMessage.S1_IS_BUSY_PLEASE_TRY_AGAIN_LATER).addName(target));
			return;
		}

		duelEvent.askDuel(player, target);
	}
}