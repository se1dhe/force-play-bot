package l2p.gameserver.clientpackets;

import l2p.gameserver.network.L2GameClient;

public class RequestNetPing extends L2GameClientPacket
{
	public static final int MIN_CLIP_RANGE = 1433;
	public static final int MAX_CLIP_RANGE = 6144;

	private int _timestamp, _clippingRange, _fps;

	@Override
	protected void runImpl()
	{
		L2GameClient client = getClient();
		if(client.getRevision() == 0)
			client.closeNow(false);
		else
			client.onPing(_timestamp, _fps, Math.max(MIN_CLIP_RANGE, Math.min(_clippingRange, MAX_CLIP_RANGE)));
	}

	@Override
	protected void readImpl()
	{
		_timestamp = readD();
		_fps = readD();
		_clippingRange = readD();
	}
}