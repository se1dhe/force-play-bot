package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Effect;

public class ShortBuffStatusUpdate extends L2GameServerPacket
{
	int _skillId;
	int _skillLevel;
	int _skillDuration;

	public ShortBuffStatusUpdate(L2Effect effect)
	{
		_skillId = effect.getSkill().getId();
		_skillLevel = effect.getSkill().getLevel();
		_skillDuration = (int) effect.getTimeLeft() / 1000;
	}

	/**
	 * Zero packet to delete skill icon.
	 */
	public ShortBuffStatusUpdate()
	{
		_skillId = 0;
		_skillLevel = 0;
		_skillDuration = 0;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_skillId); // skill id??? CD 04 00 00 = skill 1229, hex 4CD
		writeD(_skillLevel); //Skill Level??? 07 00 00 00 = casted by heal 7 lvl.
		writeD(_skillDuration); //DURATION???? 0F 00 00 00 = 15 sec = overlord's heal
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_skillId); // skill id??? CD 04 00 00 = skill 1229, hex 4CD
		writeD(_skillLevel); //Skill Level??? 07 00 00 00 = casted by heal 7 lvl.
		writeD(_skillDuration); //DURATION???? 0F 00 00 00 = 15 sec = overlord's heal
	}
}