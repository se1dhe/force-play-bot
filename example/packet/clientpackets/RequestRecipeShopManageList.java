package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2ManufactureList;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.serverpackets.RecipeShopManageList;

public class RequestRecipeShopManageList extends L2GameClientPacket
{
    @Override
    public void readImpl()
    {
    }

    @Override
    public void runImpl()
    {
        L2Player activeChar = getClient().getActiveChar();
        if(activeChar == null)
            return;

		if(activeChar.isInDuel())
        {
            activeChar.sendActionFailed();
            return;
        }

        if(activeChar.isAlikeDead())
        {
            activeChar.sendActionFailed();
            return;
        }

        if(activeChar.getCreateList() == null)
            activeChar.setCreateList(new L2ManufactureList());

        if(activeChar.getPrivateStoreType() == L2Player.STORE_PRIVATE_MANUFACTURE)
        {
            activeChar.sendPacket(new RecipeShopManageList(activeChar, true));
            activeChar.standUp();
        }
    }
}
