package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.serverpackets.updatetype.InventorySlot;

public class ExUserInfoEquipSlot extends AbstractMaskPacket<InventorySlot>
{
	private final L2Player _player;
	private final byte[] _masks = new byte[]
	{
		(byte) 0x00,
		(byte) 0x00,
		(byte) 0x00,
		(byte) 0x00,
		(byte) 0x00,
		(byte) 0x00, // 152
		(byte) 0x00, // 152
		(byte) 0x00, // 152
	};

	@Override
	protected byte[] getMasks()
	{
		return _masks;
	}

	@Override
	protected void onNewMaskAdded(InventorySlot component)
	{
	}

	public ExUserInfoEquipSlot(L2Player player)
	{
		_player = player;
		addComponentType(InventorySlot.VALUES);
	}

	public ExUserInfoEquipSlot(L2Player player, int slot)
	{
		_player = player;
		addComponentType(InventorySlot.valueOf(slot));
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_player.getObjectId());
		writeH(InventorySlot.VALUES.length);
		writeB(_masks);

		PcInventory inventory = _player.getInventory();
		for(InventorySlot slot : InventorySlot.VALUES)
		{
			if(containsMask(slot))
			{
				writeH(22); // size
				writeD(inventory.getPaperdollUserInfoObjectId(slot.getSlot(), _player.isVisibleFakeCostume()));
				writeD(inventory.getPaperdollUserInfoItemId(slot.getSlot(), _player.isVisibleFakeCostume()));
				writeQ(inventory.getPaperdollAugmentationId(slot.getSlot()));
				writeD(inventory.getPaperdollVisualItemId(slot.getSlot()));
			}
		}
	}

	@Override
	protected boolean canWriteIT()
	{
		return false;
	}
}