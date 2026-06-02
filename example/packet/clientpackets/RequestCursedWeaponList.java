package l2p.gameserver.clientpackets;

import l2p.commons.util.GArray;
import l2p.gameserver.instancemanager.CursedWeaponsManager;
import l2p.gameserver.model.L2Character;
import l2p.gameserver.serverpackets.ExCursedWeaponList;

public class RequestCursedWeaponList extends L2GameClientPacket
{
	@Override
	public void readImpl()
	{}

	@Override
	public void runImpl()
	{
		L2Character activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		GArray<Integer> list = new GArray<Integer>();
		for(int id : CursedWeaponsManager.getInstance().getCursedWeaponsIds())
			list.add(id);

		activeChar.sendPacket(new ExCursedWeaponList(activeChar.getLoc(), list));
	}
}