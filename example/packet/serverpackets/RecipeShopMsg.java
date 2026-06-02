package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Player;

public class RecipeShopMsg extends L2GameServerPacket
{
	private int _chaObjectId;
	private String _chaStoreName;

	public RecipeShopMsg(L2Player player, boolean check)
	{
		if(player.getCreateList() == null || player.getCreateList().getStoreName() == null)
			return;
		_chaObjectId = player.getObjectId();
		_chaStoreName = check && player.getCreateList().isSpam() ? "" : player.getCreateList().getStoreName();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_chaObjectId);
		writeS(_chaStoreName);
	}
}