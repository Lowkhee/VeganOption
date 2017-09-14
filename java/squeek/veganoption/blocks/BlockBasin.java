package squeek.veganoption.blocks;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoAccessor;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import squeek.veganoption.helpers.BlockHelper;
import squeek.veganoption.helpers.LangHelper;
import squeek.veganoption.helpers.MiscHelper;
import squeek.veganoption.blocks.tiles.TileEntityBasin;

import javax.annotation.Nonnull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

//@Mod.EventBusSubscriber
@Optional.Interface(iface = "mcjty.theoneprobe.api.IProbeInfoAccessor", modid = "theoneprobe")
public class BlockBasin extends Block implements IHollowBlock, IProbeInfoAccessor
{
	private static final Logger Log = LogManager.getLogger(BlockBasin.class.getCanonicalName());
	public static final PropertyBool IS_OPEN = PropertyBool.create("is_open");
	public static final double SIDE_WIDTH = 0.125D;
	public static final PropertyDirection FACING = PropertyDirection.create("facing",Lists.newArrayList(EnumFacing.values()));

	public BlockBasin(Material material)
	{
		super(material);
		setSoundType(SoundType.METAL);
		//MinecraftForge.EVENT_BUS.post(new BasinEvent());
	}

	@Nonnull
	@Override
	public BlockStateContainer createBlockState() //called first on mc loading
	{
		return new BlockStateContainer(this, IS_OPEN, FACING);
	}
	
