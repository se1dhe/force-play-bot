package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.configuration.ConfigCaptureCastle;
import l2p.gameserver.configuration.ConfigKoreanTvT;
import l2p.gameserver.configuration.ConfigSquidGame;
import l2p.gameserver.configuration.FightClubConfig;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.model.items.Inventory;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.model.skin.SkinCondition;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.templates.L2Item;

public class RequestUnEquipItem extends L2GameClientPacket
{
	private int _slot;

	@Override
	public void readImpl()
	{
		_slot = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;

		PcInventory inventory = player.getInventory();
		inventory.writeInvLock();
		try
		{
			// Нельзя снимать проклятое оружие и флаги
			if((_slot == L2Item.SLOT_R_HAND || _slot == L2Item.SLOT_L_HAND || _slot == L2Item.SLOT_LR_HAND) && (player.isCursedWeaponEquipped() || player.isFlagEquipped()))
				return;

			if(player.isCastingNow())
			{
				player.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_USE_EQUIPMENT_WHEN_USING_OTHER_SKILLS_OR_MAGIC));
				return;
			}

			if(FightClubConfig.FC_CUSTOM_ITEMS_ENABLE)
			{
				if(player.isInFightClub() && player.isFightClubCustomItemsEquipped())
				{
					player.sendActionFailed();
					return;
				}
			}

			if(_slot == L2Item.SLOT_L_HAND)
			{
				L2ItemInstance item = inventory.getPaperdollItem(Inventory.PAPERDOLL_LHAND);
				if(item != null && item.isArrow())
					return;
			}

			if(player.inTvT && Config.TvT_CustomItems || player.inCtF && Config.CtF_CustomItems || player.inLH && Config.LastHero_CustomItems || player.inDeathMatch && Config.EVENT_DEATHMATCH_CUSTOM_ITEMS || player.inDecisiveDeath && Config.EVENT_DECISIVEDEATH_CUSTOM_ITEMS
					|| player.isInCaptureCastleEvent() && ConfigCaptureCastle.CAPTURE_CASTLE_CustomItems || player.inSquidGame && ConfigSquidGame.SquidGame_CustomItems || player.inKoreanTvT && ConfigKoreanTvT.KOREAN_TVT_CustomItems)
				return;


			if(Config.SKINS_ENABLE && Config.SKIN_WEAPON_EQUIP_ENABLE)
			{
				if(!SkinCondition.canUnequipItem(player, _slot))
					return;
			}

			inventory.unEquipItemInBodySlotAndNotify(_slot, null);
		}
		finally
		{
			inventory.writeInvUnlock();
		}
		if(player.recording)
			player.recBot(3, _slot, 0, 0, 0, 0, 0);
	}
}