package l2p.gameserver.clientpackets;

import l2p.gameserver.instancemanager.QuestManager;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.quest.Quest;

public class RequestTutorialLinkHtml extends L2GameClientPacket
{
	// format: cdS
	private int _type;
	String _bypass;

	@Override
	public void readImpl()
	{
		if(!getClient().isITClient())
			_type = readD();
		_bypass = readS();
	}

	@Override
	public void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;

		Quest q = QuestManager.getQuest(255);
		if(q != null)
			player.processQuestEvent(q.getName(), _bypass, null);
	}
}