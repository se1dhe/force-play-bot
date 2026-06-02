package l2p.gameserver.serverpackets;

public class VehicleStart extends L2GameServerPacket
{
	private int _objId;
	private int _state;

	public VehicleStart(int objId, int state)
	{
		_objId = objId;
		_state = state;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_objId);
		writeD(_state);
	}
}