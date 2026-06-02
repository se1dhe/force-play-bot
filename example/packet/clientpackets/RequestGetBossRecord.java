package l2p.gameserver.clientpackets;

import java.util.Map.Entry;

import javolution.util.FastMap;
import l2p.commons.util.GArray;
import l2p.gameserver.instancemanager.RaidBossSpawnManager;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.serverpackets.ExGetBossRecord;
import l2p.gameserver.serverpackets.ExGetBossRecord.BossRecordInfo;

/**
 * Format: (ch) d
 */
public class RequestGetBossRecord extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int _bossID;

	@Override
	public void readImpl()
	{
		_bossID = readD(); // always 0?
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		int totalPoints = 0;
		int ranking = 0;

		if(activeChar == null)
			return;

		GArray<BossRecordInfo> list = new GArray<BossRecordInfo>();
		FastMap<Integer, Integer> points = RaidBossSpawnManager.getInstance().getPointsForOwnerId(activeChar.getObjectId());
		if(points != null && !points.isEmpty())
			for(Entry<Integer, Integer> e : points.entrySet())
				switch(e.getKey())
				{
					case -1: // RaidBossSpawnManager.KEY_RANK
						ranking = e.getValue();
						break;
					case 0: //  RaidBossSpawnManager.KEY_TOTAL_POINTS
						totalPoints = e.getValue();
						break;
					default:
						list.add(new BossRecordInfo(e.getKey(), e.getValue(), 0));
				}

		activeChar.sendPacket(new ExGetBossRecord(ranking, totalPoints, list));
	}
}