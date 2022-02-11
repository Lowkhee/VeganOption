package squeek.veganoption.content.recipes;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import squeek.veganoption.content.crafting.PistonCraftingHandler;
import squeek.veganoption.helpers.FluidHelper;
import squeek.veganoption.helpers.WorldHelper;
import org.apache.logging.log4j.*;

import java.util.*;
import java.util.Map.Entry;

public class PistonCraftingRecipe
{
	private static final Logger Log = LogManager.getLogger(PistonCraftingRecipe.class.getCanonicalName());
	public List<InputItemStack> itemInputs = new ArrayList<InputItemStack>();
	public FluidStack fluidInput = null;
	public List<ItemStack> itemOutputs = new ArrayList<ItemStack>();
	public FluidStack fluidOutput = null;
	protected Random rand = new Random();

	public PistonCraftingRecipe(Object output, Object... inputs)
	{
		this(new Object[]{output}, inputs);
	}

	public PistonCraftingRecipe(Object[] outputs, Object[] inputs)
	{
		for (Object input : inputs)
		{
			if (input instanceof InputItemStack)
				itemInputs.add((InputItemStack) input);
			else if (input instanceof String || input instanceof Item || input instanceof Block || input instanceof ItemStack)
				itemInputs.add(new InputItemStack(input));
			else if (input instanceof Fluid)
				fluidInput = new FluidStack((Fluid) input, Fluid.BUCKET_VOLUME);
			else if (input instanceof FluidStack)
				fluidInput = (FluidStack) input;
			else
				throw new RuntimeException("Unsupported PistonCraftingRecipe input: " + input);
		}
		for (Object output : outputs)
		{
			if (output instanceof Item)
				itemOutputs.add(new ItemStack((Item) output));
			else if (output instanceof Block)
				itemOutputs.add(new ItemStack((Block) output));
			else if (output instanceof ItemStack)
				itemOutputs.add((ItemStack) output);
			else if (output instanceof Fluid)
				fluidOutput = new FluidStack((Fluid) output, Fluid.BUCKET_VOLUME);
			else if (output instanceof FluidStack)
				fluidOutput = (FluidStack) output;
			else
				throw new RuntimeException("Unsupported PistonCraftingRecipe output: " + output);
		}
	}

	// TODO: This probably doesn't work right for multiple overlapping OreDict inputs.
	// It will likely think that the recipe works but it might use the same EntityItem
	// for multiple  different OreDict inputs, and bug out when it starts consuming stuff
	public boolean tryCraft(World world, BlockPos pos)
	{
		IFluidHandler fluidHandler = getOutputFluidHandler(world, pos);

		if (!canOutputFluid(fluidHandler))
			return false;

		PistonCraftingHandler.WorldPosition displacedPos = new PistonCraftingHandler.WorldPosition(world, pos);
		FluidStack displacedFluid = PistonCraftingHandler.displacedLiquids.get(displacedPos);

		if (!fluidInputMatches(displacedFluid))
			return false;

		List<EntityItem> entityItemsWithin = WorldHelper.getItemEntitiesWithin(displacedPos.world, displacedPos.pos);
		Map<InputItemStack, List<EntityItem>> entityItemsByInput = getEntityItemsByInput(itemInputs, entityItemsWithin);

		if (!itemInputMatches(entityItemsByInput))
			return false;

		//is it 1:1 ratio, input:output
		boolean isReplacementPossible = itemInputs.size() == itemOutputs.size() && fluidOutput == null;
		Log.log(squeek.veganoption.ModInfo.debugLevel,"Is replacment recipe (non fluids):" + (isReplacementPossible ? "true" : "false"));
		if (isReplacementPossible)
		{
			int i = 0;
			for (Entry<InputItemStack, List<EntityItem>> entry : entityItemsByInput.entrySet())
			{
				ItemStack output = itemOutputs.get(i);
				for (EntityItem inputEntity : entry.getValue())
				{
					ItemStack inputStack = inputEntity.getEntityItem();
					ItemStack newItemStack = output.copy();
					//set the stack size of the input
					newItemStack.setCount((int) (inputStack.getCount() * ((float) output.getCount() / entry.getKey().stackSize())));
					inputEntity.setEntityItemStack(newItemStack);
					Log.log(squeek.veganoption.ModInfo.debugLevel,"Output: " + output.getDisplayName());
					Log.log(squeek.veganoption.ModInfo.debugLevel,"Input: " + inputStack.getDisplayName());
					Log.log(squeek.veganoption.ModInfo.debugLevel,"New Item:" + newItemStack.getDisplayName());
				}
				i++;
			}
		}
		else
		{
			Map<ItemStack, EntityItem> entityItemsByOutput = new HashMap<ItemStack, EntityItem>();
			for (ItemStack itemOutput : itemOutputs)
			{
				Log.log(squeek.veganoption.ModInfo.debugLevel,"Outputs: " + itemOutput.getDisplayName());
				List<EntityItem> randomReferenceEntityList = entityItemsByInput.get(itemInputs.get(rand.nextInt(itemInputs.size())));
				EntityItem randomReferenceEntity = randomReferenceEntityList.get(rand.nextInt(randomReferenceEntityList.size()));
				EntityItem outputEntity = new EntityItem(world, randomReferenceEntity.posX, randomReferenceEntity.posY, randomReferenceEntity.posZ, itemOutput.copy());
				outputEntity.getEntityItem().setCount(0);
				entityItemsByOutput.put(itemOutput, outputEntity);
			}

			do
			{
				if (fluidInput != null && displacedFluid != null)
				{
					displacedFluid.amount -= fluidInput.amount;
					if (displacedFluid.amount <= 0)
					{
						PistonCraftingHandler.displacedLiquids.remove(displacedPos);
						displacedFluid = null;
					}
				}
				if (fluidOutput != null && fluidHandler != null)
				{
					fluidHandler.fill(fluidOutput, true);
				}
				for (Entry<InputItemStack, List<EntityItem>> inputEntry : entityItemsByInput.entrySet())
				{
					int numRequired = inputEntry.getKey().stackSize();
					int numConsumed = 0;
					for (EntityItem inputEntity : inputEntry.getValue())
					{
						ItemStack inputStack = inputEntity.getEntityItem();
						int numToConsume = Math.min(inputStack.getCount(), numRequired - numConsumed);
						inputStack.shrink(numToConsume);
						numConsumed += numToConsume;

						if (numConsumed >= numRequired)
							break;
					}
				}
				for (Entry<ItemStack, EntityItem> entry : entityItemsByOutput.entrySet())
				{
					entry.getValue().getEntityItem().grow(entry.getKey().getCount());
				}
			}
			while (fluidInputMatches(displacedFluid) && itemInputMatches(entityItemsByInput) && canOutputFluid(fluidHandler));

			for (Entry<ItemStack, EntityItem> entry : entityItemsByOutput.entrySet())
			{
				world.spawnEntity(entry.getValue());
			}
		}

		return true;
	}

