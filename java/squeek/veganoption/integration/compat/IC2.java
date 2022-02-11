package squeek.veganoption.integration.compat;


import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import squeek.veganoption.ModInfo;
import squeek.veganoption.content.ContentHelper;
import squeek.veganoption.content.Modifiers;
import squeek.veganoption.content.modules.Bioplastic;
import squeek.veganoption.content.modules.Composting;
import squeek.veganoption.content.modules.DollsEye;
import squeek.veganoption.content.modules.Ender;
import squeek.veganoption.content.modules.Jute;
import squeek.veganoption.content.modules.Resin;
import squeek.veganoption.content.modules.VegetableOil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import forestry.api.recipes.RecipeManagers;
import ic2.api.item.IC2Items;
import ic2.api.item.IItemAPI;
import ic2.api.recipe.ISemiFluidFuelManager;
import squeek.veganoption.integration.IntegrationHandler;
import squeek.veganoption.integration.IntegratorBase;

public class IC2  extends IntegratorBase
{
	protected static final Logger Log = LogManager.getLogger(IC2.class.getCanonicalName());
	
	public static IC2 instance;
	public static  Block rubberWood = null;
	public static  Block rubberLeaves = null;
	public static  Item rubberSapling = null;
	public static  ItemStack resin = null;
	public static  ItemStack rubber = null;
	public static  ItemStack plantBall = null;
	//public static  Item compressedPlantBall = null;
	public static  ItemStack coffeeBeans = null;
	public static  ItemStack hops = null;
	public static  ItemStack bioChaff = null;
	public static  ItemStack fertilizer = null;
	public static  ItemStack grinPowder = null;
	public static  Item terraWart = null;
	  
	public IC2()
	{
		if(IC2.instance == null)
			IC2.instance = this;
	}
	
	@Override
	public void create()
	{
		
	}
	
