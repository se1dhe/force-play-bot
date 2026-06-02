package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.serverpackets.ItemList;

public class RequestItemList extends L2GameClientPacket
{
	@Override
	public void readImpl()
	{}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null || !activeChar.getPlayerAccess().UseInventory || activeChar.isInventoryDisabled())
			return;
		activeChar.sendItemList(true);
	}
}