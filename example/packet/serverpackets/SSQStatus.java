package l2p.gameserver.serverpackets;

import l2p.gameserver.Config;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.entity.SevenSigns;
import l2p.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2p.gameserver.templates.StatsSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Seven Signs Record Update
 *
 * packet type id 0xf5
 * format:
 *
 * c cc	(Page Num = 1 -> 4, period)
 *
 * 1: [ddd cc dd ddd c ddd c]
 * 2: [hc [cd (dc (S))]
 * 3: [ccc (cccc)]
 * 4: [(cchh)]
 */
public class SSQStatus extends L2GameServerPacket
{
	private L2Player _player;
	private int _page, period;

	public SSQStatus(L2Player player, int recordPage)
	{
		_player = player;
		_page = recordPage;
		period = SevenSigns.getInstance().getCurrentPeriod();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(_page);
		writeC(period); // current period?

		switch(_page)
		{
			case 1:
				// [ddd cc dd ddd c ddd c]
				writeD(SevenSigns.getInstance().getCurrentCycle());

				switch(period)
				{
					case SevenSigns.PERIOD_COMP_RECRUITING:
						writeD(1183);
						break;
					case SevenSigns.PERIOD_COMPETITION:
						writeD(1176);
						break;
					case SevenSigns.PERIOD_COMP_RESULTS:
						writeD(1184);
						break;
					case SevenSigns.PERIOD_SEAL_VALIDATION:
						writeD(1177);
						break;
				}

				switch(period)
				{
					case SevenSigns.PERIOD_COMP_RECRUITING:
					case SevenSigns.PERIOD_COMP_RESULTS:
						writeD(1287);
						break;
					case SevenSigns.PERIOD_COMPETITION:
					case SevenSigns.PERIOD_SEAL_VALIDATION:
						writeD(1286);
						break;
				}

				writeC(SevenSigns.getInstance().getPlayerCabal(_player));
				writeC(SevenSigns.getInstance().getPlayerSeal(_player));

				if(_player.isITClient())
				{
					writeD(SevenSigns.getInstance().getPlayerStoneContrib(_player)); // Seal Stones Turned-In
					writeD(SevenSigns.getInstance().getPlayerAdenaCollect(_player)); // Ancient Adena to Collect
				}
				else
				{
					writeQ(SevenSigns.getInstance().getPlayerStoneContrib(_player)); // Seal Stones Turned-In
					writeQ(SevenSigns.getInstance().getPlayerAdenaCollect(_player)); // Ancient Adena to Collect
				}

				long dawnStoneScore = SevenSigns.getInstance().getCurrentStoneScore(SevenSigns.CABAL_DAWN);
				long dawnFestivalScore = SevenSigns.getInstance().getCurrentFestivalScore(SevenSigns.CABAL_DAWN);
				long dawnTotalScore = dawnStoneScore + dawnFestivalScore;

				long duskStoneScore = SevenSigns.getInstance().getCurrentStoneScore(SevenSigns.CABAL_DUSK);
				long duskFestivalScore = SevenSigns.getInstance().getCurrentFestivalScore(SevenSigns.CABAL_DUSK);
				long duskTotalScore = duskStoneScore + duskFestivalScore;

				long totalStoneScore = duskStoneScore + dawnStoneScore;
				totalStoneScore = totalStoneScore == 0 ? 1 : totalStoneScore; // Prevents divide by zero errors when competition begins.

				/*
				 * Scoring seems to be proportionate to a set base value, so base this on
				 * the maximum obtainable score from festivals, which is 500.
				 */
				long duskStoneScoreProp = duskStoneScore * 500 / totalStoneScore;
				long dawnStoneScoreProp = dawnStoneScore * 500 / totalStoneScore;

				long totalOverallScore = duskTotalScore + dawnTotalScore;
				totalOverallScore = totalOverallScore == 0 ? 1 : totalOverallScore; // Prevents divide by zero errors when competition begins.

				long dawnPercent = dawnTotalScore * 100 / totalOverallScore;
				long duskPercent = duskTotalScore * 100 / totalOverallScore;

				/* DUSK */
				if(_player.isITClient())
				{
					writeD((int) duskStoneScoreProp); // Seal Stone Score
					writeD((int) duskFestivalScore); // Festival Score
					writeD((int) duskStoneScoreProp + (int) duskFestivalScore); // Total Score
				}
				else
				{
					writeQ((int) duskStoneScoreProp); // Seal Stone Score
					writeQ((int) duskFestivalScore); // Festival Score
					writeQ((int) duskStoneScoreProp + (int) duskFestivalScore); // Total Score
				}

				writeC((int) duskPercent); // Dusk %

				/* DAWN */
				if(_player.isITClient())
				{
					writeD((int) dawnStoneScoreProp); // Seal Stone Score
					writeD((int) dawnFestivalScore); // Festival Score
					writeD((int) dawnStoneScoreProp + (int) dawnFestivalScore); // Total Score
				}
				else
				{
					writeQ((int) dawnStoneScoreProp); // Seal Stone Score
					writeQ((int) dawnFestivalScore); // Festival Score
					writeQ((int) dawnStoneScoreProp + (int) dawnFestivalScore); // Total Score
				}

				writeC((int) dawnPercent); // Dawn %
				break;
			case 2:
				// c cc hc [cd (dc (S))]
				if(SevenSigns.getInstance().isSealValidationPeriod())
					writeH(0);
				else
					writeH(1);

				writeC(5); // Total number of festivals

				for(int i = 0; i < 5; i++)
				{
					writeC(i + 1); // Current client-side festival ID
					writeD(SevenSignsFestival.FESTIVAL_LEVEL_SCORES[i]);

					int duskScore = SevenSignsFestival.getInstance().getHighestScore(SevenSigns.CABAL_DUSK, i);
					int dawnScore = SevenSignsFestival.getInstance().getHighestScore(SevenSigns.CABAL_DAWN, i);

					// Dusk Score \\
					writeD(duskScore);

					StatsSet highScoreData = SevenSignsFestival.getInstance().getHighestScoreData(SevenSigns.CABAL_DUSK, i);
					String[] partyMembers = highScoreData.getString("members").split(",");

					if(partyMembers != null)
					{
						writeC(partyMembers.length);

						for(String partyMember : partyMembers)
							writeS(partyMember);
					}
					else
						writeC(0);

					// Dawn Score \\
					writeD(dawnScore);

					highScoreData = SevenSignsFestival.getInstance().getHighestScoreData(SevenSigns.CABAL_DAWN, i);
					partyMembers = highScoreData.getString("members").split(",");

					if(partyMembers != null)
					{
						writeC(partyMembers.length);

						for(String partyMember : partyMembers)
							writeS(partyMember);
					}
					else
						writeC(0);
				}
				break;
			case 3:
				// c cc [ccc (cccc)]
				writeC(10); // Minimum limit for winning cabal to retain their seal
				writeC(35); // Minimum limit for winning cabal to claim a seal
				writeC(3); // Total number of seals

				int totalDawnProportion = 1;
				int totalDuskProportion = 1;

				for(int i = 1; i <= 3; i++)
				{
					totalDawnProportion += SevenSigns.getInstance().getSealProportion(i, SevenSigns.CABAL_DAWN);
					totalDuskProportion += SevenSigns.getInstance().getSealProportion(i, SevenSigns.CABAL_DUSK);
				}

				// Prevents divide by zero errors.
				totalDawnProportion = Math.max(1, totalDawnProportion);
				totalDuskProportion = Math.max(1, totalDuskProportion);

				for(int i = 1; i <= 3; i++)
				{
					int dawnProportion = SevenSigns.getInstance().getSealProportion(i, SevenSigns.CABAL_DAWN);
					int duskProportion = SevenSigns.getInstance().getSealProportion(i, SevenSigns.CABAL_DUSK);

					writeC(i);
					writeC(SevenSigns.getInstance().getSealOwner(i));
					writeC(duskProportion * 100 / totalDuskProportion);
					writeC(dawnProportion * 100 / totalDawnProportion);
				}
				break;
			case 4:
				// c cc [(cchh)]

				writeC(SevenSigns.getInstance().getCabalHighestScore()); // Overall predicted winner
				writeC(3); // Total number of seals

				for(int i = 1; i < 4; i++)
				{
					int dawnProportion = SevenSigns.getInstance().getSealProportion(i, SevenSigns.CABAL_DAWN);
					int duskProportion = SevenSigns.getInstance().getSealProportion(i, SevenSigns.CABAL_DUSK);
					int totalProportion = dawnProportion + duskProportion;

					int sealOwner = SevenSigns.getInstance().getSealOwner(i);

					writeC(i);
					writeC(sealOwner);

					/*
					 * 1289 = 10% or more voted for owned seal
					 * 1290 = 35% or more voted for non-owned seal
					 * 1291 = 10% or less voted for owned seal
					 * 1292 = 35% or less voted for non-owned seal
					 */
					if(sealOwner != SevenSigns.CABAL_NULL)
					{
						if(totalProportion >= 10)
							writeH(1289);
						else
							writeH(1291);
					}
					else if(totalProportion >= 35)
						writeH(1290);
					else
						writeH(1292);

					// Shows a short description of the seal status when not Seal Validation.
					if(SevenSigns.getInstance().isSealValidationPeriod())
						writeH(1);
					else
						writeH(0);
				}

				break;
		}
	}
}