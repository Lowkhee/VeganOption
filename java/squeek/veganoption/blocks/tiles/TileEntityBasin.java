package squeek.veganoption.blocks.tiles;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.IFluidHandler;
import squeek.veganoption.ModInfo;
import squeek.veganoption.blocks.BlockBasin;
import squeek.veganoption.content.modules.Basin;
import squeek.veganoption.helpers.BlockHelper;
import squeek.veganoption.helpers.FluidContainerHelper;
import squeek.veganoption.helpers.FluidHelper;
import squeek.veganoption.helpers.MiscHelper;
import squeek.veganoption.helpers.WorldHelper;

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
	protected boolean fluidConsumeStopped = true;
	protected int ticksUntilNextFluidConsume = FLUID_CONSUME_TICK_PERIOD; 
	protected int ticksUntilNextContainerFill = CONTAINER_FILL_TICK_PERIOD;

	public static int FLUID_CONSUME_TICK_PERIOD = MiscHelper.TICKS_PER_SEC;
	public static int CONTAINER_FILL_TICK_PERIOD = MiscHelper.TICKS_PER_SEC;
	

	/*
	 * Updating
	 */
	@Override
	public void update()
	{ 
		if (world.isRemote)
			return;
		
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
		else if(isPowered && blockStateFrontValve != null && blockStateFrontValve.getBlock() instanceof BlockFluidFinite)
		{
			Log.log(ModInfo.debugLevel, "Consume Finite Fluid: " + blockStateFrontValve.getBlock().getUnlocalizedName());
			this.consumeFluidAtValve(world, pos.offset(this.valveDirection));
			this.onFluidLevelChange();
		}
		else if(isPowered && blockStateFrontValve != null && this.valveDirection != EnumFacing.UP)
		{
			Fluid fluidAtValve = FluidHelper.getFluidTypeOfBlock(blockStateFrontValve);
			if(fluidAtValve != null && !blockStateFrontValve.isFullBlock()) //!this.isBasinEmpty() && (this.fluidTank.getFluid().getFluid() == fluidAtValve || 
				world.setBlockState(pos.offset(this.valveDirection), Blocks.AIR.getDefaultState(), 2);
		}
		//if(isPowered())
		//	consumeFluidAtValve(world, pos);
		/*if (shouldConsumeFluid())
		{
			boolean didConsume = tryConsumeFluidAbove();
			if (didConsume)
				scheduleFluidConsume();
			else
				endFluidConsume();
		}
		else
			ticksUntilNextFluidConsume = Math.max(0, ticksUntilNextFluidConsume - 1);

		if (shouldFillContainers())
		{
			tryFillContainersInside();
			scheduleFillContainers();
		}
		else
			ticksUntilNextContainerFill = Math.max(0, ticksUntilNextContainerFill - 1);*/
	}

	/*
	 * Fluid container behavior
	 */
	public boolean couldFillContainers()
	{
		return isOpen() && fluidTank.getFluidAmount() > 0;
	}

	/*public boolean shouldFillContainers()
	{
		return couldFillContainers() && ticksUntilNextContainerFill <= 0;
	}*/

	public boolean tryFillContainersInside()
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
	}

	/*public void scheduleFillContainers(int ticksUntilContainerFill)
	{
		if (ticksUntilNextContainerFill == 0)
			ticksUntilNextContainerFill = ticksUntilContainerFill;
		else
			ticksUntilNextContainerFill = Math.min(ticksUntilNextContainerFill, ticksUntilContainerFill);
	}

	public void scheduleFillContainers()
	{
		scheduleFillContainers(CONTAINER_FILL_TICK_PERIOD);
	}*/

	/*
	 * Fluid consuming behavior
	 */
	/*public boolean couldConsumeFluid()
	{
		return isOpen() && fluidTank.getFluidAmount() != fluidTank.getCapacity();
	}

	public boolean shouldConsumeFluid()
	{
		return couldConsumeFluid() && !fluidConsumeStopped && ticksUntilNextFluidConsume <= 0;
	}

	public boolean tryConsumeFluidAbove()
	{
		if (world == null || world.isRemote || !couldConsumeFluid())
			return false;

		BlockPos blockPosAbove = pos.up();
		IBlockState stateAbove = world.getBlockState(blockPosAbove);
		Fluid fluidAbove = FluidHelper.getFluidTypeOfBlock(stateAbove);

		if (fluidAbove == null)
			return false;

		FluidStack fluidToAdd = FluidHelper.consumeFluid(world, pos, blockPosAbove, fluidAbove, fluidTank.getCapacity() - fluidTank.getFluidAmount());

		if (fluidToAdd == null || !fluidTank.canFillFluidType(fluidToAdd))
			return false;

		fluidTank.fill(fluidToAdd, true);
		return true;
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
			//dissovle remaining, if source block
			/*if(isFluidSourceBlock)// && !(blockSourceFluid instanceof BlockFluidFinite))
			{
				((BlockFluidBase)stateFluidSource.getBlock()).setDensity(fluidSrcLossAmt);
				//fluidStackDrained.amount = 	fluidSrcLossAmt;			
				//fluidBlock.place(world, posFluidSource, fluidStackDrained, isFluidSourceBlock);
				//make block unbreakable?
				//fluidStackDrained = fluidHandlerSource.drain(Integer.MAX_VALUE, false);
				world.setBlockState(posFluidSource, stateFluidSource.withProperty(BlockFluidBase.LEVEL, 1), 2);
				//stateFluidSource.getBlock().isBlockSolid(worldIn, posFluidSource, side);
				stateFluidSource.getBlock().requiresUpdates();
				//stateFluidSource.getBlock().setBlockUnbreakable();
				stateFluidSource.getBlock().randomDisplayTick(stateFluidSource, world, posFluidSource, new Random());
				//stateFluidSource.getBlock().neighborChanged(stateFluidSource, world, posFluidSource, stateFluidSource.getBlock(), this.pos);
				
				//BlockFluidFinite placerholderBlock = new BlockFluidFinite(fluidStackDrained.getFluid(), stateFluidSource.getMaterial());
				//placerholderBlock.updateTick(world, posFluidSource, stateFluidSource, new Random());
			}*/
			
			/////basin leaks when not facing up with air block
		}
		
		return 0;
	}

	/*public void scheduleFluidConsume(int ticksUntilFluidConsume)
	{
		if (ticksUntilFluidConsume == 0)
			tryConsumeFluidAbove();
		else if (ticksUntilNextFluidConsume == 0)
			ticksUntilNextFluidConsume = ticksUntilFluidConsume;
		else
			ticksUntilNextFluidConsume = Math.min(ticksUntilNextFluidConsume, ticksUntilFluidConsume);

		fluidConsumeStopped = false;
	}

	public void scheduleFluidConsume()
	{
		scheduleFluidConsume(FLUID_CONSUME_TICK_PERIOD);
	}

	public void endFluidConsume()
	{
		fluidConsumeStopped = true;
	}*/

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

	/*public void onOpen()
	{
		scheduleFluidConsume();
	}

	public void onClose()
	{
		endFluidConsume();
	}*/

	//Following methods incorporated into FluidCotainerHelper
	/*
	 * Right Click Handling
	 */
	/*public boolean onBlockActivated(EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		ItemStack heldItem = player.getHeldItem(hand);
		if (FluidContainerHelper.isItemFluidContainer(heldItem))
		{
			// DoneTODO: This would be better moved directly into FluidContainerHelper
			IFluidHandler containerCap = FluidUtil.getFluidHandler(heldItem);
			if (containerCap == null)
				return false;
			for (IFluidTankProperties tankProp : containerCap.getTankProperties())
			{
				FluidStack containerFluid = tankProp.getContents();
				if (containerFluid != null && fluidTank.fill(containerFluid, false) == containerFluid.amount)
				{
					ItemStack toDrain = heldItem.splitStack(1);
					FluidContainerHelper.drainContainerIntoHandler(toDrain, fluidTank);
					tryAddItemToInventory(player, hand, toDrain);
					return true;
				} else if (fluidTank.getFluidAmount() > 0)
				{
					ItemStack toFill = heldItem.splitStack(1);
					FluidContainerHelper.drainHandlerIntoContainer(fluidTank, fluidTank.getFluid(), toFill);
					tryAddItemToInventory(player, hand, toFill);
					return true;
				}
			}
		}
		return false;
	}*/

	/**
	 * Attempts to add an item to the player's inventory in the following order:
	 * 1. Their current hand if there is no held item, or the held item has a stack size of 0
	 * 2. The first open slot in their inventory
	 * 3. Dropped in the world, if there is no free slot in the inventory.
	 */
	/*private void tryAddItemToInventory(EntityPlayer player, EnumHand hand, ItemStack newItem)
	{
		ItemStack heldItem = player.getHeldItem(hand);
		if (heldItem.isEmpty())
		{
			player.setHeldItem(hand, newItem);
			return;
		}
		if (!player.inventory.addItemStackToInventory(newItem))
			player.dropItem(newItem, false);
	}*/
	
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
	}

	/*
	 * Redstone Power Handling
	 */
	public void setPowered(boolean isPowered)
	{
		if (isPowered != isPowered())
		{
			this.isPowered = isPowered;

			if (isPowered)
				onPowered();
			else
				onUnpowered();

			if (world != null)
				world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 0);
		}
	}

	public boolean isPowered()
	{
		return isPowered;
	}

	public void onPowered()
	{
		if (world != null)
			world.notifyNeighborsOfStateChange(pos, Basin.basin, true);

		//onOpen();
	}

	public void onUnpowered()
	{
		if (world != null)
			world.notifyNeighborsOfStateChange(pos, Basin.basin, true);

		//onClose();
	}
	
	/*
	 * call everytime the basin tank drains or fills
	 */
	public void onFluidLevelChange()
	{
		if (world != null)
		{
			world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 0);
			world.notifyNeighborsOfStateChange(pos, Basin.basin, true);
		}
	}

	/*
	 * Fluid Handling
	 */
	public void onFluidLevelChanged(IFluidTank tank, FluidStack fluidDelta)
	{
		if (world != null)
		{
			world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 0);
			world.notifyNeighborsOfStateChange(pos, Basin.basin, true);
			//scheduleFluidConsume();
		}
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
		setPowered(compound.getBoolean("Powered"));
	}

	public void writeSyncedNBT(NBTTagCompound compound)
	{
		if (fluidTank.getFluid() != null)
		{
			NBTTagCompound fluidTag = new NBTTagCompound();
			fluidTank.getFluid().writeToNBT(fluidTag);
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

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
	{
		handleUpdateTag(pkt.getNbtCompound());
	}

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

	@Override
	public void handleUpdateTag(@Nonnull NBTTagCompound tag)
	{
		readSyncedNBT(tag);
	}

	/*
	 * Save data
	*/
	@Override
	public void readFromNBT(NBTTagCompound compound)
	{
		super.readFromNBT(compound);

		readSyncedNBT(compound);

		/*if (compound.hasKey("NextConsume"))
		{
			scheduleFluidConsume(compound.getInteger("NextConsume"));
		}
		else
		{
			endFluidConsume();
		}*/
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

		/*if (!fluidConsumeStopped)
		{
			compound.setInteger("NextConsume", ticksUntilNextFluidConsume);
		}*/

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
				onFluidLevelChanged(this, new FluidStack(resource.getFluid(), amountFilled));

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
				onFluidLevelChanged(this, drainedStack.copy());

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
