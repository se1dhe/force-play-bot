package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Party;
import l2p.gameserver.model.L2Player;

/**
 * ch Sddd
 */
public class ExMPCCPartyInfoUpdate extends L2GameServerPacket
{
	private L2Party _party;
	private L2Player _leader;
	private int _mode, _count;

	/**
	 * @param party
	 * @param mode 0 = Remove, 1 = Add
	 */
	public ExMPCCPartyInfoUpdate(L2Party party, int mode)
	{
		_party = party;
		_mode = mode;
		_count = _party.getMemberCount();
		_leader = _party.getPartyLeader();
	}

	@Override
	protected void writeImpl()
	{
		writeS(_leader.getName());
		writeD(_leader.getObjectId());
		writeD(_count);
		writeD(_mode); // mode 0 = Remove Party, 1 = AddParty, maybe more...
	}
}