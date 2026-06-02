package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Player;

public class PartySmallWindowDelete extends L2GameServerPacket
{
	private int member_obj_id;
	private String member_name;

	public PartySmallWindowDelete(L2Player member)
	{
		member_obj_id = member.getObjectId();
		member_name = member.getName();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(member_obj_id);
		writeS(member_name);
	}
}