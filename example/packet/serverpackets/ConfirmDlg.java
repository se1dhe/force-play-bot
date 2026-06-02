package l2p.gameserver.serverpackets;

import java.util.Vector;

public class ConfirmDlg extends L2GameServerPacket
{
	private int _messageId;
	private int _Time;
	private int _requestId;

	private static final int TYPE_ZONE_NAME = 7;
	private static final int TYPE_SKILL_NAME = 4;
	private static final int TYPE_ITEM_NAME = 3;
	private static final int TYPE_NPC_NAME = 2;
	private static final int TYPE_NUMBER = 1;
	private static final int TYPE_TEXT = 0;
	private Vector<Integer> _types = new Vector<Integer>();
	private Vector<Object> _values = new Vector<Object>();

	public ConfirmDlg(int messageId, int time)
	{
		_messageId = messageId;
		_Time = time;
	}

	public ConfirmDlg addString(String text)
	{
		_types.add(TYPE_TEXT);
		_values.add(text);
		return this;
	}

	public ConfirmDlg addNumber(Integer number)
	{
		_types.add(TYPE_NUMBER);
		_values.add(number);
		return this;
	}

	public ConfirmDlg addNumber(Short number)
	{
		_types.add(TYPE_NUMBER);
		_values.add(new Integer(number));
		return this;
	}

	public ConfirmDlg addNumber(Byte number)
	{
		_types.add(TYPE_NUMBER);
		_values.add(new Integer(number));
		return this;
	}

	public ConfirmDlg addNpcName(int id)
	{
		_types.add(TYPE_NPC_NAME);
		_values.add(new Integer(1000000 + id));
		return this;
	}

	public ConfirmDlg addItemName(Short id)
	{
		_types.add(TYPE_ITEM_NAME);
		_values.add(new Integer(id));
		return this;
	}

	public ConfirmDlg addItemName(Integer id)
	{
		_types.add(TYPE_ITEM_NAME);
		_values.add(id);
		return this;
	}

	public ConfirmDlg addZoneName(int x, int y, int z)
	{
		_types.add(new Integer(TYPE_ZONE_NAME));
		int[] coord = { x, y, z };
		_values.add(coord);
		return this;
	}

	public ConfirmDlg addSkillName(Short id, Short level)
	{
		_types.add(TYPE_SKILL_NAME);
		int[] skill = { id, level };
		_values.add(skill);
		return this;
	}

	public void setRequestId(int requestId)
	{
		_requestId = requestId;
	}

	// TODO [V] - перепроверить, так?
	@Override
	protected final void writeImpl()
	{
		writeD(_messageId);

		writeD(_types.size());

		for(int i = 0; i < _types.size(); i++)
		{
			int t = _types.get(i);

			writeD(t);

			switch(t)
			{
				case TYPE_TEXT:
				{
					if(_values.size() >= i)
						writeS((String) _values.get(i));
					break;
				}
				case TYPE_NUMBER:
				case TYPE_NPC_NAME:
				case TYPE_ITEM_NAME:
				{
					if(_values.size() < i)
						break;
					int t1 = (Integer) _values.get(i);
					writeD(t1);

					break;
				}
				case TYPE_SKILL_NAME:
				{
					if(_values.size() < i)
						break;

					int[] skill = (int[]) _values.get(i);

					writeD(skill[0]); // id
					writeD(skill[1]); // level

					break;
				}
				case TYPE_ZONE_NAME:
				{
					int[] coord = (int[]) _values.get(i);
					writeD(coord[0]);
					writeD(coord[1]);
					writeD(coord[2]);
					break;
				}
			}
		}
		writeD(_Time);
		writeD(_requestId);
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_messageId);

		writeD(_types.size());

		for(int i = 0; i < _types.size(); i++)
		{
			int t = _types.get(i);

			writeD(t);

			switch(t)
			{
				case TYPE_TEXT:
				{
					if(_values.size() >= i)
						writeS((String) _values.get(i));
					break;
				}
				case TYPE_NUMBER:
				case TYPE_NPC_NAME:
				case TYPE_ITEM_NAME:
				{
					if(_values.size() < i)
						break;
					int t1 = (Integer) _values.get(i);
					writeD(t1);

					break;
				}
				case TYPE_SKILL_NAME:
				{
					if(_values.size() < i)
						break;

					int[] skill = (int[]) _values.get(i);

					writeD(skill[0]); // id
					writeD(skill[1]); // level

					break;
				}
				case TYPE_ZONE_NAME:
				{
					int[] coord = (int[]) _values.get(i);
					writeD(coord[0]);
					writeD(coord[1]);
					writeD(coord[2]);
					break;
				}
			}
		}
		writeD(_Time);
		writeD(_requestId);
	}
}