	public boolean canOutputFluid(World world, BlockPos pos)
	{
		if (fluidOutput == null)
			return true;

		return canOutputFluid(getOutputFluidHandler(world, pos));
	}

	public boolean canOutputFluid(IFluidHandler fluidHandler)
	{
		if (fluidOutput == null)
			return true;

		if (fluidHandler == null)
			return false;

		return fluidHandler.fill(fluidOutput, false) == fluidOutput.amount;
	}

	public IFluidHandler getOutputFluidHandler(World world, BlockPos pos)
	{
		if (fluidOutput == null)
			return null;

		return FluidHelper.getFluidHandlerAt(world, pos.down(), EnumFacing.UP);
	}

	public boolean itemInputMatches(World world, BlockPos pos)
	{
		if (itemInputs.isEmpty())
			return true;

		return itemInputMatches(WorldHelper.getItemEntitiesWithin(world, pos));
	}

	public boolean itemInputMatches(List<EntityItem> entityItems)
	{
		if (itemInputs.isEmpty())
			return true;

		return itemInputMatches(getEntityItemsByInput(itemInputs, entityItems));
	}

	public boolean itemInputMatches(Map<InputItemStack, List<EntityItem>> entityItemsByInput)
	{
		if (itemInputs.isEmpty())
			return true;

		for (Entry<InputItemStack, List<EntityItem>> entityItemsByInputEntry : entityItemsByInput.entrySet())
		{
			if (getStackSizeOfEntityItems(entityItemsByInputEntry.getValue()) < entityItemsByInputEntry.getKey().stackSize())
				return false;
		}
		return true;
	}

	public static Map<InputItemStack, List<EntityItem>> getEntityItemsByInput(Collection<InputItemStack> targets, Collection<EntityItem> entityItems)
	{
		Map<InputItemStack, List<EntityItem>> entityItemsByItemStack = new HashMap<InputItemStack, List<EntityItem>>();
		for (InputItemStack target : targets)
		{
			entityItemsByItemStack.put(target, getMatchingEntityItems(target, entityItems));
		}
		return entityItemsByItemStack;
	}

	public static List<EntityItem> getMatchingEntityItems(InputItemStack target, Collection<EntityItem> entityItems)
	{
		List<EntityItem> matchingEntities = new ArrayList<EntityItem>();
		for (EntityItem entityItem : entityItems)
		{
			if (target.matches(entityItem.getEntityItem()))
			{
				matchingEntities.add(entityItem);
			}
		}
		if (!matchingEntities.isEmpty() && target.isOreDict() && target.stackSize() > 1)
		{
			List<EntityItem> entitiesOfOneTypeWithLargestStackSize = null;
			int largestStackSize = 0;
			for (EntityItem entityItem : matchingEntities)
			{
				if (entitiesOfOneTypeWithLargestStackSize != null && entitiesOfOneTypeWithLargestStackSize.get(0).getEntityItem().isItemEqual(entityItem.getEntityItem()))
					continue;

				List<EntityItem> exactMatches = getMatchingEntityItems(new InputItemStack(entityItem.getEntityItem()), matchingEntities);
				int exactMatchesStackSize = getStackSizeOfEntityItems(exactMatches);

				if (exactMatchesStackSize >= target.stackSize())
					return exactMatches;

				if (exactMatchesStackSize > largestStackSize)
				{
					entitiesOfOneTypeWithLargestStackSize = exactMatches;
					largestStackSize = exactMatchesStackSize;
				}
			}
			matchingEntities = entitiesOfOneTypeWithLargestStackSize;
		}
		return matchingEntities;
	}

	public static int getStackSizeOfEntityItems(Collection<EntityItem> entityItems)
	{
		int stackSize = 0;
		for (EntityItem entityItem : entityItems)
		{
			stackSize = entityItem.getEntityItem().getCount();
		}
		return stackSize;
	}

	public boolean fluidInputMatches(World world, BlockPos pos)
	{
		return fluidInputMatches(PistonCraftingHandler.displacedLiquids.get(new PistonCraftingHandler.WorldPosition(world, pos)));
	}

	public boolean fluidInputMatches(FluidStack fluidStack)
	{
		if (fluidStack == null)
			return fluidInput == null;
		else
			return fluidStack.isFluidEqual(fluidInput) && fluidStack.amount >= fluidInput.amount;
	}
}
