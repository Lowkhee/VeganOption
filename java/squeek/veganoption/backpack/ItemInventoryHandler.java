package squeek.veganoption.backpack;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class ItemInventoryHandler
{
	private final IItemHandler inventory;

	public ItemInventoryHandler(IItemHandler inventory) 
	{
		this.inventory = inventory;
	}
	

	public boolean transferFromBackpack(IItemHandler toInventory, boolean singleStack) 
	{
		return transfer(inventory, toInventory, singleStack);
	}

	public boolean transferToBackpack(IItemHandler fromInventory, boolean singleStack) 
	{
		return transfer(fromInventory, inventory, singleStack);
	}
	
	public static boolean transfer(IItemHandler fromInventory, IItemHandler toInventory) 
	{
		return transfer(fromInventory, toInventory, false, false);
	}
	
	public static boolean transfer(IItemHandler fromInventory, IItemHandler toInventory, boolean singleStack) 
	{
		return transfer(fromInventory, toInventory, singleStack, false);
	}

	public static boolean transfer(IItemHandler fromInventory, IItemHandler toInventory, boolean singleStack, boolean simulate) 
	{
		boolean boolMovedItems = false;
		if(fromInventory instanceof IInventory)
			if(((IInventory)fromInventory).isEmpty())
				return boolMovedItems;
		int inventorySize = fromInventory.getSlots();
		for (int index = 0; index < inventorySize; index++) 
		{
			ItemStack targetStack = fromInventory.extractItem(index, Integer.MAX_VALUE, true);
			if (!targetStack.isEmpty()) 
			{
				int targetStackSize = targetStack.getCount();
				int invDestSize = toInventory.getSlots();
				ItemStack remainingStack = null;
				for(int indexDest = 0; indexDest < invDestSize; indexDest++)
				{																			
					remainingStack = toInventory.insertItem(indexDest, targetStack, simulate); 
					if(remainingStack.isEmpty())
						break;
				}
				if(remainingStack != null)
					targetStackSize -= remainingStack.getCount();
				if(targetStackSize > 0)
				{
					fromInventory.extractItem(index, targetStackSize, simulate);
					boolMovedItems = true;
					if(singleStack)
						return true;
				}
			}
		}
		
		return boolMovedItems;
	}
	
	/*
	 * Only transfers same items till those stacks are full
	 * Return - true if any item is transfered
	 */
	public static boolean replenishInvetories(IInventory thisInventory, IInventory withThisInventory)
	{
		boolean boolMovedItems = false;
		
		if(thisInventory.isEmpty() || withThisInventory.isEmpty())
			return boolMovedItems;
		
		int withInventorySize = withThisInventory.getSizeInventory();
		for (int indexWith = 0; indexWith < withInventorySize; indexWith++) 
		{
			ItemStack moveStack = withThisInventory.getStackInSlot(indexWith);
			if (!moveStack.isEmpty()) 
			{
				int thisInventorySize = thisInventory.getSizeInventory();
				ItemStack remainingStack = null;
				for(int indexThis = 0; indexThis < thisInventorySize; indexThis++)
				{			
					ItemStack replenishStack = thisInventory.getStackInSlot(indexThis);
					int thisStackSize = replenishStack.getCount();
					int thisstackMaxSize = replenishStack.getMaxStackSize();
					if(!replenishStack.isEmpty() && thisStackSize < thisstackMaxSize && moveStack.getItem().equals(replenishStack.getItem()))
					{
						int itemSpaceAvailable = thisstackMaxSize - thisStackSize;
						ItemStack stackMoved = withThisInventory.decrStackSize(indexWith, itemSpaceAvailable);
						replenishStack.setCount(thisStackSize + stackMoved.getCount());
						thisInventory.setInventorySlotContents(indexThis, replenishStack);
						boolMovedItems = true;
					}
					if(withThisInventory.getStackInSlot(indexWith).isEmpty())
						break;
				}
			}
		}
		return boolMovedItems;
	}
	
	/*
	 * Only transfers same items till those stacks are full
	 * Return - true is any item is transfered
	 */
	public static boolean replenish(IItemHandler thisInventory, IItemHandler withThisInventory)
	{
		boolean boolMovedItems = false;
		if(withThisInventory instanceof IInventory)
			if(((IInventory)withThisInventory).isEmpty())
				return boolMovedItems;
		int inventorySize = withThisInventory.getSlots();
		for (int indexWith = 0; indexWith < inventorySize; indexWith++) 
		{
			ItemStack moveStack = withThisInventory.extractItem(indexWith, Integer.MAX_VALUE, true);
			if (!moveStack.isEmpty()) 
			{
				int invDestSize = thisInventory.getSlots();
				ItemStack remainingStack = null;
				for(int indexThis = 0; indexThis < invDestSize; indexThis++)
				{			
					ItemStack replenishStack = thisInventory.extractItem(indexThis, Integer.MAX_VALUE, true);
					if(!replenishStack.isEmpty() && replenishStack.getCount() < replenishStack.getMaxStackSize() && moveStack.getItem().equals(replenishStack.getItem()))
					{
						remainingStack = thisInventory.insertItem(indexThis, moveStack, false); 
						if(remainingStack.getCount() != moveStack.getCount())
						{
							withThisInventory.extractItem(indexWith, moveStack.getCount() - remainingStack.getCount(), false);
							moveStack.setCount(remainingStack.getCount());
							boolMovedItems = true;
						}
					}
					if(moveStack.isEmpty())
						break;
				}
			}
		}
		return boolMovedItems;
	}

	/*
	 * Add an itemstack to an inventory
	 * Return - the portion of the itemstack not added to the inventory
	 */
	public ItemStack add(ItemStack stackToAdd, boolean simulate) 
	{
		return add(this.inventory, stackToAdd, simulate);
	}
	public static ItemStack add(IItemHandler inventory, ItemStack stackToAdd)
	{
		return add(inventory, stackToAdd, false);
	}
	public static ItemStack add(IItemHandler inventory, ItemStack stackToAdd, boolean simulate)
	{
		if(stackToAdd == null || stackToAdd.isEmpty())
			return ItemStack.EMPTY;
		
		int inventorySize = inventory.getSlots();
		for (int slot = 0; slot < inventorySize; slot++) 
		{
			stackToAdd = inventory.insertItem(slot, stackToAdd, simulate);
			if(stackToAdd.isEmpty())
				break;
		}
		
		return stackToAdd;
	}
	
	/*
	 * Add to current itemStacks only
	 * Return - the portion of the ItemStack not added
	 */
	public static ItemStack addToExisting(IItemHandler inventory, ItemStack stackToAdd)
	{
		return addToExisting(inventory, stackToAdd, false);
	}
	public static ItemStack addToExisting(IItemHandler inventory, ItemStack stackToAdd, boolean simulate)
	{
		if(stackToAdd == null || stackToAdd.isEmpty())
			return ItemStack.EMPTY;
		
		int inventorySize = inventory.getSlots();
		for (int slot = 0; slot < inventorySize; slot++) 
		{
			ItemStack slotItemStack = inventory.getStackInSlot(slot);
			if(!slotItemStack.isEmpty() && slotItemStack.getItem().equals(stackToAdd.getItem()))
				stackToAdd = inventory.insertItem(slot, stackToAdd, simulate);
			if(stackToAdd.isEmpty())
				break;
		}
		
		return stackToAdd; //remaining
	}
	
	/*
	 * Remove an Item
	 */
	public ItemStack remove(Item item)
	{
		return remove(item, 1);
	}
	public ItemStack remove(Item item, int amount) 
	{
		if(item == null)
			return ItemStack.EMPTY;
		return remove(new ItemStack(item, amount));
	}
	public ItemStack remove(ItemStack itemStack)
	{
		return remove(itemStack, itemStack.getCount());
	}
	/*
	 * Removes Items anywhere in the inventory until all slots checked or the amount of item is reached
	 * ItemStack - item to be removed
	 * amount - amount of the item in itemStack to remove
	 * Return - an itemstack of the items removed
	 */
	public ItemStack remove(ItemStack itemStack, int amount)
	{
		if(itemStack == null || itemStack.isEmpty())
			return ItemStack.EMPTY;
		int inventorySize = this.inventory.getSlots();
		ItemStack stackSlot = null;
		int amountNotTaken = amount;
		for(int i = 0; i < inventorySize; i++)
		{
			stackSlot = inventory.getStackInSlot(i);
			if(stackSlot != null && stackSlot.equals(itemStack)) //equal work?
			{ 
				amountNotTaken -= (inventory.extractItem(i, amountNotTaken, false)).getCount();
				if(amountNotTaken == 0)
					break;
			}
		}
		return new ItemStack(itemStack.getItem(), amount - amountNotTaken);
	}
}
