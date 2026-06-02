package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.serverpackets.RecipeItemMakeInfo;

public class RequestRecipeItemMakeInfo extends L2GameClientPacket
{
	private int _id;

	@Override
	protected void readImpl()
	{
		_id = readD();
	}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		sendPacket(new RecipeItemMakeInfo(_id, activeChar, 0xffffffff));
	}
}