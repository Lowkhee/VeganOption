package squeek.veganoption.blocks.tiles;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.IFluidHandler;
import squeek.veganoption.ModInfo;
import squeek.veganoption.blocks.BlockBasin;
import squeek.veganoption.content.modules.Basin;
import squeek.veganoption.content.modules.Ender;
import squeek.veganoption.helpers.BlockHelper;
import squeek.veganoption.helpers.FluidContainerHelper;
import squeek.veganoption.helpers.FluidHelper;
import squeek.veganoption.helpers.MiscHelper;
import squeek.veganoption.helpers.WorldHelper;
import squeek.veganoption.items.ItemFrozenBubble;
import squeek.veganoption.items.ItemWashableWheat;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Random;

import static net.minecraftforge.fluids.capability.CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;

public class TileEntityBasin extends TileEntity implements ITickable
{
	protected static final Logger Log = LogManager.getLogger(TileEntityBasin.class.getCanonicalName());
	public FluidTank fluidTank = new BasinTank(Fluid.BUCKET_VOLUME*10); //10000=10 buckets
	protected boolean isPowered = false;
	protected EnumFacing valveDirection = EnumFacing.UP;
	

	/*
	 * Updating
	 */
	@Override
	public void update()
	{ 
		if (world.isRemote)
			return;
		
		//Basin leaks if not facing up and is open
		IBlockState blockStateFrontValve = BlockHelper.getBlockStateOnFace(world, pos, this.valveDirection);
		if(isPowered && !isBasinEmpty() && this.valveDirection != EnumFacing.UP && blockStateFrontValve != null && blockStateFrontValve.getMaterial() == Material.AIR)
		{
			//IBlockState blockStateFrontValve = BlockHelper.getBlockStateOnFace(world, pos, this.valveDirection);
			//if(blockStateFrontValve != null && blockStateFrontValve.getMaterial() == Material.AIR)
			{
				FluidHelper.STABILITY fluidStability = FluidHelper.getFluidStability(this.fluidTank.getFluid().getFluid());
				if(fluidStability != FluidHelper.STABILITY.STABLE)
				{
					int leakAmt = (int)(this.fluidTank.getCapacity() * ((float)fluidStability.ordinal() / Fluid.BUCKET_VOLUME));
					Block block = this.fluidTank.getFluid().getFluid().getBlock();//state.getBlock();
					this.fluidTank.drain(new FluidStack(this.getBasinTankFluid().getFluid(), leakAmt), true);
					BlockPos posFluid = pos.offset(this.valveDirection);
					PropertyInteger LEVEL = PropertyInteger.create("level", 0, 15);
					
					if(!(block instanceof BlockFluidFinite))
					{
						world.setBlockState(posFluid, block.getDefaultState().withProperty(LEVEL, Integer.valueOf(5)), 6);//4
						if(block instanceof BlockLiquid)
							world.getBlockState(posFluid).getBlock().randomDisplayTick(world.getBlockState(posFluid), world, posFluid, new Random());
					}
					/*else //BlockFluidFinite drain issues
					{
						world.setBlockState(posFluid, block.getDefaultState().withProperty(LEVEL, Integer.valueOf(7)), 2);
						///random display tick for finite? show particles at least
						((BlockFluidFinite)world.getBlockState(posFluid).getBlock()).randomDisplayTick(world.getBlockState(posFluid), world, posFluid, new Random());
						world.setBlockState(posFluid, Blocks.AIR.getDefaultState(), 2);
					}*/
				}
			}
		}
		//allows finite fluid to consumed by basin if not a direct source block
		else if(isPowered && blockStateFrontValve != null && blockStateFrontValve.getBlock() instanceof BlockFluidFinite)
		{
			Log.log(ModInfo.debugLevel, "Consume Finite Fluid: " + blockStateFrontValve.getBlock().getUnlocalizedName());
			this.consumeFluidAtValve(world, pos.offset(this.valveDirection));
		}
		//removes leaking fluid
		else if(isPowered && blockStateFrontValve != null && this.valveDirection != EnumFacing.UP)
		{
			Fluid fluidAtValve = FluidHelper.getFluidTypeOfBlock(blockStateFrontValve);
			if(fluidAtValve != null && !blockStateFrontValve.isFullBlock()) //!this.isBasinEmpty() && (this.fluidTank.getFluid().getFluid() == fluidAtValve || 
				world.setBlockState(pos.offset(this.valveDirection), Blocks.AIR.getDefaultState(), 2);
		}
	
	}

