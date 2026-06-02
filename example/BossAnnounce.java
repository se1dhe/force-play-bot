package services;

import gnu.trove.TIntIntHashMap;
import l2p.commons.threading.RunnableImpl;
import l2p.gameserver.Announcements;
import l2p.gameserver.Config;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.database.mysql;
import l2p.gameserver.instancemanager.SpawnManager;
import l2p.gameserver.listener.actor.CharListenerList;
import l2p.gameserver.listener.actor.OnDeathListener;
import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2ObjectsStorage;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Spawn;
import l2p.gameserver.model.instances.L2NpcInstance;
import l2p.gameserver.scripts.Functions;
import l2p.gameserver.scripts.ScriptFile;
import l2p.gameserver.serverpackets.Say2;
import l2p.gameserver.tables.NpcTable;
import l2p.gameserver.tables.SpawnTable;
import l2p.gameserver.templates.L2NpcTemplate;
import l2p.gameserver.utils.Util;
import org.apache.commons.lang3.ArrayUtils;

/**
 * @author: PaiN
 */
public class BossAnnounce extends Functions implements ScriptFile
{
	// список боссов по ID для анонса
	private static final int[] BOSSES_IDS = {25325};
	// за сколько минут до появления анонсировать
	private static final int MINS = 15;

	// список боссов с индивидуальными менеджерами
	private static final TIntIntHashMap BOSSES = new TIntIntHashMap();
	{
		BOSSES.put(29066, 29019);
		BOSSES.put(29067, 29019);
		BOSSES.put(29068, 29019);
		BOSSES.put(29020, 29020);
		BOSSES.put(29028, 29028);
		BOSSES.put(29047, 29045);
		BOSSES.put(29065, 29065);
	}
	private static final long TIME = MINS * 60000L;
	private static TIntIntHashMap STARTED = new TIntIntHashMap();

	private static void load()
	{
		if(SpawnManager.completed)
			for(int id : BOSSES_IDS)
				start(id);
		else
			ThreadPoolManager.getInstance().schedule(new RunnableImpl()
			{
				@Override
				public void runImpl() throws Exception
				{
					load();
				}
			}, 20000);
	}

	private static class AnnounceTask extends RunnableImpl
	{
		private final int _id;

		public AnnounceTask(int id)
		{
			_id = id;
		}

		@Override
		public void runImpl()
		{
			L2NpcTemplate template = NpcTable.getTemplate(_id);
			if(template == null)
				return;
			announceToAll("Через "+MINS+" "+Util.minuteFormat(true, String.valueOf(MINS))+" Босс "+template.name+" появится в мире", "In "+MINS+" "+Util.minuteFormat(true, String.valueOf(MINS))+" Boss "+template.name+" will appear in the world");
			STARTED.remove(_id);
		}
	}

	private static final OnDeathListener _listener = new ListenerImpl();

	private static final class ListenerImpl implements OnDeathListener
	{
		@Override
		public void onDeath(L2Character actor, L2Character killer)
		{
			if(actor == null)
				return;
			if(actor.isBoss() && ArrayUtils.contains(BOSSES_IDS, actor.getNpcId()))
			{
				final int id = actor.getNpcId();
				ThreadPoolManager.getInstance().schedule(new RunnableImpl()
				{
					@Override
					public void runImpl() throws Exception
					{
						start(id);
					}
				}, 60000);
			}
		}
	}

	private static void start(int id)
	{
		if(BOSSES.containsKey(id))
		{
			int i = BOSSES.get(id);
			int state = mysql.simple_get_int("`state`", "epic_boss_spawn", "bossId=" + i);
			long respawnTime = state > 1 ? (mysql.simple_get_int("respawnDate", "epic_boss_spawn", "bossId=" + i) * 1000L) : 0L;
			if(respawnTime >= (System.currentTimeMillis() + TIME) && !STARTED.containsKey(i))
			{
				STARTED.put(i, i);
				ThreadPoolManager.getInstance().schedule(new AnnounceTask(i), respawnTime - System.currentTimeMillis() - TIME);
			}
		}
		else
		{
			for(L2Spawn sp : SpawnTable.getInstance().getSpawnTable())
				if(sp.getNpcId() == id)
				{
					L2NpcInstance npc = L2ObjectsStorage.getByNpcId(id);
					if(npc != null && npc.isVisible())
						break;
					long respawnTime = sp.getRespawnTime() * 1000L;
					if(respawnTime >= (System.currentTimeMillis() + TIME) && !STARTED.containsKey(id))
					{
						STARTED.put(id, id);
						ThreadPoolManager.getInstance().schedule(new AnnounceTask(id), respawnTime - System.currentTimeMillis() - TIME);
					}
					break;
				}
		}
	}

	public static void announceToAll(String textRu, String textEn)
	{
		Say2 say2Ru = new Say2(0, Announcements.TYPE, "", textRu);
		Say2 say2En = new Say2(0, Announcements.TYPE, "", textEn);
		for(L2Player player : L2ObjectsStorage.getAllPlayersForIterate())
		{
			boolean result = player.getVarB("@announce_kill_spawn_rb", Config.ANNOUNCE_KILL_SPAWN_RB_DEFAULT);
			if(result)
			{
				if(player.isLangRus())
					player.sendPacket(say2Ru);
				else
					player.sendPacket(say2En);
			}
		}
	}

	@Override
	public void onLoad()
	{
		load();
		CharListenerList.addGlobal(_listener);
		_log.info("Loaded Service: Boss Announce");
	}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}
}