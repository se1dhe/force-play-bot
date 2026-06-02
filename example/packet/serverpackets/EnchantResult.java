package l2p.gameserver.serverpackets;

public class EnchantResult extends L2GameServerPacket
{
	public static final EnchantResult SUCCESS = new EnchantResult(0); // вещь заточилась
	public static final EnchantResult FAILED = new EnchantResult(1); // вещь разбилась, требует указания получившихся кристаллов, в статичном виде не используется
	public static final EnchantResult CANCELLED = new EnchantResult(2); // заточка невозможна
	public static final EnchantResult BLESSED_FAILED = new EnchantResult(3); // заточка не удалась, уровень заточки сброшен на 0
	public static final EnchantResult FAILED_NO_CRYSTALS = new EnchantResult(4); // вещь разбилась, но кристаллов не получилось (видимо для эвента, сейчас использовать невозможно, там заглушка)

	private final int _resultId, _crystalId;
	private final long _count;
	private final int _enchantLevel;

	private EnchantResult(int resultId)
	{
		_resultId = resultId;
		_crystalId = 0;
		_count = 0;
		_enchantLevel = 0;
	}

	public EnchantResult(int resultId, int crystalId, long count, int enchantLevel)
	{
		_resultId = resultId;
		_crystalId = crystalId;
		_count = count;
		_enchantLevel = enchantLevel;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_resultId);
		writeD(_crystalId); // item id кристаллов
		writeQ(_count); // количество кристаллов
		writeD(0x00); // TODO: secondItemId
		writeD(0x00); // TODO: secondItemCount
		writeD(0x00);
		writeD(_enchantLevel); // уровень заточки
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(_resultId);
	}
}