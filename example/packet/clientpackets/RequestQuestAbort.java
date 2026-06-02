package l2p.gameserver.clientpackets;

import l2p.gameserver.instancemanager.QuestManager;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.quest.Quest;
import l2p.gameserver.model.quest.QuestState;
import l2p.gameserver.serverpackets.SystemMessage;

public class RequestQuestAbort extends L2GameClientPacket
{
	private int _QuestID;

	@Override
	public void readImpl()
	{
		_QuestID = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		Quest quest = QuestManager.getQuest(_QuestID);
		if(activeChar == null || quest == null)
			return;
		QuestState qs = activeChar.getQuestState(quest.getName());
		if(qs != null)
		{
			QuestManager.removeHwidFromQuest(quest.getQuestIntId(), activeChar.getHWID(), activeChar.getObjectId());
			qs.abortQuest();
			activeChar.sendPacket(new SystemMessage(SystemMessage.S1_IS_ABORTED).addString(quest.getDescr()));
		}
	}
}