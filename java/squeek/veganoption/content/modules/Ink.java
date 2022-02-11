package squeek.veganoption.content.modules;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import squeek.veganoption.ModInfo;
import squeek.veganoption.VeganOption;
import squeek.veganoption.content.ContentHelper;
import squeek.veganoption.content.IContentModule;
import squeek.veganoption.content.Modifiers;
import squeek.veganoption.content.recipes.PistonCraftingRecipe;
import squeek.veganoption.content.registry.PistonCraftingRegistry;
import squeek.veganoption.content.registry.RelationshipRegistry;

public class Ink implements IContentModule
{
	public static Item blackVegetableOilInk;
	public static Item whiteVegetableOilInk;
	public static Item waxVegetable;
	public static Fluid blackInkFluid;
	public static Fluid whiteInkFluid;
	public static Block blackInk;
	public static Block whiteInk;

	@Override
	public void create()
	{
		waxVegetable = new Item()
			.setUnlocalizedName(ModInfo.MODID + ".waxVegetable")
			.setCreativeTab(VeganOption.creativeTab)
			.setRegistryName(ModInfo.MODID, "waxVegetable");
		GameRegistry.register(waxVegetable);

		blackVegetableOilInk = new Item()
			.setUnlocalizedName(ModInfo.MODID + ".inkVegetableOilBlack")
			.setCreativeTab(VeganOption.creativeTab)
			.setRegistryName(ModInfo.MODID, "inkVegetableOilBlack")
			.setContainerItem(Items.GLASS_BOTTLE);
		GameRegistry.register(blackVegetableOilInk);

		blackInkFluid = new Fluid("ink_black", new ResourceLocation(ModInfo.MODID, "blocks/black_ink_still"), new ResourceLocation(ModInfo.MODID, "blocks/black_ink_flow"));
		FluidRegistry.registerFluid(blackInkFluid);
		blackInk = new BlockFluidClassic(blackInkFluid, Material.WATER)
			.setUnlocalizedName(ModInfo.MODID + ".inkBlack")
			.setRegistryName(ModInfo.MODID, "inkBlack");
		blackInkFluid.setBlock(blackInk);
		blackInkFluid.setUnlocalizedName(blackInk.getUnlocalizedName());
		GameRegistry.register(blackInk);
		GameRegistry.register(new ItemBlock(blackInk).setRegistryName(blackInk.getRegistryName()));

		//A: type of fluid and amount B: bottle when full C: bottle when empty
		//BottleRegistry.registerFluidBottle(new FluidStack(blackInkFluid, Fluid.BUCKET_VOLUME), new ItemStack(blackVegetableOilInk));
		
		whiteVegetableOilInk = new Item()
			.setUnlocalizedName(ModInfo.MODID + ".inkVegetableOilWhite")
			.setCreativeTab(VeganOption.creativeTab)
			.setRegistryName(ModInfo.MODID, "inkVegetableOilWhite")
			.setContainerItem(Items.GLASS_BOTTLE);
		GameRegistry.register(whiteVegetableOilInk);

		whiteInkFluid = new Fluid("ink_white", new ResourceLocation(ModInfo.MODID, "blocks/white_ink_still"), new ResourceLocation(ModInfo.MODID, "blocks/white_ink_flow"));
		FluidRegistry.registerFluid(whiteInkFluid);
		whiteInk = new BlockFluidClassic(whiteInkFluid, Material.WATER)
			.setUnlocalizedName(ModInfo.MODID + ".inkWhite")
			.setRegistryName(ModInfo.MODID, "inkWhite");
		whiteInkFluid.setBlock(whiteInk);
		whiteInkFluid.setUnlocalizedName(whiteInk.getUnlocalizedName());
		GameRegistry.register(whiteInk);
		GameRegistry.register(new ItemBlock(whiteInk).setRegistryName(whiteInk.getRegistryName()));
	}

