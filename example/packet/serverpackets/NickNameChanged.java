package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Player;

public class NickNameChanged extends L2GameServerPacket
{
	private final int objectId;
	private final String title;

	public NickNameChanged(L2Player cha)
	{
		objectId = cha.getObjectId();
		if(cha.getTransformationTitle() != null)
			title = cha.getTransformationTitle();
		else
			title = cha.getTitle();
	}

	@Override
	protected void writeImpl()
	{
		writeD(objectId);
		writeS(title);
	}
}