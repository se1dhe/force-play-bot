package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Player;

public class PrivateStoreMsgSell extends L2GameServerPacket
{
	private int char_obj_id;
	private String store_name;

	public PrivateStoreMsgSell(L2Player player, boolean check)
	{
		char_obj_id = player.getObjectId();
		store_name = player.getTradeList() == null || (check && player.getTradeList().isSpamSell()) ? "" : player.getTradeList().getSellStoreName();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(char_obj_id);
		writeS(store_name);
	}
}