package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;

public class RequestCrystallizeItemCancel extends L2GameClientPacket
{
    @Override
    protected void readImpl() throws Exception
    {
        // TODO
    }

    @Override
    protected void runImpl() throws Exception
    {
        L2Player activeChar = getClient().getActiveChar();

        if (activeChar == null)
            return;

        activeChar.sendActionFailed();
    }
}