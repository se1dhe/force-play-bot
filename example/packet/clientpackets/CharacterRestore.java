package l2p.gameserver.clientpackets;

import l2p.gameserver.network.L2GameClient;
import l2p.gameserver.serverpackets.CharacterSelectionInfo;

public class CharacterRestore extends L2GameClientPacket
{
	private int _charSlot;

	@Override
	protected void readImpl()
	{
		_charSlot = readD();
	}

	@Override
	protected void runImpl()
	{
		L2GameClient client = getClient();
		if(client.getActiveChar() != null)
			return;

		int charId = client.getObjectIdByIndex(_charSlot);
		if(charId <= 0)
			return;

		client.markDeleteCharByObjId(charId, false);

		CharacterSelectionInfo csi = new CharacterSelectionInfo(client.getLoginName(), client.getSessionId().playOkID1);
		client.setPacketCharSelection(csi);
		sendPacket(csi);
		client.setCharSelection(csi.getCharInfo());
	}
}