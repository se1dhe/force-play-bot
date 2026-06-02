package l2p.gameserver.clientpackets;

import l2p.commons.util.Rnd;
import l2p.gameserver.Announcements;
import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.data.xml.holder.EnchantItemHolder;
import l2p.gameserver.data.xml.holder.ObtEnchantItemHolder;
import l2p.gameserver.model.L2ArmorSet;
import l2p.gameserver.model.L2ObjectsStorage;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Skill;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.model.instances.L2ItemInstance.ItemLocation;
import l2p.gameserver.model.items.Inventory;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.model.items.listeners.ArmorSetListener;
import l2p.gameserver.multilang.CustomMessage;
import l2p.gameserver.serverpackets.*;
import l2p.gameserver.skills.Stats;
import l2p.gameserver.tables.ArmorSetsTable;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.templates.L2Item;
import l2p.gameserver.templates.L2Weapon;
import l2p.gameserver.templates.SkillInfo;
import l2p.gameserver.templates.enchant.EnchantScroll;
import l2p.gameserver.templates.enchant.EnchantVariation;
import l2p.gameserver.templates.enchant.EnchantVariation.EnchantLevel;
import l2p.gameserver.templates.enchant.FailResultType;
import l2p.gameserver.utils.Log;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class RequestEnchantItem extends L2GameClientPacket
{
	private static final int SUCCESS_VISUAL_EFF_ID = 2025;

	private static final Logger _log = LoggerFactory.getLogger(RequestEnchantItem.class);

	private int _objectId;
	private int _catalystObjId;

	@Override
	public void readImpl()
	{
		_objectId = readD();
		if(!getClient().isITClient())
			_catalystObjId = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(Config.NO_ENCHANT_IN_COMBAT && activeChar.isInCombat())
		{
			activeChar.sendMessage("You can't enchant while in combat.");
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInFightClub())
		{
			activeChar.setEnchantScroll(null);
			activeChar.sendPacket(EnchantResult.CANCELLED);
			activeChar.sendPacket(Msg.INAPPROPRIATE_ENCHANT_CONDITIONS);
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isOutOfControl() || activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.inEvent() && !activeChar.inBattleGround || activeChar.isInGvG())
		{
			activeChar.sendMessage("You can't enchant in event.");
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isEnchantLimit())
		{
			activeChar.sendMessage("You can't enchant with active enchant limit.");
			activeChar.sendActionFailed();
			return;
		}

		if(Config.SERVICES_DISABLE_ENCHANT_ITEM && activeChar.isTradeKeyBlocked())
		{
			activeChar.sendMessage(activeChar.isLangRus() ? "Предмет нельзя заточить, отключите Lock." : "Item cannot be enchanted, turn off Lock.");
			activeChar.sendActionFailed();
			return;
		}

		PcInventory inventory = activeChar.getInventory();
		inventory.writeInvLock();
		try
		{
			L2ItemInstance item = inventory.getItemByObjectId(_objectId);
			L2ItemInstance scroll = activeChar.getEnchantScroll();

			if(item == null || scroll == null)
			{
				activeChar.sendActionFailed();
				return;
			}

			final EnchantScroll enchantScroll = Config.EVENT_OBT_ENABLE ? ObtEnchantItemHolder.getInstance().getEnchantScroll(scroll.getItemId()) : EnchantItemHolder.getInstance().getEnchantScroll(scroll.getItemId());
			if(enchantScroll == null)
			{
				activeChar.sendActionFailed();
				return;
			}

			if(item.getEnchantLevel() < enchantScroll.getMinEnchant() || enchantScroll.getMaxEnchant() != -1 && item.getEnchantLevel() >= enchantScroll.getMaxEnchant())
			{
				activeChar.sendPacket(EnchantResult.CANCELLED);
				activeChar.sendPacket(Msg.INAPPROPRIATE_ENCHANT_CONDITIONS);
				activeChar.sendActionFailed();
				return;
			}

			final int itemType = item.getItem().getType2();
			if(enchantScroll.getItems().size() > 0)
			{
				if(!enchantScroll.getItems().contains(item.getItemId()))
				{
					activeChar.sendPacket(EnchantResult.CANCELLED);
					activeChar.sendPacket(new SystemMessage(SystemMessage.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL));
					activeChar.sendActionFailed();
					return;
				}

				if(enchantScroll.getProhibitedItems().contains(item.getItemId()))
				{
					activeChar.sendPacket(EnchantResult.CANCELLED);
					activeChar.sendPacket(new SystemMessage(SystemMessage.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL));
					activeChar.sendActionFailed();
					return;
				}
			}
			else
			{
				if(enchantScroll.getItemGrades().size() > 0)
				{
					if(!enchantScroll.getItemGrades().contains(item.getItem().getItemGrade()))
					{
						activeChar.sendPacket(EnchantResult.CANCELLED);
						activeChar.sendPacket(new SystemMessage(SystemMessage.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL));
						activeChar.sendActionFailed();
						return;
					}
				}
				else
				{
					if(!enchantScroll.containsGrade(item.getItem().getItemGrade()))
					{
						activeChar.sendPacket(EnchantResult.CANCELLED);
						activeChar.sendPacket(new SystemMessage(SystemMessage.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL));
						activeChar.sendActionFailed();
						return;
					}
				}

				if(enchantScroll.getProhibitedItems().contains(item.getItemId()))
				{
					activeChar.sendPacket(EnchantResult.CANCELLED);
					activeChar.sendPacket(new SystemMessage(SystemMessage.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL));
					activeChar.sendActionFailed();
					return;
				}

				switch(enchantScroll.getType())
				{
					case ARMOR:
						if(itemType == L2Item.TYPE2_WEAPON || item.getItem().isHairAccessory())
						{
							activeChar.sendPacket(EnchantResult.CANCELLED);
							activeChar.sendPacket(new SystemMessage(SystemMessage.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL));
							activeChar.sendActionFailed();
							return;
						}
						break;
					case WEAPON:
						if(itemType == L2Item.TYPE2_SHIELD_ARMOR || itemType == L2Item.TYPE2_ACCESSORY || item.getItem().isHairAccessory())
						{
							activeChar.sendPacket(EnchantResult.CANCELLED);
							activeChar.sendPacket(new SystemMessage(SystemMessage.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL));
							activeChar.sendActionFailed();
							return;
						}
						break;
					case HAIR_ACCESSORY:
						if(!item.getItem().isHairAccessory())
						{
							activeChar.sendPacket(EnchantResult.CANCELLED);
							activeChar.sendPacket(new SystemMessage(SystemMessage.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL));
							activeChar.sendActionFailed();
							return;
						}
						break;
				}
			}

			if(!enchantScroll.getItems().contains(item.getItemId()) && !item.canBeEnchanted())
			{
				activeChar.sendPacket(EnchantResult.CANCELLED);
				activeChar.sendPacket(Msg.INAPPROPRIATE_ENCHANT_CONDITIONS);
				activeChar.sendActionFailed();
				return;
			}

			final EnchantVariation variation = Config.EVENT_OBT_ENABLE ? ObtEnchantItemHolder.getInstance().getEnchantVariation(enchantScroll.getVariationId()) : EnchantItemHolder.getInstance().getEnchantVariation(enchantScroll.getVariationId());
			if(variation == null)
			{
				activeChar.sendActionFailed();
				_log.warn("RequestEnchantItem: Cannot find variation ID[" + enchantScroll.getVariationId() + "] for enchant scroll ID[" + enchantScroll.getItemId() + "]!");
				return;
			}

			int minEnchantSteep = enchantScroll.getMinEnchantStep();
			int maxEnchantSteep = enchantScroll.getMaxEnchantStep();
			int newEnchantLvl = item.getEnchantLevel() + Rnd.get(minEnchantSteep, maxEnchantSteep);
			newEnchantLvl = Math.min(newEnchantLvl, enchantScroll.getMaxEnchant());
			if(newEnchantLvl < item.getEnchantLevel()) //  А вдруг?
			{
				activeChar.sendPacket(EnchantResult.CANCELLED);
				activeChar.sendActionFailed();
				return;
			}

			final EnchantLevel enchantLevel = variation.getLevel(item.getEnchantLevel() + 1);
			if(enchantLevel == null)
			{
				activeChar.sendActionFailed();
				activeChar.sendMessage(new CustomMessage("RequestEnchantItem.ScrollThisLevelDoesNotWork", activeChar));
				//_log.warn("RequestEnchantItem: Cannot find variation ID[" + enchantScroll.getVariationId() + "] enchant level[" + (item.getEnchantLevel() + 1) + "] for enchant scroll ID[" + enchantScroll.getItemId() + "]!");
				return;
			}

			if(item.getLocation() != ItemLocation.INVENTORY && item.getLocation() != ItemLocation.PAPERDOLL)
			{
				activeChar.sendPacket(Msg.INAPPROPRIATE_ENCHANT_CONDITIONS);
				activeChar.sendActionFailed();
				return;
			}

			if(activeChar.getPrivateStoreType() != L2Player.STORE_PRIVATE_NONE)
			{
				activeChar.sendPacket(EnchantResult.CANCELLED);
				activeChar.sendPacket(Msg.YOU_CANNOT_PRACTICE_ENCHANTING_WHILE_OPERATING_A_PRIVATE_STORE_OR_PRIVATE_MANUFACTURING_WORKSHOP);
				activeChar.sendActionFailed();
				return;
			}

			if((scroll = inventory.getItemByObjectId(scroll.getObjectId())) == null)
			{
				activeChar.sendPacket(EnchantResult.CANCELLED);
				activeChar.sendActionFailed();
				return;
			}

			// Запрет на заточку чужих вещей, баг может вылезти на серверных лагах
			if(item.getOwnerId() != activeChar.getObjectId())
			{
				activeChar.sendPacket(EnchantResult.CANCELLED);
				activeChar.sendPacket(Msg.INAPPROPRIATE_ENCHANT_CONDITIONS);
				activeChar.sendActionFailed();
				return;
			}

			L2ItemInstance removedScroll = inventory.destroyItem(scroll.getObjectId(), 1, true, "<DestroyItemEnchantRemoveScroll>");

			//tries enchant without scrolls
			if(removedScroll == null)
			{
				activeChar.sendPacket(EnchantResult.CANCELLED);
				activeChar.sendActionFailed();
				return;
			}

			int se = item.getEnchantLevel();

			final double baseChance;
			if(item.getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR)
				baseChance = enchantLevel.getFullBodyChance();
			else if(item.getItem() instanceof L2Weapon)
			{
				L2Weapon weapon = (L2Weapon) item.getItem();
				if(weapon.getItemType() == L2Weapon.WeaponType.DUAL)
					baseChance = enchantLevel.getDualWeaponChance();
				else if(weapon.isMagicWeapon())
					baseChance = enchantLevel.getMagicWeaponChance();
				else
					baseChance = enchantLevel.getBaseChance();
			}
			else
				baseChance = enchantLevel.getBaseChance();

			double chance = baseChance;

			chance = activeChar.calcStat(itemType == L2Item.TYPE2_WEAPON ? Stats.ENCHANT_CHANCE_WEAPON : Stats.ENCHANT_CHANCE_ARMOR, chance, null, null);
			if(Config.RATE_BONUS_E_ENABLED && activeChar.isPremium())
			{
				if(itemType == L2Item.TYPE2_WEAPON)
					chance += getBonusRate(Config.RATE_BONUS_E_W, newEnchantLvl);
				else if(itemType == L2Item.TYPE2_ACCESSORY)
					chance += getBonusRate(Config.RATE_BONUS_E_J, newEnchantLvl);
				else
					chance += getBonusRate(Config.RATE_BONUS_E_A, newEnchantLvl);
			}

			chance = Math.min(100, chance);

			boolean chanceOk = Rnd.chance(chance);

			if(chanceOk)
			{
				if(se == 0)
				{
					if(activeChar.isITClient())
						activeChar.sendPacket(new SystemMessage(SystemMessage.S1_HAS_BEEN_SUCCESSFULLY_ENCHANTED).addItemName(item.getItemId()));
					else
						activeChar.sendPacket(new SystemMessage(SystemMessage.S1_HAS_BEEN_SUCCESSFULLY_ENCHANTED2).addItemName(item.getItemId()));
				}
				else
				{
					SystemMessage sm;
					if(activeChar.isITClient())
						sm = new SystemMessage(SystemMessage._S1_S2_HAS_BEEN_SUCCESSFULLY_ENCHANTED);
					else
						sm = new SystemMessage(SystemMessage._S1_S2_HAS_BEEN_SUCCESSFULLY_ENCHANTED2);
					sm.addNumber(se);
					sm.addItemName(item.getItemId());
					activeChar.sendPacket(sm);
				}

				item.setEnchantLevel(newEnchantLvl);
				item.updateDatabase();

				if(itemType == L2Item.TYPE2_ACCESSORY && item.isEquipped() && item.getItem().getBodyPart() != L2Item.SLOT_NECK)
					activeChar.sendItemList(false);
				else
					activeChar.sendPacket(new InventoryUpdate().addModifiedItem(item, activeChar.isEnchantLimit()));
				activeChar.sendPacket(new EnchantResult(0, 0, 0, item.getEnchantLevel()));

				Log.LogEnchant(activeChar, Log.ItemLog.EnchantSuccess, item, se, newEnchantLvl, scroll.getItemId());
				if(activeChar.recording && item.isEquipped() && !ArrayUtils.contains(Config.BOTS_RT_EQUIP, item.getItemId()))
					activeChar.recBot(4, item.getItemId(), item.getEnchantLevel(), 0, 0, 0, 0);

				if(enchantLevel.haveSuccAnnounce())
					announceByCustomMessage("C1_HAS_SUCCESSFULLY_ENCHANTED_A_S2_S3", new String[]{activeChar.getName(), item.getName(), String.valueOf(item.getEnchantLevel())});

				if(enchantLevel.haveSuccessVisualEffect())
					activeChar.broadcastPacket(new MagicSkillUse(activeChar, activeChar, SUCCESS_VISUAL_EFF_ID, 1, 500, 1500));

				activeChar.getListeners().onEnchantItem(item, true);
			}
			else
			{
				FailResultType resultType = enchantScroll.getResultType();

				switch (resultType)
				{
					case DROP_ENCHANT:
					{
						Log.LogEnchant(activeChar, Log.ItemLog.EnchantReset, item, se, 0, scroll.getItemId());

						int enchantDropCount = enchantScroll.getEnchantDropCount();
						int enchantSafeDrop = enchantScroll.getEnchantSafeDrop();
						int dropToLevel;
						if(enchantSafeDrop > -1)
							dropToLevel = enchantSafeDrop;
						else
							dropToLevel = Math.max(item.getEnchantLevel() - enchantDropCount, 0);
						item.setEnchantLevel(dropToLevel);
						if(itemType == L2Item.TYPE2_ACCESSORY && item.isEquipped() && item.getItem().getBodyPart() != L2Item.SLOT_NECK)
							activeChar.sendItemList(false);
						else
							activeChar.sendPacket(new InventoryUpdate().addModifiedItem(item, activeChar.isEnchantLimit()));

						activeChar.sendPacket(Msg.FAILED_IN_BLESSED_ENCHANT_THE_ENCHANT_VALUE_OF_THE_ITEM_BECAME_0);

						activeChar.sendPacket(EnchantResult.BLESSED_FAILED);
						if(activeChar.recording && item.isEquipped() && !ArrayUtils.contains(Config.BOTS_RT_EQUIP, item.getItemId()))
							activeChar.recBot(4, item.getItemId(), item.getEnchantLevel(), 0, 0, 0, 0);
						activeChar.getListeners().onEnchantItem(item, false);
						break;
					}
					case CRYSTALS:
					{
						Log.LogEnchant(activeChar, Log.ItemLog.EnchantCrystallize, item, se, 0, scroll.getItemId());
						int itemId = item.getItemId();

						L2ItemInstance destroyedItem = inventory.destroyItem(item.getObjectId(), 1, true, "<DestroyItemEnchantFail>");
						if(destroyedItem == null)
						{
							_log.warn("failed to destroy " + item.getObjectId() + " after unsuccessful enchant attempt by char " + activeChar.toString());
							activeChar.sendPacket(EnchantResult.CANCELLED);
							activeChar.sendActionFailed();
							return;
						}

						if(se == 0)
							activeChar.sendPacket(new SystemMessage(SystemMessage.THE_ENCHANTMENT_HAS_FAILED_YOUR_S1_HAS_BEEN_CRYSTALLIZED).addItemName(itemId));
						else
							activeChar.sendPacket(new SystemMessage(SystemMessage.THE_ENCHANTMENT_HAS_FAILED_YOUR__S1_S2_HAS_BEEN_CRYSTALLIZED).addNumber(se).addItemName(itemId));

						int crystalId = item.getCrystalId();
						if(crystalId > 0 && item.getItem().getCrystalCount() > 0 && !item.isShadowItem() && !item.isTemporalItem())
						{
							int count = item.getItem().getCrystalCount(item.getEnchantLevel(), true);
							activeChar.sendPacket(new EnchantResult(1, crystalId, count, 0));
							activeChar.getInventory().addItem(crystalId, count, "<EnchCry>");
							activeChar.sendPacket(new SystemMessage(SystemMessage.EARNED_S2_S1_s).addItemName(crystalId).addNumber(count));
						}
						else
							activeChar.sendPacket(EnchantResult.FAILED_NO_CRYSTALS);

						activeChar.sendPacket(new StatusUpdate(activeChar).addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad()));
						activeChar.sendItemList(false);

						activeChar.getListeners().onEnchantItem(item, false);
						break;
					}
					case NOTHING:
					{
						activeChar.sendMessage(activeChar.isLangRus() ? "Неудача при заточке, однако уровень заточки сохранен." : "Failed enchant, but enchant value of item saved.");
						activeChar.sendPacket(EnchantResult.BLESSED_FAILED);

						if(activeChar.recording && item.isEquipped() && !ArrayUtils.contains(Config.BOTS_RT_EQUIP, item.getItemId()))
							activeChar.recBot(4, item.getItemId(), item.getEnchantLevel(), 0, 0, 0, 0);
						activeChar.getListeners().onEnchantItem(item, false);
						break;
					}
				}
			}

			applySkills(activeChar, item, se);

			activeChar.broadcastUserInfo(false);
		}
		finally
		{
			if(activeChar.isITClient())
				activeChar.setEnchantScroll(null);
			inventory.writeInvUnlock();
		}
	}

	private double getBonusRate(double[] rates, int enchantLevel)
	{
		if (rates == null || rates.length == 0) {
			return 0.0;
		}

		int index = Math.min(enchantLevel - 1, rates.length - 1);
		return rates[index];
	}

	public void announceByCustomMessage(String address, String[] replacements)
	{
		for(L2Player player : L2ObjectsStorage.getAllPlayersForIterate())
		{
			boolean result = player.getVarB("@announce_enchant_item", Config.ANNOUNCE_ENCHANT_ITEM_DEFAULT);
			if(result)
				Announcements.getInstance().announceToPlayerByCustomMessage(player, address, replacements);
		}
	}

	private void applySkills(L2Player activeChar, L2ItemInstance item, int se)
	{
		boolean skillsChanged = false;

		if(item.isWeapon() && item.isEquipped() && se != item.getEnchantLevel())
		{
			L2Skill sk = ((L2Weapon) item.getItem()).getEnchant4Skill();
			if(sk != null)
			{
				if(se > 3 && item.getEnchantLevel() < 4)
				{
					activeChar.removeSkill(sk, false);
					skillsChanged = true;
				}
				else if(se < 4 && item.getEnchantLevel() > 3)
				{
					activeChar.addSkill(sk, false);
					skillsChanged = true;
				}
			}
		}

		if((item.isWeapon() || item.isArmor()) && item.isEquipped() && se != item.getEnchantLevel())
		{
			List<L2Skill> removedSkills = null;
			for(int e = se; e >= 0; e--)
			{
				List<L2Skill> enchantSkills = item.getItem().getEnchantSkills(e);
				if(enchantSkills != null)
				{
					if(removedSkills == null)
						removedSkills = new ArrayList<L2Skill>();
					removedSkills.addAll(enchantSkills);
					break;
				}
			}

			List<L2Skill> addedSkills = null;
			for(int e = item.getEnchantLevel(); e >= 0; e--)
			{
				List<L2Skill> enchantSkills = item.getItem().getEnchantSkills(e);
				if(enchantSkills != null)
				{
					if(addedSkills == null)
						addedSkills = new ArrayList<L2Skill>();
					addedSkills.addAll(enchantSkills);
					break;
				}
			}

			if(removedSkills != null)
			{
				for(L2Skill skill : removedSkills)
				{
					activeChar.removeSkill(skill, false);
				}
				skillsChanged = true;
			}

			if(addedSkills != null)
			{
				for(L2Skill skill : addedSkills)
				{
					activeChar.addSkill(skill, false);
				}
				skillsChanged = true;
			}
		}

		if(item.isArmor() && item.isEquipped() && se != item.getEnchantLevel())
		{
			if(updateArmorSetSkills(activeChar, item, se))
			{
				skillsChanged = true;
			}
		}

		if(skillsChanged)
			activeChar.sendPacket(new SkillList(activeChar));
	}

	/**
	 * Обновляет навыки армор-сета после заточки предмета
	 * @param player игрок
	 * @param item заточенный предмет
	 * @param oldEnchantLevel старый уровень заточки
	 * @return true если навыки были изменены
	 */
	private boolean updateArmorSetSkills(L2Player player, L2ItemInstance item, int oldEnchantLevel)
	{
		boolean updated = false;
		Inventory inv = player.getInventory();

		// Получаем предмет нагрудника
		L2ItemInstance chestItem = inv.getPaperdollItem(Inventory.PAPERDOLL_CHEST);
		if(chestItem == null)
			return false;

		// Получаем армор-сет
		L2ArmorSet armorSet = ArmorSetsTable.getInstance().getSet(chestItem.getItemId());
		if(armorSet == null)
			return false;

		// Проверяем, входит ли заточенный предмет в этот сет
		int slot = item.getEquipSlot();
		if(!armorSet.containItem(slot, item.getItemId()))
			return false;

		// Проверяем, экипирован ли полный сет
		if(!armorSet.containAll(player))
			return false;

		// Получаем старый и новый уровни заточки сета
		int oldSetEnchantLevel = getArmorSetEnchantLevel(player, armorSet, item, oldEnchantLevel);
		int newSetEnchantLevel = armorSet.getEnchantLevel(player);

		// Если уровень заточки сета не изменился, выходим
		if(oldSetEnchantLevel == newSetEnchantLevel)
			return false;

		// Удаляем навыки старого уровня заточки
		updated |= removeArmorSetEnchantSkills(player, armorSet, oldSetEnchantLevel);

		// Добавляем навыки нового уровня заточки
		updated |= addArmorSetEnchantSkills(player, armorSet, newSetEnchantLevel);

		return updated;
	}

	/**
	 * Получает уровень заточки армор-сета с учетом старого уровня заточки одного предмета
	 * @param player игрок
	 * @param armorSet армор-сет
	 * @param changedItem измененный предмет
	 * @param oldEnchantLevel старый уровень заточки предмета
	 * @return уровень заточки сета
	 */
	private int getArmorSetEnchantLevel(L2Player player, L2ArmorSet armorSet, L2ItemInstance changedItem, int oldEnchantLevel)
	{
		Inventory inv = player.getInventory();

		L2ItemInstance chestItem = inv.getPaperdollItem(Inventory.PAPERDOLL_CHEST);
		L2ItemInstance legsItem = inv.getPaperdollItem(Inventory.PAPERDOLL_LEGS);
		L2ItemInstance headItem = inv.getPaperdollItem(Inventory.PAPERDOLL_HEAD);
		L2ItemInstance glovesItem = inv.getPaperdollItem(Inventory.PAPERDOLL_GLOVES);
		L2ItemInstance feetItem = inv.getPaperdollItem(Inventory.PAPERDOLL_FEET);

		int minEnchant = Integer.MAX_VALUE;

		if(chestItem != null)
		{
			int enchant = chestItem.getObjectId() == changedItem.getObjectId() ? oldEnchantLevel : chestItem.getEnchantLevel();
			minEnchant = Math.min(minEnchant, enchant);
		}
		if(legsItem != null && armorSet.containItem(Inventory.PAPERDOLL_LEGS, legsItem.getItemId()))
		{
			int enchant = legsItem.getObjectId() == changedItem.getObjectId() ? oldEnchantLevel : legsItem.getEnchantLevel();
			minEnchant = Math.min(minEnchant, enchant);
		}
		if(headItem != null && armorSet.containItem(Inventory.PAPERDOLL_HEAD, headItem.getItemId()))
		{
			int enchant = headItem.getObjectId() == changedItem.getObjectId() ? oldEnchantLevel : headItem.getEnchantLevel();
			minEnchant = Math.min(minEnchant, enchant);
		}
		if(glovesItem != null && armorSet.containItem(Inventory.PAPERDOLL_GLOVES, glovesItem.getItemId()))
		{
			int enchant = glovesItem.getObjectId() == changedItem.getObjectId() ? oldEnchantLevel : glovesItem.getEnchantLevel();
			minEnchant = Math.min(minEnchant, enchant);
		}
		if(feetItem != null && armorSet.containItem(Inventory.PAPERDOLL_FEET, feetItem.getItemId()))
		{
			int enchant = feetItem.getObjectId() == changedItem.getObjectId() ? oldEnchantLevel : feetItem.getEnchantLevel();
			minEnchant = Math.min(minEnchant, enchant);
		}

		return minEnchant == Integer.MAX_VALUE ? 0 : minEnchant;
	}

	/**
	 * Удаляет навыки заточки армор-сета определенного уровня
	 * @param player игрок
	 * @param armorSet армор-сет
	 * @param enchantLevel уровень заточки
	 * @return true если навыки были удалены
	 */
	private boolean removeArmorSetEnchantSkills(L2Player player, L2ArmorSet armorSet, int enchantLevel)
	{
		boolean removed = false;
		List<SkillInfo> skills = armorSet.getArmorSetEnchantSkillsByLevel(enchantLevel);

		if(skills != null && !skills.isEmpty())
		{
			for(SkillInfo skillInfo : skills)
			{
				L2Skill skill = player.getKnownSkill(skillInfo.getSkillId());
				if(skill != null && player.removeSkill(skill) != null)
				{
					removed = true;
				}
			}
		}

		return removed;
	}

	/**
	 * Добавляет навыки заточки армор-сета определенного уровня
	 * @param player игрок
	 * @param armorSet армор-сет
	 * @param enchantLevel уровень заточки
	 * @return true если навыки были добавлены
	 */
	private boolean addArmorSetEnchantSkills(L2Player player, L2ArmorSet armorSet, int enchantLevel)
	{
		boolean added = false;
		List<SkillInfo> skills = armorSet.getArmorSetEnchantSkillsByLevel(enchantLevel);

		if(skills != null && !skills.isEmpty())
		{
			for(SkillInfo skillInfo : skills)
			{
				L2Skill skill = SkillTable.getInstance().getInfo(skillInfo.getSkillId(), skillInfo.getSkillLevel());
				if(skill != null)
				{
					player.addSkill(skill, false);
					added = true;
				}
			}
		}

		return added;
	}
}