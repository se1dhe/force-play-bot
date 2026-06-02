package l2p.gameserver.serverpackets;

import gnu.trove.TIntHashSet;
import l2p.gameserver.instancemanager.RaidBossSpawnManager;
import l2p.gameserver.model.L2Spawn;

import java.util.Map;

public class ExRaidBossSpawnInfo extends L2GameServerPacket
{
	private final int[] _aliveBosses;

	public ExRaidBossSpawnInfo()
	{
		RaidBossSpawnManager raidBossSpawnManager = RaidBossSpawnManager.getInstance();
		Map<Integer, L2Spawn> allBosses = raidBossSpawnManager.getSpawnTable();
		TIntHashSet aliveBosses = new TIntHashSet();
		for(Integer bossId : allBosses.keySet())
		{
			RaidBossSpawnManager.Status status = raidBossSpawnManager.getRaidBossStatusId(bossId);
			if(status == RaidBossSpawnManager.Status.ALIVE)
				aliveBosses.add(bossId);
		}
		_aliveBosses = aliveBosses.toArray();
	}

	@Override
	protected final void writeImpl()
	{
		writeDD(_aliveBosses, true);
	}

	@Override
	protected boolean canWriteIT()
	{
		return false;
	}
}