package l2p.gameserver.serverpackets;

public class ExPledgeEmblem extends L2GameServerPacket
{
	private static final int SIZE = 14336;
	private static final int TOTAL_SIZE = 65664;

	private int _serverId, _clanId, _crestId, _crestPart, _totalSize;
	private byte[] _data;

	public ExPledgeEmblem(int serverId, int clanId, int crestId, int crestPart, int totalSize, byte[] data)
	{
		_serverId = serverId;
		_clanId = clanId;
		_crestId = crestId;
		_crestPart = crestPart;
		_totalSize = totalSize;
		_data = data;
	}

	public static ExPledgeEmblem[] packets(int serverId, int pledgeId, int pledgeCrestLargeId, byte[] data)
	{
		ExPledgeEmblem[] arrexPledgeEmblem = new ExPledgeEmblem[5];
		for(int i = 0; i < 5; i++)
		{
			int n4 = i * SIZE;
			if(n4 < data.length)
			{
				int size = Math.min(data.length - n4, SIZE);
				byte[] chunk = new byte[size];
				System.arraycopy(data, n4, chunk, 0, size);
				arrexPledgeEmblem[i] = new ExPledgeEmblem(serverId, pledgeId, pledgeCrestLargeId, i, data.length, chunk);
			}
			else
				arrexPledgeEmblem[i] = new ExPledgeEmblem(serverId, pledgeId, pledgeCrestLargeId, i, data.length, new byte[]{0});
		}
		return arrexPledgeEmblem;
	}

	public ExPledgeEmblem(int crestId, byte[] data)
	{
		_crestId = crestId;
		_data = data;
	}

	@Override
	protected boolean canWrite()
	{
		return _clanId > 0;
	}

	@Override
	protected boolean canWriteIT()
	{
		return _clanId == 0;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_serverId);
		writeD(_clanId);
		writeD(_crestId);
		writeD(_crestPart);
		writeD(_totalSize);
		writeD(_data.length);
		writeB(_data);
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(0x00);
		writeD(_crestId);
		writeD(_data.length);
		writeB(_data);
	}
}