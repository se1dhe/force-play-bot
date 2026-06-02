package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2HennaInstance;
import l2p.gameserver.serverpackets.HennaUnequipInfo;
import l2p.gameserver.tables.HennaTable;
import l2p.gameserver.templates.L2Henna;

public class RequestHennaUnequipInfo extends L2GameClientPacket
{
	private int _symbolId;

	@Override
	protected void readImpl()
	{
		_symbolId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;
		L2Henna h = HennaTable.getInstance().getTemplate(_symbolId);
		if(h != null)
			player.sendPacket(new HennaUnequipInfo(new L2HennaInstance(h), player));
	}
}