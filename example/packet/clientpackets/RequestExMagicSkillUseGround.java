package l2p.gameserver.clientpackets;

import l2p.gameserver.cache.Msg;
import l2p.gameserver.geodata.GeoEngine;
import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Skill;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.utils.Location;

public class RequestExMagicSkillUseGround extends L2GameClientPacket
{
	private Location _loc = new Location();
	private int _skillId;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;

	/**
	 * packet type id 0xd0
	 */
	@Override
	protected void readImpl()
	{
		_loc.x = readD();
		_loc.y = readD();
		_loc.z = readD();
		_skillId = readD();
		_ctrlPressed = readD() != 0;
		_shiftPressed = readC() != 0;
	}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}

		L2Skill skill = SkillTable.getInstance().getInfo(_skillId, activeChar.getSkillLevel(_skillId));
		if(skill != null)
		{
			if(skill.getAddedSkills().length == 0)
				return;

			if(!activeChar.isInRange(_loc, skill.getCastRange()))
			{
				activeChar.sendPacket(Msg.YOUR_TARGET_IS_OUT_OF_RANGE);
				activeChar.sendActionFailed();
				return;
			}

			L2Character target = skill.getAimingTarget(activeChar, activeChar.getTarget());

			if(skill.checkCondition(activeChar, target, _ctrlPressed, _shiftPressed, true))
			{
				Location loc;
				if(GeoEngine.canMoveToCoord(activeChar.getX(), activeChar.getY(), activeChar.getZ(), _loc.getX(), _loc.getY(), _loc.getZ(), activeChar.getGeoIndex()))
					loc = _loc;
				else
					loc = activeChar.getLoc();
				activeChar.setGroundSkillLoc(loc);
				activeChar.getAI().Cast(skill, target, _ctrlPressed, _shiftPressed);
			}
			else
				activeChar.sendActionFailed();
		}
		else
			activeChar.sendActionFailed();
	}
}