package l2p.gameserver.clientpackets;

import l2p.gameserver.Announcements;
import l2p.gameserver.Config;
import l2p.gameserver.data.xml.holder.AugmentCountSkillsHolder;
import l2p.gameserver.instancemanager.GlobalVariables;
import l2p.gameserver.model.L2ObjectsStorage;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2ShortCut;
import l2p.gameserver.model.L2Skill;
import l2p.gameserver.model.base.L2Augmentation;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.multilang.CustomMessage;
import l2p.gameserver.serverpackets.ExVariationResult;
import l2p.gameserver.serverpackets.InventoryUpdate;
import l2p.gameserver.serverpackets.Say2;
import l2p.gameserver.serverpackets.ShortCutRegister;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.tables.AugmentationData;
import l2p.gameserver.templates.AugmentCountSkillData;
import l2p.gameserver.templates.L2Item;
import l2p.gameserver.utils.Log;
import org.apache.commons.lang3.ArrayUtils;

public final class RequestRefine extends L2GameClientPacket
{
	// format: (ch)dddd
	private int _targetItemObjId, _refinerItemObjId, _gemstoneItemObjId;
	private int _gemstoneCount;

	@Override
	protected void readImpl()
	{
		_targetItemObjId = readD();
		_refinerItemObjId = readD();
		_gemstoneItemObjId = readD();
		// TODO [V] - long
		_gemstoneCount = getClient().isITClient() ? readD() : (int) readQ();
	}

	@Override
	protected void runImpl()
	{
		if(_gemstoneCount < 0)
			return;

		L2Player activeChar = getClient().getActiveChar();
		L2ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(_targetItemObjId);
		L2ItemInstance refinerItem = activeChar.getInventory().getItemByObjectId(_refinerItemObjId);
		L2ItemInstance gemstoneItem = activeChar.getInventory().getItemByObjectId(_gemstoneItemObjId);

		if(targetItem == null || refinerItem == null || gemstoneItem == null || targetItem.getOwnerId() != activeChar.getObjectId() || refinerItem.getOwnerId() != activeChar.getObjectId() || gemstoneItem.getOwnerId() != activeChar.getObjectId() || activeChar.getLevel() < 46)
		{
			activeChar.sendPacket(new ExVariationResult(0, 0, 0));
			activeChar.sendPacket(new SystemMessage(SystemMessage.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS));
			return;
		}

		if(TryAugmentItem(activeChar, targetItem, refinerItem, gemstoneItem, _gemstoneCount, false))
		{
			int stat12 = 0x0000FFFF & targetItem.getAugmentation().getAugmentationId();
			int stat34 = targetItem.getAugmentation().getAugmentationId() >> 16;
			activeChar.sendPacket(new ExVariationResult(stat12, stat34, 1));
			activeChar.sendPacket(new SystemMessage(SystemMessage.THE_ITEM_WAS_SUCCESSFULLY_AUGMENTED));
		}
		else
		{
			activeChar.sendPacket(new ExVariationResult(0, 0, 0));
			activeChar.sendPacket(new SystemMessage(SystemMessage.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS));
		}
	}

