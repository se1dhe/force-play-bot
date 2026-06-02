package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Summon;
import l2p.gameserver.serverpackets.updatetype.NpcInfoType;

public class ExPetInfo extends AbstractNpcPacket
{
	public ExPetInfo(L2Summon summon, L2Player player)
	{
		if(summon == null)
			return;
		_npcId = summon.getTemplate().npcId;
		_isAttackable = summon.isAutoAttackable(player);
		_rhand = 0;
		_lhand = 0;
		_enchantEffect = 0;
		_showName = true;
		_name = summon.getName();
		_title = summon.getTitle();
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