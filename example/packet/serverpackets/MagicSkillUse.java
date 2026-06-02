package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Skill;
import l2p.gameserver.tables.SkillTree;

public class MagicSkillUse extends L2GameServerPacket
{
	private final int _targetId;
	private final int _skillIdClassic;
	private final int _skillIdIT;
	private final int _skillLevel;
	private final int _hitTime;
	private final int _reuseDelay;
	private final int _chaId, _x, _y, _z;
	private final int _targetx, _targety, _targetz;

	public MagicSkillUse(L2Character cha, L2Character target, int skillIdClassic, int skillIdIT, int skillLevel, int hitTime, long reuseDelay)
	{
		_chaId = cha.getObjectId();
		_targetId = target.getObjectId();
		_skillIdClassic = skillIdClassic;
		_skillIdIT = skillIdIT;
		_skillLevel = skillLevel;
		_hitTime = hitTime;
		_reuseDelay = (int) reuseDelay;
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_targetx = target.getX();
		_targety = target.getY();
		_targetz = target.getZ();
	}

	public MagicSkillUse(L2Character cha, L2Character target, int skillId, int skillLevel, int hitTime, long reuseDelay)
	{
		_chaId = cha.getObjectId();
		_targetId = target.getObjectId();
		_skillIdClassic = skillId;
		_skillIdIT = skillId;
		_skillLevel = skillLevel;
		_hitTime = hitTime;
		_reuseDelay = (int) reuseDelay;
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_targetx = target.getX();
		_targety = target.getY();
		_targetz = target.getZ();
	}

	public MagicSkillUse(L2Character cha, L2Character target, L2Skill skill, long reuseDelay)
	{
		_chaId = cha.getObjectId();
		_targetId = target.getObjectId();
		_skillIdClassic = skill.isDisplayedInClassic() ? skill.getDisplayId() : skill.getId();
		_skillIdIT = skill.getDisplayId();
		_skillLevel = skill.getDisplayLevel() >= 100 ? SkillTree.getBaseLevels().get(skill.getId()) : skill.getDisplayLevel();
		_hitTime = skill.getHitTime();
		_reuseDelay = (int) reuseDelay;
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_targetx = target.getX();
		_targety = target.getY();
		_targetz = target.getZ();
	}

	public MagicSkillUse(L2Character cha, L2Character target, L2Skill skill, int hitTime, long reuseDelay)
	{
		_chaId = cha.getObjectId();
		_targetId = target.getObjectId();
		_skillIdClassic = skill.isDisplayedInClassic() ? skill.getDisplayId() : skill.getId();
		_skillIdIT = skill.getDisplayId();
		_skillLevel = skill.getDisplayLevel() >= 100 ? SkillTree.getBaseLevels().get(skill.getId()) : skill.getDisplayLevel();
		_hitTime = hitTime;
		_reuseDelay = (int) reuseDelay;
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_targetx = target.getX();
		_targety = target.getY();
		_targetz = target.getZ();
	}

	public MagicSkillUse(L2Character cha, int skillId, int skillLevel, int hitTime, long reuseDelay)
	{
		_chaId = cha.getObjectId();
		_targetId = cha.getTargetId();
		_skillIdClassic = skillId;
		_skillIdIT = skillId;
		_skillLevel = skillLevel;
		_hitTime = hitTime;
		_reuseDelay = (int) reuseDelay;
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_targetx = cha.getX();
		_targety = cha.getY();
		_targetz = cha.getZ();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(0x00); // dual or not
		writeD(_chaId);
		writeD(_targetId);
		writeD(_skillIdClassic);
		writeD(_skillLevel);
		writeD(_hitTime);
		writeD(0x00); // reuse Skill Id
		writeD(_reuseDelay);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeH(0); // critical Blow
		writeH(0); // ground Loc
		writeD(_targetx);
		writeD(_targety);
		writeD(_targetz);
		writeD(0); // is Pet Skill
		writeD(0); // Social Action ID
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_chaId);
		writeD(_targetId);
		writeD(_skillIdIT);
		writeD(_skillLevel);
		writeD(_hitTime);
		writeD(_reuseDelay);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(0x00);
		writeD(_targetx);
		writeD(_targety);
		writeD(_targetz);
	}

	@Override
	public L2GameServerPacket packet(L2Player player)
	{
		if(player != null)
		{
			if(player.animRange() < 0)
				return null;

			if(player.animRange() == 0)
				return _chaId == player.getObjectId() ? super.packet(player) : null;

			L2Character observer = player.getObservePoint();
			if(observer == null)
				observer = player;

			return observer.getDistance(_x, _y) < player.animRange() ? super.packet(player) : null;
		}
		return super.packet(player);
	}
}