package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;

// TODO [V] - нужно?
public class AnswerPartyLootModification extends L2GameClientPacket
{
	public int _answer;

	@Override
	protected void readImpl()
	{
		_answer = readD();
	}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		/*L2Party party = activeChar.getParty();
		if(party != null)
			party.answerLootChangeRequest(activeChar, _answer == 1);

		 */
	}
}