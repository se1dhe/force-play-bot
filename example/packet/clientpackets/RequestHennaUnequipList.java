package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.serverpackets.HennaUnequipList;

public class RequestHennaUnequipList extends L2GameClientPacket
{
    @Override
    protected void readImpl()
    {}

    @Override
    protected void runImpl()
    {
        L2Player activeChar = getClient().getActiveChar();
        if(activeChar == null)
            return;
        activeChar.sendPacket(new HennaUnequipList(activeChar));
    }
}