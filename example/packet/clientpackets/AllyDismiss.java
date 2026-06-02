package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Alliance;
import l2p.gameserver.model.L2Clan;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.tables.ClanTable;

public class AllyDismiss extends L2GameClientPacket
{
    String _clanName;

    @Override
    public void readImpl()
    {
        _clanName = readS();
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

        L2Clan leaderClan = activeChar.getClan();
        if(leaderClan == null)
        {
            activeChar.sendActionFailed();
            return;
        }
        L2Alliance alliance = leaderClan.getAlliance();
        if(alliance == null)
        {
			activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_ARE_NOT_CURRENTLY_ALLIED_WITH_ANY_CLANS));
			return;
        }

        L2Clan clan;
        if(!activeChar.isAllyLeader())
        {
			activeChar.sendPacket(new SystemMessage(SystemMessage.FEATURE_AVAILABLE_TO_ALLIANCE_LEADERS_ONLY));
            return;
        }

        if(_clanName == null)
            return;

        clan = ClanTable.getInstance().getClanByName(_clanName);

        if(clan != null)
        {
            if(!alliance.isMember(clan.getClanId()))
            {
                activeChar.sendActionFailed();
                return;
            }

            if(alliance.getLeader().equals(clan))
            {
				activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_FAILED_TO_WITHDRAW_FROM_THE_ALLIANCE));
                return;
            }

			clan.broadcastToOnlineMembers(new SystemMessage(SystemMessage.S1_S2).addString("Your clan has been expelled from " + alliance.getAllyName() + " alliance."));
			clan.broadcastToOnlineMembers(new SystemMessage(SystemMessage.A_CLAN_THAT_HAS_WITHDRAWN_OR_BEEN_EXPELLED_CANNOT_ENTER_INTO_AN_ALLIANCE_WITHIN_ONE_DAY_OF_WITHDRAWAL_OR_EXPULSION));
            clan.setAllyId(0);
            clan.setLeavedAlly();
            clan.broadcastClanStatus(true, true, true);
            alliance.removeAllyMember(clan.getClanId());
            alliance.setExpelledMember();
            activeChar.sendMessage(clan.getName() + " has been dismissed from " + alliance.getAllyName());
        }
    }
}