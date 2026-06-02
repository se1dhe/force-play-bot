package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.geodata.GeoEngine;
import l2p.gameserver.model.*;
import l2p.gameserver.model.instances.L2MonsterInstance;
import l2p.gameserver.model.instances.L2SiegeHeadquarterInstance;
import l2p.gameserver.model.instances.L2StaticObjectInstance;
import l2p.gameserver.serverpackets.ActionFail;
import l2p.gameserver.serverpackets.MyTargetSelected;
import l2p.gameserver.serverpackets.RecipeShopManageList;
import l2p.gameserver.serverpackets.SocialAction;
import l2p.gameserver.serverpackets.StatusUpdate;
import l2p.gameserver.tables.PetDataTable;
import l2p.gameserver.templates.L2PetTemplate;
import l2p.gameserver.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public class RequestActionUse extends L2GameClientPacket
{
	private static Logger _log = LoggerFactory.getLogger(RequestActionUse.class);

	private int _actionId;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;

	@Override
	public void readImpl()
	{
		_actionId = readD();
		_ctrlPressed = readD() == 1;
		_shiftPressed = readC() == 1;
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		boolean usePet;
		switch(_actionId)
		{
			case 22:
			case 23:
			case 16:
			case 17:
			case 19:
			case 32:
			case 36:
			case 39:
			case 41:
			case 42:
			case 43:
			case 44:
			case 45:
			case 46:
			case 47:
			case 48:
			case 52:
			case 53:
			case 54:
			case 1004:
			case 1005:
			case 1006:
			case 1007:
			case 1008:
			case 1009:
			case 1010:
			case 1011:
			case 1012:
			case 1013:
			case 1014:
			case 1015:
			case 1016:
			case 1017:
			case 1041:
			case 1042:
				usePet = true;
				break;
			default:
				usePet = false;
		}

		// dont do anything if player is dead or confused
		if(!usePet && (activeChar.isOutOfControl() || activeChar.isActionsDisabled()) && !(activeChar.isFakeDeath() && _actionId == 0))
		{
			activeChar.sendActionFailed();
			return;
		}

		L2Object target = activeChar.getTarget();
		L2Summon pet = activeChar.getPet();

		if(usePet && (pet == null || pet.isOutOfControl()))
		{
			activeChar.sendActionFailed();
			return;
		}

		switch(_actionId)
		{
			case 0: // Сесть/встать
				// На страйдере нельзя садиться
				if(activeChar.isMounted())
				{
					activeChar.sendActionFailed();
					break;
				}

				if(activeChar.isFakeDeath())
				{
					activeChar.breakFakeDeath();
					activeChar.updateEffectIcons();
					break;
				}

				if(!activeChar.isSitting())
				{
					if(target != null && (target instanceof L2StaticObjectInstance) && ((L2StaticObjectInstance) target).getType() == 1 && activeChar.getDistance3D(target) <= target.getActingRange())
						activeChar.sitDown(((L2StaticObjectInstance) target).getStaticObjectId());
					else
						activeChar.sitDown(0);
				}
				else
					activeChar.standUp();

				break;
			case 1: // Изменить тип передвижения, шаг/бег
				if(activeChar.isRunning())
					activeChar.setWalking();
				else
					activeChar.setRunning();
				break;
			case 10: // Запрос на создание приватного магазина продажи
				activeChar.tryOpenPrivateStore(true, false);
				break;
			case 28: // Запрос на создание приватного магазина покупки
				activeChar.tryOpenPrivateStore(false, false);
				break;
			case 15:
			case 21: // Follow для пета
				if(pet != null)
				{
					if(pet.isDepressed())
						activeChar.sendPacket(Msg.THE_PET_SERVITOR_IS_UNRESPONSIVE_AND_WILL_NOT_OBEY_ANY_ORDERS);
					else
					{
						pet.setFollowStatus(!pet.isFollow(), true);
					}
				}
				break;
			case 16:
			case 22: // Атака петом
				if(target == null || pet == target || pet.isDead())
				{
					activeChar.sendActionFailed();
					return;
				}

				if(activeChar.isInOlympiadMode() && !activeChar.isOlympiadCompStart())
				{
					activeChar.sendActionFailed();
					return;
				}

				// Sin Eater
				if (pet.getTemplate().getNpcId() == PetDataTable.SIN_EATER_ID)
				{
					return;
				}

				if(!_ctrlPressed && !target.isAutoAttackable(pet))
				{
					pet.setFollowStatus(true, true);
					return;
				}

				if(activeChar.getLevel() + 20 <= pet.getLevel())
				{
					activeChar.sendPacket(Msg.THE_PET_IS_TOO_HIGH_LEVEL_TO_CONTROL);
					return;
				}

				if(!target.isDoor() && pet.isSiegeWeapon())
				{
					activeChar.sendPacket(Msg.INCORRECT_TARGET);
					return;
				}

				if(pet.isPet() && activeChar.getDistance(activeChar.getPet()) > 1500)
					return;

				pet.getAI().Attack(target, _ctrlPressed, _shiftPressed);
				break;
			case 17:
			case 23: // Отмена действия у пета
				pet.setFollowStatus(false, true);
				break;
			case 19: // Отзыв пета
				if(pet.isDead())
				{
					activeChar.sendPacket(Msg.A_DEAD_PET_CANNOT_BE_SENT_BACK, Msg.ActionFail);
					return;
				}
				if(pet.isInCombat())
				{
					activeChar.sendPacket(Msg.A_PET_CANNOT_BE_SENT_BACK_DURING_BATTLE, Msg.ActionFail);
					break;
				}
				if(pet.isPet() && ((L2PetTemplate) pet.getTemplate()).food.size() > 0 && pet.isHungry())
				{
					activeChar.sendPacket(Msg.YOU_CANNOT_RESTORE_HUNGRY_PETS, Msg.ActionFail);
					break;
				}
				pet.unSummon();
				break;
			case 38: // Mount
				if(activeChar.isInStriderRace())
				{
					activeChar.sendPacket(Msg.YOU_ARE_NOT_ALLOWED_TO_DISMOUNT_AT_THIS_LOCATION, ActionFail.STATIC);
					return;
				}
				MountEngine.mount(activeChar, pet);
				break;
			case 32: // Wild Hog Cannon - Mode Change
				UseSkill(4230, target);
				break;
			case 36: // Soulless - Toxic Smoke
				UseSkill(4259, target);
				break;
			case 37: // Создание магазина Common Craft
				if(activeChar.isInTransaction())
					activeChar.getTransaction().cancel();
				if(activeChar.getCreateList() == null)
					activeChar.setCreateList(new L2ManufactureList());
				if(activeChar.isInStoreMode())
				{
					activeChar.setPrivateStoreType(L2Player.STORE_PRIVATE_NONE);
					activeChar.standUp();
					activeChar.broadcastUserInfo(false);
				}
				if(!activeChar.checksForShop(true))
				{
					activeChar.sendActionFailed();
					return;
				}
				activeChar.sendPacket(new RecipeShopManageList(activeChar, true));
				break;
			case 39: // Soulless - Parasite Burst
				UseSkill(4138, target);
				break;
			case 41: //Wild Hog Cannon - Attack
				if(target.isDoor())
					UseSkill(4230, target);
				else
					activeChar.sendPacket(Msg.INCORRECT_TARGET);
				break;
			case 42: // Kai the Cat - Self Damage Shield
				UseSkill(4378, activeChar);
				break;
			case 43: // Unicorn Merrow - Hydro Screw
				UseSkill(4137, target);
				break;
			case 44: // Big Boom - Boom Attack
				UseSkill(4139, target);
				break;
			case 45: // Unicorn Boxer - Master Recharge
				UseSkill(4025, activeChar);
				break;
			case 46: // Mew the Cat - Mega Storm Strike
				UseSkill(4261, target);
				break;
			case 47: // Silhouette - Steal Blood
				UseSkill(4260, target);
				break;
			case 48: // Mechanic Golem - Mech. Cannon
				UseSkill(4068, target);
				break;
			case 51: // Создание магазина Dwarven Craft
				if(!activeChar.checksForShop(true))
				{
					activeChar.sendActionFailed();
					return;
				}
				if(activeChar.getCreateList() == null)
					activeChar.setCreateList(new L2ManufactureList());
				if(activeChar.isInStoreMode())
				{
					activeChar.setPrivateStoreType(L2Player.STORE_PRIVATE_NONE);
					activeChar.standUp();
					activeChar.broadcastUserInfo(false);
				}
				activeChar.sendPacket(new RecipeShopManageList(activeChar, false));
				break;
			case 52: // unsummon
				if(pet.isInCombat())
				{
					activeChar.sendPacket(Msg.A_PET_CANNOT_BE_SENT_BACK_DURING_BATTLE);
					activeChar.sendActionFailed();
					break;
				}
				pet.unSummon();
				break;
			case 53: // move to target
			case 54: // move to target hatch/strider
				if(target != null && pet != target && !pet.isMovementDisabled())
				{
					pet.setFollowStatus(false, true);
					pet.moveToLocation(target.getLoc(), 100, true);
				}
				break;
			case 61:
				activeChar.tryOpenPrivateStore(true, true);
				break;
			case 90:
				break;
			case 1000: // Siege Golem - Siege Hammer
				if(target.isDoor())
					UseSkill(4079, target);
				else
					activeChar.sendPacket(Msg.INCORRECT_TARGET);
				break;
			case 1001: // Sin Eater - Ultimate Bombastic Buster
				break;
			case 1003: // Wind Hatchling/Strider - Wild Stun
				UseSkill(4710, target);
				break;
			case 1004: // Wind Hatchling/Strider - Wild Defense
				UseSkill(4711, activeChar);
				break;
			case 1005: // Star Hatchling/Strider - Bright Burst
				UseSkill(4712, target);
				break;
			case 1006: // Star Hatchling/Strider - Bright Heal
				UseSkill(4713, activeChar);
				break;
			case 1007: // Cat Queen - Blessing of Queen
				UseSkill(4699, activeChar);
				break;
			case 1008: // Cat Queen - Gift of Queen
				UseSkill(4700, activeChar);
				break;
			case 1009: // Cat Queen - Cure of Queen
				UseSkill(4701, target);
				break;
			case 1010: // Unicorn Seraphim - Blessing of Seraphim
				UseSkill(4702, activeChar);
				break;
			case 1011: // Unicorn Seraphim - Gift of Seraphim
				UseSkill(4703, activeChar);
				break;
			case 1012: // Unicorn Seraphim - Cure of Seraphim
				UseSkill(4704, target);
				break;
			case 1013: // Nightshade - Curse of Shade
				UseSkill(4705, target);
				break;
			case 1014: // Nightshade - Mass Curse of Shade
				UseSkill(4706, activeChar);
				break;
			case 1015: // Nightshade - Shade Sacrifice
				UseSkill(4707, target);
				break;
			case 1016: // Cursed Man - Cursed Blow
				UseSkill(4709, target);
				break;
			case 1017: // Cursed Man - Cursed Strike/Stun
				UseSkill(4708, target);
				break;
			case 1031: // Feline King - Slash
				UseSkill(5135, target);
				break;
			case 1032: // Feline King - Spinning Slash
				UseSkill(5136, target);
				break;
			case 1033: // Feline King - Grip of the Cat
				UseSkill(5137, target);
				break;
			case 1034: // Magnus the Unicorn - Whiplash
				UseSkill(5138, target);
				break;
			case 1035: // Magnus the Unicorn - Tridal Wave
				UseSkill(5139, target);
				break;
			case 1036: // Spectral Lord - Corpse Kaboom
				UseSkill(5142, target);
				break;
			case 1037: // Spectral Lord - Dicing Death
				UseSkill(5141, target);
				break;
			case 1038: // Spectral Lord - Force Curse
				UseSkill(5140, target);
				break;
			case 1039: // Swoop Cannon - Cannon Fodder (не может атаковать двери и флаги)
				if(!target.isDoor() && !(target instanceof L2SiegeHeadquarterInstance))
					UseSkill(5110, target);
				else
					activeChar.sendPacket(Msg.INCORRECT_TARGET);
				break;
			case 1040: // Swoop Cannon - Big Bang (не может атаковать двери и флаги)
				if(!target.isDoor() && !(target instanceof L2SiegeHeadquarterInstance))
					UseSkill(5111, target);
				else
					activeChar.sendPacket(Msg.INCORRECT_TARGET);
				break;
			case 12:
				activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 2));
				break;
			case 13:
				activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 3));
				break;
			case 14:
				activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 4));
				break;
			case 24:
				activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 6));
				break;
			case 25:
				activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 5));
				break;
			case 26:
				activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 7));
				break;
			case 29:
				activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 8));
				break;
			case 30:
				activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 9));
				break;
			case 31:
				activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 10));
				break;
			case 33:
				activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 11));
				break;
			case 34:
				activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 12));
				break;
			case 35:
				activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 13));
				break;
			case 62:
				activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 14));
				break;
			case 66:
				activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 15));
				break;
			case 71:
				activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 16));
				break;
			case 72:
				activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 17));
				break;
			case 73:
				activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 18));
				break;
			case 114:
				List<L2MonsterInstance> targets = getKnownMonstersInRadius(activeChar, 200,
						creature -> GeoEngine.canMoveToCoord(activeChar.getX(), activeChar.getY(), activeChar.getZ(), creature.getX(), creature.getY(), creature.getZ(), creature.getGeoIndex())
								&& !creature.isDead());

				if(targets.isEmpty())
				{
					return;
				}

				L2MonsterInstance closestTarget = null;
				int closestDistance = 999999;
				for(L2MonsterInstance monsterTarget : targets)
				{
					int distance = (int) Util.calculateDistance(activeChar, monsterTarget, false);
					if(distance < closestDistance)
					{
						closestDistance = distance;
						closestTarget = monsterTarget;
					}
				}

				if(closestTarget == null)
				{
					return;
				}

				activeChar.setTarget(closestTarget);
				activeChar.sendPacket(new MyTargetSelected(activeChar, closestTarget));
				activeChar.sendPacket(new StatusUpdate(closestTarget).addAttribute(StatusUpdate.CUR_HP, (int) closestTarget.getCurrentHp()).addAttribute(StatusUpdate.MAX_HP, closestTarget.getMaxHp()));
				break;
			default:
				if(Config.ALLOW_PETS_ACTION_SKILLS && Config.PETS_ACTION_SKILLS.containsKey(_actionId))
				{
					int id = Config.PETS_ACTION_SKILLS.get(_actionId);
					L2Skill sk = pet.getTemplate().getSkills().get(id);
					if(sk != null)
						UseSkill(id, sk.isOffensive() ? target : activeChar);
					else
						_log.warn("Not found skill " + id + " for action " + _actionId + " and pet " + pet.getNpcId() + " in config PetsActionSkills");
				}
				else
				{
					_log.warn(activeChar.toString() + " unhandled action type " + _actionId);
					// TODO [V] - при юзе действий с хроник выше, которые не реализованы но есть на хрониках, кикает.
					//activeChar.logout(true);
				}
				break;
		}
	}

	public final List<L2MonsterInstance> getKnownMonstersInRadius(L2Player player, int radius, Function<L2MonsterInstance, Boolean> condition)
	{
		final List<L2MonsterInstance> result = player.getAroundMonsters(radius, condition);
		if(result.size() == 0)
			return Collections.emptyList();

		return result;
	}

	private void UseSkill(int skillId, L2Object target)
	{
		L2Player activeChar = getClient().getActiveChar();
		L2Summon pet = activeChar.getPet();

		if(target == null || !target.isCharacter() || pet == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		HashMap<Integer, L2Skill> _skills = pet.getTemplate().getSkills();
		if(_skills.size() == 0)
		{
			activeChar.sendActionFailed();
			return;
		}

		L2Skill skill = _skills.get(skillId);
		if(skill == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.getLevel() + 20 <= pet.getLevel())
		{
			activeChar.sendPacket(Msg.THE_PET_IS_TOO_HIGH_LEVEL_TO_CONTROL);
			return;
		}

		if(skill.isOffensive() && skill.getTargetType() != L2Skill.SkillTargetType.TARGET_AURA && skill.getTargetType() != L2Skill.SkillTargetType.TARGET_MULTIFACE_AURA && (target == activeChar || target == pet))
		{
			activeChar.sendPacket(Msg.TARGET_IS_INCORRECT);
			return;
		}

		pet.setTarget(target);
		L2Character aimingTarget = skill.getAimingTarget(pet, target);
		if(skill.checkCondition(pet, aimingTarget, _ctrlPressed, _shiftPressed, true))
			pet.getAI().Cast(skill, aimingTarget, _ctrlPressed, _shiftPressed);
		else
			activeChar.sendActionFailed();
	}
}