	/*
	 * Fluid container behavior
	 */
	public boolean couldFillContainers()
	{
		return isOpen() && fluidTank.getFluidAmount() > 0;
	}


	/*public boolean tryFillContainersInside()
	{
		if (world == null || world.isRemote || !couldFillContainers())
			return false;

		List<EntityItem> entityItemsWithin = WorldHelper.getItemEntitiesWithin(world, ((BlockBasin) Basin.basin).getInnerBoundingBox(world, pos.getX(), pos.getY(), pos.getZ()));

		for (EntityItem entityItemWithin : entityItemsWithin)
		{
			if (!FluidContainerHelper.isEmptyContainer(entityItemWithin.getEntityItem()))
				continue;

			EntityItem entityItemToFill = entityItemWithin;
			ItemStack containerToFill = entityItemWithin.getEntityItem().splitStack(1);
			FluidContainerHelper.drainHandlerIntoContainer(fluidTank, fluidTank.getFluid(), containerToFill);

			if (!FluidContainerHelper.isEmptyContainer(containerToFill))
			{
				entityItemToFill = new EntityItem(entityItemToFill.world, entityItemToFill.posX, entityItemToFill.posY, entityItemToFill.posZ, containerToFill);
				entityItemToFill.setPickupDelay(10);
				entityItemToFill.world.spawnEntity(entityItemToFill);
			}
			return true;
		}

		return false;
	}*/
	
	/*
	 * is fluid tank full
	 */
	public boolean isBasinEmpty()
	{
		return this.fluidTank.getFluidAmount() <= 0 ? true : false; 
	}
	/*
	 * is fluid tank full
	 */
	public boolean isBasinFull()
	{
		return (this.fluidTank.getCapacity() - this.fluidTank.getFluidAmount()) <= 0 ? true : false; 
	}
	/*
	 * get fluid tank capacity
	 */
	public int getBasinTankCapacity()
	{
		return this.fluidTank.getCapacity();
	}
	/*
	 * get the current amount of fluid in tank
	 */
	public int getBasinTankAmount()
	{
		return this.fluidTank.getFluidAmount();
	}
	/*
	 * get the fluid in the tank, null otherwise
	 */
	public FluidStack getBasinTankFluid()
	{
		return this.fluidTank.getFluid();
	}
	
	/*
	 * get available capacity
	 */
	public int getBasinTankEmptyCapacity()
	{
		return this.fluidTank.getCapacity() - this.fluidTank.getFluidAmount();
	}
	
	/*
	 * get the basin fluidtank
	 */
	public FluidTank getBasinTank()
	{
		return this.fluidTank;
	}
	
