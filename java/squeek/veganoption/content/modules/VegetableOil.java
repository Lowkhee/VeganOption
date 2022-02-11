package squeek.veganoption.content.modules;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import squeek.veganoption.ModInfo;
import squeek.veganoption.VeganOption;
import squeek.veganoption.blocks.BlockHelianthus;
import squeek.veganoption.content.ContentHelper;
import squeek.veganoption.content.IContentModule;
import squeek.veganoption.content.Modifiers;
import squeek.veganoption.content.modifiers.DropsModifier.BlockSpecifier;
import squeek.veganoption.content.modifiers.DropsModifier.DropSpecifier;
import squeek.veganoption.content.recipes.PistonCraftingRecipe;
import squeek.veganoption.content.registry.PistonCraftingRegistry;
import squeek.veganoption.content.registry.RelationshipRegistry;
import squeek.veganoption.items.ItemSeedsGeneric;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VegetableOil implements IContentModule
{
	public static Item seedSunflower;
	public static Block helianthus;
	
	public static Item oilVegetable;
	public static Fluid fluidVegetableOil;
	public static Block fluidBlockVegetableOil;

	public static ItemStack oilPresser;

	@Override
	public void create()
	{
		oilPresser = new ItemStack(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE);
		
		helianthus = new BlockHelianthus()
			.setUnlocalizedName(ModInfo.MODID + ".helianthus")
			.setRegistryName(ModInfo.MODID, "helianthus");
		GameRegistry.register(helianthus);

		seedSunflower = new ItemSeedsGeneric(helianthus, EnumPlantType.Plains)//new ItemFood(1, 0.05f, false)
			.setUnlocalizedName(ModInfo.MODID + ".seedSunflower")
			.setCreativeTab(VeganOption.creativeTab)
			.setRegistryName(ModInfo.MODID, "seedsSunflower");
		GameRegistry.register(seedSunflower);

		fluidVegetableOil = new Fluid("fluid_oil_vegetable", new ResourceLocation(ModInfo.MODID, "blocks/vegetable_oil_still"), new ResourceLocation(ModInfo.MODID, "blocks/vegetable_oil_flow"));
		FluidRegistry.registerFluid(fluidVegetableOil);
		fluidBlockVegetableOil = new BlockFluidClassic(fluidVegetableOil, Material.WATER)
			.setUnlocalizedName(ModInfo.MODID + ".fluidOilVegetable")
			.setRegistryName(ModInfo.MODID, "fluidOilVegetable");
		fluidVegetableOil.setBlock(fluidBlockVegetableOil);
		fluidVegetableOil.setUnlocalizedName(fluidBlockVegetableOil.getUnlocalizedName());
		GameRegistry.register(fluidBlockVegetableOil);
		GameRegistry.register(new ItemBlock(fluidBlockVegetableOil).setRegistryName(fluidBlockVegetableOil.getRegistryName()));
		
		oilVegetable = new Item()
			.setUnlocalizedName(ModInfo.MODID + ".oilVegetable")
			.setCreativeTab(VeganOption.creativeTab)
			.setRegistryName(ModInfo.MODID, "oilVegetable")
			.setContainerItem(Items.GLASS_BOTTLE);
		GameRegistry.register(oilVegetable);

		//FluidContainerRegistry.registerFluidContainer(new FluidStack(fluidVegetableOil, Fluid.BUCKET_VOLUME), new ItemStack(oilVegetable), new ItemStack(oilVegetable.getContainerItem()));
	}

	@Override
	public void oredict()
	{
		OreDictionary.registerOre(ContentHelper.oilPresserOreDict, oilPresser.copy());
		OreDictionary.registerOre(ContentHelper.sunflowerSeedOreDict, new ItemStack(seedSunflower));
		OreDictionary.registerOre(ContentHelper.vegetableOilOreDict, new ItemStack(oilVegetable));
	}

	@Override
	public void recipes()
	{
		ContentHelper.remapOre(ContentHelper.sunflowerSeedOreDict, ContentHelper.vegetableOilSourceOreDict);
		ContentHelper.remapOre(ContentHelper.grapeSeedOreDict, ContentHelper.vegetableOilSourceOreDict);
		ContentHelper.remapOre(ContentHelper.soybeanOreDict, ContentHelper.vegetableOilSourceOreDict);
		ContentHelper.remapOre(ContentHelper.cottonSeedOreDict, ContentHelper.vegetableOilSourceOreDict);
		ContentHelper.remapOre(ContentHelper.coconutOreDict, ContentHelper.vegetableOilSourceOreDict);
		ContentHelper.remapOre(ContentHelper.oliveOreDict, ContentHelper.vegetableOilSourceOreDict);
		ContentHelper.remapOre(ContentHelper.cornOreDict, ContentHelper.vegetableOilSourceOreDict);
		ContentHelper.remapOre(ContentHelper.nutOreDict, ContentHelper.vegetableOilSourceOreDict);
		ContentHelper.remapOre(ContentHelper.teaSeedOreDict, ContentHelper.vegetableOilSourceOreDict);
		ContentHelper.remapOre(ContentHelper.avocadoOreDict, ContentHelper.vegetableOilSourceOreDict);

		BlockSpecifier sunflowerTopSpecifier = new BlockSpecifier(Blocks.DOUBLE_PLANT.getDefaultState().withProperty(BlockDoublePlant.VARIANT, BlockDoublePlant.EnumPlantType.SUNFLOWER), BlockDoublePlant.VARIANT);
		DropSpecifier sunflowerDropSpecifier = new DropSpecifier(new ItemStack(seedSunflower)) //, 0, 2)
		{
			@Override
			public void modifyDrops(List<ItemStack> drops, EntityPlayer harvester, int fortuneLevel, boolean isSilkTouching)
			{
				// harvester is null when breaking the top block because
				// the bottom breaks on its own once there is no longer a top
				if (harvester == null)
				{
					List<ItemStack> dropsToRemove = new ArrayList<ItemStack>();
					for (ItemStack drop : drops)
					{
						if (drop.getItem() == Item.getItemFromBlock(Blocks.DOUBLE_PLANT) && drop.getItemDamage() == BlockDoublePlant.EnumPlantType.SUNFLOWER.getMeta())
							dropsToRemove.add(drop);
					}
					drops.removeAll(dropsToRemove);

					super.modifyDrops(drops, null, fortuneLevel, isSilkTouching);
				}
			}
		};
		Modifiers.drops.addDropsToBlock(sunflowerTopSpecifier, sunflowerDropSpecifier);

		addOilRecipe(new ItemStack(oilVegetable), ContentHelper.vegetableOilSourceOreDict);

		PistonCraftingRegistry.register(new PistonCraftingRecipe(fluidVegetableOil, ContentHelper.vegetableOilSourceOreDict));
	}

	@Override
	public void finish()
	{
		RelationshipRegistry.addRelationship(new ItemStack(fluidBlockVegetableOil), new ItemStack(oilVegetable));
		RelationshipRegistry.addRelationship(new ItemStack(oilVegetable), new ItemStack(fluidBlockVegetableOil));
		RelationshipRegistry.addRelationship(new ItemStack(helianthus), new ItemStack(seedSunflower));
	}

	public static void addOilRecipe(ItemStack output, Object... inputs)
	{
		List<Object> recipeInputs = new ArrayList<Object>(Arrays.asList(inputs));
		recipeInputs.add(0, ContentHelper.oilPresserOreDict);
		if (output.getItem().hasContainerItem(output))
		{
			recipeInputs.add(output.getItem().getContainerItem(output));
		}
		GameRegistry.addRecipe(new ShapelessOreRecipe(output, recipeInputs.toArray(new Object[recipeInputs.size()])));
		if (!oilPresser.getItem().hasContainerItem(oilPresser))
		{
			Modifiers.crafting.addInputsToKeepForOutput(output, oilPresser);
		}
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
		ContentHelper.registerTypicalItemModel(seedSunflower);
		ContentHelper.registerTypicalItemModel(oilVegetable);
		ContentHelper.registerFluidMapperAndMeshDef(fluidBlockVegetableOil, "fluid_oil_vegetable");
		ContentHelper.registerTypicalBlockItemModels(helianthus, "veganoption:helianthus/" + helianthus.getRegistryName().getResourcePath());
	}
}
