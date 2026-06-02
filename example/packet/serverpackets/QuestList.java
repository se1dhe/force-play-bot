package l2p.gameserver.serverpackets;

import l2p.commons.util.GArray;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.quest.QuestState;

public class QuestList extends L2GameServerPacket
{
	private GArray<int[]> questlist = new GArray<int[]>();
	private static byte[] unk = new byte[128];

	public QuestList(L2Player player)
	{
		if(player == null)
			return;
		for(QuestState quest : player.getAllQuestsStates())
			if(quest != null && ((quest.getQuest().getQuestIntId() < 999 || quest.getQuest().getQuestIntId() > 10000) && quest.getQuest().getQuestIntId() != 255) && quest.isStarted())
				questlist.add(new int[] { quest.getQuest().getQuestIntId(), quest.getRawInt("cond") }); // stage of quest progress
	}

	@Override
	protected final void writeImpl()
	{
		if(questlist == null || questlist.size() == 0)
		{
			writeH(0);
			return;
		}

		writeH(questlist.size());
		for(int[] q : questlist)
		{
			writeD(q[0]);
			writeD(q[1]);
		}

		writeB(unk);
	}

	@Override
	protected final void writeImplIT()
	{
		if(questlist == null || questlist.size() == 0)
		{
			writeH(0);
			return;
		}

		writeH(questlist.size());
		for(int[] q : questlist)
		{
			writeD(q[0]);
			writeD(q[1]);
		}
	}
}