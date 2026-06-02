package l2p.gameserver.clientpackets;

import l2p.gameserver.cache.CrestCache;
import l2p.gameserver.serverpackets.PledgeCrest;

public class RequestPledgeCrest extends L2GameClientPacket
{
	private int _crestId;

	@Override
	public void readImpl()
	{
		_crestId = readD();
	}

	@Override
	public void runImpl()
	{
		if(_crestId == 0)
			return;
		byte[] data = CrestCache.getInstance().getPledgeCrest(_crestId);
		if(data != null)
		{
			PledgeCrest pc = new PledgeCrest(_crestId, data);
			sendPacket(pc);
		}
	}

	@Override
	public boolean isFilter()
	{
		return false;
	}
}