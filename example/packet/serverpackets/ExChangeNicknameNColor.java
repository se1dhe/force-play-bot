package l2p.gameserver.serverpackets;

public class ExChangeNicknameNColor extends L2GameServerPacket
{
	private int _itemObjId;

	public ExChangeNicknameNColor(int itemObjId)
	{
		_itemObjId = itemObjId;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_itemObjId);
	}

	@Override
	protected boolean canWriteIT()
	{
		return false;
	}
}