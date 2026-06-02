package l2p.gameserver.serverpackets;

import l2p.gameserver.Config;

public class AllianceCrest extends L2GameServerPacket
{
	private int _crestId;
	private byte[] _data;

	public AllianceCrest(int crestId, byte[] data)
	{
		_crestId = crestId;
		_data = data;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(Config.REQUEST_ID);
		writeD(_crestId);
		writeD(_data.length);
		writeB(_data);
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_crestId);
		writeD(_data.length);
		writeB(_data);
	}
}