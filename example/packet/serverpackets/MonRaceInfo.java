package l2p.gameserver.serverpackets;

import l2p.gameserver.model.instances.L2NpcInstance;

public class MonRaceInfo extends L2GameServerPacket
{
	private int _unknown1;
	private int _unknown2;
	private L2NpcInstance[] _monsters;
	private int[][] _speeds;

	public MonRaceInfo(int unknown1, int unknown2, L2NpcInstance[] monsters, int[][] speeds)
	{
		_unknown1 = unknown1;
		_unknown2 = unknown2;
		_monsters = monsters;
		_speeds = speeds;
	}

	// TODO [V] - узнать структуру
	@Override
	protected final void writeImpl()
	{
		writeD(_unknown1);
		writeD(_unknown2);
		writeD(8);

		for(int i = 0; i < 8; i++)
		{
			writeD(_monsters[i].getObjectId()); //npcObjectID
			writeD(_monsters[i].getTemplate().npcId + 1000000); //npcID
			writeD(14107); //origin X
			writeD(181875 + 58 * (7 - i)); //origin Y
			writeD(-3566); //origin Z
			writeD(12080); //end X
			writeD(181875 + 58 * (7 - i)); //end Y
			writeD(-3566); //end Z
			writeF(_monsters[i].getColHeight()); //coll. height
			writeF(_monsters[i].getColRadius()); //coll. radius
			writeD(120); // ?? unknown
			//*
			for(int j = 0; j < 20; j++)
				if(_unknown1 == 0)
					writeC(_speeds[i][j]);
				else
					writeC(0);
			//writeD(0);
			//writeD(0x00); // ? GraciaFinal
		}
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_unknown1);
		writeD(_unknown2);
		writeD(8);

		for(int i = 0; i < 8; i++)
		{
			writeD(_monsters[i].getObjectId()); //npcObjectID
			writeD(_monsters[i].getTemplate().npcId + 1000000); //npcID
			writeD(14107); //origin X
			writeD(181875 + 58 * (7 - i)); //origin Y
			writeD(-3566); //origin Z
			writeD(12080); //end X
			writeD(181875 + 58 * (7 - i)); //end Y
			writeD(-3566); //end Z
			writeF(_monsters[i].getColHeight()); //coll. height
			writeF(_monsters[i].getColRadius()); //coll. radius
			writeD(120); // ?? unknown
			//*
			for(int j = 0; j < 20; j++)
				if(_unknown1 == 0)
					writeC(_speeds[i][j]);
				else
					writeC(0);
			writeD(0);
		}
	}
}