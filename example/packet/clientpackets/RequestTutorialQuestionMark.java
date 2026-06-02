package l2p.gameserver.clientpackets;

import l2p.gameserver.instancemanager.QuestManager;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.quest.Quest;

public class RequestTutorialQuestionMark extends L2GameClientPacket
{
	int _number = 0;

	@Override
	public void readImpl()
	{
		if(!getClient().isITClient())
			readC(); // is Quest?
		_number = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;

		Quest q = QuestManager.getQuest(255);
		if(q != null)
			player.processQuestEvent(q.getName(), "QM" + _number, null);
	}
}