	public void setup()
	{
		IItemAPI ic2API = IC2Items.getItemAPI();
		if(ic2API == null)
			return;
		//rubberWood = IC2Items.getItem("rubber_wood");
		rubberWood = ic2API.getBlock("rubber_wood");//IC2Items.getItem("rubberWood");
		rubberLeaves = ic2API.getBlock("leaves");
		rubberSapling = ic2API.getItem("sapling");
		resin = ic2API.getItemStack("misc_resource", "resin").copy();
		rubber = ic2API.getItemStack("crafting", "rubber").copy();
		plantBall = ic2API.getItemStack("crafting", "plant_ball").copy();//IC2Items.getItem("crafting", "plant_ball");
		//////compressedPlantBall = ic2API.getItem("compressedPlantBall");
		fertilizer = ic2API.getItemStack("crop_res", "fertilizer").copy();
		coffeeBeans = ic2API.getItemStack("crop_res", "coffee_beans").copy();
		hops = ic2API.getItemStack("crop_res", "hops").copy();
		bioChaff = ic2API.getItemStack("crafting", "bio_chaff").copy();//IC2Items.getItem("crafting","bio_chaff");
		grinPowder = ic2API.getItemStack("crop_res","grin_powder");
		terraWart = ic2API.getItem("terra_wart");//terra_wart	Terra Wart
		
		if(rubberLeaves != null)
			OreDictionary.registerOre(Forestry.VeganFermenter, rubberLeaves);
		if(rubberSapling != null)
			OreDictionary.registerOre(Forestry.VeganFermenter, rubberSapling);
		if(plantBall != null)
			OreDictionary.registerOre(Forestry.VeganFermenter, plantBall);
		if(fertilizer != null)
			OreDictionary.registerOre(Forestry.VeganFermenter, fertilizer);
		if(coffeeBeans != null)
			OreDictionary.registerOre(Forestry.VeganFermenter, coffeeBeans);
		if(hops != null)
			OreDictionary.registerOre(Forestry.VeganFermenter, hops);
		if(bioChaff != null)
			OreDictionary.registerOre(Forestry.VeganFermenter, bioChaff);
		
		Block semiBlock = ic2API.getBlock("semifluid_generator");
		//IBlockState semiBlock = IC2Items.getItemAPI().getBlockState("te", "semifluid_generator");
		//Block semiBlock = IC2Items.getItemAPI().getBlock("semifluid_generator");
		if(semiBlock != null)
		{
			Set<Fluid> currentFluids = ic2.api.recipe.Recipes.semiFluidGenerator.getAcceptedFluids();
			for(Fluid fluid : FluidRegistry.getBucketFluids())
			{
				if(!currentFluids.contains(fluid))
				{
					if(fluid.getName().contains("fuel") || fluid.getName().contains("ethanol"))
						ic2.api.recipe.Recipes.semiFluidGenerator.addFluid(fluid.getName(), 10, 128);
					if(fluid.getName().contains("oil"))
						ic2.api.recipe.Recipes.semiFluidGenerator.addFluid(fluid.getName(), 40, 32);
				}
			}	
		}
		
		ItemStack coalDust = ic2API.getItemStack("dust", "coal");
		
		/*
		ItemStack slag = ic2API.getItemStack("misc_resource", "slag");
		slag.setCount(1);*/
		//IBlockState centrifuge = ic2API.getBlockState("te", "centrifuge");
		/*ItemStack ironPile = ic2API.getItemStack("dust", "small_iron");
		
		ic2.api.recipe.IRecipeInput inputItemSlag = ic2.api.recipe.Recipes.inputFactory.forStack(slag);
		if(centrifuge != null)
		{	
			ironPile.setCount(1);
			coalDust.setCount(4);
			if(slag != null)//!itemList.isEmpty())
				//for(ItemStack item : itemList)
				{
					NBTTagCompound requiredHeat = new NBTTagCompound();
					requiredHeat.setInteger("minHeat", 150);
					ic2.api.recipe.Recipes.centrifuge.addRecipe(inputItemSlag, requiredHeat, false, coalDust, ironPile);
				}
		}
		IBlockState extractor = ic2API.getBlockState("te", "extractor");
		if(extractor != null)
		{
			if(slag != null)
				ic2.api.recipe.Recipes.extractor.addRecipe(inputItemSlag, new NBTTagCompound(), false, slag);
		}*/
		ic2.api.recipe.IRecipeInput fertilizerVO = ic2.api.recipe.Recipes.inputFactory.forStack(new ItemStack(Composting.fertilizer, 1));
		ic2.api.recipe.Recipes.extractor.addRecipe(fertilizerVO, new NBTTagCompound(), false, fertilizer.copy());
		ic2.api.recipe.IRecipeInput hopsRecipe = ic2.api.recipe.Recipes.inputFactory.forStack(new ItemStack(terraWart, 32));
		ic2.api.recipe.Recipes.extractor.addRecipe(hopsRecipe, new NBTTagCompound(), false, hops.copy());
		
		ic2.api.recipe.IRecipeInput resinIC2 = ic2.api.recipe.Recipes.inputFactory.forStack(new ItemStack(resin.getItem(), 2));
		ic2.api.recipe.IRecipeInput resinVO = ic2.api.recipe.Recipes.inputFactory.forStack(new ItemStack(Resin.resin, 2));
		//convert coal dust to coal
		IBlockState compressor = ic2API.getBlockState("te", "Compressor");
		if(compressor != null)
		{
			if(coalDust != null)
			{
				ic2.api.recipe.IRecipeInput coalDust9 = ic2.api.recipe.Recipes.inputFactory.forStack(coalDust.copy(), 9);
				ic2.api.recipe.Recipes.compressor.addRecipe(coalDust9, new NBTTagCompound(), false, new ItemStack(Items.COAL));
			}
			ic2.api.recipe.IRecipeInput coalBlocks = ic2.api.recipe.Recipes.inputFactory.forStack(new ItemStack(Blocks.COAL_BLOCK), 32);
			ic2.api.recipe.Recipes.compressor.addRecipe(coalBlocks, new NBTTagCompound(), false, new ItemStack(Items.DIAMOND));
			
			//ic2.api.recipe.Recipes.compressor.addRecipe(resinIC2, new NBTTagCompound(), false, new ItemStack(Resin.rosin));
			//ic2.api.recipe.Recipes.compressor.addRecipe(resinVO, new NBTTagCompound(), false, new ItemStack(Resin.rosin));
		}
		ic2.api.recipe.Recipes.centrifuge.addRecipe(resinIC2, new NBTTagCompound(), false, new ItemStack(Resin.rosin));
		ic2.api.recipe.Recipes.centrifuge.addRecipe(resinVO, new NBTTagCompound(), false, new ItemStack(Resin.rosin));
		//////////////Macerator ---bio chaff
		ic2.api.recipe.IRecipeInput dollsEyeRec = ic2.api.recipe.Recipes.inputFactory.forStack(new ItemStack(DollsEye.dollsEye, 1));
		grinPowder.grow(1);
		ic2.api.recipe.Recipes.macerator.addRecipe(dollsEyeRec, new NBTTagCompound(), false, grinPowder.copy());
	
		//if(resin != null) 
		//{
		//	Modifiers.recipes.convertInput(resin, ContentHelper.resinOreDict);
		//	int[] ids = OreDictionary.getOreIDs(resin);
		//	OreDictionary.registerOre(resin.getUnlocalizedName(), new ItemStack(Resin.resin));
		//	OreDictionary.registerOre(ContentHelper.resinOreDict, resin.copy());
		//}
	}

