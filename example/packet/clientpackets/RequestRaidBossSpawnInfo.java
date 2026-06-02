package l2p.gameserver.clientpackets;

import l2p.gameserver.serverpackets.ExRaidBossSpawnInfo;

public class RequestRaidBossSpawnInfo extends L2GameClientPacket
{
	@Override
	protected void readImpl() throws Exception
	{
		//
	}

	@Override
	protected void runImpl() throws Exception
	{
		sendPacket(new ExRaidBossSpawnInfo());
	}
}