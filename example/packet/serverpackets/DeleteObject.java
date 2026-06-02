package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Object;
import l2p.gameserver.model.L2ObjectsStorage;
import l2p.gameserver.model.L2Player;

public class DeleteObject extends L2GameServerPacket
{
	private int _objectId;

	public DeleteObject(L2Object obj)
	{
		_objectId = obj.getObjectId();
	}

	@Override
	protected final void writeImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null || activeChar.getObjectId() == _objectId)
			return;

		writeD(_objectId);
		writeD(0x01); // Что-то странное. Если объект сидит верхом то при 0 он сперва будет ссажен, при 1 просто пропадет.
	}

	@Override
	public String getType()
	{
		return super.getType() + " " + L2ObjectsStorage.findObject(_objectId) + " (" + _objectId + ")";
	}
}