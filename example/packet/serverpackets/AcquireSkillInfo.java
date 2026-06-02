package l2p.gameserver.serverpackets;

import java.util.ArrayList;
import java.util.List;

public class AcquireSkillInfo extends L2GameServerPacket
{
	private List<Req> _reqs;
	private int _id;
	private int _level;
	private int _spCost;
	private int _mode;

	private static class Req
	{
		public int itemId;
		public int count;
		public int type;
		public int unk;

		public Req(int pType, int pItemId, int pCount, int pUnk)
		{
			itemId = pItemId;
			type = pType;
			count = pCount;
			unk = pUnk;
		}
	}

	public AcquireSkillInfo(int id, int level, int spCost, int mode)
	{
		_reqs = new ArrayList<Req>();
		_id = id;
		_level = level;
		_spCost = spCost;
		_mode = mode;
	}

	public void addRequirement(int type, int id, int count, int unk)
	{
		_reqs.add(new AcquireSkillInfo.Req(type, id, count, unk));
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_id);
		writeD(_level);
		writeQ(_spCost);
		writeD(_mode);
		writeD(_reqs.size());
		for(Req temp : _reqs)
		{
			writeD(temp.type);
			writeD(temp.itemId);
			writeQ(temp.count);
			writeD(temp.unk);
		}
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_id);
		writeD(_level);
		writeD(_spCost);
		writeD(_mode);
		writeD(_reqs.size());
		for(Req temp : _reqs)
		{
			writeD(temp.type);
			writeD(temp.itemId);
			writeD(temp.count);
			writeD(temp.unk);
		}
	}
}