	@Override
	public void oredict()
	{
		
	}

	@Override
	public void recipes()
	{
		setup();
		ItemStack uranium = IC2Items.getItemAPI().getItemStack("nuclear", "uranium");
		if(uranium != null)
		{
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Items.NETHER_STAR, 1),
					"BGB",
					"GUG",
					"BGB",
					'B', Bioplastic.bioplastic,
					'G', Items.GOLD_INGOT,
					'U', uranium.getItem()));
		}
		
		if(resin != null)
		{
			GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(Resin.resin), resin.copy()));
			GameRegistry.addRecipe(new ShapelessOreRecipe(resin.copy(), new ItemStack(Resin.resin)));
		}
		
		if(plantBall != null)
		{
			GameRegistry.addRecipe(new ShapedOreRecipe(plantBall.copy(),
				"XXX",
				"X X",
				"XXX",
				'X', new ItemStack(VegetableOil.seedSunflower)));
			GameRegistry.addRecipe(new ShapedOreRecipe(plantBall.copy(),
					"XXX",
					"X X",
					"XXX",
					'X', new ItemStack(Jute.juteSeeds)));
			GameRegistry.addRecipe(new ShapedOreRecipe(plantBall.copy(),
					"XXX",
					"X X",
					"XXX",
					'X', new ItemStack(Jute.juteStalk)));
			GameRegistry.addRecipe(new ShapedOreRecipe(plantBall.copy(),
					"XXX",
					"X X",
					"XXX",
					'X', new ItemStack(Items.ROTTEN_FLESH)));
					
		}
		if(terraWart != null)
		{
			GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(terraWart), new ItemStack(Items.NETHER_WART), Ender.bucketRawEnder.copy()));
			if(IntegrationHandler.integratorExists(MODID_FORESTRY))
				RecipeManagers.carpenterManager.addRecipe(10, new FluidStack(Ender.fluidRawEnder, Fluid.BUCKET_VOLUME), ItemStack.EMPTY, new ItemStack(terraWart), 
						"   ",
						" # ",
						"   ",
						'#', new ItemStack(Items.NETHER_WART));
		}
		

		//semi fluid generator
		//Block semiGen = IC2Items.getItemAPI().getBlock("semifluid_generator");
		/*ISemiFluidFuelManager semiGen = (ISemiFluidFuelManager)IC2Items.getItemAPI().getBlock("semifluid_generator");
		for(Fluid fluid : FluidRegistry.getBucketFluids())
		{
			Log.log(Level.INFO, fluid.getName());
			semiGen.addFluid(fluid.getName(), 10, 32.0);	
		}//;*/
		
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

	}
	
}
