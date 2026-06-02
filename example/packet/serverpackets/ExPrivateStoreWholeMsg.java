package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Player;
import org.apache.commons.lang3.StringUtils;

public class ExPrivateStoreWholeMsg extends L2GameServerPacket
{
	private final int _objId;
	private final String _name;

	/**
	 * Название личного магазина продажи
	 * @param player
	 */
	public ExPrivateStoreWholeMsg(L2Player player, boolean showName)
	{
		_objId = player.getObjectId();
		_name = showName ? StringUtils.defaultString(player.getTradeList().getSellStoreName()) : StringUtils.EMPTY;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_objId);
		writeS(_name);
	}

	@Override
	protected boolean canWriteIT()
	{
		return false;
	}
}