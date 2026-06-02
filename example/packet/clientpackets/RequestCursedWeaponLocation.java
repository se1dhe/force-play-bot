package l2p.gameserver.clientpackets;

import l2p.commons.util.GArray;
import l2p.gameserver.instancemanager.CursedWeaponsManager;
import l2p.gameserver.model.CursedWeapon;
import l2p.gameserver.model.L2Character;
import l2p.gameserver.serverpackets.ExCursedWeaponLocation;
import l2p.gameserver.serverpackets.ExCursedWeaponLocation.CursedWeaponInfo;
import l2p.gameserver.utils.Location;

public class RequestCursedWeaponLocation extends L2GameClientPacket
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

		GArray<CursedWeaponInfo> list = new GArray<CursedWeaponInfo>();
		for(CursedWeapon cw : CursedWeaponsManager.getInstance().getCursedWeapons())
		{
			Location pos = cw.getWorldPosition();
			if(pos != null)
				list.add(new CursedWeaponInfo(pos, cw.getItemId(), cw.isActivated() ? 1 : 0));
		}

		activeChar.sendPacket(new ExCursedWeaponLocation(list));
	}
}