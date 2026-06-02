package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Summon;

public class ExPartyPetWindowDelete extends L2GameServerPacket
{
	private int _summonObjectId;
	private int _ownerObjectId;
	private String _summonName;
	private int _type;

	public ExPartyPetWindowDelete(L2Summon servitor)
	{
		_summonObjectId = servitor.getObjectId();
		_summonName = servitor.getName();
		_ownerObjectId = servitor.getPlayer().getObjectId();
		_type = servitor.getSummonType();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_summonObjectId);
		writeD(_type);
		writeD(_ownerObjectId);
		writeS(_summonName);
	}

	@Override
	protected boolean canWriteIT()
	{
		return false;
	}
}