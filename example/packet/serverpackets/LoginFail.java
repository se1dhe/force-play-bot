package l2p.gameserver.serverpackets;

public class LoginFail extends L2GameServerPacket
{
    public static L2GameServerPacket SUCCESS = new LoginFail(0xFFFFFFFF, 0);
    public static L2GameServerPacket SYSTEM_ERROR_LOGIN_LATER = new LoginFail(0, 1);
    public static L2GameServerPacket PASSWORD_DOES_NOT_MATCH_THIS_ACCOUNT = new LoginFail(0, 2);
    public static L2GameServerPacket PASSWORD_DOES_NOT_MATCH_THIS_ACCOUNT2 = new LoginFail(0, 3);
    public static L2GameServerPacket ACCESS_FAILED_TRY_LATER = new LoginFail(0, 4);
    public static L2GameServerPacket INCORRECT_ACCOUNT_INFO_CONTACT_CUSTOMER_SUPPORT = new LoginFail(0, 5);
    public static L2GameServerPacket ACCESS_FAILED_TRY_LATER2 = new LoginFail(0, 6);
    public static L2GameServerPacket ACOUNT_ALREADY_IN_USE = new LoginFail(0, 7);
    public static L2GameServerPacket ACCESS_FAILED_TRY_LATER3 = new LoginFail(0, 8);
    public static L2GameServerPacket ACCESS_FAILED_TRY_LATER4 = new LoginFail(0, 9);
    public static L2GameServerPacket ACCESS_FAILED_TRY_LATER5 = new LoginFail(0, 10);

    private final int _reason1;
    private final int _reason2;

    public LoginFail(int reason1, int reason2)
    {
        _reason1 = reason1;
        _reason2 = reason2;
    }

    @Override
    protected final void writeImpl()
    {
        writeD(_reason1);
        writeD(_reason2);
    }

    @Override
    protected boolean canWriteIT()
    {
        return _reason1 == 0;
    }

    @Override
    protected final void writeImplIT()
    {
        writeD(_reason2);
    }
}