package l2p.gameserver.serverpackets;

import l2p.gameserver.model.entity.SevenSigns;

public class SSQInfo extends L2GameServerPacket
{
	@Override
	protected final void writeImpl()
	{
		writeH(SevenSigns.getInstance().getSky());
	}
}