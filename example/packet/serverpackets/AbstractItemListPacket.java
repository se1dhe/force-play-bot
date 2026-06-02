package l2p.gameserver.serverpackets;

import l2p.gameserver.model.items.ItemInfo;

/**
 * @author UnAfraid
 */
public abstract class AbstractItemListPacket extends L2GameServerPacket
{
	private static final int AUGMENTATION = 1 << 0;
	private static final int ATTRIBUTES = 1 << 1;
	private static final int ENCHANT_OPTIONS = 1 << 2;
	private static final int VISUAL = 1 << 3;
	private static final int SOUL_CRYSTAL = 1 << 4;
	private static final int REUSE_DELAY = 1 << 6;

	protected void writeItemInfo(ItemInfo itemInfo)
	{
		writeItemInfo(itemInfo, itemInfo.getCount());
	}

	protected void writeItemInfo(ItemInfo itemInfo, long count)
	{
		int mask = calculateMask(itemInfo);
		writeC(mask);
		writeD(itemInfo.getObjectId());
		writeD(itemInfo.getItemId());
		writeC(itemInfo.isEquipped() ? -1 : itemInfo.getEquipSlot());
		writeQ(count);
		writeC(itemInfo.getItem().getType2ForPackets());
		writeC(itemInfo.getCustomType1());
		writeH(itemInfo.isEquipped() ? 1 : 0);
		writeQ(itemInfo.getItem().getBodyPart());
		writeC(itemInfo.getEnchantLevel());
		writeC(0x01);
		writeD(itemInfo.getShadowLifeTime());
		writeD(itemInfo.getTemporalLifeTime());
		writeC(0x01); // is Blocked
		writeC(0x00); // 140 protocol
		writeC(0x00); // 140 protocol

		if(containsMask(mask, AUGMENTATION))
		{
			writeD(itemInfo.getAugmentationId() & 0x0000FFFF);
			writeD(itemInfo.getAugmentationId() >> 16);
		}

		if(containsMask(mask, ATTRIBUTES))
		{
			writeH(-1/*itemInfo.getAttackElement()*/);
			writeH(0x00/*itemInfo.getAttackElementValue()*/);
			writeH(0x00/*temInfo.getDefenceFire()*/);
			writeH(0x00/*temInfo.getDefenceWater()*/);
			writeH(0x00/*temInfo.getDefenceWind()*/);
			writeH(0x00/*temInfo.getDefenceEarth()*/);
			writeH(0x00/*temInfo.getDefenceHoly()*/);
			writeH(0x00/*temInfo.getDefenceUnholy()*/);
		}

		if(containsMask(mask, ENCHANT_OPTIONS))
		{
			//int[] enchantOptions = itemInfo.getEnchantOptions();
			writeD(0x00/*enchantOptions[0]*/);
			writeD(0x00/*enchantOptions[1]*/);
			writeD(0x00/*enchantOptions[2]*/);
		}

		if(containsMask(mask, VISUAL))
		{
			writeD(0x00);
		}

		if(containsMask(mask, SOUL_CRYSTAL))
		{
			int ensoulSlotN1 = 0/*itemInfo.getEnsoulSlotN1()*/;
			int ensoulSlotN2 = 0/*itemInfo.getEnsoulSlotN2()*/;
			if(ensoulSlotN1 > 0 && ensoulSlotN2 > 0)
			{
				writeC(0x02);
				writeD(ensoulSlotN1);
				writeD(ensoulSlotN2);
			}
			else if(ensoulSlotN1 > 0)
			{
				writeC(0x01);
				writeD(ensoulSlotN1);
			}
			else if(ensoulSlotN2 > 0)
			{
				writeC(0x01);
				writeD(ensoulSlotN2);
			}
			else
			{
				writeC(0x00);
			}

			//if(itemInfo.getEnsoulSlotBm() > 0)
			//{
			//	writeC(0x01);
			//	writeD(itemInfo.getEnsoulSlotBm());
			//}
			//else
			{
				writeC(0x00);
			}
		}

		if(containsMask(mask, REUSE_DELAY))
		{
			writeD(0x00/*itemInfo.getReuseDelay()*/); // reuse delay
		}
	}

	private boolean containsMask(int mask, int type)
	{
		return (mask & type) == type;
	}

	private int calculateMask(ItemInfo item)
	{
		int mask = 0;
		if(item.isAugmentation())
		{
			mask |= AUGMENTATION;
		}
		//if(item.haveAttributes())
		//{
		//	mask |= ATTRIBUTES;
		//}
		//if(item.haveEnchantOptions())
		//{
		//	mask |= ENCHANT_OPTIONS;
		//}
		//if(item.haveEnsoul())
		//{
		//	mask |= SOUL_CRYSTAL;
		//}
		return mask;
	}
}