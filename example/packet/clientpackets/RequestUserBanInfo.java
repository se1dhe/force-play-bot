package l2p.gameserver.clientpackets;

import l2p.gameserver.serverpackets.ExUserBanInfo;

public class RequestUserBanInfo extends L2GameClientPacket
{
	private int unk;

	@Override
	protected void readImpl()
	{
		unk = this.readD();
	}

	@Override
	protected void runImpl()
	{
		sendPacket(new ExUserBanInfo(unk));
	}
}