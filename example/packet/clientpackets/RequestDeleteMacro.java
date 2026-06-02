package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;

public class RequestDeleteMacro extends L2GameClientPacket
{
	private int _id;

	/**
	 * packet type id 0xce
	 *
	 * sample
	 *
	 * ce
	 * d // macro id
	 *
	 * format:		cd
	 * @param decrypt
	 */
	@Override
	public void readImpl()
	{
		_id = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		activeChar.deleteMacro(_id);
	}
}