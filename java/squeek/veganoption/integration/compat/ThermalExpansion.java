package squeek.veganoption.integration.compat;

import net.minecraft.block.Block;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import squeek.veganoption.content.modules.Composting;
import squeek.veganoption.content.modules.DollsEye;
import squeek.veganoption.content.modules.Egg;
import squeek.veganoption.content.modules.Ender;
import squeek.veganoption.content.modules.FrozenBubble;
import squeek.veganoption.content.modules.Gunpowder;
import squeek.veganoption.content.modules.Jute;
import squeek.veganoption.content.modules.Kapok;
import squeek.veganoption.content.modules.Resin;
import squeek.veganoption.content.modules.Seitan;
import squeek.veganoption.content.modules.VegetableOil;
import squeek.veganoption.integration.IntegrationHandler;
import squeek.veganoption.integration.IntegratorBase;
import squeek.veganoption.items.ItemWashableWheat;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.*;

import javax.annotation.Nonnull;

import buildcraft.api.fuels.IFuel;
import cofh.lib.inventory.ComparableItemStack;
import cofh.lib.util.helpers.ItemHelper;
import cofh.thermalexpansion.block.dynamo.BlockDynamo;
import cofh.thermalexpansion.util.managers.dynamo.SteamManager;
import cofh.thermalexpansion.util.managers.machine.InsolatorManager;
import cofh.thermalexpansion.util.managers.machine.InsolatorManager.InsolatorRecipe;
import cofh.thermalexpansion.util.managers.machine.RefineryManager;
import cofh.thermalfoundation.init.TFFluids;
import cofh.thermalfoundation.item.ItemFertilizer;
import cofh.thermalfoundation.item.ItemMaterial;
import forestry.api.arboriculture.IWoodType;
import forestry.api.arboriculture.WoodBlockKind;
import forestry.arboriculture.PluginArboriculture;
import forestry.arboriculture.WoodAccess;

public class ThermalExpansion extends IntegratorBase
{
	private static final Logger Log = LogManager.getLogger(ThermalExpansion.class.getCanonicalName());
	
