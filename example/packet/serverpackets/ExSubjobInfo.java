package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2SubClass;

import java.util.Collection;

// TODO [V] - надо?
public class ExSubjobInfo extends L2GameServerPacket
{
    private Collection<L2SubClass> _subClasses;
    private int _raceId, _classId;
    private boolean _openStatus;

    public ExSubjobInfo(L2Player player, boolean openStatus)
    {
        _openStatus = openStatus;
        _raceId = player.getRace().ordinal();
        _classId = player.getClassId().ordinal();
        _subClasses = player.getSubClasses().values();
    }

    @Override
    protected void writeImpl()
    {
        writeC(_openStatus);
        writeD(_classId);
        writeD(_raceId);

        writeD(_subClasses.size());
        for(L2SubClass subClass : _subClasses)
        {
            writeD(subClass.getIndex());
            writeD(subClass.getClassId());
            writeD(subClass.getLevel());
            writeC(subClass.isBase() ? 0 : 0);
        }
    }
}