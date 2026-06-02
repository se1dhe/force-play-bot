package l2p.gameserver.clientpackets;

import l2p.gameserver.RecipeController;
import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.network.L2GameClient;

public class RequestRecipeShopMakeItem extends L2GameClientPacket
{
    private int _id;
    private int _recipeId;
    @SuppressWarnings("unused")
    private int _price;

    /**
     * packet type id 0xac format: cd
     */
    @Override
    public void readImpl()
    {
        _id = readD();
        _recipeId = readD();
        _price = getClient().isITClient() ? readD() : (int) readQ();
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

        L2Player manufacturer = (L2Player) activeChar.getVisibleObject(_id);
        if(manufacturer == null || manufacturer.getPrivateStoreType() != L2Player.STORE_PRIVATE_MANUFACTURE || !manufacturer.isInActingRange(activeChar))
            return;

        RecipeController.getInstance().requestManufactureItem(manufacturer, activeChar, _price, _recipeId);
    }
}
