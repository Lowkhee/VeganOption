package squeek.veganoption.helpers;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import squeek.veganoption.backpack.ItemInventory;
import squeek.veganoption.backpack.ItemInventoryHandler;
import squeek.veganoption.items.ItemBackpack;
import squeek.veganoption.items.ItemBackpack.BackpackMode;

public class InventoryHelper
{
	//public static final InventoryHelper INSTANCE = new InventoryHelper();
	
	public static void init()
	{
		MinecraftForge.EVENT_BUS.register(new InventoryHelper());
	}
	
	public static float getPercentInventoryFilled(IInventory inventory)
	{
		if (inventory == null || inventory.getSizeInventory() == 0)
			return 0;

		float filledPercent = 0.0F;

		for (int slotNum = 0; slotNum < inventory.getSizeInventory(); ++slotNum)
		{
			ItemStack itemstack = inventory.getStackInSlot(slotNum);

			if (!itemstack.isEmpty())
			{
				filledPercent += (float) itemstack.getCount() / (float) Math.min(inventory.getInventoryStackLimit(), itemstack.getMaxStackSize());
			}
		}

		filledPercent /= inventory.getSizeInventory();
		return filledPercent;
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onItemPickup(EntityItemPickupEvent event) 
	{
		ItemStack stackPickup = event.getItem().getEntityItem();
		int originalStackAmount = stackPickup.getCount();
		
		if(stackPickup.isEmpty())
			return;
		
		EntityPlayer player = event.getEntityPlayer();
		InventoryPlayer inventory = player.inventory;
		
		//is there any backpacks in the inventory
		List<ItemStack> listBackpacks = new ArrayList<ItemStack>();
		for(int slot = 0; slot < inventory.getSizeInventory(); slot++)
		{
			ItemStack itemStack = inventory.getStackInSlot(slot);
			if(itemStack.getItem() instanceof ItemBackpack)
				listBackpacks.add(itemStack);
		}
		if(listBackpacks.isEmpty())
			return;
		
		//find the first resupply/receive backpack to handle the pickup item
		//ItemStack stackPickup = stackPickup.copy();
		for(ItemStack backpack : listBackpacks)
		{
			ItemBackpack backpackItem = (ItemBackpack)backpack.getItem();
			ItemInventory backpackInventory = new ItemInventory(player, backpackItem.getBackpackSize(), backpack);
			if(backpackItem.mode == BackpackMode.RECEIVE)
				stackPickup = ItemInventoryHandler.add(backpackInventory.getItemHandler(), stackPickup);
			if(stackPickup.isEmpty())
				break;
			if(backpackItem.mode == BackpackMode.RESUPPLY)
				stackPickup = ItemInventoryHandler.addToExisting(backpackInventory.getItemHandler(), stackPickup);
			if(stackPickup.isEmpty())
				break;
		}
		
		if(stackPickup.isEmpty())
		{
			event.getItem().setEntityItemStack(ItemStack.EMPTY);
			event.setResult(Result.ALLOW);
			return;
		}
		
		//add items to neutral and resupply if inventory is full, receive already checked above
		if(!inventory.addItemStackToInventory(stackPickup))
			for(ItemStack backpack : listBackpacks)
			{
				ItemBackpack backpackItem = (ItemBackpack)backpack.getItem();
				ItemInventory backpackInventory = new ItemInventory(player, backpackItem.getBackpackSize(), backpack);
				if(backpackItem.mode == BackpackMode.NEUTRAL && backpackItem.mode == BackpackMode.RESUPPLY)
					stackPickup = ItemInventoryHandler.add(backpackInventory.getItemHandler(), stackPickup);
				if(stackPickup.isEmpty())
					break;
			}
		if(stackPickup.getCount() == originalStackAmount)
			return;
		
		event.getEntity().dropItem(stackPickup.getItem(), stackPickup.getCount());
		event.getItem().setEntityItemStack(ItemStack.EMPTY);
		event.setResult(Result.ALLOW);
	}
}
