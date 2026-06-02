package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.instancemanager.PetitionManager;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.serverpackets.Say2;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.tables.GmListTable;

public final class RequestPetitionCancel extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(PetitionManager.getInstance().isPlayerInConsultation(activeChar))
		{
			if(activeChar.isGM())
				PetitionManager.getInstance().endActivePetition(activeChar);
			else
				activeChar.sendPacket(new SystemMessage(SystemMessage.PETITION_UNDER_PROCESS));
		}
		else if(PetitionManager.getInstance().isPlayerPetitionPending(activeChar))
		{
			if(PetitionManager.getInstance().cancelActivePetition(activeChar))
			{
				int numRemaining = Config.MAX_PETITIONS_PER_PLAYER - PetitionManager.getInstance().getPlayerTotalPetitionCount(activeChar);

				activeChar.sendPacket(new SystemMessage(SystemMessage.THE_PETITION_WAS_CANCELED_YOU_MAY_SUBMIT_S1_MORE_PETITIONS_TODAY).addString(String.valueOf(numRemaining)));

				// Notify all GMs that the player's pending petition has been cancelled.
				String msgContent = activeChar.getName() + " has canceled a pending petition.";
				GmListTable.broadcastToGMs(new Say2(activeChar.getObjectId(), Say2C.HERO_VOICE, "Petition System", msgContent));
			}
			else
				activeChar.sendPacket(new SystemMessage(SystemMessage.FAILED_TO_CANCEL_PETITION_PLEASE_TRY_AGAIN_LATER));
		}
		else
			activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_NOT_SUBMITTED_A_PETITION));
	}
}