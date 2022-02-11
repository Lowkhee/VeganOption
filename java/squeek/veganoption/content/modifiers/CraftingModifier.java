package squeek.veganoption.content.modifiers;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.UniversalBucket;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import squeek.veganoption.helpers.RandomHelper;
import org.apache.logging.log4j.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class CraftingModifier
{
	private static final Logger Log = LogManager.getLogger(CraftingModifier.class.getCanonicalName());
	public HashMap<ItemStack, ItemStack[]> inputsToRemoveForOutput = new HashMap<ItemStack, ItemStack[]>();
	public HashMap<ItemStack, ItemStack[]> inputsToKeepForOutput = new HashMap<ItemStack, ItemStack[]>();

	public CraftingModifier()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}

	public void addInputsToRemoveForOutput(ItemStack output, ItemStack... inputs)
	{
		
			inputsToRemoveForOutput.put(output, inputs);
	}

	public void addInputsToRemoveForOutput(ItemStack output, String... inputOreDicts)
	{
		for (String inputOreDict : inputOreDicts)
		{
			List<ItemStack> oreStacks = OreDictionary.getOres(inputOreDict);
			if (oreStacks.size() > 0)
				addInputsToRemoveForOutput(output.copy(), oreStacks.toArray(new ItemStack[oreStacks.size()]));
		}
	}

	public void addInputsToKeepForOutput(ItemStack output, ItemStack... inputs)
	{
		inputsToKeepForOutput.put(output, inputs);
	}

	public void addInputsToKeepForOutput(ItemStack output, String... inputOreDicts)
	{
		for (String inputOreDict : inputOreDicts)
		{
			List<ItemStack> oreStacks = OreDictionary.getOres(inputOreDict);
			if (oreStacks.size() > 0)
				addInputsToKeepForOutput(output.copy(), oreStacks.toArray(new ItemStack[oreStacks.size()]));
		}
	}

	//@SideOnly(Side.CLIENT)
	//@SubscribeEvent
	public void onItemCrafted(PlayerEvent.ItemCraftedEvent event)
	{
		List<ItemStack> inputsToRemove = getInputsToRemoveForOutput(event.crafting);
		List<ItemStack> inputsToKeep = getInputsToKeepForOutput(event.crafting);

		if (inputsToRemove.isEmpty() && inputsToKeep.isEmpty())
			return;
		boolean useBucket = false;
		boolean useBottle = false;
		Item craftedItem = event.crafting.getItem();
		if(craftedItem.getClass() == UniversalBucket.class  || craftedItem.getClass() == Items.WATER_BUCKET.getClass())
			useBucket = true;
		else if(craftedItem.getContainerItem() != null && craftedItem.getContainerItem().getClass() == Items.GLASS_BOTTLE.getClass())
			useBottle = true;
		for (int i = 0; i < event.craftMatrix.getSizeInventory(); i++)
		{
			ItemStack stackInSlot = event.craftMatrix.getStackInSlot(i);
			if (!stackInSlot.isEmpty())
			{
				Log.log(squeek.veganoption.ModInfo.debugLevel,"Crafting slot item: " + stackInSlot.getDisplayName());
				if(!inputsToRemove.isEmpty())
					for (ItemStack inputToRemove : inputsToRemove)
					{
						if (stackInSlot.getItem().hasContainerItem() || OreDictionary.itemMatches(inputToRemove, stackInSlot, false))
						{
							Log.log(squeek.veganoption.ModInfo.debugLevel,"Input to Remove: " + inputToRemove.getDisplayName());
							if (stackInSlot.getCount() > 0)
								if(stackInSlot.getItem().getClass() == UniversalBucket.class  || stackInSlot.getItem().getClass() == Items.WATER_BUCKET.getClass())
									if(useBucket)
									{
										useBucket = false;
										event.craftMatrix.setInventorySlotContents(i, ItemStack.EMPTY);
									}
									else
										event.craftMatrix.setInventorySlotContents(i, new ItemStack(Items.WATER_BUCKET)); //ItemStack.EMPTY -> unable to find recipe without an Item in slot
								else if(stackInSlot.getItem().getContainerItem() != null && stackInSlot.getItem().getContainerItem().getClass() == Items.GLASS_BOTTLE.getClass())
									{
										if(!useBottle)
											event.player.inventory.addItemStackToInventory(new ItemStack(Items.GLASS_BOTTLE));
										useBottle = false;
										stackInSlot.shrink(1);//inputToRemove.getCount());
										event.craftMatrix.setInventorySlotContents(i, stackInSlot.copy());
									}
									//else
										//event.craftMatrix.setInventorySlotContents(i, new ItemStack(Items.GLASS_BOTTLE, stackInSlot.getCount()));
								else
								{
									//int remaining = stackInSlot.getCount() - inputToRemove.getCount();
									//if(remaining <= 0)
									stackInSlot.shrink(1); //inputToRemove.getCount());
									//event.player.inventory.addItemStackToInventory(stackInSlot.copy());
									event.craftMatrix.setInventorySlotContents(i, stackInSlot.copy());//ItemStack.EMPTY);
								}
							break;
						}
					}
				if(!inputsToKeep.isEmpty())
					for (ItemStack inputToKeep : inputsToKeep)
					{
						Log.log(squeek.veganoption.ModInfo.debugLevel,"Input to Keep: " + inputToKeep.getDisplayName());
						if (OreDictionary.itemMatches(inputToKeep, stackInSlot, false))
						{
							stackInSlot.grow(inputToKeep.getCount());
							if (stackInSlot.isItemStackDamageable() && stackInSlot.attemptDamageItem(inputToKeep.getCount(), RandomHelper.random))
							stackInSlot.shrink(1);
							break;
						}
					}
			}
		}
	}

	public List<ItemStack> getInputsToRemoveForOutput(ItemStack output)
	{
		List<ItemStack> inputsToRemove = new ArrayList<ItemStack>();
		for (Entry<ItemStack, ItemStack[]> entry : inputsToRemoveForOutput.entrySet())
		{
			if (OreDictionary.itemMatches(entry.getKey(), output, false))
			{
				inputsToRemove.addAll(Arrays.asList(entry.getValue()));
			}
		}
		return inputsToRemove;
	}

	public List<ItemStack> getInputsToKeepForOutput(ItemStack output)
	{
		List<ItemStack> inputsToKeep = new ArrayList<ItemStack>();
		for (Entry<ItemStack, ItemStack[]> entry : inputsToKeepForOutput.entrySet())
		{
			if (OreDictionary.itemMatches(entry.getKey(), output, false))
			{
				inputsToKeep.addAll(Arrays.asList(entry.getValue()));
			}
		}
		return inputsToKeep;
	}
}
