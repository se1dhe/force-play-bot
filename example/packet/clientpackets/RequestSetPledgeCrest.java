package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.cache.CrestCache;
import l2p.gameserver.model.L2Clan;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.serverpackets.SystemMessage;

public class RequestSetPledgeCrest extends L2GameClientPacket
{
    private int _length;
    private byte[] _data;
    private static final byte[] CK = { 68, 68, 83, 32, 124, 0, 0, 0, 7, 16, 8, 0, 16, 0, 0, 0, 16, 0, 0, 0, Byte.MIN_VALUE, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 32, 0, 0, 0, 4, 0, 0, 0, 68, 88, 84, 49, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

    @Override
    protected void readImpl()
    {
        _length = readD();
        if(_length == CrestCache.CREST_SIZE && _length == _buf.remaining())
        {
            _data = new byte[_length];
            readB(_data);
        }
    }

    @Override
    public void runImpl()
    {
        L2Player activeChar = getClient().getActiveChar();
        if(activeChar == null)
            return;
        if(_data != null)
        {
            if(_data.length != 256)
                return;
            for(int i = 0; i < CK.length; i++)
                if(_data[i] != CK[i])
                {
                    activeChar.sendMessage("Incorrect format for clan crest!");
                    return;
                }
        }
        L2Clan clan = activeChar.getClan();
        if(clan == null)
            return;
        if(clan.getDissolvingExpiryTime() > System.currentTimeMillis())
        {
			activeChar.sendPacket(new SystemMessage(SystemMessage.DURING_THE_GRACE_PERIOD_FOR_DISSOLVING_A_CLAN_REGISTRATION_OR_DELETION_OF_A_CLANS_CREST_IS_NOT_ALLOWED));
            return;
        }
        if((activeChar.getClanPrivileges() & L2Clan.CP_CL_REGISTER_CREST) == L2Clan.CP_CL_REGISTER_CREST)
        {
            if(clan.getLevel() < Config.CLAN_CREST_LVL)
            {
                if(Config.CLAN_CREST_LVL == 3)
                    activeChar.sendPacket(new SystemMessage(SystemMessage.CLAN_CREST_REGISTRATION_IS_ONLY_POSSIBLE_WHEN_CLANS_SKILL_LEVELS_ARE_ABOVE_3));
                else
                    activeChar.sendMessage("A clan crest can only be registered when the clan's skill level is " + Config.CLAN_CREST_LVL + " or above.");
                return;
            }
            if(clan.NEXT_CREST > System.currentTimeMillis())
            {
                int sec = (int) ((clan.NEXT_CREST - System.currentTimeMillis()) / 1000L);
                activeChar.sendMessage(activeChar.isLangRus() ? ("До повторной установки " + sec + " сек.") : ("Set time remaining " + sec + " sec."));
                return;
            }
            clan.NEXT_CREST = System.currentTimeMillis() + 30000L;
            if(_length == 0 || _data == null)
            {
                if(clan.hasCrest())
                    CrestCache.getInstance().removePledgeCrest(clan.getClanId());
                clan.setCrestId(0);
                activeChar.sendPacket(new SystemMessage(SystemMessage.THE_CLANS_CREST_HAS_BEEN_DELETED));
                clan.broadcastClanStatus(false, true, false);
                return;
            }
            int crestId = CrestCache.getInstance().savePledgeCrest(clan.getClanId(), _data);
            clan.setCrestId(crestId);
            clan.broadcastClanStatus(false, true, false);
        }
    }
}