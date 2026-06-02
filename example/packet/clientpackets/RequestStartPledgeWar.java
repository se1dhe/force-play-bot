package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.model.L2Clan;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.tables.ClanTable;

public class RequestStartPledgeWar extends L2GameClientPacket
{
	String _pledgeName;
	L2Clan _clan;

	@Override
	public void readImpl()
	{
		_pledgeName = readS(32);
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		_clan = activeChar.getClan();
		if(_clan == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(!((activeChar.getClanPrivileges() & L2Clan.CP_CL_PLEDGE_WAR) == L2Clan.CP_CL_PLEDGE_WAR))
		{
			activeChar.sendActionFailed();
			return;
		}

		if(_clan.getWarsCount() >= Config.AltClanWarMax)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.A_DECLARATION_OF_WAR_AGAINST_MORE_THAN_30_CLANS_CANT_BE_MADE_AT_THE_SAME_TIME));
			activeChar.sendActionFailed();
			return;
		}

		if(_clan.getLevel() < Config.AltMinClanLvlForWar || _clan.getMembersCount() < Config.AltClanMembersForWar)
		{
			if(Config.AltMinClanLvlForWar == 3 && Config.AltClanMembersForWar == 15)
				activeChar.sendPacket(new SystemMessage(SystemMessage.A_CLAN_WAR_CAN_BE_DECLARED_ONLY_IF_THE_CLAN_IS_LEVEL_THREE_OR_ABOVE_AND_THE_NUMBER_OF_CLAN_MEMBERS_IS_FIFTEEN_OR_GREATER));
			else
				activeChar.sendMessage("A Clan War can be declared only if the clan is level " + Config.AltMinClanLvlForWar + " or above, and the number of clan members is " + Config.AltClanMembersForWar + " or greater.");
			activeChar.sendActionFailed();
			return;
		}

		L2Clan clan = ClanTable.getInstance().getClanByName(_pledgeName);
		if(clan == null)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.THE_DECLARATION_OF_WAR_CANT_BE_MADE_BECAUSE_THE_CLAN_DOES_NOT_EXIST_OR_ACT_FOR_A_LONG_PERIOD));
			activeChar.sendActionFailed();
			return;
		}

		else if(_clan.equals(clan))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.FOOL_YOU_CANNOT_DECLARE_WAR_AGAINST_YOUR_OWN_CLAN));
			activeChar.sendActionFailed();
			return;
		}

		else if(_clan.isAtWarWith(clan.getClanId()))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.THE_DECLARATION_OF_WAR_HAS_BEEN_ALREADY_MADE_TO_THE_CLAN));
			activeChar.sendActionFailed();
			return;
		}

		else if(_clan.getAllyId() == clan.getAllyId() && _clan.getAllyId() != 0)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.A_DECLARATION_OF_CLAN_WAR_AGAINST_AN_ALLIED_CLAN_CANT_BE_MADE));
			activeChar.sendActionFailed();
			return;
		}

		else if(clan.getLevel() < Config.AltMinClanLvlForWar || clan.getMembersCount() < Config.AltClanMembersForWar)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.A_CLAN_WAR_CAN_BE_DECLARED_ONLY_IF_THE_CLAN_IS_LEVEL_THREE_OR_ABOVE_AND_THE_NUMBER_OF_CLAN_MEMBERS_IS_FIFTEEN_OR_GREATER));
			activeChar.sendActionFailed();
			return;
		}

		ClanTable.getInstance().startClanWar(activeChar.getClan(), clan);
	}
}