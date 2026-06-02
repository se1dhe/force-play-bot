package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.skills.AbnormalEffect;

/**
 * @reworked by Bonux
 **/
public class ExUserInfoAbnormalVisualEffect extends L2GameServerPacket
{
	private final int _objectId;
	private final int _transformId;
	private final AbnormalEffect[] _abnormalEffects;

	public ExUserInfoAbnormalVisualEffect(L2Player player)
	{
		_objectId = player.getObjectId();
		_transformId = player.getTransformation();
		_abnormalEffects = player.getVisualAbnormalEffects(player);
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_objectId);
		writeD(_transformId);
		writeD(_abnormalEffects.length);
		for(AbnormalEffect abnormal : _abnormalEffects)
			writeH(abnormal.getClientId());
	}

	@Override
	protected boolean canWriteIT()
	{
		return false;
	}
}