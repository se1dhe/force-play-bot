package l2p.gameserver.clientpackets;

import l2p.gameserver.RecipeController;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2RecipeList;
import l2p.gameserver.serverpackets.RecipeBookItemList;

public class RequestRecipeBookDestroy extends L2GameClientPacket
{
    private int _RecipeID;

    @Override
    public void readImpl()
    {
        _RecipeID = readD();
    }

    @Override
    public void runImpl()
    {
        L2Player activeChar = getClient().getActiveChar();
        if(activeChar == null)
            return;

        if(activeChar.getPrivateStoreType() == L2Player.STORE_PRIVATE_MANUFACTURE)
        {
            activeChar.sendPacket(Msg.YOU_MAY_NOT_ALTER_YOUR_RECIPE_BOOK_WHILE_ENGAGED_IN_MANUFACTURING);
            return;
        }

        L2RecipeList rp = RecipeController.getInstance().getRecipeList(_RecipeID);

        if(rp == null)
        {
            activeChar.sendActionFailed();
            return;
        }

        activeChar.unregisterRecipe(_RecipeID);

        RecipeBookItemList response = new RecipeBookItemList(rp.isDwarvenRecipe(), (int) activeChar.getCurrentMp());

        response.setRecipes(activeChar.getDwarvenRecipeBook());

        activeChar.sendPacket(response);
    }
}
