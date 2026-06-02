package l2p.gameserver.serverpackets;

import java.util.Collection;

import l2p.commons.util.GArray;
import l2p.gameserver.model.L2ManufactureItem;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2RecipeList;

public class RecipeShopManageList extends L2GameServerPacket
{
	private GArray<CreateItemInfo> infos = new GArray<CreateItemInfo>();
	private GArray<RecipeInfo> recipes = new GArray<RecipeInfo>();
	private int seller_id, seller_adena;
	private boolean _isDwarven;

	public RecipeShopManageList(L2Player seller, boolean isDvarvenCraft)
	{
		seller_id = seller.getObjectId();
		seller_adena = seller.getAdena();

		_isDwarven = isDvarvenCraft;
		Collection<L2RecipeList> _recipes;
		if(_isDwarven)
			_recipes = seller.getDwarvenRecipeBook();
		else
			_recipes = seller.getCommonRecipeBook();

		int i = 1;
		for(L2RecipeList r : _recipes)
			recipes.add(new RecipeInfo(r.getId(), i++));

		if(seller.getCreateList() != null)
			for(L2ManufactureItem item : seller.getCreateList().getList())
				infos.add(new CreateItemInfo(item.getRecipeId(), 0, item.getCost()));
	}

	@Override
	protected final void writeImpl()
	{
		writeD(seller_id);
		writeD(seller_adena);
		writeD(_isDwarven ? 0x00 : 0x01);

		writeD(recipes.size());
		for(RecipeInfo _recipe : recipes)
		{
			writeD(_recipe._id);
			writeD(_recipe.n);
		}
		recipes.clear();

		writeD(infos.size());
		for(CreateItemInfo _info : infos)
		{
			writeD(_info._id);
			writeD(_info.unk1);
			writeQ(_info.cost);
		}
		infos.clear();
	}

	@Override
	protected final void writeImplIT()
	{
		writeD(seller_id);
		writeD(seller_adena);
		writeD(_isDwarven ? 0x00 : 0x01);

		writeD(recipes.size());
		for(RecipeInfo _recipe : recipes)
		{
			writeD(_recipe._id);
			writeD(_recipe.n);
		}
		recipes.clear();

		writeD(infos.size());
		for(CreateItemInfo _info : infos)
		{
			writeD(_info._id);
			writeD(_info.unk1);
			writeD(_info.cost);
		}
		infos.clear();
	}

	static class RecipeInfo
	{
		public int _id, n;

		public RecipeInfo(int __id, int _n)
		{
			_id = __id;
			n = _n;
		}
	}

	static class CreateItemInfo
	{
		public int _id, unk1, cost;

		public CreateItemInfo(int __id, int _unk1, int _cost)
		{
			_id = __id;
			unk1 = _unk1;
			cost = _cost;
		}
	}
}