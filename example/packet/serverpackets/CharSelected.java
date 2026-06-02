package l2p.gameserver.serverpackets;

import l2p.gameserver.GameTimeController;
import l2p.gameserver.model.L2Player;

public class CharSelected extends L2GameServerPacket
{
	private final L2Player _cha;
	private final int _sessionId;

	public CharSelected(L2Player cha, int sessionId)
	{
		_cha = cha;
		_sessionId = sessionId;
	}

	@Override
	protected final void writeImpl()
	{
		writeS(_cha.getName());
		writeD(_cha.getCharId());
		writeS(_cha.getTitle());
		writeD(_sessionId);
		writeD(_cha.getClanId());

		writeD(0x00);

		writeD(_cha.getSex());
		writeD(_cha.getRace().ordinal());
		writeD(_cha.getClassId().getId());

		writeD(0x01);

		writeD(_cha.getX());
		writeD(_cha.getY());
		writeD(_cha.getZ());

		writeF(_cha.getCurrentHp());
		writeF(_cha.getCurrentMp());
		writeQ(_cha.getSp());
		writeQ(_cha.getExp());
		writeD(_cha.getLevel());
		writeD(-_cha.getKarma());
		writeD(_cha.getPkKills());

		// extra info
		writeD(GameTimeController.getInstance().getGameTime()); // in-game time
		writeD(0x00); //
		writeD(0x00); // Default classId

		writeB(new byte[16]);
		writeD(0x00);
		writeD(0x00);
		writeD(0x00);
		writeD(0x00);
		writeD(0x00);
		writeD(0x00);
		writeD(0x00);
		writeD(0x00);
		writeD(0x00);
		writeB(new byte[28]);
		writeD(0x00);
	}

	@Override
	protected final void writeImplIT()
	{
		writeS(_cha.getName());
		writeD(_cha.getCharId());
		writeS(_cha.getTitle());
		writeD(_sessionId);
		writeD(_cha.getClanId());

		writeD(0x00);

		writeD(_cha.getSex());
		writeD(_cha.getRace().ordinal());
		writeD(_cha.getClassId().getId());

		writeD(0x01);

		writeD(_cha.getX());
		writeD(_cha.getY());
		writeD(_cha.getZ());

		writeF(_cha.getCurrentHp());
		writeF(_cha.getCurrentMp());
		writeD(_cha.getSp());
		writeQ(_cha.getExp());
		writeD(_cha.getLevel());
		writeD(_cha.getKarma());
		writeD(_cha.getPkKills());
		writeD(_cha.getINT());
		writeD(_cha.getSTR());
		writeD(_cha.getCON());
		writeD(_cha.getMEN());
		writeD(_cha.getDEX());
		writeD(_cha.getWIT());
		for(int i = 0; i < 30; i++)
			writeD(0);

		writeD(0x00);
		writeD(0x00);

		writeD(GameTimeController.getInstance().getGameTime());

		writeD(0x00);

		writeD(_cha.getClassId().getId());

		writeD(0x00);
		writeD(0x00);
		writeD(0x00);
		writeD(0x00);
	}
}