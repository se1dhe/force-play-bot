package l2p.gameserver.serverpackets;

import l2p.gameserver.Config;

public class VersionCheck extends L2GameServerPacket
{
	private byte[] _data;

	public VersionCheck(byte[] data)
	{
		_data = data;
	}

	// TODO [V] - сделать так как в люцере?
	@Override
	public void writeImpl()
	{
		if(_data == null || _data.length == 0)
		{
			writeC(0x00);
		}
		else
		{
			writeC(0x01);
			for(int i = 0; i < 8; i++)
				writeC(_data[i]);
			writeD(0x01);
			writeD(Config.REQUEST_ID);	// Server ID
			writeC(0x01);
			writeD(0x00); // Seed (obfuscation key)
			writeC(0x01);	// Classic
			writeC(0x00);	// Arena
		}
	}

	@Override
	public void writeImplIT()
	{
		if(_data == null || _data.length == 0)
		{
			writeC(0x00);
		}
		else
		{
			writeC(0x01);
			writeB(_data);
			writeD(0x01);
			writeD(0x01);
		}
	}
}