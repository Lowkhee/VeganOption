package squeek.veganoption.content.registry;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import squeek.veganoption.helpers.LangHelper;
import squeek.veganoption.helpers.MiscHelper;

import java.util.*;

import org.apache.logging.log4j.*;

public class DescriptionRegistry
{
	private static final Logger Log = LogManager.getLogger(DescriptionRegistry.class.getCanonicalName());
	
	public static List<ItemStack> itemStacksWithUsageDescriptions = new ArrayList<ItemStack>();
	public static List<ItemStack> itemStacksWithCraftingDescriptions = new ArrayList<ItemStack>();
	public static Map<ItemStack, String> itemStacksWithCustomUsageDescriptions = new HashMap<ItemStack, String>();
	public static Map<ItemStack, String> itemStacksWithCustomCraftingDescriptions = new HashMap<ItemStack, String>();

	public static void registerAllDescriptions()
	{
		long millisecondsStart = System.currentTimeMillis();
		int numRegistered = 0;

		for (net.minecraft.util.ResourceLocation location : Item.REGISTRY.getKeys())
		{
			Item item = Item.REGISTRY.getObject(location);
			if (item == null || item.getRegistryName() == null)
				continue;

			numRegistered += tryRegisterItemAndSubtypes(item);
		}

		for(net.minecraft.util.ResourceLocation location : Item.REGISTRY.getKeys())//for (Block block : Block.REGISTRY)
		{
			Block block = Block.REGISTRY.getObject(location);
			if (block == Blocks.AIR || block.getRegistryName() == null)
				continue;

			numRegistered += tryRegisterItemAndSubtypes(Item.getItemFromBlock(block));
		}

		long timeSpentInMilliseconds = System.currentTimeMillis() - millisecondsStart;
		String timeTakenString = "took " + (timeSpentInMilliseconds / 1000.0f) + " seconds";
		Log.log(Level.INFO, "Found and registered " + numRegistered + " items/blocks with description text (" + timeTakenString + ")");
	}

	public static int tryRegisterItemAndSubtypes(Item item)
	{
		int numRegistered = 0;

		NonNullList<ItemStack> stacks;
		if (item.getHasSubtypes())
		{
			stacks = NonNullList.create();
			item.getSubItems(item, CreativeTabs.SEARCH, stacks);
		}
		else
		{
			stacks = NonNullList.create();
			stacks.addAll(Collections.singletonList(new ItemStack(item)));
		}

		for (ItemStack stack : stacks)
		{
			if (tryRegisterDescriptions(stack))
				numRegistered++;
		}

		return numRegistered;
	}

	public static boolean tryRegisterDescriptions(ItemStack itemStack)
	{
		boolean didRegister = false;
		if (hasUsageText(itemStack) && !MiscHelper.isItemStackInList(itemStacksWithUsageDescriptions, itemStack))
		{
			itemStacksWithUsageDescriptions.add(itemStack);
			didRegister = true;
		}
		if (hasCraftingText(itemStack) && !MiscHelper.isItemStackInList(itemStacksWithCraftingDescriptions, itemStack))
		{
			itemStacksWithCraftingDescriptions.add(itemStack);
			didRegister = true;
		}
		return didRegister;
	}

	public static boolean hasUsageText(ItemStack itemStack)
	{
		return LangHelper.existsRaw(itemStack.getUnlocalizedName() + ".vowiki.usage") || !RelationshipRegistry.getChildren(itemStack).isEmpty();
	}

	public static boolean hasCraftingText(ItemStack itemStack)
	{
		return LangHelper.existsRaw(itemStack.getUnlocalizedName() + ".vowiki.crafting") || !RelationshipRegistry.getParents(itemStack).isEmpty();
	}

	public static void registerCustomUsageText(ItemStack itemStack, String unlocalizedUsageText)
	{
		itemStacksWithCustomUsageDescriptions.put(itemStack, unlocalizedUsageText);
		itemStacksWithUsageDescriptions.add(itemStack);
	}

	public static void registerCustomCraftingText(ItemStack itemStack, String unlocalizedCraftingText)
	{
		itemStacksWithCustomCraftingDescriptions.put(itemStack, unlocalizedCraftingText);
		itemStacksWithCraftingDescriptions.add(itemStack);
	}
}
