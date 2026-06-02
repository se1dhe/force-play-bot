package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Player;

public class ExSetCompassZoneCode extends L2GameServerPacket
{
	public static int ZONE_ALTERED = 8; // 9, 10 - Danger Area???
	public static int ZONE_SIEGE = 11;
	public static int ZONE_PEACE = 12;
	public static int ZONE_SS = 13;
	public static int ZONE_PVP = 14; // 1, 2, 3, 4, 5, 6, 7
	public static int ZONE_GENERAL_FIELD = 15; //0 и > 15

	int _zone = -1;

	public ExSetCompassZoneCode(L2Player player)
	{
		if(player.isInDangerArea())
			_zone = ZONE_ALTERED;
		else if(player.isOnSiegeField())
			_zone = ZONE_SIEGE;
		else if(player.isInCombatZone())
			_zone = ZONE_PVP;
		else if(player.isInZonePeace())
			_zone = ZONE_PEACE;
		else if(player.isInSSZone())
			_zone = ZONE_SS;
		else
			_zone = ZONE_GENERAL_FIELD;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_zone);
	}
}