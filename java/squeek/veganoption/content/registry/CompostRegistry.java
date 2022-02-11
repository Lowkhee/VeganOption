package squeek.veganoption.content.registry;

import org.apache.logging.log4j.*;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import squeek.veganoption.helpers.MiscHelper;

import java.util.ArrayList;
import java.util.List;

public class CompostRegistry
{
	private static final Logger Log = LogManager.getLogger(CompostRegistry.class.getCanonicalName());
	
	public static List<ItemStack> browns = new ArrayList<ItemStack>();
	public static List<ItemStack> greens = new ArrayList<ItemStack>();
	public static List<FoodSpecifier> uncompostableFoods = new ArrayList<FoodSpecifier>();

	public static void registerAllFoods()
	{
		long millisecondsStart = System.currentTimeMillis();
		int numRegistered = 0;

		for(net.minecraft.util.ResourceLocation location : Item.REGISTRY.getKeys())//for (Item item : Item.REGISTRY)
		{
			Item item = Item.REGISTRY.getObject(location);
			if (item == null)
				continue;

			if (isCompostableFood(new ItemStack(item)))
			{
				addGreen(item);
				numRegistered++;
			}
		}

		long timeSpentInMilliseconds = System.currentTimeMillis() - millisecondsStart;
		String timeTakenString = "took " + (timeSpentInMilliseconds / 1000.0f) + " seconds";
		Log.log(Level.INFO, "Found and registered " + numRegistered + " compostable foods (" + timeTakenString + ")");
	}

	public abstract static class FoodSpecifier
	{
		public abstract boolean matches(ItemStack itemStack);
	}

	public static boolean isCompostable(ItemStack itemStack)
	{
		return isGreen(itemStack) || isBrown(itemStack);
	}

	public static boolean isBrown(ItemStack itemStack)
	{
		return MiscHelper.isItemStackInList(browns, itemStack);
	}

	public static boolean isGreen(ItemStack itemStack)
	{
		return MiscHelper.isItemStackInList(greens, itemStack);
	}

	public static boolean isCompostableFood(ItemStack itemStack)
	{
		// TODO: optionally use AppleCore's method?
		if (itemStack != null && itemStack.getItem() instanceof ItemFood && !itemStack.getItem().hasContainerItem(itemStack))
		{
			for (FoodSpecifier uncompostableFood : uncompostableFoods)
			{
				if (uncompostableFood.matches(itemStack))
					return false;
			}
			return true;
		}
		return false;
	}

	public static void addBrown(Item item)
	{
		addBrown(new ItemStack(item, 1, OreDictionary.WILDCARD_VALUE));
	}

	public static void addBrown(Block block)
	{
		addBrown(new ItemStack(block, 1, OreDictionary.WILDCARD_VALUE));
	}

	public static void addBrown(ItemStack itemStack)
	{
		browns.add(itemStack);
	}

	public static void addBrown(String oredictName)
	{
		browns.addAll(OreDictionary.getOres(oredictName));
	}

	public static void addGreen(Item item)
	{
		addGreen(new ItemStack(item, 1, OreDictionary.WILDCARD_VALUE));
	}

	public static void addGreen(Block block)
	{
		addGreen(new ItemStack(block, 1, OreDictionary.WILDCARD_VALUE));
	}

	public static void addGreen(ItemStack itemStack)
	{
		greens.add(itemStack);
	}

	public static void addGreen(String oredictName)
	{
		greens.addAll(OreDictionary.getOres(oredictName));
	}

	public static void blacklist(FoodSpecifier foodSpecifier)
	{
		uncompostableFoods.add(foodSpecifier);
	}
}
