package squeek.veganoption.integration.compat;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import forestry.api.core.IModelManager;
import forestry.core.items.IColoredItem;
import forestry.core.items.ItemForestry;
import forestry.core.utils.ItemStackUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import squeek.veganoption.ModInfo;
import squeek.veganoption.VeganOption;
import squeek.veganoption.content.ContentHelper;

public class VeganCrate extends ItemForestry implements IColoredItem
{
	private static final Logger Log = LogManager.getLogger(VeganCrate.class.getCanonicalName());
	ItemStack contained;
	String oreDictName;
	
	public VeganCrate(ItemStack contained, String oreDictName) 
	{
		super(VeganOption.creativeTab);
		this.contained = contained;
		this.oreDictName = oreDictName;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModel(Item item, IModelManager manager)
	{
		Log.log(ModInfo.debugLevel, "ITEM - DisplayName:" + (item.getDefaultInstance()).getDisplayName());
				//+ "---- RegisterName: " + item.getRegistryName()
				//+ "---- Unlocallized: " + item.getUnlocalizedName());
		if(contained.isEmpty())
		{
			ContentHelper.registerTypicalItemStackModel(item, 0, "veganoption:crate");
			ContentHelper.registerTypicalItemStackModel(item, 1, "veganoption:crate-filled");
		}
		else
		{
			ModelResourceLocation modelLocation = new ModelResourceLocation("veganoption:crate-filled", "crate-filled");
			ModelLoader.setCustomModelResourceLocation(item, 0, modelLocation);
		}
	}
	
	public ItemStack getContained()
	{
		return contained;
	}

	@Nullable
	public String getOreDictName() 
	{
		return oreDictName;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) 
	{
		ItemStack heldItem = playerIn.getHeldItem(handIn);
		if (!worldIn.isRemote) 
		{
			if (contained.isEmpty() || heldItem.isEmpty())
				return ActionResult.newResult(EnumActionResult.PASS, heldItem);

			heldItem.shrink(1);

			ItemStack dropStack = contained.copy();
			dropStack.setCount(9);
			ItemStackUtil.dropItemStackAsEntity(dropStack, worldIn, playerIn.posX, playerIn.posY, playerIn.posZ, 40);
		}
		return ActionResult.newResult(EnumActionResult.SUCCESS, heldItem);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getColorFromItemstack(ItemStack stack, int renderPass) 
	{
		ItemColors colors = Minecraft.getMinecraft().getItemColors();
		
		if (contained.isEmpty() || renderPass == 100) 
			return -1;
		int color = colors.getColorFromItemstack(contained, renderPass);
		
		if (color != -1) 
			return color;
		
		return -1;
	}
	
}
