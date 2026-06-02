package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.skills.effects.EffectCubic;

public class ExUserInfoCubic extends L2GameServerPacket
{
	private final int _objectId, _agationId;
	private final EffectCubic[] _cubics;

	public ExUserInfoCubic(L2Player character)
	{
		_objectId = character.getObjectId();
		_cubics = character.getCubics().toArray(new EffectCubic[character.getCubics().size()]);
		_agationId = character.getAgathionId();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_objectId);
		writeH(_cubics.length);
		for(EffectCubic cubic : _cubics)
			writeH(cubic == null ? 0 : cubic.getId());
		writeD(_agationId);
	}

	@Override
	protected boolean canWriteIT()
	{
		return false;
	}
}