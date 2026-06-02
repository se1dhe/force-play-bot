package l2p.gameserver.clientpackets;

import l2p.gameserver.serverpackets.QuestList;

public class RequestQuestList extends L2GameClientPacket
{
	@Override
	public void readImpl()
	{}

	@Override
	public void runImpl()
	{
		sendPacket(new QuestList(getClient().getActiveChar()));
	}
}