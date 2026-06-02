package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;

public class AnswerCoupleAction extends L2GameClientPacket
{
	private int _charObjId;
	private int _actionId;
	private int _answer;

	@Override
	protected void readImpl()
	{
		_actionId = readD();
		_answer = readD();
		_charObjId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;
	}
}