package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2ManufactureItem;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.serverpackets.RecipeShopItemInfo;

public class RequestRecipeShopMakeInfo extends L2GameClientPacket
{
	private int _manufacturerId;
	private int _recipeId;

	@Override
	protected void readImpl()
	{
		_manufacturerId = readD();
		_recipeId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInDuel())
		{
			activeChar.sendActionFailed();
			return;
		}

		L2Player manufacturer = (L2Player) activeChar.getVisibleObject(_manufacturerId);
		if(manufacturer == null || manufacturer.getPrivateStoreType() != L2Player.STORE_PRIVATE_MANUFACTURE || !manufacturer.isInActingRange(activeChar))
		{
			activeChar.sendActionFailed();
			return;
		}

		int price = -1;
		for(L2ManufactureItem i : manufacturer.getCreateList().getList())
			if(i.getRecipeId() == _recipeId)
			{
				price = i.getCost();
				break;
			}

		if(price == -1)
		{
			activeChar.sendActionFailed();
			return;
		}

		activeChar.sendPacket(new RecipeShopItemInfo(_manufacturerId, _recipeId, price, 0xFFFFFFFF, activeChar));
	}
}