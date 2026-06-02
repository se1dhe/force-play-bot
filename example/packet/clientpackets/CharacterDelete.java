package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.instancemanager.PlayerManager;
import l2p.gameserver.model.CharSelectInfo;
import l2p.gameserver.model.L2Clan;
import l2p.gameserver.model.L2ObjectsStorage;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.network.L2GameClient;
import l2p.gameserver.serverpackets.CharacterDeleteFail;
import l2p.gameserver.serverpackets.CharacterDeleteSuccess;
import l2p.gameserver.serverpackets.CharacterSelectionInfo;
import l2p.gameserver.tables.ClanTable;

public class CharacterDelete extends L2GameClientPacket
{
	private int _charSlot;

	@Override
	protected void readImpl()
	{
		_charSlot = readD();
	}

	@Override
	protected void runImpl()
	{
		L2GameClient client = getClient();
		if(client.getActiveChar() != null)
			return;

		CharSelectInfo[] cs = client.getCharacters();
		if(_charSlot < 0 || _charSlot >= cs.length)
			return;

		CharSelectInfo csi = cs[_charSlot];
		if(csi == null)
			return;

		int charId = csi.getObjectId();
		if(charId <= 0)
			return;

		L2Player player = L2ObjectsStorage.getPlayer(charId);
		if(player != null)
		{
			sendPacket(new CharacterDeleteFail(CharacterDeleteFail.REASON_DELETION_FAILED));
			sendPacket(client.getPacketCharSelection());
			return;
		}

		if(csi.getClanId() > 0)
		{
			L2Clan clan = ClanTable.getInstance().getClan(csi.getClanId());
			if(clan != null)
			{
				if(clan.getLeaderId() == csi.getObjectId())
					sendPacket(new CharacterDeleteFail(CharacterDeleteFail.REASON_CLAN_LEADERS_MAY_NOT_BE_DELETED));
				else
					sendPacket(new CharacterDeleteFail(CharacterDeleteFail.REASON_YOU_MAY_NOT_DELETE_CLAN_MEMBER));
				sendPacket(client.getPacketCharSelection());
				return;
			}
		}

		if(Config.SERVICES_LOCK_CHAR_HWID && Config.GUARD_ENABLED && client.charLockHWID(charId))
		{
			client.close(Msg.LogOutOk);
			return;
		}

		if(Config.DELETE_DAYS == 0)
			PlayerManager.deleteCharByObjId(charId, true);
		else
			client.markDeleteCharByObjId(charId, true);

		sendPacket(new CharacterDeleteSuccess());

		CharacterSelectionInfo cl = new CharacterSelectionInfo(client.getLoginName(), client.getSessionId().playOkID1);
		sendPacket(cl);
		client.setCharSelection(cl.getCharInfo());
	}
}