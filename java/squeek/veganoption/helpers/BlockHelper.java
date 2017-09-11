package squeek.veganoption.helpers;

import net.minecraft.block.Block;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fluids.BlockFluidFinite;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
//import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.SideOnly;
import squeek.veganoption.content.registry.RelationshipRegistry;

//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
import java.util.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockHelper 
{
	public static final float BLOCK_HARDNESS_UNBREAKABLE = -1.0f;

	//Deprecated + invoke = crash, This only seems true during launch, not in-game
	//private static final Method createStackedBlock = ReflectionHelper.findMethod(Block.class, null, new String[]{"createStackedBlock", "func_180643_i"}, IBlockState.class);
	
	public static ItemStack blockStateToItemStack(IBlockState state)
	{
		//from protected method Block#getSilkTouchDrop
		Block block = state.getBlock();
		Item item = Item.getItemFromBlock(block);
		//item.set
        int i = 0;

        if (item.getHasSubtypes())
        {
            i = block.getMetaFromState(state);
        }

        return new ItemStack(item, 1, i);
		/*try
		{			
			//return (ItemStack) createStackedBlock.invoke(state.getBlock(), state);
		}
		catch (IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
		catch (InvocationTargetException e)
		{
			throw new RuntimeException(e);
		}*/
	}
	
	/*
	  * Find the direction/directions a block is facing, null if no PropertyDirection is defined
	  */
	 public static List<EnumFacing> getBlockFaces(@Nonnull IBlockAccess world, @Nonnull BlockPos pos)
	 {
		IBlockState blockState = world.getBlockState(pos);
	 	List<EnumFacing> listFaces = new ArrayList<EnumFacing>(6);
		Collection<IProperty<?>> listProperties = blockState.getPropertyKeys();
		for(IProperty<?> property : listProperties)
			if(property instanceof PropertyDirection)
				listFaces.add((EnumFacing)blockState.getValue(property));      
		if(listFaces.isEmpty())
			return null;
		return listFaces;
	 }
	
	/*
	 * get the "first" face of a block with a block of Type, returns null if none found
	 */
	public static <T extends Block> EnumFacing getBlockFaceWithAdjacentBlockType(@Nonnull World world, @Nonnull BlockPos blockPos, @Nonnull Class<T> blockType)
	{
		if(blockType == null)
			return null;
		List<EnumFacing> faces = BlockHelper.getBlockFaces(world, blockPos);
		if(faces == null)
			return null;
		for(EnumFacing face : faces)
			if(blockType.isInstance((BlockHelper.getBlockStateOnFace(world, blockPos, face)).getBlock()))
				return face;
		return null;
	}
	
	/*
	 * get the blockstate on the face of the block at fromBlockPos
	 */
	public static IBlockState getBlockStateOnFace(@Nonnull World world, @Nonnull BlockPos fromBlockPos, @Nonnull EnumFacing direction)
	{
		return world.getBlockState(fromBlockPos.offset(direction));
	}
	
	/*
	 * Finds the first solid block in the given direction
	 * search starts from the block adjacent to fromBlockPos in the direction specified - will only check up to 256 blocks away
	 */
	public static BlockPos findFirstSolidBlock(@Nonnull World world, @Nonnull BlockPos fromBlockPos, @Nonnull EnumFacing direction)
	{
		BlockPos blockToCheck = fromBlockPos.offset(direction);
		for(int i = 1; i <= 256 && !(world.getBlockState(blockToCheck).getMaterial().blocksMovement()); i++)
			blockToCheck = blockToCheck.offset(direction);
		return blockToCheck;
	}

	public static boolean isMaterial(World world, BlockPos blockPos, Material material)
	{
		return world.getBlockState(blockPos).getMaterial() == material;
	}

	public static boolean isAdjacentToMaterial(World world, BlockPos blockPos, Material material)
	{
		for (BlockPos blockToCheck : getHorizontalBlocksAdjacentTo(blockPos))
		{
			if (isMaterial(world, blockToCheck, material))
				return true;
		}
		return false;
	}

	public static BlockPos[] getHorizontalBlocksAdjacentTo(BlockPos blockPos)
	{
		return new BlockPos[]{
			blockPos.offset(EnumFacing.NORTH), blockPos.offset(EnumFacing.SOUTH),
			blockPos.offset(EnumFacing.EAST), blockPos.offset(EnumFacing.WEST)
		};
	}

	public static boolean isWater(World world, BlockPos blockPos)
	{
		return isMaterial(world, blockPos, Material.WATER);
	}

	public static boolean isAdjacentToOrCoveredInWater(World world, BlockPos blockPos)
	{
		return isWater(world, blockPos.up()) || isAdjacentToWater(world, blockPos);
	}

	public static boolean isAdjacentToWater(World world, BlockPos blockPos)
	{
		return isAdjacentToMaterial(world, blockPos, Material.WATER);
	}

	public static BlockPos followWaterStreamToSourceBlock(World world, @Nullable BlockPos posTo, BlockPos posFrom)
	{
		return followFluidStreamToSourceBlock(world, posTo, posFrom, FluidRegistry.WATER);
	}

	public static BlockPos followFluidStreamToSourceBlock(World world, @Nullable BlockPos posTo, BlockPos posFrom, Fluid fluid)
	{
		return followFluidStreamToSourceBlock(world, posTo, posFrom, fluid, new LinkedHashSet<BlockPos>());
	}

	public static BlockPos followFluidStreamToSourceBlock(World world, @Nullable BlockPos posTo, BlockPos posFrom, Fluid fluid, LinkedHashSet<BlockPos> blocksChecked)
	{
		BlockPos previousBlockPos = blocksChecked.isEmpty() ? null : (BlockPos)(blocksChecked.toArray()[blocksChecked.size() - 1]);
		//block is a fluidtank? facing the opposite direction of last blockPos
		//if(previousBlockPos != null && FluidHelper.getFluidHandlerAt(world, blockPos, BlockHelper.sideBlockLocated(blockPos, previousBlockPos)) != null)
		//	return blockPos;
		
		if(fluid.getBlock() instanceof BlockFluidFinite || FluidHelper.getFluidLevel(world, posFrom) == FluidHelper.getStillFluidLevel(fluid))
			return posFrom;

		List<BlockPos> blocksToCheck = new ArrayList<BlockPos>(Arrays.asList(getHorizontalBlocksAdjacentTo(posFrom)));
		blocksToCheck.add(posFrom.offset(EnumFacing.UP));
		//blocksToCheck.add(blockPos.offset(EnumFacing.DOWN));
		//blocksToCheck.addAll(Arrays.asList(getBlocksAdjacentTo(blockPos)));
		previousBlockPos = posFrom;
		
		for(BlockPos blockToCheck : blocksToCheck)
		{
			if(posTo == null || blockToCheck.compareTo(posTo) != 0)
			{
				IFluidHandler fluidHandler = FluidHelper.getFluidHandlerAt(world, blockToCheck, BlockHelper.sideBlockLocated(previousBlockPos, blockToCheck));
				if(posTo != null && fluidHandler != null)
				{
					FluidStack fluidToCheck = fluidHandler.drain(Integer.MAX_VALUE, false);
					if(fluidToCheck != null && fluid.getBlock() == fluidToCheck.getFluid().getBlock())
						return blockToCheck;
				}
				//!--must check fluid type for this t work--- the originating fluid block level should be greater than (or equal to? depends on level as block flows down) the adjacent block (ie.. block fluid level is increasing as we move away from the source)
				//if(previousBlockPos != null && (FluidHelper.getFluidLevel(world, blockToCheck) > FluidHelper.getFluidLevel(world, previousBlockPos)))
					//blocksChecked.add(blockToCheck);
				if(FluidHelper.getFluidTypeOfBlock(world.getBlockState(blockToCheck)) == fluid && !blocksChecked.contains(blockToCheck))
				{
					if(FluidHelper.getFluidLevel(world, blockToCheck) == FluidHelper.getStillFluidLevel(fluid))
						return blockToCheck;
					else
					{
						blocksChecked.add(blockToCheck);
						BlockPos foundSourceBlock = followFluidStreamToSourceBlock(world, posTo, blockToCheck, fluid, blocksChecked);

						if (foundSourceBlock != null)
							return foundSourceBlock;
					}
				}
			}
		}
		return null;
	}

	public static BlockPos[] getBlocksInRadiusAround(BlockPos centerBlock, int radius)
	{
		Set<BlockPos> blocks = new HashSet<BlockPos>();
		int radiusSq = radius * radius;
		for (int xOffset = 0; xOffset <= radius; xOffset++)
		{
			for (int yOffset = 0; yOffset <= radius; yOffset++)
			{
				for (int zOffset = 0; zOffset <= radius; zOffset++)
				{
					BlockPos block = centerBlock.add(xOffset, yOffset, zOffset);
					int xDelta = block.getX() - centerBlock.getX();
					int yDelta = block.getY() - centerBlock.getY();
					int zDelta = block.getZ() - centerBlock.getZ();
					int deltaLengthSq = xDelta * xDelta + yDelta * yDelta + zDelta * zDelta;
					if (deltaLengthSq <= radiusSq)
					{
						blocks.add(block);
						blocks.add(centerBlock.add(-xOffset, yOffset, zOffset));
						blocks.add(centerBlock.add(xOffset, yOffset, -zOffset));
						blocks.add(centerBlock.add(-xOffset, yOffset, -zOffset));
						blocks.add(centerBlock.add(xOffset, -yOffset, zOffset));
						blocks.add(centerBlock.add(xOffset, -yOffset, -zOffset));
						blocks.add(centerBlock.add(-xOffset, -yOffset, zOffset));
						blocks.add(centerBlock.add(-xOffset, -yOffset, -zOffset));
					}
				}
			}
		}
		return blocks.toArray(new BlockPos[0]);
	}

	public static BlockPos[] filterBlockListToBreakableBlocks(World world, BlockPos... blocks)
	{
		List<BlockPos> filteredBlocks = new ArrayList<BlockPos>();
		for (BlockPos blockPos : blocks)
		{
			IBlockState state = world.getBlockState(blockPos);
			Block block = state.getBlock();

			if (block == null)
				continue;

			if (block.isAir(state, world, blockPos))
				continue;

			if (isBlockUnbreakable(world, blockPos))
				continue;

			if (state.getMaterial().isLiquid())
				continue;

			filteredBlocks.add(blockPos);
		}
		return filteredBlocks.toArray(new BlockPos[0]);
	}

	public static boolean isBlockUnbreakable(World world, BlockPos pos)
	{
		return world.getBlockState(pos).getBlockHardness(world, pos) == BLOCK_HARDNESS_UNBREAKABLE;
	}

	public static TileEntity getTileEntitySafely(IBlockAccess world, BlockPos pos)
	{
		return world instanceof ChunkCache ? ((ChunkCache) world).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK) : world.getTileEntity(pos);
	}
	
	/*
	 * Determines which side(face) blockB is placed on blockA in the world, returns null if both blocks do not share an axis
	 */
	public static EnumFacing sideBlockLocated(BlockPos blockAPos, BlockPos blockBPos)
	{
		BlockPos position = blockAPos.crossProduct(blockBPos);
		if(position.getX() == 0)
			return blockAPos.getX() > blockBPos.getX() ? EnumFacing.WEST : EnumFacing.EAST;
		else if(position.getY() == 0)
			return blockAPos.getY() > blockBPos.getY() ? EnumFacing.DOWN : EnumFacing.UP;
		else if(position.getZ() == 0)
			return blockAPos.getZ() > blockBPos.getZ() ? EnumFacing.NORTH : EnumFacing.SOUTH;
		return null;
	}
	/*
	 *adds a particle effect to a block. Used in @override randomDisplayTick of block class
	 */
	
	 @SideOnly(Side.CLIENT)
	 public static void spawnParticles(@Nonnull World world, @Nonnull BlockPos pos, EnumParticleTypes particle, double xChange, double yChange, double zChange, double xSpeed, double ySpeed, double zSpeed)
	 {
		 Random random = world.rand;

	        for (int i = 0; i < 6; ++i)
	        {
	            double d1 = (double)((float)pos.getX() + random.nextFloat());
	            double d2 = (double)((float)pos.getY() + random.nextFloat());
	            double d3 = (double)((float)pos.getZ() + random.nextFloat());

	            if (i == 0 && !world.getBlockState(pos.up()).isOpaqueCube())
	            {
	                d2 = (double)pos.getY() + 0.0625D + yChange;
	            }

	            if (i == 1 && !world.getBlockState(pos.down()).isOpaqueCube())
	            {
	                d2 = (double)pos.getY() - 0.0625D;
	            }

	            if (i == 2 && !world.getBlockState(pos.south()).isOpaqueCube())
	            {
	                d3 = (double)pos.getZ() + 0.0625D + zChange;
	            }

	            if (i == 3 && !world.getBlockState(pos.north()).isOpaqueCube())
	            {
	                d3 = (double)pos.getZ() - 0.0625D;
	            }

	            if (i == 4 && !world.getBlockState(pos.east()).isOpaqueCube())
	            {
	                d1 = (double)pos.getX() + 0.0625D + xChange;
	            }

	            if (i == 5 && !world.getBlockState(pos.west()).isOpaqueCube())
	            {
	                d1 = (double)pos.getX() - 0.0625D;
	            }

	            if (d1 < (double)pos.getX() || d1 > (double)(pos.getX() + 1) || d2 < 0.0D || d2 > (double)(pos.getY() + 1) || d3 < (double)pos.getZ() || d3 > (double)(pos.getZ() + 1))
	            {
	                world.spawnParticle(particle, d1, d2, d3, xSpeed, ySpeed, zSpeed, new int[0]);
	            }
	        }
	    }
	 
	 @Nullable
	    public static Block getBlockFromItemName(@Nullable ResourceLocation resourcelocation, @Nullable String name)
	    {
		 	if(name == null || name.isEmpty())
	        	name = resourcelocation.getResourcePath();
	        
	        Block block = Block.getBlockFromName(name);
	        if(block != null)
	        	return block;
	        
	        Item item = (Item)Item.REGISTRY.getObject(resourcelocation);
	        if(item == null)
	        	Item.getByNameOrId(name);
	        
	        if(item != null)
	        {
	        	List<ItemStack> listRelationship = RelationshipRegistry.getParents(new ItemStack(item, 1));
	        	for(ItemStack itemStack : listRelationship)
	        	{
	        		block = Block.getBlockFromItem(itemStack.getItem());
	        		if(block != null)
	        			return block;
	        	}
	        	block = Block.getBlockFromItem(item);
	        	if(block != null)
	        		return block;
	        }
	        
	        return null;
	    
	    }
}
