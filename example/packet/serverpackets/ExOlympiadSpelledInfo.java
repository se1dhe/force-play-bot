package l2p.gameserver.serverpackets;

import l2p.commons.util.GArray;
import l2p.gameserver.model.L2Player;

public class ExOlympiadSpelledInfo extends L2GameServerPacket
{
	// chdd(dhd)
	private int char_obj_id = 0;
	private GArray<Effect> _effects;

	class Effect
	{
		int skillId;
		int skillLevel;
		int duration;

		public Effect(int skillId, int skillLevel, int duration)
		{
			this.skillId = skillId;
			this.skillLevel = skillLevel;
			this.duration = duration;
		}
	}

	public ExOlympiadSpelledInfo()
	{
		_effects = new GArray<Effect>();
	}

	public void addEffect(int skillId, int skillLevel, int duration)
	{
		_effects.add(new Effect(skillId, skillLevel, duration));
	}

	public void addSpellRecivedPlayer(L2Player cha)
	{
		if(cha != null)
			char_obj_id = cha.getObjectId();
	}

	@Override
	protected boolean canWrite()
	{
		if(char_obj_id == 0)
			return false;
		return true;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(char_obj_id);
		writeD(_effects.size());
		for(Effect temp : _effects)
		{
			writeD(temp.skillId);
			writeH(temp.skillLevel); // @Rivelia. Skill level by mask.
			writeD(0x00); // Abnormal Type
			writeOptionalD(temp.duration);
		}
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(char_obj_id);
		writeD(_effects.size());
		for(Effect temp : _effects)
		{
			writeD(temp.skillId);
			writeH(temp.skillLevel);
			writeD(temp.duration);
		}
	}
}