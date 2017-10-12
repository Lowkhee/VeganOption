package squeek.veganoption.backpack;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import squeek.veganoption.backpack.ItemInventory;

public class ContainerBackpack extends Container 
{
	public final ItemInventory inventory;
	public enum Size 
	{
		BASIC(3, 5, 44, 19),
		ADVANCED(5, 9, 8, 8);

		final int rows;
		final int columns;
		final int startX;
		final int startY;

		Size(int rows, int columns, int startX, int startY) 
		{
			this.rows = rows;
			this.columns = columns;
			this.startX = startX;
			this.startY = startY;
		}

		public int getSize() 
		{
			return rows * columns;
		}
	}
	
	public ContainerBackpack(ItemInventory inventory, InventoryPlayer playerInventory, int xInv, int yInv) 
	{
		this.inventory = inventory;
		addPlayerInventory(playerInventory, xInv, yInv);
	}

	public ContainerBackpack(EntityPlayer player, Size size, ItemStack parent) 
	{
		inventory = new ItemInventory(player, size.getSize(), parent);
		addPlayerInventory(player.inventory, 8, 11 + size.startY + size.rows * 18);
		// Inventory
		for (int j = 0; j < size.rows; j++) 
		{
			for (int k = 0; k < size.columns; k++) 
			{
				int slot = k + j * size.columns;
				addSlotToContainer(new BackpackSlot(inventory, slot, size.startX + k * 18, size.startY + j * 18));
			}
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) 
	{
		return inventory.isUsableByPlayer(playerIn);
	}

	public ItemInventory getItemInventory() 
	{
		return inventory;
	}
	
	/*
	 * All inventories (backpack, storage, hotbar) become a single inventory
	 */
	public void addPlayerInventory(InventoryPlayer playerInventory, int xInv, int yInv) 
	{
		// Player inventory
		for (int row = 0; row < 3; row++) 
			for (int column = 0; column < 9; column++)
				addSlotToContainer(new Slot(playerInventory, column + row * 9 + 9, xInv + column * 18, yInv + row * 18));
		
		// Player hotbar
		for (int column = 0; column < 9; column++)
			addHotbarSlot(playerInventory, column, xInv + column * 18, yInv + 58);
	}

	public void addHotbarSlot(InventoryPlayer playerInventory, int slot, int x, int y) 
	{
		addSlotToContainer(new Slot(playerInventory, slot, x, y));
	}

	@Override
	public Slot addSlotToContainer(Slot p_75146_1_) 
	{
		return super.addSlotToContainer(p_75146_1_);
	}

	@Override
	public ItemStack slotClick(int slotId, int dragType_or_button, ClickType clickTypeIn, EntityPlayer player) 
	{
		if (clickTypeIn == ClickType.SWAP && dragType_or_button >= 0 && dragType_or_button < 9) 
		{
			// hotkey used to move item from slot to hotbar
			int hotbarSlotIndex = PLAYER_STORAGE_SIZE + dragType_or_button;
			Slot hotbarSlot = getSlot(hotbarSlotIndex);
			if (hotbarSlot instanceof BackpackSlot)
				return ItemStack.EMPTY;
		}

		Slot slot = slotId < 0 ? null : getSlot(slotId);
		if (slot instanceof BackpackSlot) 
		{
			BackpackSlot slotForestry = (BackpackSlot) slot;
			if (slotForestry.isPhantom()) 
				return slotClickPhantom(slotForestry, dragType_or_button, clickTypeIn, player);
		}

		ItemStack result = super.slotClick(slotId, dragType_or_button, clickTypeIn, player);
		if (slotId > 0) 
			inventory.onSlotClick(inventorySlots.get(slotId).getSlotIndex(), player);
		return result;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) 
	{
		return takeInventoryStackInSlot(inventorySlots, player, slotIndex);
	}
	
	public static ItemStack slotClickPhantom(BackpackSlot slot, int mouseButton, ClickType clickTypeIn, EntityPlayer player) 
	{
		ItemStack stack = ItemStack.EMPTY;

		ItemStack stackSlot = slot.getStack();
		if (!stackSlot.isEmpty()) 
			stack = stackSlot.copy();

		if (mouseButton == 2) 
			fillPhantomSlot(slot, ItemStack.EMPTY, mouseButton);
		else if (mouseButton == 0 || mouseButton == 1) 
		{
			InventoryPlayer playerInv = player.inventory;

			ItemStack stackHeld = playerInv.getItemStack();

			if (stackSlot.isEmpty()) 
			{
				if (!stackHeld.isEmpty() && slot.isItemValid(stackHeld)) 
					fillPhantomSlot(slot, stackHeld, mouseButton);
			} 
			else if (stackHeld.isEmpty()) 
				adjustPhantomSlot(slot, mouseButton, clickTypeIn);
			else if (slot.isItemValid(stackHeld)) 
			{
				if (isIdenticalItem(stackSlot, stackHeld)) 
					adjustPhantomSlot(slot, mouseButton, clickTypeIn);
				else 
					fillPhantomSlot(slot, stackHeld, mouseButton);
			}
		} 
		else if (mouseButton == 5) 
		{
			InventoryPlayer playerInv = player.inventory;
			ItemStack stackHeld = playerInv.getItemStack();
			if (!slot.getHasStack()) 
				fillPhantomSlot(slot, stackHeld, mouseButton);
		}
		return stack;
	}
	
	public static void fillPhantomSlot(BackpackSlot slot, ItemStack stackHeld, int mouseButton) 
	{
		if (!slot.canAdjustPhantom()) 
			return;

		if (stackHeld.isEmpty()) 
		{
			slot.putStack(ItemStack.EMPTY);
			return;
		}

		int stackSize = mouseButton == 0 ? stackHeld.getCount() : 1;
		if (stackSize > slot.getSlotStackLimit())
			stackSize = slot.getSlotStackLimit();
		ItemStack phantomStack = stackHeld.copy();
		phantomStack.setCount(stackSize);

		slot.putStack(phantomStack);
	}
	
	public static void adjustPhantomSlot(BackpackSlot slot, int mouseButton, ClickType clickTypeIn) 
	{
		if (!slot.canAdjustPhantom()) 
			return;
		ItemStack stackSlot = slot.getStack();
		int stackSize;
		if (clickTypeIn == ClickType.QUICK_MOVE) 
			stackSize = mouseButton == 0 ? (stackSlot.getCount() + 1) / 2 : stackSlot.getCount() * 2;
		else
			stackSize = mouseButton == 0 ? stackSlot.getCount() - 1 : stackSlot.getCount() + 1;

		if (stackSize > slot.getSlotStackLimit())
			stackSize = slot.getSlotStackLimit();

		stackSlot.setCount(stackSize);

		slot.putStack(stackSlot);
	}
	
	public static boolean isIdenticalItem(ItemStack backpackA, ItemStack backpackB) 
	{
		if (backpackA == backpackB)
			return true;

		if (backpackA.isEmpty() || backpackB.isEmpty())
			return false;

		if (backpackA.getItem() != backpackB.getItem()) 
			return false;

		if (backpackA.getItemDamage() != OreDictionary.WILDCARD_VALUE) 
			if (backpackA.getItemDamage() != backpackB.getItemDamage()) 
				return false;

		return ItemStack.areItemStackTagsEqual(backpackA, backpackB);
	}
	
	/*
	 * Return - the taken stack in slot
	 */
	public static ItemStack takeInventoryStackInSlot(List<Slot> inventorySlots, EntityPlayer player, int slotIndex) 
	{
		Slot slot = inventorySlots.get(slotIndex);
		if (slot == null || !slot.getHasStack())
			return ItemStack.EMPTY;

		int numSlots = inventorySlots.size();
		ItemStack stackInSlot = slot.getStack();
		ItemStack originalStack = stackInSlot.copy();

		if (!isMovedToInventorySlot(inventorySlots, stackInSlot, slotIndex, numSlots)) 
			return ItemStack.EMPTY;

		slot.onSlotChange(stackInSlot, originalStack);
		if (stackInSlot.isEmpty())
			slot.putStack(ItemStack.EMPTY);
		else
			slot.onSlotChanged();

		if (stackInSlot.getCount() == originalStack.getCount()) 
			return ItemStack.EMPTY;

		slot.onTake(player, stackInSlot);
		return originalStack;
	}
	
	public static boolean isMovedToInventorySlot(List<Slot> inventorySlots, ItemStack stackInSlot, int slotIndex, int numSlots) 
	{
		if (slotIndex < PLAYER_INVENTORY_SIZE) 
			if (slotIndex >= PLAYER_STORAGE_SIZE) //is in hotbar
				return shiftClickWithinRange(inventorySlots, stackInSlot, 0, PLAYER_STORAGE_SIZE);
			else
				return shiftClickWithinRange(inventorySlots, stackInSlot, PLAYER_STORAGE_SIZE, PLAYER_HOTBAR_SIZE);
	
		return shiftClickToInventory(inventorySlots, stackInSlot); //outside inventory
	}
	/*
	 * Try to move item into inventory, which is not in the hotbar or storage
	 */
	public static boolean shiftClickToInventory(List<Slot> inventorySlots, ItemStack stackInSlot) 
	{
		boolean shifted = shiftClickMerge(inventorySlots, stackInSlot, PLAYER_STORAGE_SIZE, PLAYER_HOTBAR_SIZE);
		shifted |= shiftClickMerge(inventorySlots, stackInSlot, 0, PLAYER_STORAGE_SIZE);

		shifted |= shiftClickOpenSlots(inventorySlots, stackInSlot, PLAYER_STORAGE_SIZE, PLAYER_HOTBAR_SIZE);
		shifted |= shiftClickOpenSlots(inventorySlots, stackInSlot, 0, PLAYER_STORAGE_SIZE);
		return shifted;
	}
	
	public static boolean shiftClickWithinRange(List<Slot> inventorySlots, ItemStack stackToShift, int start, int count) 
	{
		boolean changed = shiftClickMerge(inventorySlots, stackToShift, start, count);
		changed |= shiftClickOpenSlots(inventorySlots, stackToShift, start, count);
		return changed;
	}
	
	public static boolean shiftClickMerge(List<Slot> inventorySlots, ItemStack stackToShift, int start, int count) 
	{
		if (!stackToShift.isStackable() || stackToShift.isEmpty()) 
			return false;

		boolean changed = false;
		for (int slotIndex = start; !stackToShift.isEmpty() && slotIndex < start + count; slotIndex++) 
		{
			Slot slot = inventorySlots.get(slotIndex);
			ItemStack stackInSlot = slot.getStack();
			if (!stackInSlot.isEmpty() && isIdenticalItem(stackInSlot, stackToShift)) 
			{
				int resultingStackSize = stackInSlot.getCount() + stackToShift.getCount();
				int max = Math.min(stackToShift.getMaxStackSize(), slot.getSlotStackLimit());
				if (resultingStackSize <= max) 
				{
					stackToShift.setCount(0);
					stackInSlot.setCount(resultingStackSize);
					slot.onSlotChanged();
					changed = true;
				} 
				else if (stackInSlot.getCount() < max) 
				{
					stackToShift.shrink(max - stackInSlot.getCount());
					stackInSlot.setCount(max);
					slot.onSlotChanged();
					changed = true;
				}
			}
		}
		return changed;
	}
	
	public static boolean shiftClickOpenSlots(List<Slot> inventorySlots, ItemStack stackToShift, int start, int count) 
	{
		if (stackToShift.isEmpty()) 
			return false;

		boolean changed = false;
		for (int slotIndex = start; !stackToShift.isEmpty() && slotIndex < start + count; slotIndex++) 
		{
			Slot slot = inventorySlots.get(slotIndex);
			ItemStack stackInSlot = slot.getStack();
			if (stackInSlot.isEmpty()) 
			{
				int max = Math.min(stackToShift.getMaxStackSize(), slot.getSlotStackLimit());
				stackInSlot = stackToShift.copy();
				stackInSlot.setCount(Math.min(stackToShift.getCount(), max));
				stackToShift.shrink(stackInSlot.getCount());
				slot.putStack(stackInSlot);
				slot.onSlotChanged();
				changed = true;
			}
		}
		return changed;
	}
	
	public static final int PLAYER_STORAGE_SIZE = 27; //0-26
	public static final int PLAYER_HOTBAR_SIZE = 9; //hotbar starts at the end of storage - 27-35 
	public static final int PLAYER_INVENTORY_SIZE = PLAYER_HOTBAR_SIZE + PLAYER_STORAGE_SIZE; //0-35
}

