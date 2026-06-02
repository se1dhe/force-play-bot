package l2p.gameserver.clientpackets;

import l2p.commons.util.Rnd;
import l2p.gameserver.Config;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.instancemanager.InstanceManager;
import l2p.gameserver.instancemanager.ZoneManager;
import l2p.gameserver.model.L2ObjectsStorage;
import l2p.gameserver.model.L2Party;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Zone;
import l2p.gameserver.model.base.Transaction;
import l2p.gameserver.model.base.Transaction.TransactionType;
import l2p.gameserver.model.entity.instance.Instance;
import l2p.gameserver.multilang.CustomMessage;
import l2p.gameserver.serverpackets.AskJoinParty;
import l2p.gameserver.serverpackets.SystemMessage;

public class RequestJoinParty extends L2GameClientPacket
{
	private String _name;
	private int _itemDistribution;

	@Override
	public void readImpl()
	{
		_name = readS();
		_itemDistribution = readD();
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
		if(Config.ENABLE_FIXED_RESTART_POINT)
		{
			if(activeChar.isInZone(ZoneManager.getInstance().getZoneById(L2Zone.ZoneType.battle_zone, Config.ID_FIXED_RESPAWN_ZONE, false)))
			{
				activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestJoinParty.FixedZone1", activeChar));
				activeChar.sendActionFailed();
				return;
			}
		}

		if(activeChar.isNoParty())
		{
			activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestJoinParty.FixedZone1", activeChar));
			return;
		}

		L2Player target = L2ObjectsStorage.getPlayer(_name);

		if(target == null || target == activeChar)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInTransaction())
		{
			activeChar.sendPacket(Msg.WAITING_FOR_ANOTHER_REPLY, Msg.ActionFail);
			return;
		}
		if(target.isInOlympiadMode())
		{
			activeChar.sendMessage("A user currently participating in the Olympiad cannot accept party and friend invitations.");
			return;
		}
		if(Config.ENABLE_FIXED_RESTART_POINT)
		{
			if(target.isInZone(ZoneManager.getInstance().getZoneById(L2Zone.ZoneType.battle_zone, Config.ID_FIXED_RESPAWN_ZONE, false)))
			{
				activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestJoinParty.FixedZone2", activeChar));
				activeChar.sendActionFailed();
				return;
			}
		}
		if(target.isNoParty())
		{
			activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestJoinParty.FixedZone2", activeChar));
			return;
		}
		SystemMessage problem = target.canJoinParty(activeChar);
		if(problem != null)
		{
			activeChar.sendPacket(problem);
			return;
		}

		if(!activeChar.isInParty())
			createNewParty(_itemDistribution, target, activeChar);
		else
			addTargetToParty(_itemDistribution, target, activeChar);
	}

	private static void addTargetToParty(int itemDistribution, L2Player target, L2Player activeChar)
	{
		if(activeChar.getParty().getMemberCount() >= 9)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.PARTY_IS_FULL));
			return;
		}

		// Только Party Leader может приглашать новых членов
		if(Config.PARTY_LEADER_ONLY_CAN_INVITE && !activeChar.getParty().isLeader(activeChar))
		{
			activeChar.sendPacket(Msg.ONLY_THE_LEADER_CAN_GIVE_OUT_INVITATIONS);
			return;
		}

		if(!activeChar.isInParty())
		{
			activeChar.sendActionFailed();
			return;
		}

		Instance inst = InstanceManager.getInstance().getInstanceByPlayer(activeChar);
		if(inst != null && !inst.isInside(target.getObjectId()))
		{
			if(activeChar.isLangRus())
				activeChar.sendMessage("Нельзя пригласить, поскольку группа закрыта.");
			else
				activeChar.sendMessage("Can't invite because the group is closed.");
			return;
		}
		if(activeChar.getParty().isInDimensionalRift())
		{
			activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestJoinParty.InDimensionalRift", activeChar));
			activeChar.sendActionFailed();
			return;
		}

		if(!target.isInTransaction())
		{
			new Transaction(TransactionType.PARTY, activeChar, target, 10000);

			target.sendPacket(new AskJoinParty(activeChar.getName(), itemDistribution));
			activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_INVITED_S1_TO_YOUR_PARTY).addString(target.getName()));
			if(Config.BOTS_CAN_JOIN_PARTY && target.isFashion && Rnd.chance(Config.BOTS_CHANCE_JOIN_PARTY))
			{
				final int ans = Rnd.chance(Config.BOTS_CHANCE_JOIN_PARTY) ? 1 : (Rnd.chance(Config.BOTS_CHANCE_REFUSE_PARTY) ? 0 : 2);
				if(ans < 2)
				{
					final long id = target.getStoredId();
					ThreadPoolManager.getInstance().schedule(new Runnable()
					{
						@Override
						public void run()
						{
							L2Player bot = L2ObjectsStorage.getAsPlayer(id);
							if(bot != null)
								RequestAnswerJoinParty.answer(bot, ans);
						}
					}, Rnd.get(2500, 8500));
				}

			}
		}
		else
			activeChar.sendPacket(new SystemMessage(SystemMessage.S1_IS_BUSY_PLEASE_TRY_AGAIN_LATER).addString(target.getName()));
	}

	private static void createNewParty(int itemDistribution, L2Player target, L2Player requestor)
	{
		if(!target.isInTransaction())
		{
			Instance inst = InstanceManager.getInstance().getInstanceByPlayer(requestor);
			if(inst != null && !inst.isInside(target.getObjectId()))
			{
				if(requestor.isLangRus())
					requestor.sendMessage("Нельзя пригласить, поскольку группа закрыта.");
				else
					requestor.sendMessage("Can't invite because the group is closed.");
				return;
			}

			requestor.setParty(new L2Party(requestor, itemDistribution));
			new Transaction(TransactionType.PARTY, requestor, target, 10000);
			target.sendPacket(new AskJoinParty(requestor.getName(), itemDistribution));
			requestor.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_INVITED_S1_TO_YOUR_PARTY).addString(target.getName()));
			if(Config.BOTS_CAN_JOIN_PARTY && target.isFashion && Rnd.chance(Config.BOTS_CHANCE_JOIN_PARTY))
			{
				final int ans = Rnd.chance(Config.BOTS_CHANCE_JOIN_PARTY) ? 1 : (Rnd.chance(Config.BOTS_CHANCE_REFUSE_PARTY) ? 0 : 2);
				if(ans < 2)
				{
					final long id = target.getStoredId();
					ThreadPoolManager.getInstance().schedule(new Runnable()
					{
						@Override
						public void run()
						{
							L2Player bot = L2ObjectsStorage.getAsPlayer(id);
							if(bot != null)
								RequestAnswerJoinParty.answer(bot, ans);
						}
					}, Rnd.get(2500, 8500));
				}

			}
		}
		else
			requestor.sendPacket(new SystemMessage(SystemMessage.S1_IS_BUSY_PLEASE_TRY_AGAIN_LATER).addString(target.getName()));
	}
}