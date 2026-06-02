package l2p.gameserver.clientpackets;

import l2p.gameserver.model.L2Character;
import l2p.gameserver.model.L2Effect;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2Skill;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.tables.SkillTree;

public class RequestDispel extends L2GameClientPacket
{
	private int _objectId, _id, _level;
	private int skillId;
	private byte[] _data;

	@Override
	public void readImpl()
	{
		if(getClient().isITClient())
		{
			_data = new byte[1];
			readB(_data);
			skillId = readD();
		}
		else
		{
			_objectId = readD();
			_id = readD();
			_level = readD();
		}
	}

	@Override
	public void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;
		if(player.isITClient())
			dispel(player, skillId);
		else
			dispelGk(player, _objectId, _id, _level);
	}

	public void dispel(L2Player player, int id)
	{
		if(player.isInOlympiadMode())
		{
			player.sendActionFailed();
			return;
		}
		L2Skill skill = SkillTable.getInstance().getInfo(id, 1);
		if(skill == null)
		{
			player.sendActionFailed();
			return;
		}
		if(skill.isSelfDispellable())
		{
			player.getEffectList().stopEffect(id);
			player.sendPacket(new SystemMessage(SystemMessage.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(id, 1));
		}
		else
			player.sendActionFailed();
	}

	public void dispelGk(L2Player player, int objectId, int skillId, int skillLevel)
	{
		if(player.getObjectId() != objectId && player.getPet() == null)
			return;

		if(player.isInOlympiadMode())
		{
			player.sendActionFailed();
			return;
		}

		if(skillLevel >= 100)
			skillLevel = SkillTree.getBaseLevels().get(skillId);

		L2Character target = player;
		if(player.getObjectId() != objectId)
			target = player.getPet();

		for(L2Effect e : target.getEffectList().getAllEffects())
		{
			if(e.getDisplayId() == skillId && e.getDisplayLevel() == skillLevel)
				if(e.getSkill().isSelfDispellable())
					e.exit();
				else
					return;
		}
		player.sendPacket(new SystemMessage(SystemMessage.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(skillId, skillLevel));
	}
}