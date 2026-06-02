package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2Player;

public class MagicSkillCanceled extends L2GameServerPacket
{
	private final int _casterId;
	private final int _casterX;
	private final int _casterY;

	public MagicSkillCanceled(int objectId, int x, int y)
	{
		_casterId = objectId;
		_casterX = x;
		_casterY = y;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_casterId);
	}

	@Override
	public L2GameServerPacket packet(L2Player player)
	{
		if(player != null)
		{
			if(player.animRange() < 0)
				return null;

			if(player.animRange() == 0)
				return _casterId == player.getObjectId() ? super.packet(player) : null;

			L2Character observer = player.getObservePoint();
			if(observer == null)
				observer = player;

			return observer.getDistance(_casterX, _casterY) < player.animRange() ? super.packet(player) : null;
		}
		return super.packet(player);
	}
}