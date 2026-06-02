package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.serverpackets.RecipeShopSellList;

// Deprecated?
public class RequestRecipeShopManageCancel extends L2GameClientPacket
{
	@Override
	public void readImpl()
	{}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null || activeChar.getTarget() == null)
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

		if(!activeChar.getTarget().isPlayer())
		{
			activeChar.sendActionFailed();
			return;
		}

		L2Player target = (L2Player) activeChar.getTarget();
		activeChar.sendPacket(new RecipeShopSellList(activeChar, target));
	}
}