package squeek.veganoption.helpers;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import squeek.veganoption.blocks.tiles.TileEntityBasin;
import squeek.veganoption.content.modules.Ender;
import squeek.veganoption.content.registry.RelationshipRegistry;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import java.lang.reflect.Method;
import java.util.List;


public class FluidContainerHelper
{
	private static boolean eventCanceled = false;
	
	public static void init()
	{
		MinecraftForge.EVENT_BUS.register(new FluidContainerHelper());
	}

	@SuppressWarnings("deprecation")
	protected static Method rayTraceMethod = ReflectionHelper.findMethod(Item.class, null, new String[]{"rayTrace", "func_77621_a", "a"}, World.class, EntityPlayer.class, boolean.class);

	/*
	// fix non-water fluids being able to create water buckets
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onFillBucket(FillBucketEvent event)
	{
		if (event.isCanceled() || event.getResult() != Event.Result.DEFAULT)
			return;

		if (event.getTarget() == null)
			return;

		Block block = event.getWorld().getBlockState(event.getTarget().getBlockPos()).getBlock();

		// if we've gotten this far, then it shouldn't be able to be picked up by a bucket
		// ItemBucketGeneric would have handled it if it was possible to pick it up
		// this stops BlockFluidGenerics creating water buckets if they don't have a bucket item
		if (block instanceof BlockFluidClassic)
		{
			event.setCanceled(true);
			event.setResult(Event.Result.DENY);
		}
	}*/

	// all this just for picking up generic fluids with a glass bottle
	// and fixing non-water fluids being able to create water bottles
	//
	// note: this *could* be expanded to support all containers registered in the FluidContainerRegistry,
	// but that is likely to cause unwanted behavior due to containers being registered
	// that are only intended to be filled via specific non-right-click methods (ex: TE florbs)
	@SubscribeEvent
	public void onPlayerInteract(PlayerInteractEvent.RightClickBlock event)
	{
		if (event.isCanceled() || event.getResult() != Event.Result.DEFAULT)
			return;
		
		boolean cancelEvent = false;
		EntityPlayer player = event.getEntityPlayer();
		EnumHand hand = event.getHand();
		ItemStack itemStackEvent = event.getItemStack();
		ItemStack heldItemStack = player.getHeldItem(hand);
		Item heldItem = heldItemStack.getItem();
		
		World world = event.getWorld();
		RayTraceResult rayTraceResult = null;
		try
		{
			rayTraceResult = (RayTraceResult) rayTraceMethod.invoke(itemStackEvent.getItem(), world, player, true);
		}
		catch (RuntimeException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		if (rayTraceResult == null)
			return;
		if (rayTraceResult.typeOfHit != RayTraceResult.Type.BLOCK)
			return;
		BlockPos pos = rayTraceResult.getBlockPos();
		Block blockTarget = world.getBlockState(pos).getBlock();
		TileEntity tile = world.getTileEntity(pos);
		TileEntityBasin tileBasin = tile instanceof TileEntityBasin ? (TileEntityBasin)tile : null;
		
		//Glass Bottle Handling - only Items with container glassbottle and registered in RelationshipRegistry are usable on the basin
		if (!heldItemStack.isEmpty() && (heldItem.getContainerItem() == Items.GLASS_BOTTLE || heldItem == Items.GLASS_BOTTLE))//	&& world.canMineBlockBody(event.getEntityPlayer(), pos) && event.getEntityPlayer().canPlayerEdit(pos, rayTraceResult.sideHit, heldItem))
		{
			Block blockItem = BlockHelper.getBlockFromItemName(heldItem.delegate.name(), null);
			
			if(tileBasin != null && tileBasin.isOpen() && blockItem != null)
			{
				if(blockItem == Blocks.AIR && !tileBasin.isBasinEmpty() && tileBasin.getBasinTankAmount() >= Fluid.BUCKET_VOLUME)
				{
					blockTarget = tileBasin.fluidTank.getFluid().getFluid().getBlock();
					List<ItemStack> itemStackList = RelationshipRegistry.getChildren(new ItemStack(blockTarget));
					Item itemBottle = null;
					for(ItemStack itemStack : itemStackList) 
						if(itemStack.getItem().getContainerItem() == Items.GLASS_BOTTLE)
							itemBottle = itemStack.getItem();
					if(itemBottle != null)
					{
						tryAddItemToInventory(player, hand, new ItemStack(itemBottle, 1));
						player.getHeldItem(hand).splitStack(1);
						tileBasin.fluidTank.drain(Fluid.BUCKET_VOLUME, true);
					}
					else
						cancelEvent = true;
				}
				else if(blockItem != Blocks.AIR && (tileBasin.isBasinEmpty() || (tileBasin.getBasinTankEmptyCapacity() >= Fluid.BUCKET_VOLUME && FluidRegistry.lookupFluidForBlock(blockItem) == tileBasin.getBasinTankFluid().getFluid())))
				{
					tryAddItemToInventory(player, hand, new ItemStack(Items.GLASS_BOTTLE, 1));
					player.getHeldItem(hand).splitStack(1);
					tileBasin.fluidTank.fill(new FluidStack(FluidRegistry.lookupFluidForBlock(blockItem), Fluid.BUCKET_VOLUME), true);
				}
			
			//if bottle is full and block solid and side clicked is air, dump contents
			
			//pickup fluid blocks with bottle
			}
			else if(blockTarget instanceof BlockFluidClassic)
			{
				List<ItemStack> itemStackList = RelationshipRegistry.getChildren(new ItemStack(blockTarget));
				Item itemBottle = null;
				for(ItemStack itemStack : itemStackList) 
					if(itemStack.getItem().getContainerItem() == Items.GLASS_BOTTLE)
						itemBottle = itemStack.getItem();
				if(itemBottle != null)
				{
					tryAddItemToInventory(player, hand, new ItemStack(itemBottle, 1));
					player.getHeldItem(hand).splitStack(1); //setCount(player.getHeldItem(hand).getCount() - 1);
					world.setBlockToAir(pos);
				}
				else
					cancelEvent = true;
				
			}
			//cancel bottle pickup on finite fluids
			else if(blockTarget instanceof BlockFluidFinite)
				if(((BlockFluidFinite)blockTarget).getFluid() == Ender.fluidRawEnder)
					cancelEvent = true;
			//vanilla fluids are handled already
		}
		else if(!heldItemStack.isEmpty() && tileBasin != null  && tileBasin.isOpen() && FluidContainerHelper.isItemFluidContainer(heldItemStack))
		{
			IFluidHandler fluidHandlerBasin  = (IFluidHandler)tileBasin.getBasinTank();
			IFluidHandler containerCap = FluidUtil.getFluidHandler(heldItemStack);
			if (containerCap == null)
				return;
			for (IFluidTankProperties tankProp : containerCap.getTankProperties())
			{
				FluidStack containerFluid = tankProp.getContents();
				if (containerFluid != null && tileBasin.getBasinTank().fill(containerFluid, false) == containerFluid.amount)
				{
					ItemStack toDrain = event.getItemStack().splitStack(1);
					toDrain = FluidContainerHelper.drainContainerIntoHandler(toDrain, fluidHandlerBasin);
					if(toDrain != null)
						tryAddItemToInventory(player, hand, toDrain);
					return;
				} 
				else if (tileBasin.getBasinTank().getFluidAmount() > 0)
				{
					ItemStack toFill = event.getItemStack().splitStack(1);
					toFill = FluidContainerHelper.drainHandlerIntoContainer(tileBasin.getBasinTank(), tileBasin.getBasinTank().getFluid(), toFill);
					if(toFill != null)
						tryAddItemToInventory(player, hand, toFill);
					return;
				}
			}
		}
		else if(!heldItemStack.isEmpty() && tileBasin != null)
		{
			Block blockItem = Block.getBlockFromItem(heldItem);
			if(blockItem == Blocks.TORCH || blockItem == Blocks.REDSTONE_TORCH)
				cancelEvent = true;
		}
		if(cancelEvent)
		{
			eventCanceled = true;
			event.setCanceled(true);
			event.setResult(Event.Result.DENY);
		}
	}
	
	/**
	 * Attempts to add an item to the player's inventory in the following order:
	 * 1. Their current hand if there is no held item, or the held item has a stack size of 0
	 * 2. The first open slot in their inventory
	 * 3. Dropped in the world, if there is no free slot in the inventory.
	 */
	public static void tryAddItemToInventory(EntityPlayer player, EnumHand hand, ItemStack newItem)
	{
		ItemStack heldItem = player.getHeldItem(hand);
		if (heldItem.isEmpty())
		{
			player.setHeldItem(hand, newItem);
			return;
		}
		if (!player.inventory.addItemStackToInventory(newItem))
			player.dropItem(newItem, false);
	}
	
	//Prevents an Item in the right hand from being used (such as a glass bottle)
	@SubscribeEvent
    public void playerRightClickItem(PlayerInteractEvent.RightClickItem event)
    {
		if(eventCanceled)
		{
			event.setCanceled(true);
			eventCanceled = false;
		}
    }

	public static boolean isFluidContainer(ItemStack container)
	{
		return container != null && (container.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null));
	}
	
