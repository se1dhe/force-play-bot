package l2p.gameserver.clientpackets;

import l2p.gameserver.listener.actor.OnAnswerListener;
import l2p.gameserver.model.L2Player;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DlgAnswer extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(DlgAnswer.class);

	private int _answer;
	private int _requestId;

	@Override
	protected void readImpl()
	{
		readD();
		_answer = readD();
		_requestId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.isKeyBlocked())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(_requestId == 2)
			activeChar.reviveAnswer(_answer);
		else
		{
			Pair<Integer, OnAnswerListener> entry = activeChar.getAskListener(true);
			if(entry == null || entry.getKey() != _requestId)
				return;

			OnAnswerListener listener = entry.getValue();
			if(_answer == 1)
				listener.sayYes();
			else
				listener.sayNo();
		}
	}
}