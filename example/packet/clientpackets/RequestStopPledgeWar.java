package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.model.L2Clan;
import l2p.gameserver.model.L2ClanMember;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.multilang.CustomMessage;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.tables.ClanTable;

public class RequestStopPledgeWar extends L2GameClientPacket
{
	private String _pledgeName;

	@Override
	protected void readImpl()
	{
		_pledgeName = readS(32);
	}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		L2Clan playerClan = activeChar.getClan();
		if(playerClan == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(!((activeChar.getClanPrivileges() & L2Clan.CP_CL_PLEDGE_WAR) == L2Clan.CP_CL_PLEDGE_WAR))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT));
			activeChar.sendActionFailed();
			return;
		}

		L2Clan clan = ClanTable.getInstance().getClanByName(_pledgeName);

		if(clan == null)
		{
			activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestStopPledgeWar.NoSuchClan", activeChar));
			activeChar.sendActionFailed();
			return;
		}

		if(!playerClan.isAtWarWith(clan.getClanId()))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_NOT_DECLARED_A_CLAN_WAR_TO_S1_CLAN));
			activeChar.sendActionFailed();
			return;
		}

		if(Config.NO_COMBAT_STOP_CLAN_WAR)
			for(L2ClanMember mbr : playerClan.getMembers())
				if(mbr.isOnline() && mbr.getPlayer().isInCombat())
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.A_CEASE_FIRE_DURING_A_CLAN_WAR_CAN_NOT_BE_CALLED_WHILE_MEMBERS_OF_YOUR_CLAN_ARE_ENGAGED_IN_BATTLE));
					activeChar.sendActionFailed();
					return;
				}

		if(Config.STOP_WAR_DELAY > 0 && playerClan.STOP_WARS.containsKey(clan.getClanId()))
		{
			long time = Config.STOP_WAR_DELAY * 60000L + playerClan.STOP_WARS.get(clan.getClanId());
			if (time > System.currentTimeMillis())
			{
				int min = (int) Math.max((time - System.currentTimeMillis()) / 60000L, 1L);
				activeChar.sendMessage(activeChar.isLangRus() ? ("До возможности отмены войны с кланом " + clan.getName() + " " + min + " мин.") : ("To possibility stop war with clan " + clan.getName() + " left " + min + " min."));
				return;
			}
		}
		playerClan.STOP_WARS.put(clan.getClanId(), System.currentTimeMillis());

		ClanTable.getInstance().stopClanWar(playerClan, clan);
	}
}