package l2p.gameserver.clientpackets;

import l2p.gameserver.network.L2GameClient;

public class CharacterSelected extends L2GameClientPacket
{
	private int _index;

	@Override
	protected void readImpl()
	{
		_index = readD();
	}

	@Override
	protected void runImpl()
	{
		L2GameClient client = getClient();
		if(System.currentTimeMillis() < client.getTimeEnter())
		{
			client.sendPacket(client.getPacketCharSelection());
			return;
		}
		client.playerSelected(_index);
	}
}