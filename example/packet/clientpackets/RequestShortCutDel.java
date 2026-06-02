package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;

public class RequestShortCutDel extends L2GameClientPacket
{
	private int _slot;
	private int _page;

	@Override
	public void readImpl()
	{
		int id = readD();
		_slot = id % 12;
		_page = id / 12;
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		activeChar.deleteShortCut(_slot, _page);
	}
}