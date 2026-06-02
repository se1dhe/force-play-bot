package l2p.gameserver.clientpackets;

import l2p.gameserver.model.base.ClassId;
import l2p.gameserver.serverpackets.NewCharacterSuccess;
import l2p.gameserver.tables.CharTemplateTable;

public class NewCharacter extends L2GameClientPacket
{
	@Override
	public void readImpl()
	{}

	@Override
	public void runImpl()
	{
		NewCharacterSuccess ct = new NewCharacterSuccess();

		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.Fighter, false));
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.Mage, false));
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.ElvenFighter, false));
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.ElvenMage, false));
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.DarkFighter, false));
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.DarkMage, false));
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.OrcFighter, false));
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.OrcMage, false));
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.DwarvenFighter, false));

		sendPacket(ct);
	}
}