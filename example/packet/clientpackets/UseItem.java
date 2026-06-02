package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.configuration.ConfigBattleGround;
import l2p.gameserver.configuration.ConfigCaptureCastle;
import l2p.gameserver.configuration.ConfigKoreanTvT;
import l2p.gameserver.configuration.ConfigSquidGame;
import l2p.gameserver.configuration.FightClubConfig;
import l2p.gameserver.handler.IItemHandler;
import l2p.gameserver.handler.ItemHandler;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.model.skin.SkinCondition;
import l2p.gameserver.serverpackets.ShowCalculator;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.tables.ItemTable;
import l2p.gameserver.tables.PetDataTable;
import l2p.gameserver.templates.L2Item;
import l2p.gameserver.utils.Util;
import org.apache.commons.lang3.ArrayUtils;

public class UseItem extends L2GameClientPacket
{
	private int _objectId;
	private boolean ctrl_pressed;

	@Override
	public void readImpl()
	{
		_objectId = readD();
		ctrl_pressed = readD() == 1;
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;


		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isJailed())
		{
			activeChar.sendMessage("You cannot use items in Jail.");
			return;
		}

		activeChar.setActive();

		PcInventory inventory = activeChar.getInventory();
		inventory.writeInvLock();
		try
		{
			L2ItemInstance item = inventory.getItemByObjectId(_objectId);

			if(item == null || item.isArrow())
			{
				activeChar.sendActionFailed();
				return;
			}

			boolean equip = item.isEquipable();
			if(!equip)
			{
				if(System.currentTimeMillis() - activeChar.getLastItemPacket() < Config.ITEM_PACKET_DELAY)
				{
					activeChar.sendActionFailed();
					return;
				}
				activeChar.setLastItemPacket();
			}

			if(activeChar.isInTrade())
			{
				activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_PICK_UP_OR_USE_ITEMS_WHILE_TRADING));
				return;
			}

			int itemId = item.getItemId();
			if(activeChar.isInStoreMode() && itemId != 728)
			{
				if(PetDataTable.isPetControlItem(item))
					activeChar.sendPacket(Msg.YOU_CANNOT_SUMMON_DURING_A_TRADE_OR_WHILE_USING_THE_PRIVATE_SHOPS);
				else if(Config.AGATHION_DATAS.containsKey(item.getItemId()))
					activeChar.sendPacket(Msg.YOU_CANNOT_SUMMON_DURING_A_TRADE_OR_WHILE_USING_THE_PRIVATE_SHOPS);
				else
					activeChar.sendPacket(Msg.YOU_MAY_NOT_USE_ITEMS_IN_A_PRIVATE_STORE_OR_PRIVATE_WORK_SHOP);
				activeChar.sendActionFailed();
				return;
			}

			if(itemId == 57)
			{
				activeChar.sendActionFailed();
				return;
			}

			if(activeChar.isFishing() && (itemId < 6535 || itemId > 6540))
			{
				// You cannot do anything else while fishing
				activeChar.sendPacket(Msg.YOU_CANNOT_DO_ANYTHING_ELSE_WHILE_FISHING);
				return;
			}

			if(FightClubConfig.FC_CUSTOM_ITEMS_ENABLE)
			{
				if(activeChar.isInFightClub() && activeChar.isFightClubCustomItemsEquipped() && item.isEquipable())
				{
					activeChar.sendActionFailed();
					return;
				}
			}

			if(Config.EVENT_OBT_ENABLE)
			{
				if(ArrayUtils.contains(Config.EVENT_OBT_DISABLED_USE_ITEMS, itemId))
				{
					activeChar.sendActionFailed();
					return;
				}
			}

			if(activeChar.isDead())
			{
				activeChar.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(itemId));
				return;
			}

			if(item.getItem().isForPet())
			{
				activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_EQUIP_A_PET_ITEM).addItemName(itemId));
				return;
			}

			if(activeChar.isInOlympiadMode() && ArrayUtils.contains(Config.OLY_RESTRICTED_ITEMS, itemId))
			{
				if(equip)
					activeChar.sendPacket(new SystemMessage(SystemMessage.THIS_ITEM_CANT_BE_EQUIPPED_FOR_THE_OLYMPIAD_EVENT));
				else
					activeChar.sendPacket(new SystemMessage(SystemMessage.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
				activeChar.sendActionFailed();
				return;
			}

			if((activeChar.inBattleGround && ArrayUtils.contains(ConfigBattleGround.BATTLE_GROUND_RESTRICTED_ITEMS, item.getItemId())) || (activeChar.inEvent && item.getItem().noEvent()) || (activeChar.inLH && item.getItem().noLH()) || (activeChar.inGvG && item.getItem().noGvG()) || (activeChar.isInCaptureCastleEvent() && ArrayUtils.contains(ConfigCaptureCastle.CAPTURE_CASTLE_INCLUDE_ITEMS, item.getItemId())))
			{
				if(equip)
					activeChar.sendMessage(activeChar.isLangRus() ? "Этот предмет нельзя экипировать в эвенте." : "This item can't be equipped in event.");
				else
					activeChar.sendMessage(activeChar.isLangRus() ? "Этот предмет недоступен в эвенте." : "This item is not available in event.");
				activeChar.sendActionFailed();
				return;
			}

			if(Config.SKINS_ENABLE && Config.SKIN_WEAPON_EQUIP_ENABLE)
			{
				if(!SkinCondition.canEquipItem(activeChar, item))
				{
					return;
				}
			}

			if(equip)
			{
				// Нельзя снимать/одевать любое снаряжение при этих условиях
				if(activeChar.isStunned() || activeChar.isSleeping() || activeChar.isParalyzed() || activeChar.isAlikeDead())
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(itemId));
					return;
				}

				if(activeChar.inTvT && Config.TvT_CustomItems || activeChar.inCtF && Config.CtF_CustomItems || activeChar.inLH && Config.LastHero_CustomItems || activeChar.inDeathMatch && Config.EVENT_DEATHMATCH_CUSTOM_ITEMS || activeChar.inDecisiveDeath && Config.EVENT_DECISIVEDEATH_CUSTOM_ITEMS
						|| activeChar.isInCaptureCastleEvent() && ConfigCaptureCastle.CAPTURE_CASTLE_CustomItems || activeChar.inSquidGame && ConfigSquidGame.SquidGame_CustomItems || activeChar.inKoreanTvT && ConfigKoreanTvT.KOREAN_TVT_CustomItems)
				{
					activeChar.sendMessage(activeChar.isLangRus() ? "Экипировка недоступна в эвенте." : "Equipment not available in event.");
					activeChar.sendActionFailed();
					return;
				}

				int bodyPart = item.getBodyPart();

				if(bodyPart == L2Item.SLOT_LR_HAND || bodyPart == L2Item.SLOT_L_HAND || bodyPart == L2Item.SLOT_R_HAND)
				{
					// Нельзя снимать/одевать оружие, сидя на пете
					if(activeChar.isMounted())
					{
						activeChar.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(itemId));
						return;
					}

					// Нельзя снимать/одевать проклятое оружие и флаги
					if(activeChar.isCursedWeaponEquipped() || activeChar.isFlagEquipped())
					{
						activeChar.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(itemId));
						return;
					}
				}

				// Нельзя снимать/одевать проклятое оружие
				if(item.isCursed())
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(itemId));
					return;
				}

				if((item.getCustomFlags() & L2ItemInstance.FLAG_NO_UNEQUIP) == L2ItemInstance.FLAG_NO_UNEQUIP)
				{
					activeChar.sendActionFailed();
					return;
				}
				// Don't allow weapon/shield hero equipment during Olympiads
				if(activeChar.isInOlympiadMode() && item.isHeroWeapon())
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.THIS_ITEM_CANT_BE_EQUIPPED_FOR_THE_OLYMPIAD_EVENT));
					activeChar.sendActionFailed();
					return;
				}

				if(activeChar.isCastingNow())
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_USE_EQUIPMENT_WHEN_USING_OTHER_SKILLS_OR_MAGIC));
					return;
				}

				if(activeChar.getInventory().isLockedItem(item))
					return;

				if(item.isEquipped())
				{
					if(activeChar.recording)
						activeChar.recBot(3, item.getBodyPart(), 1, 0, 0, 0, 0);
					inventory.unEquipItemInBodySlotAndNotify(item.getBodyPart(), item);
					activeChar.getListeners().onEquipUnEquipItem(item, false);
					return;
				}

				if(Config.ZONE_EQUIP && activeChar.restrictEquipZone(itemId))
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(itemId));
					return;
				}

				inventory.equipItem(item, true);
				activeChar.getListeners().onEquipUnEquipItem(item, true);
				if(!item.isEquipped())
				{
					activeChar.sendActionFailed();
					return;
				}

				SystemMessage sm;
				if(item.getEnchantLevel() > 0)
				{
					sm = new SystemMessage(SystemMessage.EQUIPPED__S1_S2);
					sm.addNumber(item.getEnchantLevel());
					sm.addItemName(itemId);
				}
				else
					sm = new SystemMessage(SystemMessage.YOU_HAVE_EQUIPPED_YOUR_S1).addItemName(itemId);
				activeChar.sendPacket(sm);
				if(item.isTemporalItem())
					activeChar.sendPacket(new SystemMessage(SystemMessage.S1_S2).addString((activeChar.isLangRus() ? ": осталось " : ": remained ") + Util.formatTime(item.getLifeTimeRemaining(), activeChar.isLangRus())).addItemName(item.getItemId()));

				if(item.getItem().getType2() != L2Item.TYPE2_ACCESSORY)
					activeChar.broadcastUserInfo(false);
				return;
			}
			if(item.isTemporalItem())
				activeChar.sendPacket(new SystemMessage(SystemMessage.S1_S2).addString((activeChar.isLangRus() ? ": осталось " : ": remained ") + Util.formatTime(item.getLifeTimeRemaining(), activeChar.isLangRus())).addItemName(item.getItemId()));

			if(itemId == 4393)
			{
				activeChar.sendPacket(new ShowCalculator(itemId));
				return;
			}

			if(activeChar.getInventory().isLockedItem(item))
				return;

			if(ItemTable.useHandler(activeChar, item, ctrl_pressed))
				return;

			IItemHandler handler = ItemHandler.getInstance().getItemHandler(itemId);
			if(handler != null)
				handler.useItem(activeChar, item, ctrl_pressed);
		}
		finally
		{
			inventory.writeInvUnlock();
		}
	}
}