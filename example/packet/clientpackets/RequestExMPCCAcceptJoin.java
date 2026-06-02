package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.L2CommandChannel;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.base.Transaction;
import l2p.gameserver.model.base.Transaction.TransactionType;
import l2p.gameserver.multilang.CustomMessage;
import l2p.gameserver.serverpackets.SystemMessage;

public class RequestExMPCCAcceptJoin extends L2GameClientPacket
{
	private int _response;

	@Override
	public void readImpl()
	{
		if(_buf.hasRemaining())
			_response = readD();
		else
			_response = 0;
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		Transaction transaction = activeChar.getTransaction();

		if(transaction == null)
			return;

		if(!transaction.isValid() || !transaction.isTypeOf(TransactionType.CHANNEL))
		{
			transaction.cancel();
			activeChar.sendPacket(Msg.TIME_EXPIRED, Msg.ActionFail);
			return;
		}

		L2Player requestor = transaction.getOtherPlayer(activeChar);

		transaction.cancel();

		if(!requestor.isInParty() || !activeChar.isInParty() || activeChar.getParty().isInCommandChannel())
		{
			requestor.sendPacket(new SystemMessage(SystemMessage.NO_USER_HAS_BEEN_INVITED_TO_THE_COMMAND_CHANNEL));
			return;
		}

		if(_response == 1)
		{
			if(activeChar.isTeleporting())
			{
				activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_JOIN_A_COMMAND_CHANNEL_WHILE_TELEPORTING));
				requestor.sendPacket(new SystemMessage(SystemMessage.NO_USER_HAS_BEEN_INVITED_TO_THE_COMMAND_CHANNEL));
				return;
			}

			if(requestor.getParty().isInCommandChannel())
			{
				L2CommandChannel channel = requestor.getParty().getCommandChannel();
				if(!channel.canAddMoreMembers(activeChar.getParty().getMemberCount()))
				{
					requestor.sendMessage(new CustomMessage("command.channel.maximum.members.reached", requestor).addNumber(Config.MAX_COMMAND_CHANNEL_MEMBERS));
					activeChar.sendMessage(new CustomMessage("command.channel.maximum.members.reached", activeChar).addNumber(Config.MAX_COMMAND_CHANNEL_MEMBERS));
					return;
				}
				requestor.getParty().getCommandChannel().addParty(activeChar.getParty());
			}
			else if(L2CommandChannel.checkAuthority(requestor))
			{
				int totalMembers = requestor.getParty().getMemberCount() + activeChar.getParty().getMemberCount();
				if(totalMembers > Config.MAX_COMMAND_CHANNEL_MEMBERS)
				{
					requestor.sendMessage(new CustomMessage("command.channel.maximum.members.reached", requestor).addNumber(Config.MAX_COMMAND_CHANNEL_MEMBERS));
					activeChar.sendMessage(new CustomMessage("command.channel.maximum.members.reached", activeChar).addNumber(Config.MAX_COMMAND_CHANNEL_MEMBERS));
					return;
				}
				// CC можно создать, если есть клановый скилл Clan Imperium
				boolean haveSkill = requestor.getSkillLevel(L2CommandChannel.CLAN_IMPERIUM_ID) > 0;
				// Ищем Strategy Guide в инвентаре
				boolean haveItem = activeChar.getInventory().getItemByItemId(L2CommandChannel.STRATEGY_GUIDE_ID) != null;
				// Скила нету, придется расходовать предмет
				if(!haveSkill && haveItem)
				{
					requestor.getInventory().destroyItemByItemId(L2CommandChannel.STRATEGY_GUIDE_ID, 1, false);
					requestor.sendPacket(SystemMessage.removeItems(L2CommandChannel.STRATEGY_GUIDE_ID, 1));
				}

				L2CommandChannel channel = new L2CommandChannel(requestor); // Создаём Command Channel
				requestor.sendPacket(new SystemMessage(SystemMessage.THE_COMMAND_CHANNEL_HAS_BEEN_FORMED));
				channel.addParty(activeChar.getParty()); // Добавляем приглашенную партию
			}
		}
		else
			requestor.sendPacket(new SystemMessage(SystemMessage.S1_HAS_DECLINED_THE_CHANNEL_INVITATION).addString(activeChar.getName()));
	}
}