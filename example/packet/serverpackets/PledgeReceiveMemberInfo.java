package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Clan;
import l2p.gameserver.model.L2ClanMember;

public class PledgeReceiveMemberInfo extends L2GameServerPacket
{
	private L2ClanMember _member;
	private String _name;

	public PledgeReceiveMemberInfo(L2ClanMember member)
	{
		_member = member;
		L2Clan clan = _member != null ? _member.getClan() : null;
		_name = clan != null ? (_member.getPledgeType() != 0 ? clan.getSubPledge(this._member.getPledgeType()).getName() : clan.getName()) : null;
	}

	@Override
	protected final void writeImpl()
	{
		if(_member == null || _name == null)
			return;

		writeD(_member.getPledgeType());
		writeS(_member.getName());
		writeS(_member.getTitle());
		writeD(_member.getPowerGrade());

		if(_member.getPledgeType() != 0)
			writeS(_member.getClan().getSubPledge(_member.getPledgeType()).getName());
		else
			writeS(_member.getClan().getName());

		writeS(_member.getRelatedName()); // apprentice/sponsor name if any
	}
}