	public static boolean TryAugmentItem(L2Player player, L2ItemInstance targetItem, L2ItemInstance refinerItem, L2ItemInstance gemstoneItem, int modifyGemstoneCount, boolean service)
	{
		if(targetItem.isAugmented())
		{
			player.sendPacket(new SystemMessage(SystemMessage.ONCE_AN_ITEM_IS_AUGMENTED_IT_CANNOT_BE_AUGMENTED_AGAIN));
			return false;
		}
		if(player.getPrivateStoreType() != L2Player.STORE_PRIVATE_NONE)
		{
			player.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_AUGMENT_ITEMS_WHILE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP_IS_IN_OPERATION));
			return false;
		}
		if(player.isDead())
		{
			player.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_AUGMENT_ITEMS_WHILE_DEAD));
			return false;
		}
		if(player.isInTrade())
		{
			player.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_AUGMENT_ITEMS_WHILE_TRADE));
			return false;
		}
		if(player.isParalyzed())
		{
			player.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_AUGMENT_ITEMS_WHILE_PARALYZED));
			return false;
		}
		if(player.isFishing())
		{
			player.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_AUGMENT_ITEMS_WHILE_FISHING));
			return false;
		}
		if(player.isSitting())
		{
			player.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_AUGMENT_ITEMS_WHILE_SITTING_DOWN));
			return false;
		}
		if(player.isBlocked())
		{
			player.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_AUGMENT_ITEMS_WHILE_FROZEN));
			return false;
		}
		if(player.isInFightClub())
		{
			player.sendActionFailed();
			return false;
		}
		if(targetItem.isHeroItem() || targetItem.isCursed() || targetItem.isShadowItem() || targetItem.isTemporalItem())
			return false;

		int itemGrade = targetItem.getItem().getItemGrade();
		int itemType = targetItem.getItem().getType2();
		int lifeStoneId = service ? 0 : refinerItem.getItemId();
		int gemstoneItemId = service ? 0 : gemstoneItem.getItemId();
		boolean newLifeStones = ArrayUtils.contains(Config.NEW_LIFE_STONE_IDS, lifeStoneId);

		if(!service && !newLifeStones && (lifeStoneId < 8723 || lifeStoneId > 8762))
			return false;

		if(itemGrade < L2Item.CRYSTAL_C || itemType != L2Item.TYPE2_WEAPON)
			return false;

		int lifeStoneLevel = service || newLifeStones ? 10 : getLifeStoneLevel(lifeStoneId);
		int lifeStoneGrade = service || newLifeStones ? 3 : getLifeStoneGrade(lifeStoneId);
		if(!service)
		{	switch(itemGrade)
			{
				case L2Item.CRYSTAL_C:
					if(player.getLevel() < 46 || gemstoneItemId != 2130)
						return false;
					modifyGemstoneCount = 20;
					break;
				case L2Item.CRYSTAL_B:
					if(player.getLevel() < 52 || gemstoneItemId != 2130)
						return false;
					modifyGemstoneCount = 30;
					break;
				case L2Item.CRYSTAL_A:
					if(player.getLevel() < 61 || gemstoneItemId != 2131)
						return false;
					modifyGemstoneCount = 20;
					break;
				case L2Item.CRYSTAL_S:
					if(player.getLevel() < 76 || gemstoneItemId != 2131)
						return false;
					modifyGemstoneCount = 25;
					break;
			}

			// check if the lifestone is appropriate for this player
			switch(lifeStoneLevel)
			{
				case 1:
					if(player.getLevel() < 46)
						return false;
					break;
				case 2:
					if(player.getLevel() < 49)
						return false;
					break;
				case 3:
					if(player.getLevel() < 52)
						return false;
					break;
				case 4:
					if(player.getLevel() < 55)
						return false;
					break;
				case 5:
					if(player.getLevel() < 58)
						return false;
					break;
				case 6:
					if(player.getLevel() < 61)
						return false;
					break;
				case 7:
					if(player.getLevel() < 64)
						return false;
					break;
				case 8:
					if(player.getLevel() < 67)
						return false;
					break;
				case 9:
					if(player.getLevel() < 70)
						return false;
					break;
				case 10:
					if(player.getLevel() < 76)
						return false;
					break;
			}

			if(gemstoneItem.getIntegerLimitedCount() < modifyGemstoneCount)
				return false;
			player.getInventory().destroyItem(gemstoneItem.getObjectId(), modifyGemstoneCount, true);

			// consume the life stone
			player.getInventory().destroyItem(refinerItem.getObjectId(), 1, true);
		}
		// generate augmentation
		lifeStoneLevel = Math.min(lifeStoneLevel, 10) - 1;

		AugmentCountSkillsHolder countSkillsHolder = AugmentCountSkillsHolder.getInstance();
		L2Augmentation aug = AugmentationData.getInstance().generateRandomAugmentation(player, countSkillsHolder, targetItem, lifeStoneId, lifeStoneLevel, lifeStoneGrade, service, newLifeStones, player.isPremium());
		if(aug == null)
		{
			player.sendMessage("You cannot insert life stone with this level.");
			return false;
		}
		targetItem.setAugmentation(aug);

		if(targetItem.isEquipped())
			targetItem.getAugmentation().applyBoni(player);
		player.sendPacket(new InventoryUpdate().addModifiedItem(targetItem, player.isEnchantLimit()));
		for(L2ShortCut sc : player.getAllShortCuts())
			if(sc.getId() == targetItem.getObjectId() && sc.getType() == L2ShortCut.TYPE_ITEM)
				player.sendPacket(new ShortCutRegister(sc));

		if(targetItem.isEquipped())
			player.broadcastUserInfo(false);

		Log.LogItem(player, Log.Refine, targetItem);
		Log.LogEnchant(player, Log.ItemLog.AddRefine, targetItem, lifeStoneId);
		player.getListeners().onAugmentItem(targetItem, aug);

		L2Skill skill = aug.getAugmentSkill();
		if(skill != null)
		{
			if(!Config.EVENT_OBT_ENABLE)
			{
				if(!service && countSkillsHolder.isEnable())
				{
					AugmentCountSkillData augmentCountSkill = countSkillsHolder.getAugmentCountSkillData(skill.getId());
					if(augmentCountSkill != null)
					{
						int count = GlobalVariables.getInstance().getVarInt("AugmentSkillCount" + skill.getId(), 0);

						GlobalVariables.getInstance().setVar("AugmentSkillCount" + skill.getId(), String.valueOf(count + 1), (System.currentTimeMillis() / 1000L) + augmentCountSkill.getDelay());
					}
				}
			}

			if(ArrayUtils.contains(Config.AUGMENT_SUCCESS_ANNOUNCE_SKILL_IDS, skill.getId()))
			{
				String skillName = skill.getName();
				if(skillName.startsWith("Item Skill: "))
				{
					skillName = skillName.substring(12);
				}
				else if(skillName.startsWith("Item Skill:"))
				{
					skillName = skillName.substring(11);
				}
				announceByCustomMessage("C1_GOT_THE_S4_S5_AUGMENTATION_SKILL_IN_S3_S2", new String[]{player.getName(), targetItem.getName(), String.valueOf(targetItem.getEnchantLevel()), skillName, String.valueOf(skill.getLevel())});
			}
		}

		return true;
	}

	private static int getLifeStoneGrade(int itemId)
	{
		itemId -= 8723;
		if(itemId < 10)
			return 0;
		if(itemId < 20)
			return 1;
		if(itemId < 30)
			return 2;
		return 3;
	}

	private static int getLifeStoneLevel(int itemId)
	{
		itemId -= 10 * getLifeStoneGrade(itemId);
		itemId -= 8722;
		return itemId;
	}

	public static void announceByCustomMessage(String address, String[] replacements)
	{
		for(L2Player player : L2ObjectsStorage.getAllPlayersForIterate())
		{
			boolean result = player.getVarB("@announce_augment_item", Config.ANNOUNCE_AUGMENT_ITEM_DEFAULT);
			if(result)
				Announcements.getInstance().announceToPlayerByCustomMessage(player, address, replacements);
		}
	}
}