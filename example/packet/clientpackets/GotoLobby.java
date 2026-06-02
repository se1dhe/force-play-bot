package l2p.gameserver.clientpackets;

import l2p.gameserver.serverpackets.CharacterSelectionInfo;

public class GotoLobby extends L2GameClientPacket
{
    @Override
    protected void readImpl()
    {}

    @Override
    protected void runImpl()
    {
        //CharacterSelectionInfo characterSelectionInfo = new CharacterSelectionInfo(getClient().getLoginName(), getClient().getSessionId().playOkID1);
        //sendPacket(characterSelectionInfo);
        final CharacterSelectionInfo csi = new CharacterSelectionInfo(getClient().getLoginName(), getClient().getSessionId().playOkID1);
        getClient().setPacketCharSelection(csi);
        getClient().sendPacket(csi);
        getClient().setCharSelection(csi.getCharInfo());
    }
}