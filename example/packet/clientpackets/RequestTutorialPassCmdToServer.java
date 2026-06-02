package l2p.gameserver.clientpackets;

import l2p.gameserver.instancemanager.QuestManager;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.quest.Quest;

public class RequestTutorialPassCmdToServer extends L2GameClientPacket
{
	String _bypass = null;

	@Override
	public void readImpl()
	{
		_bypass = readS();
	}

	@Override
	public void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;

		Quest tutorial = QuestManager.getQuest(255);

		if(tutorial != null)
			player.processQuestEvent(tutorial.getName(), _bypass, null);
	}
}