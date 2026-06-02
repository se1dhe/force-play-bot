package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.data.xml.holder.ChatFilterHolder;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2World;
import l2p.gameserver.model.chatfilter.ChatFilter;
import l2p.gameserver.model.chatfilter.ChatType;
import l2p.gameserver.multilang.CustomMessage;
import l2p.gameserver.serverpackets.L2FriendSay;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.utils.Log;
import l2p.gameserver.utils.SpamFilter;

public class RequestSendFriendMsg extends L2GameClientPacket
{
    private String _message;
    private String _receiver;

    @Override
    protected void readImpl()
    {
		_message = readS(2048);
        _receiver = readS(Config.CNAME_MAXLEN);
    }

    @Override
    protected void runImpl()
    {
        L2Player activeChar = getClient().getActiveChar();
        if(activeChar == null)
            return;

        L2Player targetPlayer = L2World.getPlayer(_receiver);
        if(targetPlayer == null || (!targetPlayer.isConnected() && !targetPlayer.isFashion))
        {
			activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_NOT_FOUND_IN_THE_GAME));
            return;
        }

        loop: if(!activeChar.getPlayerAccess().CanAnnounce)
        {
            for(ChatFilter f : ChatFilterHolder.getInstance().getFilters())
            {
                if(f.isMatch(activeChar, ChatType.getTypeById(Say2C.L2FRIEND), _message, targetPlayer))
                {
                    switch(f.getAction())
                    {
                        case ChatFilter.ACTION_BAN_CHAT:
                            activeChar.updateNoChannel(Integer.parseInt(f.getValue()) * 1000L);
                            break loop;
                        case ChatFilter.ACTION_WARN_MSG:
                            activeChar.sendMessage(new CustomMessage(f.getValue(), activeChar));
                            return;
                        case ChatFilter.ACTION_REPLACE_MSG:
                            _message = f.getValue();
                            break loop;
                    }
                }
            }
        }

        if(activeChar.getNoChannel() != 0)
        {
            if(activeChar.getNoChannelRemained() > 0 || activeChar.getNoChannel() < 0)
            {
                activeChar.sendPacket(new SystemMessage(SystemMessage.CHATTING_IS_CURRENTLY_PROHIBITED_IF_YOU_TRY_TO_CHAT_BEFORE_THE_PROHIBITION_IS_REMOVED_THE_PROHIBITION_TIME_WILL_BECOME_EVEN_LONGER));
                return;
            }
            activeChar.updateNoChannel(0);
        }

        if(targetPlayer.getMessageRefusal())
        {
            activeChar.sendPacket(new SystemMessage(SystemMessage.THE_PERSON_IS_IN_A_MESSAGE_REFUSAL_MODE));
            return;
        }

        if(!activeChar.getFriendList().getList().containsKey(targetPlayer.getObjectId()))
            return;

        if(!targetPlayer.isConnected())
        {
            activeChar.sendMessage(targetPlayer.getName() + " in offline mode.");
            return;
        }

        Log.LogChat("FRIENDTELL", activeChar.getName(), _receiver, _message);

        if(SpamFilter.getInstance().checkSpam(activeChar, _message, 12) || activeChar.getHWID().equals(targetPlayer.getHWID()))
            targetPlayer.sendPacket(new L2FriendSay(activeChar.getName(), _receiver, _message));
    }
}