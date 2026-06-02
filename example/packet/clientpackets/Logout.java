package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.model.L2Party;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.base.Transaction;
import l2p.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2p.gameserver.multilang.CustomMessage;
import l2p.gameserver.serverpackets.SystemMessage;

public class Logout extends L2GameClientPacket
{
	@Override
	public void readImpl()
	{}

	@Override
	public void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;
		if(player.getEnchantScroll() != null)
		{
			player.sendActionFailed();
			return;
		}
		if(player.isInTransaction())
		{
			if(player.getTransaction().isTypeOf(Transaction.TransactionType.CASINO))
				player.getTransaction().cancel();
			player.sendPacket(new SystemMessage(SystemMessage.ALREADY_TRADING));
			player.sendActionFailed();
			return;
		}
		// Dont allow leaving if player is fighting
		if(player.isInCombat() && !player.isGM())
		{
			player.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_LOGOUT_WHILE_IN_COMBAT));
			player.sendActionFailed();
			return;
		}

		if(player.isFishing())
		{
			player.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_DO_ANYTHING_ELSE_WHILE_FISHING));
			player.sendActionFailed();
			return;
		}

		if(player.isBlocked())
		{
			player.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.Logout.OutOfControl", player));
			player.sendActionFailed();
			return;
		}

		if(player.isFestivalParticipant())
		{
			if(SevenSignsFestival.getInstance().isFestivalInitialized())
			{
				player.sendMessage("You cannot log out while you are a participant in a festival.");
				player.sendActionFailed();
				return;
			}
			L2Party playerParty = player.getParty();
			if(playerParty != null)
				playerParty.broadcastMessageToPartyMembers(player.getName() + " has been removed from the upcoming festival.");
		}

		if(player.isInOlympiadMode())
		{
			if(Config.OLY_NO_ER || player.olyGameNotStart())
			{
				player.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.Logout.Olympiad", player));
				player.sendActionFailed();
				return;
			}
		}
		if(player.isReg() || player.isInStriderRace())
		{
			player.sendMessage(player.isLangRus() ? "Вы участник регистрационного события и не можете покинуть игру." : "You can't do it by participating in the event.");
			player.sendActionFailed();
			return;
		}

		if(player.inObserverMode())
		{
			player.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.Logout.Observer", player));
			player.sendActionFailed();
			return;
		}

		player.client_request = true;
		player.logout(false);
	}
}