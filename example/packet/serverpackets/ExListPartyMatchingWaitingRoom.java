package l2p.gameserver.serverpackets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import l2p.gameserver.instancemanager.PartyRoomManager;
import l2p.gameserver.model.L2Player;
import org.apache.commons.lang3.ArrayUtils;

public class ExListPartyMatchingWaitingRoom extends L2GameServerPacket
{
	private List<PartyMatchingWaitingInfo> _waitingList = Collections.emptyList();
	private final int _fullSize;

	public ExListPartyMatchingWaitingRoom(L2Player searcher, int minLevel, int maxLevel, int page, boolean showAll)
	{
		int first = (page - 1) * 64;
		int firstNot = page * 64;
		int i = 0;

		List<L2Player> temp = PartyRoomManager.getInstance().getWaitingList(minLevel, maxLevel, showAll);
		_fullSize = temp.size();

		_waitingList = new ArrayList<PartyMatchingWaitingInfo>(_fullSize);
		for(L2Player pc : temp)
		{
			if(i < first || i >= firstNot)
				continue;
			_waitingList.add(new PartyMatchingWaitingInfo(pc));
			i++;
		}
	}

	@Override
	protected void writeImpl()
	{
		writeD(_fullSize);
		writeD(_waitingList.size());
		for(PartyMatchingWaitingInfo waiting_info : _waitingList)
		{
			writeS(waiting_info.name);
			writeD(waiting_info.classId);
			writeD(waiting_info.level);

			writeD(waiting_info.currentInstance);
			writeD(waiting_info.instanceReuses.length);
			for(int i : waiting_info.instanceReuses)
				writeD(i);
		}
	}

	@Override
	protected void writeImplIT()
	{
		writeD(_fullSize);
		writeD(_waitingList.size());
		for(PartyMatchingWaitingInfo waiting_info : _waitingList)
		{
			writeS(waiting_info.name);
			writeD(waiting_info.classId);
			writeD(waiting_info.level);
		}
	}

	static class PartyMatchingWaitingInfo
	{
		public final int classId, level, currentInstance;
		public final String name;
		public final int[] instanceReuses;

		public PartyMatchingWaitingInfo(L2Player member)
		{
			name = member.getName();
			classId = member.getClassId().getId();
			level = member.getLevel();
			currentInstance = member.getInstanceId();
			instanceReuses = new int[0]/*ArrayUtils.toArray(member.getInstanceReuses().keySet())*/;
		}
	}
}