package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Summon;
import l2p.gameserver.serverpackets.updatetype.NpcInfoType;

public class SummonInfo extends AbstractNpcPacket
{
	public SummonInfo(L2Summon summon, L2Character creature)
	{
		if(summon == null)
			return;
		L2Player player = summon.getPlayer();
		if(player != null && player.isInvisible())
			return;

		_npcId = summon.getTemplate().npcId;
		_isAttackable = summon.isAutoAttackable(creature);
		_rhand = 0;
		_lhand = 0;
		if(summon.isPet())
			_name = summon.getName();
		_title = summon.getTitle();
		_titleColor = summon.isSummon() ? 1 : 0;
		_showSpawnAnimation = summon.getSpawnAnimation();
		_isPet = true;
		setValues(summon, NpcInfoType.VALUES);
	}

	@Override
	protected boolean canWrite()
	{
		return can_writeImpl;
	}

	@Override
	protected void writeImpl()
	{
		writeData();
	}

	@Override
	protected boolean canWriteIT()
	{
		return false;
	}
}