package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.serverpackets.ExPutIntensiveResultForVariationMake;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.templates.L2Item;
import org.apache.commons.lang3.ArrayUtils;

public class RequestConfirmRefinerItem extends L2GameClientPacket
{
	// format: (ch)dd
	private static final int GEMSTONE_D = 2130;
	private static final int GEMSTONE_C = 2131;

	private int _targetItemObjId;
	private int _refinerItemObjId;

	@Override
	public void readImpl()
	{
		_targetItemObjId = readD();
		_refinerItemObjId = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		L2ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(_targetItemObjId);
		L2ItemInstance refinerItem = activeChar.getInventory().getItemByObjectId(_refinerItemObjId);

		if(targetItem == null || refinerItem == null)
			return;

		int itemGrade = targetItem.getItem().getItemGrade();
		int refinerItemId = refinerItem.getItem().getItemId();
		//int lifeStoneLevel = getLifeStoneLevel(refinerItemId);

		// is the item a life stone?
		boolean newLifeStones = ArrayUtils.contains(Config.NEW_LIFE_STONE_IDS, refinerItemId);
		if((refinerItemId < 8723 || refinerItemId > 8762) && !newLifeStones)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.THIS_IS_NOT_A_SUITABLE_ITEM));
			return;
		}
		if(activeChar.isInFightClub())
		{
			activeChar.sendActionFailed();
			return;
		}

		int gemstoneCount = 0;
		int gemstoneItemId = 0;
		SystemMessage sm = new SystemMessage(SystemMessage.REQUIRES_S1_S2);
		switch(itemGrade)
		{
			case L2Item.CRYSTAL_C:
				gemstoneCount = 20;
				gemstoneItemId = GEMSTONE_D;
				sm.addNumber(gemstoneCount);
				sm.addString("Gemstone D");
				break;
			case L2Item.CRYSTAL_B:
				/*if(lifeStoneLevel < 3)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.THIS_IS_NOT_A_SUITABLE_ITEM));
					return;
				}*/
				gemstoneCount = 30;
				gemstoneItemId = GEMSTONE_D;
				sm.addNumber(gemstoneCount);
				sm.addString("Gemstone D");
				break;
			case L2Item.CRYSTAL_A:
				/*if(lifeStoneLevel < 6)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.THIS_IS_NOT_A_SUITABLE_ITEM));
					return;
				}*/
				gemstoneCount = 20;
				gemstoneItemId = GEMSTONE_C;
				sm.addNumber(gemstoneCount);
				sm.addString("Gemstone C");
				break;
			case L2Item.CRYSTAL_S:
				/*if(lifeStoneLevel < 10)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.THIS_IS_NOT_A_SUITABLE_ITEM));
					return;
				}*/
				gemstoneCount = 25;
				gemstoneItemId = GEMSTONE_C;
				sm.addNumber(gemstoneCount);
				sm.addString("Gemstone C");
				break;
		}

		activeChar.sendPacket(new ExPutIntensiveResultForVariationMake(_refinerItemObjId, refinerItemId, gemstoneItemId, gemstoneCount));
	}

	private int getLifeStoneGrade(int itemId)
	{
		itemId -= 8723;
		if(itemId < 10)
			return 0; // normal grade
		if(itemId < 20)
			return 1; // mid grade
		if(itemId < 30)
			return 2; // high grade
		return 3; // top grade
	}

	private int getLifeStoneLevel(int itemId)
	{
		itemId -= 10 * getLifeStoneGrade(itemId);
		itemId -= 8722;
		return itemId;
	}
}