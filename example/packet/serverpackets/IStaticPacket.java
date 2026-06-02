package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Player;

public interface IStaticPacket
{
	L2GameServerPacket packet(L2Player player);
}