	/*
	 * Buckets/UniversalBucket
	 */
	public static boolean isItemFluidContainer(ItemStack container)
	{
		return container != null && container.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
	}

	public static boolean isEmptyContainer(ItemStack container)
	{
		if (isFluidContainer(container))
		{
			IFluidHandler handler = FluidUtil.getFluidHandler(container);
			if (handler != null)
			{
				for (IFluidTankProperties prop : handler.getTankProperties())
				{
					if (prop.getContents() != null)
						return prop.getContents().amount <= 0;
				}
			}
		}
		return false;
	}

	public static int fillContainer(FluidStack fluid, ItemStack into)
	{
		if (isItemFluidContainer(into) || isFluidContainer(into))
		{
			IFluidHandler intoCapability = FluidUtil.getFluidHandler(into);
			if (intoCapability != null)
				return intoCapability.fill(fluid, true);
		}
		return 0;
	}

	public static ItemStack drainHandlerIntoContainer(IFluidHandler from, FluidStack toFill, ItemStack into)
	{
		if (isItemFluidContainer(into) || isFluidContainer(into))
		{
			IFluidHandler intoCapability = FluidUtil.getFluidHandler(into);
			if (intoCapability != null)
			{
				from.drain(intoCapability.fill(toFill, true), true);
				return ((IFluidHandlerItem)intoCapability).getContainer();
			}
		}
		return null;
	}

	public static ItemStack drainContainerIntoHandler(ItemStack from, IFluidHandler into)
	{
		if (isItemFluidContainer(from) || isFluidContainer(from))
		{
			IFluidHandler fromCapability = FluidUtil.getFluidHandler(from);
			if (fromCapability != null)
			{
				for (IFluidTankProperties fromTank : fromCapability.getTankProperties())
					fromCapability.drain(into.fill(fromTank.getContents(), true), true);
				return ((IFluidHandlerItem)fromCapability).getContainer();
			}
		}
		return null;
	}
}
