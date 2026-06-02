package l2p.gameserver.serverpackets;

import l2p.gameserver.model.instances.L2ItemInstance;

import java.util.ArrayList;
import java.util.List;

public class ExGetCrystalizingEstimation extends L2GameServerPacket
{
    private final List<ItemInfo> _crystalyzeItems;

    public ExGetCrystalizingEstimation(L2ItemInstance item)
    {
        int crystalCount = item.getItem().getCrystalCount();
        int crystalType = item.getItem().getCrystalType();

        _crystalyzeItems = new ArrayList<ItemInfo>();

        ItemInfo itemInfo = new ItemInfo();
        itemInfo.itemId = crystalType;
        itemInfo.count = crystalCount;
        itemInfo.chance = 100.0f;
        _crystalyzeItems.add(itemInfo);
    }

    @Override
    protected boolean canWriteIT()
    {
        return false;
    }

    @Override
    protected void writeImpl()
    {
        writeD(_crystalyzeItems.size());
        for(ItemInfo item : _crystalyzeItems)
        {
            writeD(item.itemId);
            writeQ(item.count);
            writeF(item.chance);
        }
    }

    private static class ItemInfo
    {
        int itemId;
        long count;
        float chance;

        private ItemInfo()
        {
        }
    }
}