	/*
	 * consume fluid at the open valve, returns the int amount of consumed fluid
	 */
	public int consumeFluidAtValve(@Nonnull World world, @Nonnull BlockPos posFluidSource)
	{
		IBlockState stateFluidSource = world.getBlockState(posFluidSource);
		EnumFacing faceFluidSource = this.valveDirection.getOpposite();
		if(!this.isBasinFull() && this.isPowered && stateFluidSource.getBlock() != Blocks.AIR)
		{
			if(FluidHelper.getTypeOfFluidSource(world, posFluidSource, faceFluidSource) == null)
				return 0;
			
			Block blockSourceFluid = stateFluidSource.getBlock();
			boolean isFluidSourceBlock = (blockSourceFluid instanceof BlockLiquid) || (blockSourceFluid instanceof BlockFluidBase);			
			
			FluidStack fluidStackDrained = null;
			IFluidHandler fluidHandlerSource = FluidUtil.getFluidHandler(world, posFluidSource, faceFluidSource);
			if(fluidHandlerSource != null)
				fluidStackDrained = fluidHandlerSource.drain(Integer.MAX_VALUE, false);
			
			if(fluidStackDrained == null && isFluidSourceBlock)
				fluidStackDrained = new FluidStack(FluidHelper.getFluidTypeOfBlock(stateFluidSource), Fluid.BUCKET_VOLUME);// .drain(world, posFluidSource, false);
			
			if(fluidStackDrained == null)
				return 0;
			
			int fluidSourceAmt = fluidStackDrained.amount;
			if(fluidSourceAmt <= 0)
				return 0;
			//can basin accept fluid
			if(this.getBasinTankFluid() != null && fluidStackDrained.getFluid() != this.getBasinTankFluid().getFluid())
				return 0;
			
			int basinAvailCapacityAmt = this.getBasinTankEmptyCapacity();
			//if fluid source is a source block, % of loss, based on stability. Could be changed to amount consumed
			int fluidSrcLossAmt = isFluidSourceBlock ? Math.round((float)fluidSourceAmt * (float)(FluidHelper.getFluidStability(fluidStackDrained.getFluid()).ordinal() * .10)) : 0; 
			int fluidSrcAvailAmt = fluidSourceAmt - fluidSrcLossAmt;
			
			int fluidSrcTransferAmt = basinAvailCapacityAmt >= fluidSrcAvailAmt ? fluidSrcAvailAmt : (fluidSrcAvailAmt - basinAvailCapacityAmt);
			
			//if soource block, then loss of fluid, if fluidtank, then reduced flow - based on stability
			fluidStackDrained = FluidHelper.consumeFluid(world, pos, posFluidSource, fluidStackDrained.getFluid(), fluidSrcTransferAmt);
			if(fluidStackDrained == null)
				return 0;
			//fluidStackDrained = fluidHandlerSource.drain(fluidSrcTransferAmt, true); //what is the point of Amount? returns null if Amount is less than FluidBlockWrapper#108#simulatedDrain.amount, which is the full amount
			fluidStackDrained.amount = fluidSrcTransferAmt;
			this.fluidTank.fill(fluidStackDrained, true);
		}
		
		return 0;
	}

	/*
	 * Open/closed state
	 */
	public boolean isOpen()
	{
		return isPowered();
	}

	public boolean isClosed()
	{
		return !isOpen();
	}

	/*
	 * Right Click Handling
	 * The majority of Basin fluid handling is incorporated into FluidCotainerHelper
	 */
	public boolean onBlockActivated(EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		ItemStack heldItemStack = player.getHeldItem(hand);
		net.minecraft.item.Item heldItem = heldItemStack.getItem();
		//FrozenBubble directly usable on basin
		if(heldItem instanceof ItemFrozenBubble && this.getBasinTankAmount() >= Fluid.BUCKET_VOLUME && this.getBasinTankFluid().getFluid() == Ender.fluidRawEnder)
		{
			heldItemStack.splitStack(1);
			this.fluidTank.drainInternal(Fluid.BUCKET_VOLUME, true);
			FluidContainerHelper.tryAddItemToInventory(player, hand, new ItemStack(Items.ENDER_PEARL, 1));
			return true;	
		}
		else if(heldItem instanceof ItemWashableWheat && !ItemWashableWheat.isReadyToCook(heldItemStack) && this.getBasinTankAmount() >= Fluid.BUCKET_VOLUME && this.getBasinTankFluid().getFluid() == FluidRegistry.WATER)
		{
			ItemStack itemStackCrafted = heldItemStack.splitStack(1);
			this.fluidTank.drainInternal(Fluid.BUCKET_VOLUME, true);
			FluidContainerHelper.tryAddItemToInventory(player, hand, ItemWashableWheat.wash(itemStackCrafted, 1)); 
			return true;
		}
		
		return false;
	}
	
	/*
	 * get valve direction
	 */
	public EnumFacing getValveDirection()
	{
		return this.valveDirection;
	}
	/*
	 * set valve direction
	 */
	public void setValveDirection(EnumFacing direction)
	{
		this.valveDirection = direction;
		onTileUpdate();
	}
	
	public void setPowered(boolean boolPowered)
	{
		this.isPowered = boolPowered;
		onTileUpdate();
	}

	public boolean isPowered()
	{
		return isPowered;
	}
	
