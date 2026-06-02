package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Player;

public class EtcStatusUpdate extends L2GameServerPacket
{
	private static final int NO_CHAT_FLAG = 1 << 0;
	private static final int DANGER_AREA_FLAG = 1 << 1;
	private static final int CHARM_OF_COURAGE_FLAG = 1 << 2;

	private int IncreasedForce, WeightPenalty, MessageRefusal, DangerArea;
	private int armorExpertisePenalty, weaponExpertisePenalty, _expertisePenalty, CharmOfCourage, DeathPenaltyLevel, ConsumedSouls;
	private int _flags;
	private boolean can_writeImpl = false;

	public EtcStatusUpdate(L2Player player)
	{
		if(player == null)
			return;
		IncreasedForce = player.getIncreasedForce();
		WeightPenalty = player.getWeightPenalty();
		MessageRefusal = player.getMessageRefusal() || player.getNoChannel() != 0 || player.isBlockAll() ? 1 : 0;
		DangerArea = player.isInDangerArea() ? 1 : 0;
		armorExpertisePenalty = player.getArmorsExpertisePenalty();
		weaponExpertisePenalty = player.getWeaponsExpertisePenalty();
		_expertisePenalty = player.getExpertisePenalty();
		CharmOfCourage = player.isCharmOfCourage() ? 1 : 0;
		DeathPenaltyLevel = player.getDeathPenalty() == null ? 0 : player.getDeathPenalty().getLevel();

		if(MessageRefusal > 0)
			_flags |= NO_CHAT_FLAG; //skill id 4269, 1 lvl
		if(DangerArea > 0)
			_flags |= DANGER_AREA_FLAG; // skill id 4268, 1 lvl
		if(CharmOfCourage > 0)
			_flags |= CHARM_OF_COURAGE_FLAG; //Charm of Courage, "Prevents experience value decreasing if killed during a siege war".

		can_writeImpl = true;
	}

	@Override
	protected boolean canWrite()
	{
		return can_writeImpl;
	}

	@Override
	protected final void writeImpl()
	{
		// dddddddd
		writeC(IncreasedForce); // skill id 4271, 7 lvl
		writeD(WeightPenalty); // skill id 4270, 4 lvl
		writeC(weaponExpertisePenalty); // weapon grade penalty, skill 6209 in epilogue
		writeC(armorExpertisePenalty); // armor grade penalty, skill 6213 in epilogue
		writeC(DeathPenaltyLevel); //Death Penalty max lvl 15, "Combat ability is decreased due to death."
		writeC(ConsumedSouls);
		writeC(_flags);
	}

	@Override
	protected final void writeImplIT()
	{
		// dddddddd
		writeD(IncreasedForce); // skill id 4271, 7 lvl
		writeD(WeightPenalty); // skill id 4270, 4 lvl
		writeD(MessageRefusal); //skill id 4269, 1 lvl
		writeD(DangerArea); // skill id 4268, 1 lvl
		writeD(_expertisePenalty); // weapon grade penalty, skill 6209 in epilogue, skill id 4267, 1 lvl at off c4 server scripts
		writeD(CharmOfCourage); //Charm of Courage, "Prevents experience value decreasing if killed during a siege war".
		writeD(DeathPenaltyLevel); //Death Penalty max lvl 15, "Combat ability is decreased due to death."
	}
}