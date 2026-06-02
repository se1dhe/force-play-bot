package l2p.gameserver.clientpackets;

import l2p.gameserver.RecipeController;
import l2p.gameserver.model.L2Player;

public class RequestRecipeBookOpen extends L2GameClientPacket
{
	private boolean isDwarvenCraft = true;

	@Override
	public void readImpl()
	{
		if(_buf.hasRemaining())
			isDwarvenCraft = readD() == 0;
		else
			return;
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		RecipeController.getInstance().requestBookOpen(activeChar, isDwarvenCraft);
	}
}