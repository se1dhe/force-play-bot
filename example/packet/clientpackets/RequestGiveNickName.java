package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.L2Clan;
import l2p.gameserver.model.L2ClanMember;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.utils.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestGiveNickName extends L2GameClientPacket
{
	static Logger _log = LoggerFactory.getLogger(RequestGiveNickName.class);

	private String _target;
	private String _title;

	@Override
	public void readImpl()
	{
		_target = readS(Config.CNAME_MAXLEN);
		_title = readS();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(!_title.equals("") && !Util.isMatchingRegexp(_title, Config.CLAN_TITLE_TEMPLATE))
		{
			activeChar.sendMessage("Incorrect title.");
			return;
		}

		// Дворяне могут устанавливать/менять себе title
		if(activeChar.isNoble() && _target.equals(activeChar.getName()))
		{
			if(Config.CTITLE_DENY_PATTERN.matcher(_title.toLowerCase()).find())
			{
				activeChar.sendMessage(activeChar.isLangRus() ? "Запрещенный титул." : "Forbidden title.");
				return;
			}
			activeChar.setTitle(_title);
			activeChar.sendPacket(Msg.TITLE_HAS_CHANGED);
			activeChar.broadcastTitleInfo();
			return;
		}
		// Can the player change/give a title?
		else if((activeChar.getClanPrivileges() & L2Clan.CP_CL_GIVE_TITLE) != L2Clan.CP_CL_GIVE_TITLE)
		{
			activeChar.sendPacket(Msg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}

		if(activeChar.getClan().getLevel() < 3)
		{
			activeChar.sendPacket(Msg.TITLE_ENDOWMENT_IS_ONLY_POSSIBLE_WHEN_CLANS_SKILL_LEVELS_ARE_ABOVE_3);
			return;
		}

		L2ClanMember member = activeChar.getClan().getClanMember(_target);
		if(member != null)
		{
			if(Config.CTITLE_DENY_PATTERN.matcher(_title.toLowerCase()).find())
			{
				member.getPlayer().sendMessage(member.getPlayer().isLangRus() ? "Запрещенный титул." : "Forbidden title.");
				return;
			}
			member.setTitle(_title);
			if(member.isOnline())
			{
				member.getPlayer().sendPacket(Msg.TITLE_HAS_CHANGED);
				if(activeChar != member.getPlayer())
					activeChar.sendPacket(new SystemMessage(SystemMessage.CLAN_MEMBER_S1S_TITLE_HAS_BEEN_CHANGED_TO_S2).addString(member.getName()).addString(_title));
				member.getPlayer().broadcastTitleInfo();
			}
		}
		else
			activeChar.sendPacket(Msg.THE_TARGET_MUST_BE_A_CLAN_MEMBER);
	}
}