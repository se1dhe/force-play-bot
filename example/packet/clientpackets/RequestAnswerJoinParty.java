package l2p.gameserver.clientpackets;

import static l2p.gameserver.model.L2Zone.ZoneType.OlympiadStadia;

import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.instancemanager.ZoneManager;
import l2p.gameserver.model.L2Party;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Zone;
import l2p.gameserver.model.base.Transaction;
import l2p.gameserver.model.base.Transaction.TransactionType;
import l2p.gameserver.multilang.CustomMessage;
import l2p.gameserver.serverpackets.JoinParty;
import l2p.gameserver.serverpackets.SystemMessage;

public class RequestAnswerJoinParty extends L2GameClientPacket
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

		if(!activeChar.isReg() && activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}
		answer(activeChar, _response);
	}

	protected static void answer(L2Player activeChar, int response)
	{
		Transaction transaction = activeChar.getTransaction();

		if(transaction == null)
			return;

		if(!transaction.isValid() || !transaction.isTypeOf(TransactionType.PARTY))
		{
			transaction.cancel();
			activeChar.sendPacket(Msg.TIME_EXPIRED, Msg.ActionFail);
			return;
		}

		L2Player requestor = transaction.getOtherPlayer(activeChar);
		L2Party party = requestor.getParty();
		transaction.cancel();

		if(party == null || party.getPartyLeader() == null)
		{
			activeChar.sendPacket(Msg.ActionFail);
			requestor.sendPacket(new JoinParty(0));
			return;
		}

		if(Config.ENABLE_FIXED_RESTART_POINT)
		{
			if(activeChar.isInZone(ZoneManager.getInstance().getZoneById(L2Zone.ZoneType.battle_zone, Config.ID_FIXED_RESPAWN_ZONE, false)))
			{
				activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestAnswerJoinParty.FixedZone1", activeChar));
				requestor.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestAnswerJoinParty.FixedZone2", requestor));
				activeChar.sendPacket(Msg.ActionFail);
				requestor.sendPacket(new JoinParty(0));
				return;
			}

			if(requestor.isInZone(ZoneManager.getInstance().getZoneById(L2Zone.ZoneType.battle_zone, Config.ID_FIXED_RESPAWN_ZONE, false)))
			{
				activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestAnswerJoinParty.FixedZone3", activeChar));
				requestor.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestAnswerJoinParty.FixedZone4", requestor));
				activeChar.sendPacket(Msg.ActionFail);
				requestor.sendPacket(new JoinParty(0));
				return;
			}
		}

		SystemMessage problem = activeChar.canJoinParty(requestor);
		if(problem != null)
		{
			activeChar.sendPacket(problem, Msg.ActionFail);
			requestor.sendPacket(new JoinParty(0));
			return;
		}

		requestor.sendPacket(new JoinParty(response));

		if(response == 1)
		{
			if(!activeChar.isReg() && activeChar.isInZone(OlympiadStadia))
			{
				activeChar.sendMessage("A party cannot be formed in this area.");
				requestor.sendMessage("A party cannot be formed in this area.");
				return;
			}

			if(party.getMemberCount() >= 9)
			{
				activeChar.sendPacket(Msg.PARTY_IS_FULL);
				requestor.sendPacket(Msg.PARTY_IS_FULL);
				return;
			}

			activeChar.joinParty(party);
		}
		else if(party != null && party.getMemberCount() == 1)
			requestor.setParty(null);
	}
}