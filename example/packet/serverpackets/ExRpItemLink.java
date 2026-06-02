package l2p.gameserver.serverpackets;

import l2p.gameserver.model.items.ItemInfo;

/**
 * ddQhdhhhhhdhhhhhhhh - Gracia Final
 */
public class ExRpItemLink extends AbstractItemListPacket
{
    private ItemInfo _item;

    public ExRpItemLink(ItemInfo item)
    {
        _item = item;
    }

    @Override
    protected final void writeImpl()
    {
        writeItemInfo(_item);
    }

    @Override
    protected boolean canWriteIT()
    {
        return false;
    }
}