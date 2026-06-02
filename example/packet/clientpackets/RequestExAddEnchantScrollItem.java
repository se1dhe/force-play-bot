package l2p.gameserver.clientpackets;

import l2p.commons.util.Rnd;
import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.data.xml.holder.EnchantItemHolder;
import l2p.gameserver.data.xml.holder.ObtEnchantItemHolder;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.model.instances.L2ItemInstance.ItemLocation;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.multilang.CustomMessage;
import l2p.gameserver.serverpackets.ExPutEnchantScrollItemResult;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.templates.L2Item;
import l2p.gameserver.templates.enchant.EnchantScroll;
import l2p.gameserver.templates.enchant.EnchantVariation;
import l2p.gameserver.templates.enchant.EnchantVariation.EnchantLevel;
import l2p.gameserver.utils.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestExAddEnchantScrollItem extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(RequestExAddEnchantScrollItem.class);

	private int _scrollObjectId;
	private int _itemObjectId;

	@Override
	public void readImpl()
	{
		_scrollObjectId = readD();
		_itemObjectId = readD();
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
			activeChar.sendPacket(ExPutEnchantScrollItemResult.FAIL);
			return;
		}

		if(activeChar.isOutOfControl() || activeChar.isActionsDisabled())
		{
			activeChar.sendPacket(ExPutEnchantScrollItemResult.FAIL);
			return;
		}

		if(activeChar.inEvent() || activeChar.isInGvG())
		{
			activeChar.sendMessage("You can't enchant in event.");
			activeChar.sendPacket(ExPutEnchantScrollItemResult.FAIL);
			return;
		}

		if(activeChar.isEnchantLimit())
		{
			activeChar.sendMessage("You can't enchant with active enchant limit.");
			activeChar.sendPacket(ExPutEnchantScrollItemResult.FAIL);
			return;
		}

		PcInventory inventory = activeChar.getInventory();
		L2ItemInstance item = inventory.getItemByObjectId(_itemObjectId);
		L2ItemInstance scroll = inventory.getItemByObjectId(_scrollObjectId);

		if(item == null || scroll == null)
		{
			activeChar.sendPacket(ExPutEnchantScrollItemResult.FAIL);
			return;
		}

		Log.add(activeChar.getName() + "|Trying to put enchant|" + item.getItemId() + "|+" + item.getEnchantLevel() + "|" + item.getObjectId(), "enchants");

		final EnchantScroll enchantScroll = Config.EVENT_OBT_ENABLE ? ObtEnchantItemHolder.getInstance().getEnchantScroll(scroll.getItemId()) : EnchantItemHolder.getInstance().getEnchantScroll(scroll.getItemId());
		if(enchantScroll == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(item.getEnchantLevel() < enchantScroll.getMinEnchant() || enchantScroll.getMaxEnchant() != -1 && item.getEnchantLevel() >= enchantScroll.getMaxEnchant())
		{
			activeChar.sendPacket(ExPutEnchantScrollItemResult.FAIL);
			activeChar.sendPacket(Msg.INAPPROPRIATE_ENCHANT_CONDITIONS);
			activeChar.sendActionFailed();
			return;
		}

		final int itemType = item.getItem().getType2();
		if(enchantScroll.getItems().size() > 0)
		{
			if(!enchantScroll.getItems().contains(item.getItemId()))
			{
				activeChar.sendPacket(ExPutEnchantScrollItemResult.FAIL);
				activeChar.sendPacket(new SystemMessage(SystemMessage.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL));
				activeChar.sendActionFailed();
				return;
			}

			if(enchantScroll.getProhibitedItems().contains(item.getItemId()))
			{
				activeChar.sendPacket(ExPutEnchantScrollItemResult.FAIL);
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
					activeChar.sendPacket(ExPutEnchantScrollItemResult.FAIL);
					activeChar.sendPacket(new SystemMessage(SystemMessage.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL));
					activeChar.sendActionFailed();
					return;
				}
			}
			else
			{
				if(!enchantScroll.containsGrade(item.getItem().getItemGrade()))
				{
					activeChar.sendPacket(ExPutEnchantScrollItemResult.FAIL);
					activeChar.sendPacket(new SystemMessage(SystemMessage.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL));
					activeChar.sendActionFailed();
					return;
				}
			}

			if(enchantScroll.getProhibitedItems().contains(item.getItemId()))
			{
				activeChar.sendPacket(ExPutEnchantScrollItemResult.FAIL);
				activeChar.sendPacket(new SystemMessage(SystemMessage.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL));
				activeChar.sendActionFailed();
				return;
			}

			switch(enchantScroll.getType())
			{
				case ARMOR:
					if(itemType == L2Item.TYPE2_WEAPON || item.getItem().isHairAccessory())
					{
						activeChar.sendPacket(ExPutEnchantScrollItemResult.FAIL);
						activeChar.sendPacket(new SystemMessage(SystemMessage.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL));
						activeChar.sendActionFailed();
						return;
					}
					break;
				case WEAPON:
					if(itemType == L2Item.TYPE2_SHIELD_ARMOR || itemType == L2Item.TYPE2_ACCESSORY || item.getItem().isHairAccessory())
					{
						activeChar.sendPacket(ExPutEnchantScrollItemResult.FAIL);
						activeChar.sendPacket(new SystemMessage(SystemMessage.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL));
						activeChar.sendActionFailed();
						return;
					}
					break;
				case HAIR_ACCESSORY:
					if(!item.getItem().isHairAccessory())
					{
						activeChar.sendPacket(ExPutEnchantScrollItemResult.FAIL);
						activeChar.sendPacket(new SystemMessage(SystemMessage.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL));
						activeChar.sendActionFailed();
						return;
					}
					break;
			}
		}

		if(!enchantScroll.getItems().contains(item.getItemId()) && !item.canBeEnchanted())
		{
			activeChar.sendPacket(ExPutEnchantScrollItemResult.FAIL);
			activeChar.sendPacket(Msg.INAPPROPRIATE_ENCHANT_CONDITIONS);
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
			activeChar.sendPacket(ExPutEnchantScrollItemResult.FAIL);
			activeChar.sendActionFailed();
			return;
		}

		final EnchantLevel enchantLevel = variation.getLevel(item.getEnchantLevel() + 1);
		if(enchantLevel == null)
		{
			activeChar.sendActionFailed();
			_log.warn("RequestEnchantItem: Cannot find variation ID[" + enchantScroll.getVariationId() + "] enchant level[" + (item.getEnchantLevel() + 1) + "] for enchant scroll ID[" + enchantScroll.getItemId() + "]!");
			return;
		}

		if(item.getLocation() != ItemLocation.INVENTORY && item.getLocation() != ItemLocation.PAPERDOLL)
		{
			activeChar.sendPacket(ExPutEnchantScrollItemResult.FAIL);
			activeChar.sendPacket(Msg.INAPPROPRIATE_ENCHANT_CONDITIONS);
			return;
		}

		if(activeChar.getPrivateStoreType() != L2Player.STORE_PRIVATE_NONE)
		{
			activeChar.sendPacket(ExPutEnchantScrollItemResult.FAIL);
			activeChar.sendPacket(Msg.YOU_CANNOT_PRACTICE_ENCHANTING_WHILE_OPERATING_A_PRIVATE_STORE_OR_PRIVATE_MANUFACTURING_WORKSHOP);
			return;
		}

		if((scroll = inventory.getItemByObjectId(scroll.getObjectId())) == null)
		{
			activeChar.sendPacket(ExPutEnchantScrollItemResult.FAIL);
			return;
		}

		// Запрет на заточку чужих вещей, баг может вылезти на серверных лагах
		if(item.getOwnerId() != activeChar.getObjectId())
		{
			activeChar.sendPacket(ExPutEnchantScrollItemResult.FAIL);
			activeChar.sendPacket(Msg.INAPPROPRIATE_ENCHANT_CONDITIONS);
			return;
		}

		activeChar.sendPacket(new ExPutEnchantScrollItemResult(scroll.getObjectId()));
		activeChar.setEnchantScroll(scroll);
	}
}