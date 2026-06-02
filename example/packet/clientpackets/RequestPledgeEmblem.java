package l2p.gameserver.clientpackets;

import l2p.gameserver.cache.CrestCache;
import l2p.gameserver.serverpackets.ExPledgeEmblem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestPledgeEmblem extends L2GameClientPacket
{
	private static Logger _log = LoggerFactory.getLogger(RequestPledgeEmblem.class);

	private int _crestId;
	private int _pledgeId = 0;

	@Override
	public void readImpl()
	{
		_crestId = readD();
		if(!getClient().isITClient())
			_pledgeId = readD();
	}

	@Override
	public void runImpl()
	{
		if(_crestId == 0)
			return;

		if(getClient().isITClient())
		{
			byte[] data = CrestCache.getInstance().getPledgeCrestLarge(_crestId);
			if(data != null)
			{
				ExPledgeEmblem pcl = new ExPledgeEmblem(_crestId, data);
				sendPacket(pcl);
			}
		}
		else
		{
			if(_crestId == 0 || _pledgeId == 0)
				return;

			int serverId = getClient().getServerId();
			int pledgeCrestLargeId = CrestCache.getInstance().getPledgeCrestLargeId(_pledgeId);
			byte[] data = CrestCache.getInstance().getPledgeCrestLarge(pledgeCrestLargeId);
			if(pledgeCrestLargeId == _crestId && data != null && data.length > 0)
				sendPacket(ExPledgeEmblem.packets(serverId, _pledgeId, pledgeCrestLargeId, data));
		}
	}

	@Override
	public boolean isFilter()
	{
		return false;
	}
}