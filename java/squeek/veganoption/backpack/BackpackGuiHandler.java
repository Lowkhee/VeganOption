package squeek.veganoption.backpack;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import squeek.veganoption.VeganOption;
import squeek.veganoption.helpers.GuiHelper;
import squeek.veganoption.items.ItemBackpack;

public class BackpackGuiHandler extends GuiHelper
{

	public static void openGui(EntityPlayer entityplayer, int guiId) 
	{
		entityplayer.openGui(VeganOption.instance, guiId, entityplayer.world, 0, 0, 0);
	}
	
	public static Object getClient(int guiId, EntityPlayer player, World world, int x, int y, int z) 
	{
		//BlockPos pos = new BlockPos(x, y, z);
		for (EnumHand hand : EnumHand.values()) 
		{
			ItemStack heldItem = player.getHeldItem(hand);
			if (!heldItem.isEmpty()) 
			{
				Item item = heldItem.getItem();
				if (heldItem.getItem() instanceof ItemBackpack) 
					return ((ItemBackpack) item).getGui(player, heldItem, guiId);			
			}
		}		
		return null;
	}

	public static Object getServer(int guiId, EntityPlayer player, World world, int x, int y, int z) 
	{
		//BlockPos pos = new BlockPos(x, y, z);
		for (EnumHand hand : EnumHand.values()) 
		{
			ItemStack heldItem = player.getHeldItem(hand);
			if (!heldItem.isEmpty()) 
			{
				Item item = heldItem.getItem();
				if (heldItem.getItem() instanceof ItemBackpack) 
					return ((ItemBackpack) item).getContainer(player, heldItem, guiId);		
			}
		}
		return null;
	}
}

