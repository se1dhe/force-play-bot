package l2p.gameserver.clientpackets;

import l2p.gameserver.instancemanager.QuestManager;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.quest.Quest;
import l2p.gameserver.model.quest.QuestState;

/**
 * @author VISTALL
 * @date 14:47/26.02.2011
 */
// TODO [V] - реализовать
public class RequestAddExpandQuestAlarm extends L2GameClientPacket
{
	private int _questId;

	@Override
	protected void readImpl() throws Exception
	{
		_questId = readD();
	}

	@Override
	protected void runImpl() throws Exception
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;

		Quest quest = QuestManager.getQuest(_questId);
		if(quest == null)
			return;

		QuestState state = player.getQuestState(quest.getName());
		if(state == null)
			return;

		//player.sendPacket(new ExQuestNpcLogList(state));
	}
}