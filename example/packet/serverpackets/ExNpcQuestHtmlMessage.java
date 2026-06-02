package l2p.gameserver.serverpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO [V] - реализовать?
public class ExNpcQuestHtmlMessage extends NpcHtmlMessage
{
	private static final Logger _log = LoggerFactory.getLogger(ExNpcQuestHtmlMessage.class);

	private int _questId;

	public ExNpcQuestHtmlMessage(int npcObjId, int questId)
	{
		super(npcObjId);
		_questId = questId;
	}

	@Override
	protected void writeImpl()
	{
		if(_html != null)
		{
			writeD(_npcObjId);
			writeS(_html);
			writeD(_questId);
		}
	}
}