package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Character;

public class UserAck extends L2GameServerPacket
{
	private int _chaId;
	private int _unk1;
	private int _unk2;

	public UserAck(L2Character cha, int unk1, int unk2)
	{
		_chaId = cha.getObjectId();
		_unk1 = unk1;
		_unk2 = unk2;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_chaId);
		writeD(_unk1);
	}
}