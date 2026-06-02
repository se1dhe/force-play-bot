package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Clan;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.serverpackets.AllianceInfo;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.tables.ClanTable;

public class RequestAllyInfo extends L2GameClientPacket
{
	@Override
	public void readImpl()
	{}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		SystemMessage sm;
		if(activeChar.getAlliance() == null)
			return;

		activeChar.sendPacket(new SystemMessage(SystemMessage._ALLIANCE_INFORMATION_));
		activeChar.sendPacket(new SystemMessage(SystemMessage.ALLIANCE_NAME_S1).addString(activeChar.getClan().getAlliance().getAllyName()));
		int clancount = 0;
		L2Clan leaderclan = activeChar.getAlliance().getLeader();
		clancount = ClanTable.getInstance().getAlliance(leaderclan.getAllyId()).getMembers().length;
		int[] online = new int[clancount + 1];
		int[] count = new int[clancount + 1];
		L2Clan[] clans = activeChar.getAlliance().getMembers();
		for(int i = 0; i < clancount; i++)
		{
			online[i + 1] = clans[i].getOnlineMembers(0).length;
			count[i + 1] = clans[i].getMembers().length;
			online[0] += online[i + 1];
			count[0] += count[i + 1];
		}
		//Connection
		sm = new SystemMessage(SystemMessage.CONNECTION_S1_TOTAL_S2);
		sm.addNumber(online[0]);
		sm.addNumber(count[0]);
		activeChar.sendPacket(sm);
		sm = new SystemMessage(SystemMessage.ALLIANCE_LEADER_S2_OF_S1);
		sm.addString(leaderclan.getName());
		sm.addString(leaderclan.getLeaderName());
		activeChar.sendPacket(sm);
		//clan count
		activeChar.sendPacket(new SystemMessage(SystemMessage.AFFILIATED_CLANS_TOTAL_S1_CLAN_S).addNumber(clancount));
		activeChar.sendPacket(new SystemMessage(SystemMessage._CLAN_INFORMATION_));
		for(int i = 0; i < clancount; i++)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.CLAN_NAME_S1).addString(clans[i].getName()));
			activeChar.sendPacket(new SystemMessage(SystemMessage.CLAN_LEADER_S1).addString(clans[i].getLeaderName()));
			activeChar.sendPacket(new SystemMessage(SystemMessage.CLAN_LEVEL_S1).addNumber(clans[i].getLevel()));
			sm = new SystemMessage(SystemMessage.CONNECTION_S1_TOTAL_S2);
			sm.addNumber(online[i + 1]);
			sm.addNumber(count[i + 1]);
			activeChar.sendPacket(sm);
			activeChar.sendPacket(new SystemMessage(SystemMessage.__DASHES__));
		}
		activeChar.sendPacket(new SystemMessage(SystemMessage.__EQUALS__));
		activeChar.sendPacket(new AllianceInfo());
	}
}