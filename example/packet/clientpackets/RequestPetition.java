package l2p.gameserver.clientpackets;

import l2p.gameserver.instancemanager.PetitionManager;
import l2p.gameserver.model.L2Player;

public final class RequestPetition extends L2GameClientPacket
{
	private String _content;
	private int _type;

	@Override
	protected void readImpl()
	{
		_content = readS(4096);
		_type = readD();
	}

	@Override
	protected void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;

		PetitionManager.getInstance().handle(player, _type, _content);
	}
}