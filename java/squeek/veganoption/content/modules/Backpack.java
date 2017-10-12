package squeek.veganoption.content.modules;

import java.awt.Color;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.ShapedOreRecipe;

import squeek.veganoption.ModInfo;
import squeek.veganoption.VeganOption;
import squeek.veganoption.content.IContentModule;
import squeek.veganoption.helpers.ColorHelper.ItemColorHandler;
import squeek.veganoption.items.ItemBackpack;

public class Backpack implements IContentModule
{
	public static Item backpackBasic;
	public static Item backpackAdvanced;

	@Override
	public void create()
	{
		backpackBasic = new ItemBackpack(new Color(0xfaf0e6), Color.GREEN, ItemBackpack.BackpackType.BASIC) //;backpackInterface.createBackpack(VEGAN_BACKPACK, EnumBackpackType.NORMAL);
			.setUnlocalizedName(ModInfo.MODID + ".backpackBasic")
			.setMaxStackSize(1)
			.setCreativeTab(VeganOption.creativeTab)
			.setRegistryName("backpackBasic");
		GameRegistry.register(backpackBasic);
		
		backpackAdvanced = new ItemBackpack(new Color(0xc5a981), Color.BLUE, ItemBackpack.BackpackType.ADVANCED)//backpackInterface.createBackpack(VEGAN_BACKPACK, EnumBackpackType.WOVEN); 
			.setUnlocalizedName(ModInfo.MODID + ".backpackAdvanced")
			.setMaxStackSize(1)
			.setCreativeTab(VeganOption.creativeTab)
			.setRegistryName("backpackAdvanced");
		GameRegistry.register(backpackAdvanced);
		
	}

	@Override
	public void oredict()
	{
	}

	@Override
	public void recipes()
	{
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(backpackBasic),
				"XYX",
				"#Z#",
				"XYX",
				'#', Bioplastic.plasticRod,
				'X', Kapok.kapokTuft,
				'Y', Items.EMERALD,
				'Z', Composting.composter));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(backpackAdvanced),
				"X#X",
				"XYX",
				"X#X",
				'X', Burlap.burlap,
				'#', Items.DIAMOND,
				'Y', backpackBasic));
		
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler(ItemColorHandler.INSTANCE, backpackBasic, backpackAdvanced);
	}

	@Override
	public void finish()
	{
		
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void clientSidePost()
	{
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void clientSidePre()
	{
		ItemBackpack.registerModel(backpackBasic);
		ItemBackpack.registerModel(backpackAdvanced);
	}
}
