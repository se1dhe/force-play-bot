package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Character;
import l2p.gameserver.skills.AbnormalEffect;

/**
 * @reworked by Bonux
 **/
public class NpcInfoAbnormalVisualEffect extends L2GameServerPacket
{
	private final int _objectId;
	private final int _transformId;
	private final AbnormalEffect[] _abnormalEffects;

	public NpcInfoAbnormalVisualEffect(L2Character npc)
	{
		_objectId = npc.getObjectId();
		_transformId = 0;
		_abnormalEffects = npc.getAbnormalEffects();
	}

	@Override
	protected boolean canWriteIT()
	{
		return false;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_objectId);
		writeD(_transformId);
		writeH(_abnormalEffects.length);
		for(AbnormalEffect abnormal : _abnormalEffects)
			writeH(abnormal.getClientId());
	}
}