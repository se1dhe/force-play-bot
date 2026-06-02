package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.model.L2Party;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2p.gameserver.multilang.CustomMessage;
import l2p.gameserver.network.L2GameClient;
import l2p.gameserver.serverpackets.CharacterSelectionInfo;
import l2p.gameserver.serverpackets.RestartResponse;
import l2p.gameserver.serverpackets.SystemMessage;

public class RequestRestart extends L2GameClientPacket
{
    @Override
    public void readImpl()
    {
    }

    @Override
    public void runImpl()
    {
        L2Player player = getClient().getActiveChar();

        if(player == null)
            return;
        if(player.getEnchantScroll() != null)
        {
            sendPacket(RestartResponse.valueOf(false));
            player.sendActionFailed();
            return;
        }
        if(player.isInTransaction())
        {
            player.sendPacket(new SystemMessage(SystemMessage.ALREADY_TRADING));
            sendPacket(RestartResponse.valueOf(false));
            player.sendActionFailed();
            return;
        }

        if(player.isInOlympiadMode())
        {
            if(Config.OLY_NO_ER || player.olyGameNotStart())
            {
                player.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestRestart.Olympiad", player));
                sendPacket(RestartResponse.valueOf(false));
                player.sendActionFailed();
                return;
            }
        }

        if(player.isReg() || player.isInStriderRace())
        {
            player.sendMessage(player.isLangRus() ? "Вы участник регистрационного события и не можете покинуть игру." : "You can't do it by participating in the event.");
            sendPacket(RestartResponse.valueOf(false));
            player.sendActionFailed();
            return;
        }

        if(player.inObserverMode())
        {
            player.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestRestart.Observer", player));
            sendPacket(RestartResponse.valueOf(false));
            player.sendActionFailed();
            return;
        }

        if(player.isInCombat() && !player.isGM())
        {
            player.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_RESTART_WHILE_IN_COMBAT));
            sendPacket(RestartResponse.valueOf(false));
            player.sendActionFailed();
            return;
        }

        if(player.isFishing())
        {
            player.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_DO_ANYTHING_ELSE_WHILE_FISHING));
            sendPacket(RestartResponse.valueOf(false));
            player.sendActionFailed();
            return;
        }

        if(player.isBlocked() && !player.isFlying())
        {
            player.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestRestart.OutOfControl", player));
            sendPacket(RestartResponse.valueOf(false));
            player.sendActionFailed();
            return;
        }

        if(player.isFestivalParticipant())
        {
            if(SevenSignsFestival.getInstance().isFestivalInitialized())
            {
                player.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestRestart.Festival", player));
                sendPacket(RestartResponse.valueOf(false));
                player.sendActionFailed();
                return;
            }
            L2Party playerParty = player.getParty();

            if(playerParty != null)
                playerParty.broadcastMessageToPartyMembers(player.getName() + " has been removed from the upcoming festival.");
        }

        L2GameClient client = getClient();
        if(client == null)
            return;

        client.setTimeEnter(Config.RESTART_ENTER_DELAY);
        client.setState(L2GameClient.GameClientState.AUTHED);
        player.restart();
        sendPacket(RestartResponse.valueOf(true));
        CharacterSelectionInfo csi = new CharacterSelectionInfo(client.getLoginName(), client.getSessionId().playOkID1);
        client.setPacketCharSelection(csi);
        sendPacket(csi);
        client.setCharSelection(csi.getCharInfo());
    }
}