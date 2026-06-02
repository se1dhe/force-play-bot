package l2p.gameserver.clientpackets;

import l2p.gameserver.cache.CrestCache;
import l2p.gameserver.serverpackets.AllianceCrest;

public class RequestAllyCrest extends L2GameClientPacket
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
		byte[] data = CrestCache.getInstance().getAllyCrest(_crestId);
		if(data != null)
		{
			AllianceCrest ac = new AllianceCrest(_crestId, data);
			sendPacket(ac);
		}
	}

	@Override
	public boolean isFilter()
	{
		return false;
	}
}