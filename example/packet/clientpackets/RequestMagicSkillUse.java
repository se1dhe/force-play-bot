package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.ai.PlayableAI;
import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Skill;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.skills.Formulas;
import l2p.gameserver.tables.SkillTable;

public class RequestMagicSkillUse extends L2GameClientPacket
{
	private Integer _magicId;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;

	@Override
	public void readImpl()
	{
		_magicId = readD();
		_ctrlPressed = readD() != 0;
		_shiftPressed = readC() != 0;
	}

	@Override
	public void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;

		if(player.isOutOfControl())
		{
			player.sendActionFailed();
			return;
		}

		if(System.currentTimeMillis() - player.getLastSkillPacket() < Config.SKILL_PACKET_DELAY)
		{
			player.sendActionFailed();
			return;
		}
		player.setActive();

		L2Skill skill = SkillTable.getInstance().getInfo(_magicId, player.getSkillLevel(_magicId));
		if(skill != null)
		{
			skill = Formulas.checkForOlySkill(player, skill);

			if(!skill.isActive() && !skill.isToggle())
			{
				player.sendActionFailed();
				return;
			}

			if(skill.isToggle() && player.getEffectList().getEffectsBySkill(skill) != null)
			{
				if(!Config.ALT_TOGGLE)
				{
					if(player.isActionsDisabled())
					{
						if(_magicId != 60)
							player.getAI().setNextAction(PlayableAI.nextAction.CAST, skill, player, _ctrlPressed, _shiftPressed);
						else if(Config.USE_BREAK_FAKEDEATH && player.isFakeDeath())
						{
							player.breakFakeDeath();
							player.updateEffectIcons();
						}
						player.sendActionFailed();
						return;
					}
					if(player.isSitting())
					{
						player.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_MOVE_WHILE_SITTING));
						player.sendActionFailed();
						return;
					}
					if(skill.stopActor())
						player.stopMove(false);
				}
				player.getListeners().onMagicUse(skill, player, false);
				player.getEffectList().stopEffect(skill.getId());
				player.sendPacket(new SystemMessage(SystemMessage.S1_IS_ABORTED).addSkillName(skill.getId(), skill.getDisplayLevel()));
				return;
			}

			L2Character target = skill.getAimingTarget(player, player.getTarget());

			player.setGroundSkillLoc(null);
			player.getAI().Cast(skill, target, _ctrlPressed, _shiftPressed);
		}
		else
			player.sendActionFailed();
	}
}