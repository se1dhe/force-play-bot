package l2p.gameserver.serverpackets;

import l2p.gameserver.model.entity.SevenSigns;

public class ShowMiniMap extends L2GameServerPacket
{
	private int _mapId, period;

	public ShowMiniMap(int mapId)
	{
		_mapId = mapId;
		period = SevenSigns.getInstance().getCurrentPeriod();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_mapId);
		writeD(period);
	}
}