	public void onTileUpdate()
	{
		if (world != null)
		{
			world.notifyNeighborsOfStateChange(pos, Basin.basin, true);
			world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 0);
		}
	}
	
	public static TileEntityBasin getBasinEntityAt(@Nonnull World world, @Nonnull BlockPos pos)
	{
		TileEntity tile = BlockHelper.getTileEntitySafely(world, pos); //world.getTileEntity(pos);
		if (tile instanceof TileEntityBasin)
			return (TileEntityBasin)tile;
		return null;
	}
	
	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState)
	{
		return (oldState.getBlock() != newState.getBlock());
	}
	
	@Override
	public void markDirty()
    {
		super.markDirty();
    }

	/*
	 * Synced data
	 */
	public void readSyncedNBT(NBTTagCompound compound)
	{
		if (compound.hasKey("Fluid"))
			fluidTank.setFluid(FluidStack.loadFluidStackFromNBT(compound.getCompoundTag("Fluid")));
		else
			fluidTank.setFluid(null);

		this.setValveDirection(EnumFacing.values()[compound.getInteger("Facing")]);
		this.isPowered = compound.getBoolean("Powered");
	}

	public void writeSyncedNBT(NBTTagCompound compound)
	{
		TileEntityBasin tileBasin = TileEntityBasin.getBasinEntityAt(world, pos);
		if (tileBasin != null && tileBasin.fluidTank.getFluid() != null)
		{
			NBTTagCompound fluidTag = new NBTTagCompound();
			tileBasin.fluidTank.getFluid().writeToNBT(fluidTag);
			compound.setTag("Fluid", fluidTag);
		}
		IBlockState state = world.getBlockState(pos);
		if(state != null)
		{
			this.isPowered = state.getValue(BlockBasin.IS_OPEN);
			this.valveDirection = state.getValue(BlockBasin.FACING);
		}
		//if state is null, default values will be written
		compound.setInteger("Facing", this.getValveDirection().ordinal());
		compound.setBoolean("Powered", isPowered());
	}

	//some tile entity data packet received - 2
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
	{
		handleUpdateTag(pkt.getNbtCompound());
	}

	//1
	@Override
	public SPacketUpdateTileEntity getUpdatePacket()
	{
		return new SPacketUpdateTileEntity(pos, 1, getUpdateTag());
	}

	@Nonnull
	@Override
	public NBTTagCompound getUpdateTag()
	{
		NBTTagCompound tag = super.getUpdateTag();
		writeSyncedNBT(tag);
		return tag;
	}

	//3
	@Override
	public void handleUpdateTag(@Nonnull NBTTagCompound tag)
	{
		readFromNBT(tag);
	}

	/*
	 * Save data
	*/
	@Override
	public void readFromNBT(NBTTagCompound compound)
	{
		super.readFromNBT(compound);

		readSyncedNBT(compound);
	}

	/*
	 * Called after BlockBasin#onBlockPlacedBy 
	 * @see net.minecraft.tileentity.TileEntity#writeToNBT(net.minecraft.nbt.NBTTagCompound)
	 */
	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound)
	{
		compound = super.writeToNBT(compound);

		writeSyncedNBT(compound);

		return compound;
	}

	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing)
	{
		return capability == FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing)
	{
		return capability == FLUID_HANDLER_CAPABILITY ? (T) fluidTank : super.getCapability(capability, facing);
	}

	private class BasinTank extends FluidTank
	{
		public BasinTank(int capacity)
		{
			super(capacity);
		}

		@Override
		public int fill(FluidStack resource, boolean doFill)
		{
			if (resource == null || !canFillFluidType(resource))
				return 0;

			int amountFilled = super.fill(resource, doFill);

			if (doFill && amountFilled > 0)
				onTileUpdate();

			return amountFilled;
		}

		@Override
		public FluidStack drain(FluidStack resource, boolean doDrain)
		{
			if (resource == null || !canDrainFluidType(resource))
				return null;

			return drain(resource.amount, doDrain);
		}

		@Override
		public FluidStack drain(int maxDrain, boolean doDrain)
		{
			FluidStack drainedStack = super.drain(maxDrain, doDrain);

			if (doDrain && drainedStack != null && drainedStack.amount > 0)
				onTileUpdate();

			return drainedStack;
		}

		@Override
		public boolean canFillFluidType(FluidStack fluid)
		{
			return this.fluid == null || this.fluid.getFluid() == null || this.fluid.getFluid() == fluid.getFluid();
		}

		@Override
		public boolean canDrainFluidType(FluidStack fluid)
		{
			return this.fluid != null && this.fluid.getFluid() != null && this.fluid.getFluid() == fluid.getFluid();
		}
	}
}
