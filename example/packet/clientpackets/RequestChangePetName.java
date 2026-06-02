package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Summon;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.model.instances.L2PetInstance;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.tables.PetDataTable;
import l2p.gameserver.utils.Util;

public class RequestChangePetName extends L2GameClientPacket
{
	// format: cS

	private String _name;

	@Override
	public void readImpl()
	{
		_name = readS();
	}

	@Override
	public void runImpl()
	{
		L2Player cha = getClient().getActiveChar();
		L2Summon pet = cha.getPet();
		if(pet != null && (pet.getName() == null || pet.getName().isEmpty() || pet.getName().equalsIgnoreCase(pet.getTemplate().name)))
		{
			if(PetDataTable.petNameExist(_name))
			{
				cha.sendPacket(new SystemMessage(SystemMessage.ALREADY_IN_USE_BY_ANOTHER_PET));
				return;
			}
			if(_name.length() > 8)
			{
				cha.sendPacket(new SystemMessage(SystemMessage.YOUR_PETS_NAME_CAN_BE_UP_TO_8_CHARACTERS));
				return;
			}
			if(!Util.isMatchingRegexp(_name, Config.CNAME_TEMPLATE))
			{
				cha.sendPacket(new SystemMessage(SystemMessage.AN_INVALID_CHARACTER_IS_INCLUDED_IN_THE_PETS_NAME));
				return;
			}
			pet.setName(_name);
			pet.broadcastCharInfo();

			if(pet.isPet())
			{
				L2PetInstance _pet = (L2PetInstance) pet;
				L2ItemInstance controlItem = _pet.getControlItem();
				if(controlItem != null)
				{
					controlItem.setCustomType2(1);
					controlItem.setPriceToSell(0); // Костыль, иначе CustomType2 = 1 не пишется в базу
					controlItem.updateDatabase();
					_pet.updateControlItem();
				}
			}
		}
		else
			cha.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_SET_THE_NAME_OF_THE_PET));
	}
}