	@Override
	public void preInit()
	{
		super.preInit();
	}
	@Override
	public void init()
	{
		super.init();
		int extract = 100;
		int fill = -1;

		Fluid mClay = FluidRegistry.getFluid("clay");
		if(mClay != null)
		{
			addTransposerRecipe(4000, new ItemStack(Blocks.ICE), new ItemStack(Blocks.CLAY), new FluidStack(mClay, 600), fill, false);
			addTransposerRecipe(2000, new ItemStack(Items.SNOWBALL), new ItemStack(Items.CLAY_BALL), new FluidStack(mClay, 150), fill, false);
		}
		
		addTransposerRecipe(4000, new ItemStack(FrozenBubble.frozenBubble), new ItemStack(Items.ENDER_PEARL), new FluidStack(Ender.fluidRawEnder, Ender.RAW_ENDER_PER_PEARL), fill, true);
		addTransposerRecipe(4000, new ItemStack(FrozenBubble.frozenBubble), new ItemStack(Items.ENDER_PEARL), FluidRegistry.getFluidStack("ender", Fluid.BUCKET_VOLUME / 4), fill, false);

		addTransposerRecipe(3000, new ItemStack(VegetableOil.seedSunflower, 5), new ItemStack(Composting.fertilizer, 1), new FluidStack(VegetableOil.fluidVegetableOil, Fluid.BUCKET_VOLUME), extract, false);
		
		addTransposerRecipe(2000, Seitan.wheatFlourStack.copy(), Seitan.wheatDoughStack.copy(), new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME), fill, false);
		addTransposerRecipe(2000, Seitan.wheatDoughStack.copy(), Seitan.seitanUnwashedStack.copy(), new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME), fill, false);
		for (int outputMeta = ItemWashableWheat.META_UNWASHED_START + 1; outputMeta < ItemWashableWheat.META_UNWASHED_END; outputMeta++)
		{
			addTransposerRecipe(2000, new ItemStack(Seitan.washableWheat, 1, outputMeta - 1), new ItemStack(Seitan.washableWheat, 1, outputMeta), new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME), fill, false);
		}
		addTransposerRecipe(2000, new ItemStack(Seitan.washableWheat, 1, ItemWashableWheat.META_RAW - 1), Seitan.seitanRawStack.copy(), new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME), fill, false);

		addPulverizerRecipe(1600, new ItemStack(Items.POTATO), new ItemStack(Egg.potatoStarch, 2), ItemStack.EMPTY, 100);
		
		for(Item item : ForgeRegistries.ITEMS.getValues())
		{
			ItemStack stack = new ItemStack(item);
			
			if(stack != null &&  stack.getUnlocalizedName().toLowerCase().contains("bitumen"))
				addCrucibleRecipe(600, new ItemStack(item), "crude_oil", 250);
		}
		
		
		int CROP_MULTIPLIER_RICH = 3;
		int CROP_MULTIPLIER_FLUX = 5;
		int DEFAULT_ENERGY = 5000;
		int DEFAULT_ENERGY_RICH = 7500;
		int DEFAULT_ENERGY_FLUX = 10000;
		int secondaryChance = 80;
		
		addInsolatorRecipe(3000, new ItemStack(Blocks.TALLGRASS, 1, BlockTallGrass.EnumType.GRASS.getMeta()), new ItemStack(Composting.fertilizer), new ItemStack(Blocks.TALLGRASS, 2, BlockTallGrass.EnumType.GRASS.getMeta()), new ItemStack(DollsEye.dollsEye), 50);
		
		InsolatorManager.addRecipe(DEFAULT_ENERGY, new ItemStack(Jute.juteSeeds, 1), ItemFertilizer.fertilizerBasic, new ItemStack(Jute.juteStalk, 6), new ItemStack(Jute.juteSeeds), secondaryChance);
		InsolatorManager.addRecipe(DEFAULT_ENERGY_RICH, new ItemStack(Jute.juteSeeds, 1), ItemFertilizer.fertilizerRich, new ItemStack(Jute.juteStalk, 12), new ItemStack(Jute.juteSeeds), secondaryChance < 100 ? 100 : secondaryChance < 125 ? 125 : secondaryChance);
		InsolatorManager.addRecipe(DEFAULT_ENERGY_FLUX, new ItemStack(Jute.juteSeeds, 1), ItemFertilizer.fertilizerFlux, new ItemStack(Jute.juteStalk, 18), new ItemStack(Jute.juteSeeds), secondaryChance < 150 ? 150 : secondaryChance);
		
		InsolatorManager.addRecipe(DEFAULT_ENERGY, new ItemStack(Blocks.SAPLING, 1, 3), ItemFertilizer.fertilizerBasic, new ItemStack(Blocks.LOG, 6, 3), new ItemStack(Kapok.kapokTuft, 4), secondaryChance, true, InsolatorManager.Type.TREE);
		InsolatorManager.addRecipe(DEFAULT_ENERGY_RICH, new ItemStack(Blocks.SAPLING, 1, 3), ItemFertilizer.fertilizerRich, new ItemStack(Blocks.LOG, 12, 3), new ItemStack(Kapok.kapokTuft, 8), secondaryChance < 100 ? 100 : secondaryChance < 125 ? 125 : secondaryChance, true, InsolatorManager.Type.TREE);
		InsolatorManager.addRecipe(DEFAULT_ENERGY_FLUX, new ItemStack(Blocks.SAPLING, 1, 3), ItemFertilizer.fertilizerFlux, new ItemStack(Blocks.LOG, 18, 3), new ItemStack(Kapok.kapokTuft, 12), secondaryChance < 150 ? 150 : secondaryChance, true, InsolatorManager.Type.TREE);
		
		
		
		Collection<IFuel> fuels = buildcraft.api.fuels.BuildcraftFuelRegistry.fuel.getFuels();
		//Log.error("--------------------------------");
		//Log.error(fuels.size());
		//Log.error("--------------------------------");
		if(fuels != null)
		for(IFuel fuel: fuels)
		{ 
			//Log.error(fuel.getFluid().getLocalizedName() + " ======= " + fuel.getFluid().getUnlocalizedName());
			//Log.error("--------------------------------");
			RefineryManager.addRecipe(4000, new FluidStack(fuel.getFluid(), 100), new FluidStack(TFFluids.fluidRefinedOil, 100), ItemMaterial.globTar.copy());
		}
		
		RefineryManager.addRecipe(2000, new FluidStack(Ender.fluidRawEnder, 100), new FluidStack(TFFluids.fluidMana, 10), new ItemStack(Resin.resin));
		
		
		/*Set<ComparableItemStack> steamFuels = SteamManager.getFuels();
		for(String oreName : OreDictionary.getOreNames())//Item item : Item.REGISTRY)
		{
			Log.error("--------------------------------");
			Log.error(oreName);
			Log.error("--------------------------------");
			NonNullList<ItemStack> oresStack = OreDictionary.getOres(oreName);//new ItemStack(item, 1);
			for(ItemStack ore : oresStack)
			{	
				Log.error("--------------------------------");
				Log.error(ore.getDisplayName());
				Log.error("--------------------------------");
				//if(!this.itemFuelExist(steamFuels, ore))
			//OreDictionary.getOreID(oreName);
					SteamManager.addFuel(new ItemStack(ore.getItem(), 1), 8000);
			}
		}*/
		for(Item itemToAdd : ForgeRegistries.ITEMS.getValues())
		{
			SteamManager.addFuel(new ItemStack(itemToAdd, 1), 8000);
		}
		for(Block blockToAdd : ForgeRegistries.BLOCKS.getValues())
		{
			SteamManager.addFuel(new ItemStack(blockToAdd, 1), 10000);
		}
		
		/*for(ItemStack item : materialItems)
		{
			String[] itemName = item.getItem().getUnlocalizedName().split(".");
			if(itemName.length > 0)
			{
				Log.error("--------------------------------");
				Log.error(itemName[itemName.length]);
				Log.error("--------------------------------");
				OreDictionary.registerOre(itemName[itemName.length - 1], item.copy());
			}
		}*/
		ItemStack[] materialItems = new ItemStack[]{ItemMaterial.nuggetIron, ItemMaterial.nuggetGold, ItemMaterial.ingotIron, ItemMaterial.ingotGold, ItemMaterial.gemDiamond, ItemMaterial.dustIron, ItemMaterial.dustGold, ItemMaterial.nuggetDiamond, ItemMaterial.gearIron, ItemMaterial.gearGold, ItemMaterial.plateIron, ItemMaterial.plateGold, ItemMaterial.ingotCopper, ItemMaterial.ingotTin, ItemMaterial.ingotSilver, ItemMaterial.ingotLead, ItemMaterial.ingotAluminum, ItemMaterial.ingotNickel, ItemMaterial.ingotPlatinum, ItemMaterial.ingotIridium, ItemMaterial.ingotMithril, ItemMaterial.ingotSteel, ItemMaterial.ingotElectrum, ItemMaterial.ingotInvar, ItemMaterial.ingotBronze, ItemMaterial.ingotConstantan, ItemMaterial.ingotSignalum, ItemMaterial.ingotLumium, ItemMaterial.ingotEnderium, ItemMaterial.dustCopper, ItemMaterial.dustTin, ItemMaterial.dustSilver, ItemMaterial.dustLead, ItemMaterial.dustAluminum, ItemMaterial.dustNickel, ItemMaterial.dustPlatinum, ItemMaterial.dustIridium, ItemMaterial.dustMithril, ItemMaterial.dustSteel, ItemMaterial.dustElectrum, ItemMaterial.dustInvar, ItemMaterial.dustBronze, ItemMaterial.dustConstantan, ItemMaterial.dustSignalum, ItemMaterial.dustLumium, ItemMaterial.dustEnderium, ItemMaterial.nuggetCopper, ItemMaterial.nuggetTin, ItemMaterial.nuggetSilver, ItemMaterial.nuggetLead, ItemMaterial.nuggetAluminum, ItemMaterial.nuggetNickel, ItemMaterial.nuggetPlatinum, ItemMaterial.nuggetIridium, ItemMaterial.nuggetMithril, ItemMaterial.nuggetSteel, ItemMaterial.nuggetElectrum, ItemMaterial.nuggetInvar, ItemMaterial.nuggetBronze, ItemMaterial.nuggetConstantan, ItemMaterial.nuggetSignalum, ItemMaterial.nuggetLumium, ItemMaterial.nuggetEnderium, ItemMaterial.gearCopper, ItemMaterial.gearTin, ItemMaterial.gearSilver, ItemMaterial.gearLead, ItemMaterial.gearNickel, ItemMaterial.gearAluminum, ItemMaterial.gearPlatinum, ItemMaterial.gearIridium, ItemMaterial.gearMithril, ItemMaterial.gearSteel, ItemMaterial.gearElectrum, ItemMaterial.gearInvar, ItemMaterial.gearBronze, ItemMaterial.gearConstantan, ItemMaterial.gearSignalum, ItemMaterial.gearLumium, ItemMaterial.gearEnderium, ItemMaterial.plateCopper, ItemMaterial.plateTin, ItemMaterial.plateSilver, ItemMaterial.plateLead, ItemMaterial.plateAluminum, ItemMaterial.plateNickel, ItemMaterial.platePlatinum, ItemMaterial.plateIridium, ItemMaterial.plateMithril, ItemMaterial.plateSteel, ItemMaterial.plateElectrum, ItemMaterial.plateInvar, ItemMaterial.plateBronze, ItemMaterial.plateConstantan, ItemMaterial.plateSignalum, ItemMaterial.plateLumium, ItemMaterial.plateEnderium, ItemMaterial.redstoneServo, ItemMaterial.powerCoilGold, ItemMaterial.powerCoilSilver, ItemMaterial.powerCoilElectrum, ItemMaterial.dustCoal, ItemMaterial.dustCharcoal, ItemMaterial.dustObsidian, ItemMaterial.dustSulfur, ItemMaterial.dustNiter, ItemMaterial.dustWood, ItemMaterial.dustWoodCompressed, ItemMaterial.gemCoke, ItemMaterial.globRosin, ItemMaterial.globTar, ItemMaterial.crystalSlag, ItemMaterial.crystalSlagRich, ItemMaterial.crystalCinnabar, ItemMaterial.crystalCrudeOil, ItemMaterial.crystalRedstone, ItemMaterial.crystalGlowstone, ItemMaterial.crystalEnder, ItemMaterial.dustPyrotheum, ItemMaterial.dustCryotheum, ItemMaterial.dustAerotheum, ItemMaterial.dustPetrotheum, ItemMaterial.dustMana, ItemMaterial.rodBlizz, ItemMaterial.dustBlizz, ItemMaterial.rodBlitz, ItemMaterial.dustBlitz, ItemMaterial.rodBasalz, ItemMaterial.dustBasalz};
		
		for(ItemStack material : materialItems)
		{
			SteamManager.addFuel(material.copy(), 9000);
		}
		
		/*insolator cannot deal with the overly complicated forestry sapling and wood format
		int CROP_MULTIPLIER_RICH = 3;
		int CROP_MULTIPLIER_FLUX = 4;
		int DEFAULT_ENERGY = 5000;
		int DEFAULT_ENERGY_RICH = 7500;
		int DEFAULT_ENERGY_FLUX = 10000;
		int secondaryChance = 80;
		
		if(IntegrationHandler.integratorExists(MODID_FORESTRY))
		{
		//NonNullList<ItemStack> ores = OreDictionary.getOres("logWood");
		
			NonNullList<ItemStack> saplingList = NonNullList.create();
			PluginArboriculture.getItems().sapling.addCreativeItems(saplingList, false);
		
			WoodAccess woodAccess = WoodAccess.getInstance();
			
			for(ItemStack sapling : saplingList)
				for(IWoodType woodType : woodAccess.getRegisteredWoodTypes())
				{
					if(sapling != null && woodType != null)
					{
						ItemStack logStack = woodAccess.getStack(woodType, WoodBlockKind.LOG, false);
					
						String logName = logStack.getDisplayName().replaceFirst(" Wood", "");
						///Log.error("====================================");
						//Log.error("(1)---" + logName + "---");
						if(sapling.getDisplayName().contains(logName))
						{
							ItemStack primaryInput = sapling.copy();
							ItemStack primaryOutput = logStack.copy();
							ItemStack secondaryOutput = sapling.copy();
							
							Log.error("++++++++++++++++++++++++++++++++");
							Log.error("(2)---" + primaryOutput.getDisplayName() + "---" + primaryOutput.getUnlocalizedName() + "---" + "---" + primaryOutput.getItemDamage() + "---");
						
							InsolatorManager.addRecipe(DEFAULT_ENERGY, primaryInput, ItemFertilizer.fertilizerBasic, primaryOutput, secondaryOutput, secondaryChance);//, true, InsolatorManager.Type.TREE);
							InsolatorManager.addRecipe(DEFAULT_ENERGY_RICH, primaryInput, ItemFertilizer.fertilizerRich, ItemHelper.cloneStack(primaryOutput, primaryOutput.getCount() * CROP_MULTIPLIER_RICH), secondaryOutput, secondaryChance < 100 ? 100 : secondaryChance < 125 ? 125 : secondaryChance);//, true, InsolatorManager.Type.TREE);
							InsolatorManager.addRecipe(DEFAULT_ENERGY_FLUX, primaryInput, ItemFertilizer.fertilizerFlux, ItemHelper.cloneStack(primaryOutput, primaryOutput.getCount() * CROP_MULTIPLIER_FLUX), secondaryOutput, secondaryChance < 150 ? 150 : secondaryChance);//, true, InsolatorManager.Type.TREE);
						}
					}
				}
		}*/
	}
	
	@Override
	public void recipes()
	{
		super.recipes();
		/*ItemStack uranium = IC2Items.getItemAPI().getItemStack("nuclear", "uranium");
		if(uranium != null)
		{
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Items.NETHER_STAR, 1),
					"BGB",
					"GUG",
					"BGB",
					'B', Bioplastic.bioplastic,
					'G', Items.GOLD_INGOT,
					'U', uranium.getItem()));
		}*/
		
		if(ItemMaterial.dustSulfur != null)
		{
			GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(Gunpowder.sulfur), ItemMaterial.dustSulfur.copy()));
		}
	}
	
	public static void addCrucibleRecipe(int energy, @Nonnull ItemStack input, String output, int outputAmount)
	{
		NBTTagCompound toSend = new NBTTagCompound();

		toSend.setInteger("energy", energy);
		
		NBTTagCompound inputTag = new NBTTagCompound();
		inputTag = input.writeToNBT(inputTag);
		toSend.setTag("input", inputTag);
		
		NBTTagCompound outputTag = new NBTTagCompound();
		outputTag.setString("FluidName", output);
		outputTag.setInteger("Amount", outputAmount);
		toSend.setTag("output", outputTag);
		
	//ADD_CRUCIBLE_RECIPE:
		//CrucibleManager.addRecipe(nbt.getInteger(ENERGY), new ItemStack(nbt.getCompoundTag(INPUT)), FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag(OUTPUT)));
		FMLInterModComms.sendMessage(MODID_THERMAL_EXPANSION, "addcruciblerecipe", toSend);
	}
	
	public static void addInsolatorRecipe(int energy, @Nonnull ItemStack primaryInput, @Nonnull ItemStack secondaryInput, @Nonnull ItemStack primaryOutput, @Nonnull ItemStack secondaryOutput, int secondaryChance)
	{
		NBTTagCompound toSend = new NBTTagCompound();

		toSend.setInteger("energy", energy);
		
		NBTTagCompound primaryInputTag = new NBTTagCompound();
		primaryInputTag = primaryInput.writeToNBT(primaryInputTag);
		toSend.setTag("primaryInput", primaryInputTag);
		
		NBTTagCompound secondaryInputTag = new NBTTagCompound();
		secondaryInputTag = secondaryInput.writeToNBT(secondaryInputTag);
		toSend.setTag("secondaryInput", secondaryInputTag);
		
		NBTTagCompound primaryOutputTag = new NBTTagCompound();
		primaryOutputTag = primaryOutput.writeToNBT(primaryOutputTag);
		toSend.setTag("primaryOutput", primaryOutputTag);
		
		NBTTagCompound secondaryOutputTag = new NBTTagCompound();
		secondaryOutputTag = secondaryOutput.writeToNBT(secondaryOutputTag);
		toSend.setTag("secondaryOutput", secondaryOutputTag);
		
		toSend.setInteger("chance", secondaryChance);
		
		FMLInterModComms.sendMessage(MODID_THERMAL_EXPANSION, "addInsolatorRecipe", toSend);
	}

	public static void addPulverizerRecipe(int energy, @Nonnull ItemStack input, @Nonnull ItemStack primaryOutput, @Nonnull ItemStack secondaryOutput, int secondaryChance)
	{
		NBTTagCompound toSend = new NBTTagCompound();

		toSend.setInteger("energy", energy);
		toSend.setInteger("secondaryChance", secondaryChance);

		NBTTagCompound inputTag = new NBTTagCompound();
		inputTag = input.writeToNBT(inputTag);
		toSend.setTag("input", inputTag);

		NBTTagCompound primaryOutputTag = new NBTTagCompound();
		primaryOutputTag = primaryOutput.writeToNBT(primaryOutputTag);
		toSend.setTag("primaryOutput", primaryOutputTag);

		if (!secondaryOutput.isEmpty())
		{
			NBTTagCompound secondaryOutputTag = new NBTTagCompound();
			secondaryOutputTag = secondaryOutput.writeToNBT(secondaryOutputTag);
			toSend.setTag("secondaryOutput", secondaryOutputTag);
		}

		FMLInterModComms.sendMessage(MODID_THERMAL_EXPANSION, "addpulverizerrecipe", toSend);
	}

	public static void addTransposerRecipe(int energy, ItemStack input, ItemStack output, FluidStack fluid, int chance, boolean isReversible)
	{
		NBTTagCompound toSend = new NBTTagCompound();

		toSend.setInteger("energy", energy);
		toSend.setBoolean("reversible", isReversible);

		NBTTagCompound inputTag = new NBTTagCompound();
		inputTag = input.writeToNBT(inputTag);
		toSend.setTag("input", inputTag);

		NBTTagCompound outputTag = new NBTTagCompound();
		outputTag = output.writeToNBT(outputTag);
		toSend.setTag("output", outputTag);

		NBTTagCompound fluidTag = new NBTTagCompound();
		fluidTag = fluid.writeToNBT(fluidTag);
		toSend.setTag("fluid", fluidTag);
		
		if(chance > 0)
		{
			toSend.setInteger("chance", chance);
			FMLInterModComms.sendMessage(MODID_THERMAL_EXPANSION, "addtransposerextractrecipe", toSend);
		}
		else
			FMLInterModComms.sendMessage(MODID_THERMAL_EXPANSION, "addtransposerfillrecipe", toSend);
	}
	
	public static void addRefineryRecipe(int energy, @Nonnull FluidStack input, @Nonnull FluidStack primaryOutput, @Nonnull ItemStack secondaryOutput)
	{
		NBTTagCompound toSend = new NBTTagCompound();

		toSend.setInteger("energy", energy);

		NBTTagCompound inputTag = new NBTTagCompound();
		inputTag = input.writeToNBT(inputTag);
		toSend.setTag("input", inputTag);

		NBTTagCompound primaryOutputTag = new NBTTagCompound();
		primaryOutputTag = primaryOutput.writeToNBT(primaryOutputTag);
		toSend.setTag("primaryOutput", primaryOutputTag);

		if (!secondaryOutput.isEmpty())
		{
			NBTTagCompound secondaryOutputTag = new NBTTagCompound();
			secondaryOutputTag = secondaryOutput.writeToNBT(secondaryOutputTag);
			toSend.setTag("secondaryOutput", secondaryOutputTag);
		}

		FMLInterModComms.sendMessage(MODID_THERMAL_EXPANSION, "addrefineryrecipe", toSend);
	}
}