package squeek.veganoption.items;

import com.google.common.base.Preconditions;

import javax.annotation.Nullable;

import java.awt.Color;
import java.util.List;
import java.util.Locale;

import squeek.veganoption.VeganOption;
import squeek.veganoption.backpack.*;
import squeek.veganoption.gui.GuiBackpackBasic;
import squeek.veganoption.helpers.GuiHelper;
import squeek.veganoption.helpers.LangHelper;
import squeek.veganoption.gui.GuiBackpackAdvanced;
import net.minecraftforge.client.model.ModelLoader;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import net.minecraftforge.items.wrapper.PlayerOffhandInvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class ItemBackpack extends Item implements IItemColor
{
	public static enum BackpackMode 
	{
		NEUTRAL("neutral"),
		LOCKED("locked"),
		RECEIVE("receive"),
		RESUPPLY("resupply");
		
		public String modeInfo;
		BackpackMode(String info)  {this.modeInfo = info;}
	}
	public static enum BackpackType
	{
		BASIC(15),
		ADVANCED(45);
		
		public int storageSize;
		BackpackType(int storageSize) {this.storageSize = storageSize;}
	}
	
	public final int primaryColor;
	public final int secondaryColor;
	public final BackpackType type;
	public BackpackMode mode;

	public ItemBackpack() 
	{
		this(Color.WHITE.getRGB(), Color.BLACK.getRGB(), BackpackType.BASIC);
	}
	public ItemBackpack(Color primaryColor, Color secondaryColor, BackpackType type) 
	{
		this(primaryColor.getRGB(), secondaryColor.getRGB(), type);
	}
	public ItemBackpack(int primaryColor, int secondaryColor, BackpackType type) 
	{
		Preconditions.checkNotNull(type, "Missing backpack type. (Basic/Adanced)");
		
		this.setCreativeTab(VeganOption.creativeTab);
		setMaxStackSize(1);
		this.primaryColor = primaryColor;
		this.secondaryColor = secondaryColor;
		this.type = type;
		this.mode = BackpackMode.NEUTRAL;
	}
	
	@Override
	public boolean onDroppedByPlayer(ItemStack itemstack, EntityPlayer player)
	{
		if (itemstack != null && player instanceof EntityPlayerMP && player.openContainer instanceof ContainerBackpack) 
			player.closeScreen();

		return super.onDroppedByPlayer(itemstack, player);
	}

	@Override
	public boolean getShareTag() 
	{
		return true;
	}

	public void openGui(EntityPlayer entityplayer) 
	{
		BackpackGuiHandler.openGui(entityplayer, GuiHelper.GuiIds.BACKPACK.ordinal());
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) 
	{
		if (!worldIn.isRemote)
			if (!playerIn.isSneaking()) 
			{
				openGui(playerIn);
				ItemStack stack = playerIn.getHeldItem(handIn);
				return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
			}
			else 
			{
				ItemStack heldItem = playerIn.getHeldItem(handIn);
				int newMode = (mode.ordinal() + 1) == BackpackMode.values().length? 0 : mode.ordinal() + 1;
				mode = BackpackMode.values()[newMode];
				heldItem.setItemDamage(newMode);
				return ActionResult.newResult(EnumActionResult.SUCCESS, heldItem);
			}
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) 
	{
		if (getInventoryClicked(worldIn, pos, facing) != null)
			return EnumActionResult.SUCCESS;
		
		return EnumActionResult.FAIL;
	}

	@Override
	public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand)
	{
		if (player.isSneaking()) 
		{
			ItemStack heldItem = player.getHeldItem(hand);
			return evaluateClick(heldItem, player, world, pos, side) ? EnumActionResult.PASS : EnumActionResult.FAIL;
		}
		return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
	}
	
	public ItemBackpack getBackPack()
	{
		return this;
	}
	public int getPrimaryColor()
	{
		return primaryColor;
	}
	public int getSecondaryColor()
	{
		return secondaryColor;
	}
	
	@Override
	public String getItemStackDisplayName(ItemStack itemstack) 
	{
		if(type == BackpackType.BASIC)
			return LangHelper.translateToLocalFormatted("item.veganoption.backpackBasic.name", "basic");
		else
			return LangHelper.translateToLocalFormatted("item.veganoption.backpackAdvanced.name", "advanced");
	}

	@Nullable
	private static IItemHandler getInventoryClicked(World world, BlockPos pos, EnumFacing side) 
	{
		TileEntity tile = world.getTileEntity(pos);
		return getInventoryFromTile(tile, side);
	}

	//shift clicking with backpack on another container
	public boolean evaluateClick(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side) 
	{
		IItemHandler inventory = getInventoryClicked(world, pos, side);
		// Process only inventories
		if (inventory != null) 
		{
			// Must have inventory slots
			if (inventory.getSlots() <= 0) 
				return true;

			if (!world.isRemote) 
			{
				//current backpack inventory
				ItemInventory backpackInventory = new ItemInventory(player, getBackpackSize(), stack);

				BackpackMode mode = getMode(stack);
				switch(mode)
				{
					case RECEIVE: //recieve all items from container already in backpack (not full stacks)
						return receiveFromInventory(backpackInventory, inventory);
					case RESUPPLY: //add items from the backpack to a container with the same items (not full stacks)
						return resupplyInventory(backpackInventory, inventory);
					case NEUTRAL: //take all items from the container
						return transferToBackpack(backpackInventory, inventory);
					case LOCKED: //send all items to the container
						return transferToInventory(backpackInventory, inventory);
					default:
				}	
			}
			return true;
		}

		return false;
	}

	private static boolean resupplyInventory(ItemInventory backpackInventory, IItemHandler inventory) 
	{
		return ItemInventoryHandler.replenish(inventory, backpackInventory.getItemHandler());
	}
	
	private static boolean transferToInventory(ItemInventory backpackInventory, IItemHandler inventory) 
	{
		return ItemInventoryHandler.transfer(backpackInventory.getItemHandler(), inventory); 
	}
	
	private static boolean transferToBackpack(ItemInventory backpackInventory, IItemHandler inventory) 
	{
		return ItemInventoryHandler.transfer(inventory, backpackInventory.getItemHandler()); 
	}

	private boolean receiveFromInventory(ItemInventory backpackInventory, IItemHandler inventory) 
	{
		return ItemInventoryHandler.transfer(inventory, backpackInventory.getItemHandler()); 
	}

	public int getBackpackSize() 
	{
		return type.storageSize;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemstack, EntityPlayer player, List<String> list, boolean flag)
	{
		super.addInformation(itemstack, player, list, flag);

		int slotsUsed = ItemInventory.getOccupiedSlotCount(itemstack);

		String modeName = getMode(itemstack).modeInfo;
		list.add(Character.toUpperCase(modeName.charAt(0)) + modeName.substring(1, modeName.length()));
		list.add(slotsUsed + " / " + getBackpackSize());
	}

	@SideOnly(Side.CLIENT)
	public static ModelResourceLocation[] models;

	@SideOnly(Side.CLIENT)
	public static void registerModel(Item item) 
	{
		String typeTag = "backpack/" + ((ItemBackpack)item).type.toString().toLowerCase(Locale.ENGLISH);
		models = new ModelResourceLocation[4];
		models[0] = new ModelResourceLocation("veganoption:" + typeTag + "_neutral", "inventory");
		models[1] = new ModelResourceLocation("veganoption:" + typeTag + "_locked", "inventory");
		models[2] = new ModelResourceLocation("veganoption:" + typeTag + "_receive", "inventory");
		models[3] = new ModelResourceLocation("veganoption:" + typeTag + "_resupply", "inventory");
		ModelBakery.registerItemVariants(item, new ResourceLocation("veganoption:" + typeTag + "_neutral"));
		ModelBakery.registerItemVariants(item, new ResourceLocation("veganoption:" + typeTag + "_locked"));
		ModelBakery.registerItemVariants(item, new ResourceLocation("veganoption:" + typeTag + "_receive"));
		ModelBakery.registerItemVariants(item, new ResourceLocation("veganoption:" + typeTag + "_resupply"));
		ModelLoader.setCustomMeshDefinition(item, new BackpackMeshDefinition());
	}

	@SideOnly(Side.CLIENT)
	private static class BackpackMeshDefinition implements ItemMeshDefinition 
	{
		@Override
		public ModelResourceLocation getModelLocation(ItemStack stack) 
		{
			BackpackMode mode = getMode(stack);
			return models[mode.ordinal()];
		}
	}

	@Override
	public int getColorFromItemstack(ItemStack itemstack, int tintToChange) 
	{
		if (tintToChange == 0) 
			return this.primaryColor;
		else if (tintToChange == 1)
			return this.secondaryColor;
		else
			return 0xffffff; //change to black
	}

	public static BackpackMode getMode(ItemStack backpack) 
	{
		Preconditions.checkArgument(backpack.getItem() instanceof ItemBackpack, backpack.getDisplayName() + " is not a backpack.");
		return ((ItemBackpack)backpack.getItem()).mode;
	}
	
	/*
	 * Set an ItemBackpack to a BackpackMode
	 * ItemStack - The ItemBackpack to change.
	 * BackpackMode - The BackpackMode to change.
	 * Return - The BackpackMode prior to the change.
	 */
	public static BackpackMode setMode(ItemStack backpack,  BackpackMode mode)
	{
		Preconditions.checkArgument(backpack.getItem() instanceof ItemBackpack, backpack.getDisplayName() + " is not a backpack.");
		int meta = backpack.getItemDamage();
		backpack.setItemDamage(mode.ordinal());
		((ItemBackpack)backpack.getItem()).mode = mode;
		return BackpackMode.values()[meta];
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		Item oldItem = oldStack.getItem();
		Item newItem = newStack.getItem();
		return oldItem != newItem || getMode(oldStack) != getMode(newStack);
	}

	@Nullable
	@SideOnly(Side.CLIENT)
	public GuiContainer getGui(EntityPlayer player, ItemStack heldItem, int data) 
	{
		if (data != GuiHelper.GuiIds.BACKPACK.ordinal()) 
			return null;
		ContainerBackpack container = new ContainerBackpack(player, ContainerBackpack.Size.values()[type.ordinal()], heldItem);
		switch(type)
		{
			case BASIC:
				return new GuiBackpackBasic(container);
			case ADVANCED:
				return new GuiBackpackAdvanced(container);
			default:
				return null;
		}
	}

	@Nullable
	public Container getContainer(EntityPlayer player, ItemStack heldItem, int data) 
	{
		if (data != GuiHelper.GuiIds.BACKPACK.ordinal()) 
			return null;
		return new ContainerBackpack(player, ContainerBackpack.Size.values()[type.ordinal()], heldItem);
	}
	
	@Nullable
	public static IItemHandler getInventoryFromTile(@Nullable TileEntity tile, @Nullable EnumFacing side) 
	{
		if (tile == null) 
			return null;

		if (tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)) 
			return tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
		
		if (tile instanceof ISidedInventory) 
			return new SidedInvWrapper((ISidedInventory) tile, side);

		if (tile instanceof IInventory)
			return new InvWrapper((IInventory) tile);

		return null;
	}
	
	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
	{
		super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
		
		if(stack.isEmpty() || !(stack.getItem() instanceof ItemBackpack))
			return;
		if(mode == BackpackMode.LOCKED || mode == BackpackMode.RECEIVE)
			return;
		if(!(entityIn instanceof EntityPlayer))
			return;
		EntityPlayer player = (EntityPlayer)entityIn; 
		InventoryPlayer playerInventory = player.inventory;
		
		if(player.openContainer instanceof ContainerBackpack)
			if(((ContainerBackpack)player.openContainer).inventory.isParentItemInventory(stack))
				return;
		
		PlayerMainInvWrapper mainInventory = new PlayerMainInvWrapper(playerInventory); //without armor
		PlayerOffhandInvWrapper offhandInventory = new PlayerOffhandInvWrapper(playerInventory);
		ItemInventory backpackInventory = new ItemInventory(player, getBackpackSize(), stack);
		boolean inventoryChanged = false;
		if(mode == BackpackMode.RECEIVE)
		{
			inventoryChanged = ItemInventoryHandler.replenish(backpackInventory.getItemHandler(), mainInventory);
			inventoryChanged = ItemInventoryHandler.replenish(backpackInventory.getItemHandler(), offhandInventory);
			player.openContainer.detectAndSendChanges();
		}
		else if(mode == BackpackMode.RESUPPLY)
		{
			inventoryChanged = ItemInventoryHandler.replenish(mainInventory,backpackInventory.getItemHandler());
			inventoryChanged = ItemInventoryHandler.replenish(offhandInventory, backpackInventory.getItemHandler());
			player.openContainer.detectAndSendChanges();
		}
	}

}

