package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Clan;
import l2p.gameserver.model.L2ClanMember;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.multilang.CustomMessage;
import l2p.gameserver.serverpackets.PledgeReceiveMemberInfo;
import l2p.gameserver.serverpackets.PledgeShowMemberListUpdate;
import l2p.gameserver.serverpackets.SystemMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestPledgeSetAcademyMaster extends L2GameClientPacket
{
	// format: (ch)dSS
	static Logger _log = LoggerFactory.getLogger(RequestPledgeSetAcademyMaster.class);

	private int _mode; // 1=set, 0=unset
	private String _sponsorName;
	private String _apprenticeName;

	@Override
	public void readImpl()
	{
		_mode = readD();
		_sponsorName = readS();
		_apprenticeName = readS();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		L2Clan clan = activeChar.getClan();
		if(clan == null)
			return;

		if((activeChar.getClanPrivileges() & L2Clan.CP_CL_MASTER_RIGHTS) == L2Clan.CP_CL_MASTER_RIGHTS)
		{
			L2ClanMember sponsor = activeChar.getClan().getClanMember(_sponsorName);
			L2ClanMember apprentice = activeChar.getClan().getClanMember(_apprenticeName);
			if(sponsor != null && apprentice != null)
			{
				if(apprentice.getPledgeType() != L2Clan.SUBUNIT_ACADEMY || sponsor.getPledgeType() == L2Clan.SUBUNIT_ACADEMY)
					return; // hack?

				if(_mode == 1)
				{
					if(sponsor.hasApprentice())
					{
						activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestOustAlly.MemberAlreadyHasApprentice", activeChar));
						return;
					}
					if(apprentice.hasSponsor())
					{
						activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestOustAlly.ApprenticeAlreadyHasSponsor", activeChar));
						return;
					}
					sponsor.setApprentice(apprentice.getObjectId());
					clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(apprentice));
					clan.broadcastToOnlineMembers(new SystemMessage(SystemMessage.S2_HAS_BEEN_DESIGNATED_AS_THE_APPRENTICE_OF_CLAN_MEMBER_S1).addString(sponsor.getName()).addString(apprentice.getName()));
				}
				else
				{
					if(!sponsor.hasApprentice())
					{
						activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestOustAlly.MemberHasNoApprentice", activeChar));
						return;
					}
					sponsor.setApprentice(0);
					clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(apprentice));
					clan.broadcastToOnlineMembers(new SystemMessage(SystemMessage.S2_CLAN_MEMBER_S1S_APPRENTICE_HAS_BEEN_REMOVED).addString(sponsor.getName()).addString(apprentice.getName()));
				}
				if(apprentice.isOnline())
					apprentice.getPlayer().broadcastUserInfo(false);
				activeChar.sendPacket(new PledgeReceiveMemberInfo(sponsor));
			}
		}
		else
			activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestOustAlly.NoMasterRights", activeChar));
	}
}