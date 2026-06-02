package l2p.gameserver.serverpackets;

import l2p.commons.util.GArray;
import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2Player;

import java.util.Collection;

public class MagicSkillLaunched extends L2GameServerPacket
{
	private final int _casterId;
	private final int _casterX;
	private final int _casterY;
	private final int _skillIdClassic;
	private final int _skillIdIT;
	private final int _skillLevel;
	private final Collection<Integer> _targets;

	public MagicSkillLaunched(L2Character caster, int skillIdClassic, int skillIdIT, int skillLevel, L2Character target)
	{
		_casterId = caster.getObjectId();
		_casterX = caster.getX();
		_casterY = caster.getY();
		_skillIdClassic = skillIdClassic;
		_skillIdIT = skillIdIT;
		_skillLevel = skillLevel;
		_targets = new GArray<Integer>();
		_targets.add(target.getObjectId());
	}

	public MagicSkillLaunched(L2Character caster, int skillId, int skillLevel, L2Character target)
	{
		_casterId = caster.getObjectId();
		_casterX = caster.getX();
		_casterY = caster.getY();
		_skillIdClassic = skillId;
		_skillIdIT = skillId;
		_skillLevel = skillLevel;
		_targets = new GArray<Integer>();
		_targets.add(target.getObjectId());
	}

	public MagicSkillLaunched(L2Character caster, int skillIdClassic, int skillIdIT, int skillLevel, Collection<L2Character> targets)
	{
		_casterId = caster.getObjectId();
		_casterX = caster.getX();
		_casterY = caster.getY();
		_skillIdClassic = skillIdClassic;
		_skillIdIT = skillIdIT;
		_skillLevel = skillLevel;
		_targets = new GArray<Integer>();
		for(L2Character target : targets)
			if(target != null)
				_targets.add(target.getObjectId());
	}

	public MagicSkillLaunched(L2Character caster, int skillId, int skillLevel, Collection<L2Character> targets)
	{
		_casterId = caster.getObjectId();
		_casterX = caster.getX();
		_casterY = caster.getY();
		_skillIdClassic = skillId;
		_skillIdIT = skillId;
		_skillLevel = skillLevel;
		_targets = new GArray<Integer>();
		for(L2Character target : targets)
			if(target != null)
				_targets.add(target.getObjectId());
	}

	@Override
	protected final void writeImpl()
	{
		writeD(0); // dual or not
		writeD(_casterId);
		writeD(_skillIdClassic);
		writeD(_skillLevel);
		writeD(_targets.size());
		for(int id : _targets)
			writeD(id);
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_casterId);
		writeD(_skillIdIT);
		writeD(_skillLevel);
		writeD(_targets.size());
		for(int id : _targets)
			writeD(id);
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