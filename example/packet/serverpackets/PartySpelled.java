package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2Effect;
import l2p.gameserver.utils.EffectsComparator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PartySpelled extends L2GameServerPacket
{
	private int _type;
	private int _objId;
	private List<Effect> _effects;
	private boolean _cond = true;

	public PartySpelled(L2Character activeChar, boolean full)
	{
		if(activeChar == null)
			return;

		_objId = activeChar.getObjectId();
		_type = activeChar.isPet() ? 1 : (activeChar.isServitor() ? 2 : 0);
		// 0 - L2Player // 1 - петы // 2 - саммоны
		_effects = new ArrayList<Effect>();
		if(full)
		{
			L2Effect[] effects = activeChar.getEffectList().getAllFirstEffects();
			Arrays.sort(effects, EffectsComparator.getInstance());
			for(L2Effect effect : effects)
				if(effect != null && effect.isInUse())
					effect.addPartySpelledIcon(this);
		}

		_cond = false;
	}

	@Override
	protected boolean canWrite()
	{
		return !_cond;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_type);
		writeD(_objId);

		writeD(_effects.size());
		for(Effect temp : _effects)
		{
			writeD(temp._skillId);
			writeH(temp._level); // @Rivelia. Skill level by mask.
			writeD(0x00); // clientAbnormalId
			writeOptionalD(temp._duration);
		}
	}

	@Override
	protected boolean canWriteIT()
	{
		return !_cond;
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_type);
		writeD(_objId);

		writeD(_effects.size());
		for(Effect temp : _effects)
		{
			writeD(temp._skillId);
			writeH(temp._level);
			writeD(temp._duration);
		}
	}

	public void addPartySpelledEffect(int skillId, int level, int duration)
	{
		_effects.add(new Effect(skillId, level, duration));
	}

	static class Effect
	{
		final int _skillId;
		final int _level;
		final int _duration;

		public Effect(int skillId, int level, int duration)
		{
			_skillId = skillId;
			_level = level;
			_duration = duration;
		}
	}
}