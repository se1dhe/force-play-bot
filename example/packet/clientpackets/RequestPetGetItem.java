package l2p.gameserver.clientpackets;

import l2p.gameserver.ai.CtrlIntention;
import l2p.gameserver.model.L2ObjectsStorage;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2ItemInstance;
import l2p.gameserver.model.instances.L2PetInstance;

public class RequestPetGetItem extends L2GameClientPacket
{
	// format: cd
	private int _objectId;

	@Override
	public void readImpl()
	{
		_objectId = readD();
	}

	@Override
	public void runImpl()
	{
		L2ItemInstance item = L2ObjectsStorage.getItemByObjId(_objectId);
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar.getPet() instanceof L2PetInstance)
		{
			L2PetInstance pet = (L2PetInstance) activeChar.getPet();
			if(pet == null || pet.isDead() || pet.isOutOfControl())
			{
				activeChar.sendActionFailed();
				return;
			}
			pet.getAI().setIntention(CtrlIntention.AI_INTENTION_PICK_UP, item, null);
		}
		else
		{
			activeChar.sendActionFailed();
			return;
		}
	}
}