	@Override
	public void oredict()
	{
		OreDictionary.registerOre(ContentHelper.blackInkOreDict, ContentHelper.inkSac.copy());

		OreDictionary.registerOre(ContentHelper.blackPigmentOreDict, ContentHelper.charcoal.copy());
		OreDictionary.registerOre(ContentHelper.whitePigmentOreDict, Items.QUARTZ);
		OreDictionary.registerOre(ContentHelper.blackDyeOreDict, blackVegetableOilInk);
		OreDictionary.registerOre(ContentHelper.blackInkOreDict, blackVegetableOilInk);
		OreDictionary.registerOre(ContentHelper.whiteDyeOreDict, whiteVegetableOilInk);
		OreDictionary.registerOre(ContentHelper.whiteInkOreDict, whiteVegetableOilInk);
		OreDictionary.registerOre(ContentHelper.waxOreDict, new ItemStack(waxVegetable));
		OreDictionary.registerOre(ContentHelper.waxOreDictForestry, new ItemStack(waxVegetable));
		OreDictionary.registerOre(ContentHelper.waxOreDictHarvestCraft, new ItemStack(waxVegetable));
	}

	@Override
	public void recipes()
	{
		Modifiers.recipes.convertInput(ContentHelper.inkSac.copy(), ContentHelper.blackInkOreDict);

		ContentHelper.addOreSmelting(ContentHelper.vegetableOilOreDict, new ItemStack(waxVegetable), 0.2f);

		GameRegistry.addRecipe(new ShapelessOreRecipe(blackVegetableOilInk, ContentHelper.vegetableOilSourceOreDict, ContentHelper.waxOreDict, ContentHelper.rosinOreDict, ContentHelper.blackPigmentOreDict));
		//Modifiers.crafting.addInputsToRemoveForOutput(new ItemStack(blackVegetableOilInk), new ItemStack(Items.GLASS_BOTTLE));
		//Modifiers.crafting.addInputsToRemoveForOutput(new ItemStack(blackVegetableOilInk), ContentHelper.vegetableOilSourceOreDict, ContentHelper.waxOreDict, ContentHelper.rosinOreDict, ContentHelper.blackPigmentOreDict);

		PistonCraftingRegistry.register(new PistonCraftingRecipe(blackInkFluid, VegetableOil.fluidVegetableOil, ContentHelper.waxOreDict, ContentHelper.rosinOreDict, ContentHelper.blackPigmentOreDict));

		GameRegistry.addRecipe(new ShapelessOreRecipe(whiteVegetableOilInk, ContentHelper.vegetableOilSourceOreDict, ContentHelper.waxOreDict, ContentHelper.rosinOreDict, ContentHelper.whitePigmentOreDict));
		//Modifiers.crafting.addInputsToRemoveForOutput(new ItemStack(whiteVegetableOilInk), new ItemStack(VegetableOil.oilVegetable));

		PistonCraftingRegistry.register(new PistonCraftingRecipe(whiteInkFluid, VegetableOil.fluidVegetableOil, ContentHelper.waxOreDict, ContentHelper.rosinOreDict, ContentHelper.whitePigmentOreDict));
	}

	@Override
	public void finish()
	{
		RelationshipRegistry.addRelationship(new ItemStack(whiteVegetableOilInk), new ItemStack(whiteInk));
		RelationshipRegistry.addRelationship(new ItemStack(whiteInk), new ItemStack(whiteVegetableOilInk));
		RelationshipRegistry.addRelationship(new ItemStack(blackVegetableOilInk), new ItemStack(blackInk));
		RelationshipRegistry.addRelationship(new ItemStack(blackInk), new ItemStack(blackVegetableOilInk));
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
		ContentHelper.registerTypicalItemModel(blackVegetableOilInk);
		ContentHelper.registerTypicalItemModel(whiteVegetableOilInk);
		ContentHelper.registerTypicalItemModel(waxVegetable);
		ContentHelper.registerFluidMapperAndMeshDef(blackInk, "ink_black");
		ContentHelper.registerFluidMapperAndMeshDef(whiteInk, "ink_white");
	}

}

