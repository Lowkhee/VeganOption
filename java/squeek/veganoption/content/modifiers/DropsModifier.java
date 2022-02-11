package squeek.veganoption.content.modifiers;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;
import squeek.veganoption.helpers.BlockHelper;
import squeek.veganoption.helpers.RandomHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class DropsModifier
{
	private static final HashMap<BlockSpecifier, DropSpecifier> blockDrops = new HashMap<BlockSpecifier, DropSpecifier>();

	public DropsModifier()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}

	public void addDropsToBlock(Block block, Item drop)
	{
		addDropsToBlock(block, new ItemStack(drop));
	}

	public void addDropsToBlock(Block block, ItemStack drop)
	{
		addDropsToBlock(new BlockSpecifier(block.getDefaultState()), new DropSpecifier(drop));
	}

	public void addDropsToBlock(BlockSpecifier blockSpecifier, DropSpecifier dropSpecifier)
	{
		blockDrops.put(blockSpecifier, dropSpecifier);
	}

	public boolean dropExists(ItemStack itemStack)
	{
		for (Entry<BlockSpecifier, DropSpecifier> blockDropSpecifier : blockDrops.entrySet())
		{
			if (OreDictionary.itemMatches(blockDropSpecifier.getValue().itemStack, itemStack, false))
			{
				return true;
			}
		}
		return false;
	}

	public boolean hasDrops(ItemStack itemStack)
	{
		for (Entry<BlockSpecifier, DropSpecifier> blockDropSpecifier : blockDrops.entrySet())
		{
			BlockSpecifier block = blockDropSpecifier.getKey();
			if (OreDictionary.itemMatches(block.itemStackForComparison, itemStack, false))
			{
				return true;
			}
		}
		return false;
	}

	public List<DropInfo> getSubsetByDroppedItem(ItemStack itemStack)
	{
		List<DropInfo> subset = new ArrayList<DropInfo>();
		for (Entry<BlockSpecifier, DropSpecifier> blockDropSpecifier : blockDrops.entrySet())
		{
			if (OreDictionary.itemMatches(blockDropSpecifier.getValue().itemStack, itemStack, false))
			{
				subset.add(new DropInfo(blockDropSpecifier.getKey(), blockDropSpecifier.getValue()));
			}
		}
		return subset;
	}

	public List<DropInfo> getSubsetByBlock(ItemStack itemStack)
	{
		List<DropInfo> subset = new ArrayList<DropInfo>();
		for (Entry<BlockSpecifier, DropSpecifier> blockDropSpecifier : blockDrops.entrySet())
		{
			BlockSpecifier block = blockDropSpecifier.getKey();
			if (OreDictionary.itemMatches(block.itemStackForComparison, itemStack, false))
			{
				subset.add(new DropInfo(blockDropSpecifier.getKey(), blockDropSpecifier.getValue()));
			}
		}
		return subset;
	}

	public List<DropInfo> getAllDrops()
	{
		List<DropInfo> subset = new ArrayList<DropInfo>();
		for (Entry<BlockSpecifier, DropSpecifier> blockDropSpecifier : blockDrops.entrySet())
		{
			subset.add(new DropInfo(blockDropSpecifier.getKey(), blockDropSpecifier.getValue()));
		}
		return subset;
	}

	public static class BlockSpecifier
	{
		public final IBlockState stateToMatch;
		public final IProperty<?>[] propertiesToMatch;
		public final ItemStack itemStackForDisplay;
		public final ItemStack itemStackForComparison;

		public BlockSpecifier(IBlockState stateToMatch, IProperty<?>... propertiesToMatch)
		{
			this(stateToMatch, null, propertiesToMatch);
		}

		public BlockSpecifier(IBlockState stateToMatch, ItemStack itemStackForDisplay, IProperty<?>... propertiesToMatch)
		{
			this.stateToMatch = stateToMatch;
			this.propertiesToMatch = propertiesToMatch;
			this.itemStackForDisplay = itemStackForDisplay;
			this.itemStackForComparison = BlockHelper.blockStateToItemStack(stateToMatch);
		}

		public boolean matches(IBlockAccess world, BlockPos pos, IBlockState state)
		{
			return blockMatches(state.getBlock()) && stateMatches(state);
		}

		public boolean blockMatches(Block block)
		{
			return this.stateToMatch.getBlock() == block;
		}

		public boolean stateMatches(IBlockState state)
		{
			for (IProperty<?> property : propertiesToMatch)
			{
				if (!stateToMatch.getValue(property).equals(state.getValue(property)))
					return false;
			}
			return true;
		}
	}

	public static class DropSpecifier
	{
		public final ItemStack itemStack;
		public final float dropChance;
		public final int dropsMin;
		public final int dropsMax;

		public DropSpecifier(ItemStack itemStack)
		{
			this(itemStack, 1f);
		}

		public DropSpecifier(ItemStack itemStack, float dropChance)
		{
			this(itemStack, dropChance, 1, 1);
		}

		public DropSpecifier(ItemStack itemStack, int dropsMin, int dropsMax)
		{
			this(itemStack, 1f, dropsMin, dropsMax);
		}

		public DropSpecifier(ItemStack itemStack, float dropChance, int dropsMin, int dropsMax)
		{
			this.itemStack = itemStack;
			this.dropsMin = dropsMin;
			this.dropsMax = dropsMax;
			this.dropChance = dropChance;
		}

		public boolean shouldDrop(EntityPlayer harvester, int fortuneLevel, boolean isSilkTouching)
		{
			return RandomHelper.random.nextFloat() < dropChance && !isSilkTouching;
		}

		public int amountToDrop()
		{
			return RandomHelper.getRandomIntFromRange(dropsMin, dropsMax);
		}
		
		public List<ItemStack> getDrops()
		{
			List<ItemStack> drops = new ArrayList<ItemStack>();
			
			int amountToDrop = amountToDrop();
			for (int i = 0; i < amountToDrop; i++)
				drops.add(itemStack.copy());
			
			return drops;
		}

		public List<ItemStack> getDrops(EntityPlayer harvester, int fortuneLevel, boolean isSilkTouching)
		{
			List<ItemStack> drops = new ArrayList<ItemStack>();
			if (shouldDrop(harvester, fortuneLevel, isSilkTouching))
			{
				int amountToDrop = amountToDrop() + RandomHelper.getRandomIntFromRange(0, fortuneLevel);
				for (int i = 0; i < amountToDrop; i++)
					drops.add(itemStack.copy());
			}
			return drops;
		}

		public void modifyDrops(List<ItemStack> drops, EntityPlayer harvester, int fortuneLevel, boolean isSilkTouching)
		{
			drops.addAll(getDrops(harvester, fortuneLevel, isSilkTouching));
		}
	}

	// only shows in NEI, doesn't actually modify the drops
	public static class NEIBlockSpecifier extends BlockSpecifier
	{
		public NEIBlockSpecifier(IBlockState stateToMatch, ItemStack itemStackForDisplay)
		{
			super(stateToMatch, itemStackForDisplay);
		}

		@Override
		public boolean matches(IBlockAccess world, BlockPos pos, IBlockState state)
		{
			return false;
		}
	}

	// only shows in NEI, doesn't actually modify the drops
	public static class NEIDropSpecifier extends DropSpecifier
	{
		public NEIDropSpecifier(ItemStack itemStack, float dropChance, int dropsMin, int dropsMax)
		{
			super(itemStack, dropChance, dropsMin, dropsMax);
		}

		@Override
		public void modifyDrops(List<ItemStack> drops, EntityPlayer harvester, int fortuneLevel, boolean isSilkTouching)
		{
		}
	}

	public static class DropInfo
	{
		public DropSpecifier drop;
		public BlockSpecifier dropper;

		public DropInfo(BlockSpecifier dropper, DropSpecifier drop)
		{
			this.dropper = dropper;
			this.drop = drop;
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void onGetHarvestDrops(BlockEvent.HarvestDropsEvent event)
	{
		//Block block = event.getState().getBlock();
		for (Entry<BlockSpecifier, DropSpecifier> blockDropSpecifier : blockDrops.entrySet())
		{
			if (blockDropSpecifier.getKey().matches(event.getWorld(), event.getPos(), event.getState()))
			{
				blockDropSpecifier.getValue().modifyDrops(event.getDrops(), event.getHarvester(), event.getFortuneLevel(), event.isSilkTouching());
			}
		}
	}
}
