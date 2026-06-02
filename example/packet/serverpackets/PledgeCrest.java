package l2p.gameserver.serverpackets;

import l2p.gameserver.Config;

public class PledgeCrest extends L2GameServerPacket
{
	private final int _crestId;
	private final byte[] _data;

	public PledgeCrest(int crestId, byte[] data)
	{
		_crestId = crestId;
		_data = data;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(Config.REQUEST_ID);
		writeD(_crestId);
		if(_data != null)
		{
			writeD(_data.length);
			writeB(_data);
		}
		else
			writeD(0);
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_crestId);
		if(_data != null)
		{
			writeD(_data.length);
			writeB(_data);
		}
		else
			writeD(0);
	}
}
