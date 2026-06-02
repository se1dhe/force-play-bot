package l2p.gameserver.serverpackets;

import java.util.Map;

import l2p.gameserver.model.items.Inventory;

public class ShopPreviewInfo extends L2GameServerPacket
{
    private Map<Integer, Integer> _itemlist;

    public ShopPreviewInfo(Map<Integer, Integer> itemlist)
    {
        _itemlist = itemlist;
    }

    @Override
    protected void writeImpl()
    {
        writeD(Inventory.PAPERDOLL_MAX_GK);
        for(int PAPERDOLL_ID : Inventory.PAPERDOLL_ORDER_GK)
            writeD(getFromList(PAPERDOLL_ID));
    }

    @Override
    protected void writeImplIT()
    {
        writeD(Inventory.PAPERDOLL_MAX);
        for(int PAPERDOLL_ID : PAPERDOLL_ORDER)
            writeD(getFromList(PAPERDOLL_ID));
    }

    private int getFromList(int key)
    {
      return ((_itemlist.get(key) != null) ? _itemlist.get(key) : 0);
    }

    public static final int[] PAPERDOLL_ORDER = {
            Inventory.PAPERDOLL_REAR,
            Inventory.PAPERDOLL_LEAR,
            Inventory.PAPERDOLL_NECK,
            Inventory.PAPERDOLL_RFINGER,
            Inventory.PAPERDOLL_LFINGER,
            Inventory.PAPERDOLL_HEAD,
            Inventory.PAPERDOLL_RHAND,
            Inventory.PAPERDOLL_LHAND,
            Inventory.PAPERDOLL_GLOVES,
            Inventory.PAPERDOLL_CHEST,
            Inventory.PAPERDOLL_LEGS,
            Inventory.PAPERDOLL_FEET,
            Inventory.PAPERDOLL_BACK,
            Inventory.PAPERDOLL_RHAND,
            Inventory.PAPERDOLL_HAIR,
            Inventory.PAPERDOLL_DHAIR,
            Inventory.PAPERDOLL_UNDER
    };
}
