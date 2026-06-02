package l2p.gameserver.serverpackets;

import l2p.gameserver.RecipeController;
import l2p.gameserver.model.L2Player;
import l2p.gameserver.model.L2RecipeList;

public class RecipeItemMakeInfo extends L2GameServerPacket
{
	private int _id;
	private int _status;
	private int _CurMP;
	private int _MaxMP;

	public RecipeItemMakeInfo(int id, L2Player pl, int status)
	{
		_id = id;
		_status = status;
		_CurMP = (int) pl.getCurrentMp();
		_MaxMP = pl.getMaxMp();
	}

	@Override
	protected final void writeImpl()
	{
		L2RecipeList recipeList = RecipeController.getInstance().getRecipeList(_id);
		if(recipeList == null)
			return;

		writeD(_id); //Точно: ID рецепта
		writeD(recipeList.isDwarvenRecipe() ? 0 : 1);
		writeD(_CurMP); //Точно: текущее состояние полоски Creator MP
		writeD(_MaxMP); //Точно: максимальное состояние полоски Creator MP
		writeD(_status); //Точно: итог крафта; 0xFFFFFFFF нет статуса, 0 удача, 1 провал
	}

	@Override
	protected final void writeImplIT()
	{
		L2RecipeList recipeList = RecipeController.getInstance().getRecipeList(_id);
		if(recipeList == null)
			return;

		writeD(_id); //Точно: ID рецепта
		writeD(recipeList.isDwarvenRecipe() ? 0 : 1);
		writeD(_CurMP); //Точно: текущее состояние полоски Creator MP
		writeD(_MaxMP); //Точно: максимальное состояние полоски Creator MP
		writeD(_status); //Точно: итог крафта; 0xFFFFFFFF нет статуса, 0 удача, 1 провал
	}
}