	/*
	 * called first on loading world with basin - called first? on breaking - use for non metadata informatio(non-Javadoc)
	 * @see net.minecraft.block.Block#getActualState(net.minecraft.block.state.IBlockState, net.minecraft.world.IBlockAccess, net.minecraft.util.math.BlockPos)
	 */
	@Nonnull
	@Override
	public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess world, BlockPos pos) //called second on save load
	{
		/*TileEntity tile = BlockHelper.getTileEntitySafely(world, pos);
		boolean open = false; 
		EnumFacing direction = EnumFacing.UP;
		if (tile != null && tile instanceof TileEntityBasin)
		{
			open = ((TileEntityBasin) tile).isOpen();
			direction = ((TileEntityBasin) tile).getValveDirection();
		}
		return state.withProperty(IS_OPEN, open).withProperty(FACING, direction);
		*/
		
		
		return world.getBlockState(pos);
	}

	@Nonnull
	@Override
	public IBlockState getStateFromMeta(int meta) //called second on mc loading and first on save load
	{
		return this.getDefaultState().withProperty(IS_OPEN, (meta > 5 ? true : false)).withProperty(FACING, EnumFacing.getFront(meta > 5 ? meta - 6 : meta));
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		return state.getValue(IS_OPEN) ? state.getValue(FACING).getIndex() + 6 : state.getValue(FACING).getIndex();
	}

	@Override
	public boolean hasTileEntity(IBlockState state)
	{
		return true;
	}

	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state)
	{
		return new TileEntityBasin();
	}

	/*
	 * Returns false when valve side and is powered, which allows for powering from valve (nice quirk?)
	 */
	//@Override		isSideSolid(IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, EnumFacing side)
	public boolean isSideSolid(IBlockState state, EnumFacing side)
	{
		if (side == state.getValue(FACING) && (boolean)state.getValue(IS_OPEN))
			return false;

		/*TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileEntityBasin)
		{
			return ((TileEntityBasin) tile).isClosed();
		}*/

		return true;
	}

	/*
	 * Events - this will not account for a BlockFluidFinite if it is not the source block since all flow is a blockitem?
	 */
	@Override
	public void neighborChanged(IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, Block blockChanged, BlockPos posChanged)
	{
		//super.neighborChanged(state, world, pos, blockChanged, posChanged);

		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileEntityBasin)
		{
			boolean isPowered = world.isBlockPowered(pos);
			TileEntityBasin tileBasin = (TileEntityBasin) tile;
			if(isPowered != tileBasin.isPowered() || isPowered != state.getValue(IS_OPEN))
				world.setBlockState(pos, state.withProperty(IS_OPEN, isPowered), 2);
			
			boolean isSolidSide = this.isSideSolid(state, BlockHelper.sideBlockLocated(pos, posChanged));
			
			//save for hopper or any other connection
			
			if(!isSolidSide)
				tileBasin.consumeFluidAtValve(world, posChanged);
			//if(!isSolidSide && blockChanged instanceof BlockFluidBase)
			//	((TileEntityBasin) tile).scheduleFluidConsume();
		}
		
	}
	
	/*
	 * if facing up, and empty basin or basin has water, fill with rain water
	 * @see net.minecraft.block.Block#fillWithRain(net.minecraft.world.World, net.minecraft.util.math.BlockPos)
	 */
	@Override
	public void fillWithRain(World worldIn, BlockPos pos)
    {
    }
	
	/*
	 * check whether basin is full
	 */
	public boolean isBasinFull(@Nonnull World world, @Nonnull BlockPos pos)
	{
		TileEntity tile = world.getTileEntity(pos);
		if(tile instanceof TileEntityBasin)
			return ((TileEntityBasin)tile).isBasinFull();
		return false;
	}
	
	/*
	 * get capacity of basin
	 */
	public int getBasinCapacity(@Nonnull World world, @Nonnull BlockPos pos)
	{
		TileEntity tile = world.getTileEntity(pos);
		if(tile instanceof TileEntityBasin)
			return ((TileEntityBasin)tile).getBasinTankCapacity();
		return -1;
	}
	/*
	 * get the amount of fluid in the basin
	 */
		public int getBasinFluidAmount(@Nonnull World world, @Nonnull BlockPos pos)
		{
			TileEntity tile = world.getTileEntity(pos);
			if(tile instanceof TileEntityBasin)
				return ((TileEntityBasin)tile).getBasinTankAmount();
			return -1;
		}
	/*
	 * get fluid in basin, null otherwise
	 */
	public FluidStack getBasinFluid(@Nonnull World world, @Nonnull BlockPos pos)
	{
		TileEntity tile = world.getTileEntity(pos);
		if(tile instanceof TileEntityBasin)
			return ((TileEntityBasin)tile).getBasinTankFluid();
		return null;
	}
	
	
	/*
	 * called first on block placement(non-Javadoc)
	 * @see net.minecraft.block.Block#onBlockPlacedBy(net.minecraft.world.World, net.minecraft.util.math.BlockPos, net.minecraft.block.state.IBlockState, net.minecraft.entity.EntityLivingBase, net.minecraft.item.ItemStack)
	 */
	@Override
	public void onBlockPlacedBy(@Nonnull World world, @Nonnull BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
		EnumFacing facing = placer.getLookVec().yCoord <= -.8 ? EnumFacing.UP : placer.getLookVec().yCoord >= .8 ? EnumFacing.DOWN : placer.getHorizontalFacing().getOpposite();
		boolean powered = world.isBlockPowered(pos);
		/*TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileEntityBasin)
		{
			((TileEntityBasin) tile).setPowered(powered);
			//((TileEntityBasin) tile).scheduleFluidConsume();
			((TileEntityBasin) tile).setValveDirection(facing);
		}*/
		world.setBlockState(pos, state.withProperty(IS_OPEN, powered).withProperty(FACING, facing), 2);
		super.onBlockPlacedBy(world, pos, state, placer, stack);
    }

	/*
	 * called after onBlockPlacedBy(non-Javadoc)
	 * @see net.minecraft.block.Block#onBlockAdded(net.minecraft.world.World, net.minecraft.util.math.BlockPos, net.minecraft.block.state.IBlockState)
	 */
	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state)
	{
		super.onBlockAdded(world, pos, state);
	}

	/*
	 * called when player interacts (right/left click?)
	 */
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		return super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
	}

	/*
	 * Bounding box/collisions
	 */
	public AxisAlignedBB getSideBoundingBox(EnumFacing side)
	{
		return getSideBoundingBox(side, 0, 0, 0);
	}

	public AxisAlignedBB getSideBoundingBox(EnumFacing side, double offsetX, double offsetY, double offsetZ)
	{
		return getSideBoundingBox(side, offsetX, offsetY, offsetZ, 1f);
	}

	public AxisAlignedBB getSideBoundingBox(EnumFacing side, double offsetX, double offsetY, double offsetZ, float depthScale)
	{
		return getSideBoundingBox(side, offsetX, offsetY, offsetZ, depthScale, 1f, 1f);
	}

	public AxisAlignedBB getSideBoundingBox(EnumFacing side, double offsetX, double offsetY, double offsetZ, float depthScale, float widthScale, float heightScale)
	{
		double minX = FULL_BLOCK_AABB.minX, minY = FULL_BLOCK_AABB.minY, minZ = FULL_BLOCK_AABB.minZ;
		double maxX = FULL_BLOCK_AABB.maxX, maxY = FULL_BLOCK_AABB.maxY, maxZ = FULL_BLOCK_AABB.maxZ;

		if (side.getFrontOffsetX() != 0)
		{
			if (side.getFrontOffsetX() > 0)
				minX = maxX - SIDE_WIDTH * depthScale;
			else
				maxX = minX + SIDE_WIDTH * depthScale;
			if (widthScale != 1) // z axis
			{
				double width = maxZ - minZ;
				if (widthScale > 0)
					maxZ = minZ + width * widthScale;
				else
					minZ = maxZ + width * widthScale;
			}
			if (heightScale != 1) // y axis
			{
				double height = maxZ - minZ;
				if (heightScale > 0)
					maxY = minY + height * heightScale;
				else
					minY = maxY + height * heightScale;
			}
		}
		if (side.getFrontOffsetY() != 0)
		{
			if (side.getFrontOffsetY() > 0)
				minY = maxY - SIDE_WIDTH * depthScale;
			else
				maxY = minY + SIDE_WIDTH * depthScale;
			if (widthScale != 1) // z axis
			{
				double width = maxZ - minZ;
				if (widthScale > 0)
					maxZ = minZ + width * widthScale;
				else
					minZ = maxZ + width * widthScale;
			}
			if (heightScale != 1) // x axis
			{
				double height = maxX - minX;
				if (heightScale > 0)
					maxX = minX + height * heightScale;
				else
					minX = maxX + height * heightScale;
			}
		}
		if (side.getFrontOffsetZ() != 0)
		{
			if (side.getFrontOffsetZ() > 0)
				minZ = maxZ - SIDE_WIDTH * depthScale;
			else
				maxZ = minZ + SIDE_WIDTH * depthScale;
			if (widthScale != 1) // x axis
			{
				double width = maxX - minX;
				if (widthScale > 0)
					maxX = minX + width * widthScale;
				else
					minX = maxX + width * widthScale;
			}
			if (heightScale != 1) // y axis
			{
				double height = maxY - minY;
				if (heightScale > 0)
					maxY = minY + height * heightScale;
				else
					minY = maxY + height * heightScale;
			}
		}

		return new AxisAlignedBB(offsetX + minX, offsetY + minY, offsetZ + minZ, offsetX + maxX, offsetY + maxY, offsetZ + maxZ);
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull AxisAlignedBB collidingAABB, @Nonnull List<AxisAlignedBB> collidingBoundingBoxes, Entity collidingEntity, boolean unused)
	{
		// hack... ??? what is the point of this Override? Used to add collision to the top? seems to be randomly called being within vicinity, more efficient way? why not setBoundingBox when block created
		//added to a method variable which resets on each call
		// this function is called with a null entity in World.isBlockFullCube
		if (collidingEntity == null)
			return;

		TileEntity tile = world.getTileEntity(pos);
		if (tile != null && tile instanceof TileEntityBasin && ((TileEntityBasin) tile).isPowered())
		{
			for (EnumFacing side : EnumFacing.VALUES)
			{
				//if (side == EnumFacing.UP)
					//continue;

				List<AxisAlignedBB> AABBs = new ArrayList<AxisAlignedBB>(4);

				AABBs.add(getSideBoundingBox(side, pos.getX(), pos.getY(), pos.getZ()));

				for (AxisAlignedBB AABB : AABBs)
				{
					if (AABB != null && collidingAABB.intersectsWith(AABB))
					{
						collidingBoundingBoxes.add(AABB);
					}
				}
			}
		}
		else
		{
			AxisAlignedBB AABB = getOuterBoundingBox(world, pos.getX(), pos.getY(), pos.getZ());

			if (AABB != null && collidingAABB.intersectsWith(AABB))
			{
				collidingBoundingBoxes.add(AABB);
			}
		}
	}

	public AxisAlignedBB getOuterBoundingBox(World world, int x, int y, int z)
	{
		return new AxisAlignedBB(x + FULL_BLOCK_AABB.minX, y + FULL_BLOCK_AABB.minY, z + FULL_BLOCK_AABB.minZ,
								 x + FULL_BLOCK_AABB.maxX, y + FULL_BLOCK_AABB.maxY, z + FULL_BLOCK_AABB.maxZ);
	}

	public AxisAlignedBB getInnerBoundingBox(World world, int x, int y, int z)
	{
		return new AxisAlignedBB(x + FULL_BLOCK_AABB.minX + SIDE_WIDTH, y + FULL_BLOCK_AABB.minY + SIDE_WIDTH, z + FULL_BLOCK_AABB.minZ + SIDE_WIDTH,
								 x + FULL_BLOCK_AABB.maxX - SIDE_WIDTH, y + FULL_BLOCK_AABB.maxY - SIDE_WIDTH, z + FULL_BLOCK_AABB.maxZ - SIDE_WIDTH);
	}

	/*
	 * IHollowBlock - returns false when block is powered
	 */
	@Override
	public boolean isBlockFullCube(World world, BlockPos pos)
	{
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileEntityBasin)
		{
			return ((TileEntityBasin) tile).isClosed();
		}
		return true;
	}

	/*
	 * Rendering
	 */

	@Override
	public boolean isOpaqueCube(IBlockState state)
	{
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, EnumFacing side)
	{
		return true;
	}

	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer()
	{
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data)
	{
		TileEntityBasin basin = (TileEntityBasin) world.getTileEntity(data.getPos());
		if (basin == null)
			return;
		probeInfo.text(LangHelper.translate("info.basin." + (basin.isPowered() ? "open" : "closed")));
		probeInfo.text(LangHelper.translate("info.basin." + basin.getValveDirection().getName()));

	}
	

	public float getFilledPercent(World world, BlockPos pos)
	{
		return (float) this.getBasinFluidAmount(world, pos) / this.getBasinCapacity(world, pos);
	}

	@Override
	public boolean hasComparatorInputOverride(IBlockState state)
	{
		return true;
	}

	@Override
	public int getComparatorInputOverride(IBlockState blockState, World world, BlockPos pos)
	{
		return MathHelper.floor(getFilledPercent(world, pos) * MiscHelper.MAX_REDSTONE_SIGNAL_STRENGTH);
	}
	
	
}
