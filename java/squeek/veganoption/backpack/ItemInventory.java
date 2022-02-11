package squeek.veganoption.backpack;

import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public class ItemInventory  implements IInventory, ICapabilityProvider
{

	private static final String SLOTS = "Slots";
	private static final String UID = "unique_id";
	private static final Random rand = new Random();

	private final IItemHandler itemHandler = new InvWrapper(this);

	public final EntityPlayer player;
	private final ItemStack parentItem;
	private final NonNullList<ItemStack> inventoryItemStacks;

	public ItemInventory(EntityPlayer player, int size, ItemStack parent) 
	{
		Preconditions.checkArgument(!parent.isEmpty(), "Parent cannot be empty.");

		this.player = player;
		this.parentItem = parent;
		this.inventoryItemStacks = NonNullList.withSize(size, ItemStack.EMPTY);

		NBTTagCompound nbt = parent.getTagCompound();
		if (nbt == null)
		{
			nbt = new NBTTagCompound();
			parent.setTagCompound(nbt);
		}
		setUID(nbt); // Set a uid to identify the itemStack on SMP

		NBTTagCompound nbtSlots = nbt.getCompoundTag(SLOTS);
		for (int i = 0; i < inventoryItemStacks.size(); i++)
		{
			String slotKey = getSlotNBTKey(i);
			if (nbtSlots.hasKey(slotKey)) 
			{
				NBTTagCompound itemNbt = nbtSlots.getCompoundTag(slotKey);
				ItemStack itemStack = new ItemStack(itemNbt);
				inventoryItemStacks.set(i, itemStack);
			} 
			else 
				inventoryItemStacks.set(i, ItemStack.EMPTY);
		}
	}

	public static int getOccupiedSlotCount(ItemStack itemStack)
	{
		NBTTagCompound nbt = itemStack.getTagCompound();
		if (nbt == null)
			return 0;

		NBTTagCompound slotNbt = nbt.getCompoundTag(SLOTS);
		return slotNbt.getKeySet().size();
	}

	private void setUID(NBTTagCompound nbt) 
	{
		if (!nbt.hasKey(UID)) 
			nbt.setInteger(UID, rand.nextInt());
	}

	public boolean isParentItemInventory(ItemStack itemStack) 
	{
		ItemStack parent = getParent();
		return hasSameItemInventory(parent, itemStack);
	}

	public ItemStack getParent() 
	{
		for (EnumHand hand : EnumHand.values()) 
		{
			ItemStack held = player.getHeldItem(hand);
			if (hasSameItemInventory(held, parentItem)) 
				return held;
		}
		return parentItem;
	}

	public static boolean hasSameItemInventory(ItemStack itemStackA, ItemStack itemStackB) 
	{
		if(itemStackA.isEmpty() || itemStackB.isEmpty()) 
			return false;

		if(itemStackA.getItem() != itemStackB.getItem()) 
			return false;

		NBTTagCompound baseTagCompound = itemStackA.getTagCompound();
		NBTTagCompound comparisonTagCompound = itemStackB.getTagCompound();
		if(baseTagCompound == null || comparisonTagCompound == null) 
			return false;

		if(!baseTagCompound.hasKey(UID) || !comparisonTagCompound.hasKey(UID)) 
			return false;

		int baseUID = baseTagCompound.getInteger(UID);
		int comparisonUID = comparisonTagCompound.getInteger(UID);
		return baseUID == comparisonUID;
	}

	private void writeToParentNBT() 
	{
		ItemStack parent = getParent();

		NBTTagCompound nbt = parent.getTagCompound();
		if (nbt == null) 
		{
			nbt = new NBTTagCompound();
			parent.setTagCompound(nbt);
		}

		NBTTagCompound slotsNbt = new NBTTagCompound();
		for (int i = 0; i < getSizeInventory(); i++) 
		{
			ItemStack itemStack = getStackInSlot(i);
			if (!itemStack.isEmpty()) 
			{
				String slotKey = getSlotNBTKey(i);
				NBTTagCompound itemNbt = new NBTTagCompound();
				itemStack.writeToNBT(itemNbt);
				slotsNbt.setTag(slotKey, itemNbt);
			}
		}

		nbt.setTag(SLOTS, slotsNbt);
	}

	private static String getSlotNBTKey(int i) 
	{
		return Integer.toString(i, Character.MAX_RADIX);
	}

	public void onSlotClick(int slotIndex, EntityPlayer player) 
	{
	}

	@Override
	public boolean isEmpty() 
	{
		for (ItemStack itemstack : this.inventoryItemStacks) 
			if (!itemstack.isEmpty())
				return false;

		return true;
	}

	@Override
	public ItemStack decrStackSize(int index, int count) 
	{
		ItemStack itemstack = ItemStackHelper.getAndSplit(this.inventoryItemStacks, index, count);

		if (!itemstack.isEmpty())
			this.markDirty();

		return itemstack;
	}

	public void setInventorySlotContents(int index, ItemStack itemstack) 
	{
		inventoryItemStacks.set(index, itemstack);

		ItemStack parent = getParent();

		NBTTagCompound nbt = parent.getTagCompound();
		if (nbt == null) 
		{
			nbt = new NBTTagCompound();
			parent.setTagCompound(nbt);
		}

		NBTTagCompound slotNbt;
		if (!nbt.hasKey(SLOTS)) 
		{
			slotNbt = new NBTTagCompound();
			nbt.setTag(SLOTS, slotNbt);
		} 
		else
			slotNbt = nbt.getCompoundTag(SLOTS);

		String slotKey = getSlotNBTKey(index);

		if (itemstack.isEmpty())
			slotNbt.removeTag(slotKey);
		else 
		{
			NBTTagCompound itemNbt = new NBTTagCompound();
			itemstack.writeToNBT(itemNbt);

			slotNbt.setTag(slotKey, itemNbt);
		}
	}

	@Override
	public ItemStack getStackInSlot(int i) 
	{
		return inventoryItemStacks.get(i);
	}

	@Override
	public int getSizeInventory() 
	{
		return inventoryItemStacks.size();
	}

	@Override
	public String getName() 
	{
		return "Vegan Backpack";
	}

	@Override
	public ITextComponent getDisplayName() 
	{
		return new TextComponentString(getName());
	}

	@Override
	public int getInventoryStackLimit() 
	{
		return 64;
	}

	@Override
	public final void markDirty() 
	{
		writeToParentNBT();
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer entityplayer) 
	{
		return true;
	}

	@Override
	public boolean hasCustomName() 
	{
		return true;
	}

	@Override
	public boolean isItemValidForSlot(int slotIndex, ItemStack itemStack)
	{
		return canSlotAccept(slotIndex, itemStack);
	}

	@Override
	public void openInventory(EntityPlayer player) 
	{
	}

	@Override
	public void closeInventory(EntityPlayer player) 
	{
	}

	@Override
	public ItemStack removeStackFromSlot(int slot) 
	{
		ItemStack removedStack = getStackInSlot(slot);

		if (!removedStack.isEmpty()) {
			setInventorySlotContents(slot, ItemStack.EMPTY);
		}

		return removedStack;
	}

	public boolean canSlotAccept(int slotIndex, ItemStack itemStack) 
	{
		return true;
	}

	public boolean isLocked(int slotIndex) 
	{
		return false;
	}

	/* Fields */
	@Override
	public int getField(int id) 
	{
		return 0;
	}

	@Override
	public int getFieldCount() 
	{
		return 0;
	}

	@Override
	public void clear() 
	{
	}

	@Override
	public void setField(int id, int value) 
	{
	}

	@Override
	@Nullable
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) 
	{
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemHandler);

		return null;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) 
	{
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
	}

	public IItemHandler getItemHandler() 
	{
		return itemHandler;
	}


}