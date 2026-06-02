package l2p.gameserver.serverpackets;

import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.instances.L2DoorInstance;
import l2p.gameserver.model.instances.L2StaticObjectInstance;

public class StaticObject extends L2GameServerPacket
{
	private int _staticObjectId;
	private int _objectId;
	private int _type;
	private int _isTargetable;
	private int _meshIndex;
	private int _isClosed;
	private int _isEnemy;
	private int _maxHp;
	private int _currentHp;
	private int _showHp;
	private int _damageGrade;

	public StaticObject(L2StaticObjectInstance obj)
	{
		_staticObjectId = obj.getStaticObjectId();
		_objectId = obj.getObjectId();
		_type = 0;
		_isTargetable = 1;
		_meshIndex = 0/*obj.getMeshIndex()*/; // TODO [V] - такое?
		_isClosed = 0;
		_isEnemy = 0;
		_maxHp = 0;
		_currentHp = 0;
		_showHp = 0;
		_damageGrade = 0;
	}

	public StaticObject(L2DoorInstance door, L2Player player)
	{
		_staticObjectId = door.getDoorId();
		_objectId = door.getObjectId();
		_type = 1;
		_isTargetable = door.isTargetable() ? 1 : 0; // TODO [V] - такое?
		_meshIndex = 1;
		_isClosed = door.isOpen() ? 0 : 1; //opened 0 /closed 1
		_isEnemy = door.isAutoAttackable(player) ? 1 : 0;
		_currentHp = (int) door.getCurrentHp();
		_maxHp = door.getMaxHp();
		_showHp = door.isHPVisible() ? 1 : 0; //TODO [G1ta0] статус двери для осаждающих
		_damageGrade = door.getDamage();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_staticObjectId);
		writeD(_objectId);
		writeD(_type);
		writeD(_isTargetable);
		writeD(_meshIndex);
		writeD(_isClosed);
		writeD(_isEnemy);
		writeD(_currentHp);
		writeD(_maxHp);
		writeD(_showHp);
		writeD(_damageGrade);
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_staticObjectId);
		writeD(_objectId);
	}
}