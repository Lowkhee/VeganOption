package squeek.veganoption.integration.compat;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemSeedFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Predicate;

import forestry.api.arboriculture.EnumForestryWoodType;
import forestry.api.arboriculture.IWoodType;
import forestry.api.arboriculture.TreeManager;
import forestry.api.arboriculture.WoodBlockKind;
import forestry.api.core.ForestryAPI;
import forestry.api.farming.IFarmRegistry;
import forestry.api.fuels.FermenterFuel;
import forestry.api.fuels.FuelManager;
import forestry.api.fuels.GeneratorFuel;
import forestry.api.recipes.RecipeManagers;
import forestry.apiculture.PluginApiculture;
import forestry.apiculture.blocks.BlockRegistryApiculture;
import forestry.apiculture.items.EnumHoneyComb;
import forestry.apiculture.items.EnumPollenCluster;
import forestry.apiculture.items.ItemRegistryApiculture;
import forestry.arboriculture.PluginArboriculture;
import forestry.arboriculture.WoodAccess;
import forestry.arboriculture.blocks.BlockArbLog;
import forestry.core.PluginCore;
import forestry.core.PluginFluids;
import forestry.core.config.Constants;
import forestry.core.fluids.Fluids;
import forestry.core.items.EnumContainerType;
import forestry.core.items.FluidHandlerItemForestry;
import forestry.core.items.IColoredItem;
import forestry.core.items.ItemRegistryCore;
import forestry.core.items.ItemRegistryFluids;
import forestry.core.models.ModelEntry;
import forestry.core.models.ModelManager;
import forestry.core.proxy.Proxies;
import forestry.core.recipes.RecipeUtil;
import forestry.core.utils.ItemStackUtil;
import forestry.core.utils.Log;
import forestry.core.utils.MigrationHelper;
import forestry.core.utils.OreDictUtil;
import forestry.farming.logic.FarmableDoubleCrop;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.UniversalBucket;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import squeek.veganoption.ModInfo;
import squeek.veganoption.VeganOption;
import squeek.veganoption.backpack.ContainerBackpack;
import squeek.veganoption.blocks.BlockHelianthus;
import squeek.veganoption.blocks.BlockJutePlant;
import squeek.veganoption.content.ContentHelper;
import squeek.veganoption.content.Modifiers;
import squeek.veganoption.integration.IntegratorBase;
import squeek.veganoption.items.ItemBackpack;
import squeek.veganoption.items.ItemWashableWheat;
import squeek.veganoption.content.modules.Composting;
import squeek.veganoption.content.modules.Resin;
import squeek.veganoption.content.modules.Seitan;
import squeek.veganoption.content.modules.Soap;
import squeek.veganoption.content.modules.Ink;
import squeek.veganoption.content.modules.VegetableOil;
import squeek.veganoption.content.modules.Bioplastic;
import squeek.veganoption.content.modules.Burlap;
import squeek.veganoption.content.modules.DollsEye;
import squeek.veganoption.content.modules.Egg;
import squeek.veganoption.content.modules.Ender;
import squeek.veganoption.content.modules.Feather;
import squeek.veganoption.content.modules.FrozenBubble;
import squeek.veganoption.content.modules.Gunpowder;
import squeek.veganoption.content.modules.Jute;
import squeek.veganoption.content.modules.Kapok;
import squeek.veganoption.content.modules.MobHeads;
import squeek.veganoption.content.modules.PlantMilk;
import squeek.veganoption.content.modules.ToxicMushroom;
import squeek.veganoption.content.modules.ProofOfSuffering;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Forestry extends IntegratorBase
{
	protected static final Logger Log = LogManager.getLogger(Forestry.class.getCanonicalName());
	
	public static ItemStack seitanRawStack = Seitan.seitanRawStack.copy();
	public static ItemStack seitanUnwashedStack = Seitan.seitanUnwashedStack.copy();
	public static ItemStack wheatFlourStack = Seitan.wheatFlourStack.copy();
	public static ItemStack wheatDoughStack = Seitan.wheatDoughStack.copy();
	public static ItemStack juteStalk = new ItemStack(Jute.juteStalk);
	public static ItemStack sunflowerSeed = new ItemStack(VegetableOil.seedSunflower);
	public static ItemStack fertilizer = new ItemStack(Composting.fertilizer);
	public static ItemStack juteSeed = new ItemStack(Jute.juteSeeds);
	public static ItemStack compost =  new ItemStack(Composting.compost);
	public static ItemStack falseMorel =  new ItemStack(ToxicMushroom.falseMorel);
	public static ItemStack falseMorelFermented = new ItemStack(ToxicMushroom.falseMorelFermented);
	public static ItemStack resin = new ItemStack(Resin.resin);
	public static ItemStack rosin = new ItemStack(Resin.rosin);
	public static ItemStack juteFibre = new ItemStack(Jute.juteFibre);
	public static ItemStack seitanCooked = new ItemStack(Seitan.seitanCooked);
	public static ItemStack kapokTuft = new ItemStack(Kapok.kapokTuft);
	public static ItemStack fragmentOfSuffering = new ItemStack(ProofOfSuffering.fragmentOfSuffering);
	public static ItemStack proofOfSuffering = new ItemStack(ProofOfSuffering.proofOfSuffering);
	public static ItemStack waxVegetable = new ItemStack(Ink.waxVegetable);
	public static ItemStack sulfur = new ItemStack(Gunpowder.sulfur);
	public static ItemStack saltpeter = new ItemStack(Gunpowder.saltpeter);
	public static ItemStack frozenBubble = new ItemStack(FrozenBubble.frozenBubble);
	public static ItemStack fauxFeather = new ItemStack(Feather.fauxFeather);
	public static ItemStack potatoStarch = new ItemStack(Egg.potatoStarch);
	public static ItemStack appleSauce = new ItemStack(Egg.appleSauce);
	public static ItemStack plasticEgg = new ItemStack(Egg.plasticEgg);
	public static ItemStack dollsEye = new ItemStack(DollsEye.dollsEye);
	public static ItemStack rottenPlants = new ItemStack(Composting.rottenPlants);
	public static ItemStack burlap = new ItemStack(Burlap.burlap);
	public static ItemStack bioPlastic = new ItemStack(Bioplastic.bioplastic);
	public static ItemStack plasticRod = new ItemStack(Bioplastic.plasticRod);
	
	public static ItemStack[] itemList = {appleSauce,bioPlastic,burlap,compost,dollsEye,falseMorel,falseMorelFermented,fauxFeather,fertilizer,fragmentOfSuffering,frozenBubble,juteFibre,juteSeed,juteStalk,kapokTuft,plasticEgg,plasticRod,potatoStarch,proofOfSuffering,resin,rosin,rottenPlants,saltpeter,seitanCooked,sulfur,sunflowerSeed,waxVegetable,seitanRawStack,seitanUnwashedStack,wheatDoughStack,wheatFlourStack};
	public static List<VeganCrate> crates = new ArrayList<VeganCrate>();
	
	public Block juteCrop = Jute.jutePlant;
	public Block helianthus = VegetableOil.helianthus;
	
	public static VeganCrate crate;
	
	public static final String VeganFermenter = "veganfermenter";
	
	public static Predicate<ItemStack> filter = new Predicate<ItemStack>()
	{
		public boolean test(ItemStack stack){return true;}
		public boolean isEqual(ItemStack stack){return true;}
	};
	
	
	@Override
	public void preInit()
	{
		super.preInit();
		MinecraftForge.EVENT_BUS.register(this);
	
	}
	
	@Override
	public void create()
	{
		registerCrateModel();
		crate = new VeganCrate(ItemStack.EMPTY, null);
		crate.setUnlocalizedName(ModInfo.MODID + ".crate");
		crate.setRegistryName("crate");
		MigrationHelper.addItemName("crate");
		GameRegistry.register(crate);
		Proxies.common.registerItem(crate);
		crate.registerModel(crate, null);
		registerCrates();
		
		//Predicate<ItemStack> filter = BackpackManager.backpackInterface.createNaturalistBackpackFilter("rootBees");
		//backpackInterface = new BackpackInterface(); //`BackpackManager.backpackInterface;
		
		/*BackpackDefinition definition = new BackpackDefinition(Color.WHITE, Color.GREEN, filter); //, filter);
		//IBackpackInterface backpackInterface = new VeganBackpackInterface(definition);
		//backpackInterface.registerBackpackDefinition(VEGAN_BACKPACK, definition);
		BackpackDefinition definitionT2 = new BackpackDefinition(new Color(0xc5a981), Color.BLUE, filter); //, filter);
		//backpackInterface.registerBackpackDefinition(VEGAN_BACKPACK, definition);
		
		veganBackpack = new Backpack(definition, EnumBackpackType.NORMAL); //;backpackInterface.createBackpack(VEGAN_BACKPACK, EnumBackpackType.NORMAL);
		veganBackpack.setUnlocalizedName(ModInfo.MODID + ".vegan_bag");
		veganBackpack.setRegistryName("vegan_bag");
		MigrationHelper.addItemName("vegan_bag");
		GameRegistry.register(veganBackpack);
		Proxies.common.registerItem(veganBackpack);
		veganBackpackT2 = veganBackpack = new Backpack(definitionT2, EnumBackpackType.WOVEN);//backpackInterface.createBackpack(VEGAN_BACKPACK, EnumBackpackType.WOVEN); 
		veganBackpackT2.setUnlocalizedName(ModInfo.MODID + ".vegan_bag_t2");
		veganBackpackT2.setRegistryName("vegan_bag_t2");
		MigrationHelper.addItemName("vegan_bag_t2");
		GameRegistry.register(veganBackpackT2);
		Proxies.common.registerItem(veganBackpackT2);*/
	}
	
	@Override
	public void oredict()
	{
		OreDictionary.registerOre(VeganFermenter, seitanCooked);
		OreDictionary.registerOre(VeganFermenter, potatoStarch);
		OreDictionary.registerOre(VeganFermenter, rottenPlants);
		OreDictionary.registerOre(VeganFermenter, falseMorel);
		OreDictionary.registerOre(VeganFermenter, juteStalk);
		OreDictionary.registerOre(VeganFermenter, appleSauce);
		OreDictionary.registerOre(VeganFermenter, seitanRawStack);
		OreDictionary.registerOre(VeganFermenter, wheatFlourStack);
		OreDictionary.registerOre(VeganFermenter, seitanUnwashedStack);
		OreDictionary.registerOre(VeganFermenter, wheatDoughStack); 
		OreDictionary.registerOre(VeganFermenter, rottenPlants);
		OreDictionary.registerOre(VeganFermenter, fertilizer);
		OreDictionary.registerOre(VeganFermenter, compost);
		OreDictionary.registerOre(VeganFermenter, new ItemStack(Items.APPLE));
		OreDictionary.registerOre(VeganFermenter, new ItemStack(Items.POTATO));
		OreDictionary.registerOre(VeganFermenter, new ItemStack(Items.CARROT));
		OreDictionary.registerOre(VeganFermenter, new ItemStack(Items.BEETROOT));
		OreDictionary.registerOre(VeganFermenter, new ItemStack(Items.BREAD));
		OreDictionary.registerOre(VeganFermenter, new ItemStack(Items.MELON));
		OreDictionary.registerOre(VeganFermenter, new ItemStack(Items.SUGAR));
		OreDictionary.registerOre(VeganFermenter, new ItemStack(Items.WHEAT));
		OreDictionary.registerOre(VeganFermenter, new ItemStack(Item.getItemFromBlock(Blocks.CACTUS)));
		//OreDictionary.registerOre(VeganFermenter, new ItemStack(Item.getItemFromBlock(Blocks.COCOA)));
		OreDictionary.registerOre(VeganFermenter, new ItemStack(Item.getItemFromBlock(Blocks.PUMPKIN)));
		
	}
	
	@Override
	public void init()
	{
		super.init();
		
		ItemStack beeswax = new ItemStack(PluginCore.getItems().beeswax);
		if(beeswax != null) 
		{
			Modifiers.recipes.convertInput(beeswax, ContentHelper.waxOreDict);
			ContentHelper.remapOre(OreDictUtil.ITEM_BEESWAX, ContentHelper.waxOreDict);
			ContentHelper.remapOre(ContentHelper.waxOreDict, ContentHelper.waxOreDictForestry);
		}
		
		
		ItemColors itemColors = Minecraft.getMinecraft().getItemColors();
		for(int i = 0; i < itemList.length; i++)
			itemColors.registerItemColorHandler(ColoredItemItemColor.INSTANCE, itemList[i].getItem());
	}
	
	@Override
	public void recipes()
	{
		super.recipes();		
		registerMoistenerRecipes();
		//registerSqueezerRecipes();
		registerCarpenterRecipes();
		registerFermenterRecipes();
		registerVegetableBeeWaxRecipes();
		
		//replace Bee related items
		ItemRegistryApiculture items = PluginApiculture.getItems();
		ItemStack stack = items.beeComb.get(EnumHoneyComb.COCOA, 1);
		GameRegistry.addShapelessRecipe(items.beeComb.get(EnumHoneyComb.HONEY, 5), new ItemStack(Bioplastic.bioplastic), new ItemStack(Bioplastic.plasticRod), new ItemStack(Ink.waxVegetable));
		GameRegistry.addShapelessRecipe(items.beeComb.get(EnumHoneyComb.COCOA, 5), new ItemStack(Bioplastic.bioplastic), new ItemStack(Bioplastic.plasticRod), new ItemStack(Ink.waxVegetable),
				new ItemStack(Items.SUGAR), new ItemStack(Items.DYE, 2, 3));
		GameRegistry.addShapelessRecipe(items.beeComb.get(EnumHoneyComb.DARKENED, 5), new ItemStack(Bioplastic.bioplastic), new ItemStack(Bioplastic.plasticRod), new ItemStack(Ink.waxVegetable),
				new ItemStack(Items.SUGAR), new ItemStack(Items.COAL, 2));
		GameRegistry.addShapelessRecipe(items.beeComb.get(EnumHoneyComb.DRIPPING, 5), new ItemStack(Bioplastic.bioplastic), new ItemStack(Bioplastic.plasticRod), new ItemStack(Ink.waxVegetable),
				new ItemStack(Items.SUGAR), PlantMilk.bucketPlantMilk.copy());
		GameRegistry.addShapelessRecipe(items.beeComb.get(EnumHoneyComb.FROZEN, 5), new ItemStack(Bioplastic.bioplastic), new ItemStack(Bioplastic.plasticRod), new ItemStack(Ink.waxVegetable),
				new ItemStack(Items.SUGAR), new ItemStack(Blocks.ICE, 2));
		//Irradiated appears to be Dripping
		GameRegistry.addShapelessRecipe(items.beeComb.get(EnumHoneyComb.IRRADIATED, 5).copy(), new ItemStack(Bioplastic.bioplastic), new ItemStack(Bioplastic.plasticRod), new ItemStack(Ink.waxVegetable),
				new ItemStack(Items.SUGAR), Soap.bucketLyeWater.copy());
		GameRegistry.addShapelessRecipe(items.beeComb.get(EnumHoneyComb.MELLOW, 5), new ItemStack(Bioplastic.bioplastic), new ItemStack(Bioplastic.plasticRod), new ItemStack(Ink.waxVegetable),
				new ItemStack(Items.SUGAR), new ItemStack(Items.MELON, 3));
		GameRegistry.addShapelessRecipe(items.beeComb.get(EnumHoneyComb.MOSSY, 5), new ItemStack(Bioplastic.bioplastic), new ItemStack(Bioplastic.plasticRod), new ItemStack(Ink.waxVegetable),
				new ItemStack(Items.SUGAR), new ItemStack(Blocks.VINE, 3));
		GameRegistry.addShapelessRecipe(items.beeComb.get(EnumHoneyComb.MYSTERIOUS, 5), new ItemStack(Bioplastic.bioplastic), new ItemStack(Bioplastic.plasticRod), new ItemStack(Ink.waxVegetable),
				new ItemStack(Items.SUGAR), new ItemStack(Items.QUARTZ));
		GameRegistry.addShapelessRecipe(items.beeComb.get(EnumHoneyComb.OMEGA, 5), new ItemStack(Bioplastic.bioplastic), new ItemStack(Bioplastic.plasticRod), new ItemStack(Ink.waxVegetable),
				new ItemStack(Items.SUGAR), new ItemStack(Items.ENDER_PEARL));
		GameRegistry.addShapelessRecipe(items.beeComb.get(EnumHoneyComb.PARCHED, 5), new ItemStack(Bioplastic.bioplastic), new ItemStack(Bioplastic.plasticRod), new ItemStack(Ink.waxVegetable),
				new ItemStack(Items.SUGAR), new ItemStack(Blocks.SAND, 2));
		GameRegistry.addShapelessRecipe(items.beeComb.get(EnumHoneyComb.POWDERY, 5), new ItemStack(Bioplastic.bioplastic), new ItemStack(Bioplastic.plasticRod), new ItemStack(Ink.waxVegetable),
				new ItemStack(Items.SUGAR), new ItemStack(Items.BONE, 2));
		GameRegistry.addShapelessRecipe(items.beeComb.get(EnumHoneyComb.REDDENED, 5), new ItemStack(Bioplastic.bioplastic), new ItemStack(Bioplastic.plasticRod), new ItemStack(Ink.waxVegetable),
				new ItemStack(Items.SUGAR), new ItemStack(Items.REDSTONE, 2));
		GameRegistry.addShapelessRecipe(items.beeComb.get(EnumHoneyComb.SILKY, 5), new ItemStack(Bioplastic.bioplastic), new ItemStack(Bioplastic.plasticRod), new ItemStack(Ink.waxVegetable),
				new ItemStack(Items.SUGAR), new ItemStack(Blocks.WEB, 2)); //Item.getItemFromBlock(blockIn) 
		GameRegistry.addShapelessRecipe(items.beeComb.get(EnumHoneyComb.SIMMERING, 5), new ItemStack(Bioplastic.bioplastic), new ItemStack(Items.BLAZE_ROD), new ItemStack(Ink.waxVegetable),
				new ItemStack(Items.SUGAR));
		GameRegistry.addShapelessRecipe(items.beeComb.get(EnumHoneyComb.STRINGY, 5), new ItemStack(Bioplastic.bioplastic), new ItemStack(Bioplastic.plasticRod), new ItemStack(Ink.waxVegetable),
				new ItemStack(Items.SUGAR), new ItemStack(Items.IRON_INGOT));
		GameRegistry.addShapelessRecipe(items.beeComb.get(EnumHoneyComb.WHEATEN, 5), new ItemStack(Bioplastic.bioplastic), new ItemStack(Bioplastic.plasticRod), new ItemStack(Ink.waxVegetable),
				new ItemStack(Items.SUGAR), new ItemStack(Items.WHEAT,2));
		
		GameRegistry.addRecipe(new ShapelessOreRecipe(UniversalBucket.getFilledBucket(ForgeModContainer.getInstance().universalBucket, Fluids.ICE.getFluid()),
				  Soap.bucketLyeWater.copy(),
				  new ItemStack(Blocks.PACKED_ICE, 10)));
		
		
	}

	@Override
	public void postInit() 
	{
		
		super.postInit();
		
		//unmanaged farms
		IFarmRegistry farmRegistry = ForestryAPI.farmRegistry;
		if(juteSeed != null && juteCrop != null && juteCrop != Blocks.AIR) 
		{
			PropertyBool propertyTop = BlockJutePlant.HAS_TOP;
			PropertyInteger propertyGrowth = BlockJutePlant.GROWTH_STAGE;
			
			if (propertyTop != null && propertyGrowth != null) 
			{
				IBlockState defaultState = juteCrop.getDefaultState();
				IBlockState planted = defaultState.withProperty(BlockJutePlant.GROWTH_STAGE, 0).withProperty(BlockJutePlant.HAS_TOP, false).withProperty(BlockJutePlant.HALF, BlockDoublePlant.EnumBlockHalf.LOWER);
				IBlockState mature = defaultState.withProperty(BlockJutePlant.GROWTH_STAGE, 6).withProperty(BlockJutePlant.HAS_TOP, true).withProperty(BlockJutePlant.HALF, BlockDoublePlant.EnumBlockHalf.LOWER);; //Blocks.DOUBLE_PLANT.getDefaultState().withProperty(BlockDoublePlant.VARIANT, BlockDoublePlant.EnumPlantType.FERN).withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.LOWER);//defaultState.withProperty(propertyGrowth, 6);
				IBlockState topMature = defaultState.withProperty(BlockJutePlant.GROWTH_STAGE, 10).withProperty(BlockJutePlant.HAS_TOP, false).withProperty(BlockJutePlant.HALF, BlockDoublePlant.EnumBlockHalf.UPPER);; //Blocks.DOUBLE_PLANT.getDefaultState().withProperty(BlockDoublePlant.VARIANT, BlockDoublePlant.EnumPlantType.FERN).withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.UPPER);//defaultState.withProperty(propertyTop, true);

				farmRegistry.registerFarmables("farmWheat", new FarmableDoubleCrop(juteSeed, planted, mature, topMature, true));
				farmRegistry.registerFarmables("farmOrchard", new FarmableDoubleCrop(juteSeed, planted, mature, topMature, true));
			}
		}
		
		if(sunflowerSeed != null && helianthus != null && helianthus != Blocks.AIR)
		{
			PropertyBool propertyTop = BlockHelianthus.HAS_TOP;
			PropertyInteger propertyGrowth = BlockHelianthus.GROWTH_STAGE;
			
			if (propertyTop != null && propertyGrowth != null) 
			{
				IBlockState defaultState = helianthus.getDefaultState();
				IBlockState planted = defaultState.withProperty(BlockHelianthus.GROWTH_STAGE, 1).withProperty(BlockHelianthus.HAS_TOP, false).withProperty(BlockHelianthus.HALF, BlockDoublePlant.EnumBlockHalf.LOWER);
				IBlockState mature = defaultState.withProperty(BlockHelianthus.GROWTH_STAGE, 5).withProperty(BlockHelianthus.HAS_TOP, true).withProperty(BlockHelianthus.HALF, BlockDoublePlant.EnumBlockHalf.LOWER);; //Blocks.DOUBLE_PLANT.getDefaultState().withProperty(BlockDoublePlant.VARIANT, BlockDoublePlant.EnumPlantType.FERN).withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.LOWER);//defaultState.withProperty(propertyGrowth, 6);
				IBlockState topMature = defaultState.withProperty(BlockHelianthus.GROWTH_STAGE, 9).withProperty(BlockHelianthus.HAS_TOP, false).withProperty(BlockHelianthus.HALF, BlockDoublePlant.EnumBlockHalf.UPPER);; //Blocks.DOUBLE_PLANT.getDefaultState().withProperty(BlockDoublePlant.VARIANT, BlockDoublePlant.EnumPlantType.FERN).withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.UPPER);//defaultState.withProperty(propertyTop, true);

				farmRegistry.registerFarmables("farmWheat", new FarmableDoubleCrop(sunflowerSeed, planted, mature, topMature, true));
				farmRegistry.registerFarmables("farmOrchard", new FarmableDoubleCrop(sunflowerSeed, planted, mature, topMature, true));
			}
		}
		
		//add creosote oil as bio fuel
		if (ImmersiveEngineering.creosote != null) 
		{
			GeneratorFuel creosoteFuel = new GeneratorFuel(new FluidStack(ImmersiveEngineering.creosote, 1), (int) (32 * ForestryAPI.activeMode.getFloatSetting("fuel.creosote.generator")), 8); //8
			FuelManager.generatorFuel.put(ImmersiveEngineering.creosote, creosoteFuel);
		}
		
		//for(Fluid fluid : FluidRegistry.getBucketFluids())
		//{
		//	Log.log(Level.INFO, fluid.getName());
		//}
		
		//fertilizer alt
		if(fertilizer != null)
		{
			farmRegistry.registerFertilizer(fertilizer, 400);
			int cyclesCompost = ForestryAPI.activeMode.getIntegerSetting("fermenter.cycles.compost");
			int valueCompost = ForestryAPI.activeMode.getIntegerSetting("fermenter.value.compost");
			FuelManager.fermenterFuel.put(fertilizer, new FermenterFuel(fertilizer, valueCompost, cyclesCompost));
		}
		if(compost != null)
		{
			farmRegistry.registerFertilizer(compost, 500);
			int cyclesCompost = ForestryAPI.activeMode.getIntegerSetting("fermenter.cycles.compost");
			int valueCompost = ForestryAPI.activeMode.getIntegerSetting("fermenter.value.compost");
			FuelManager.fermenterFuel.put(compost, new FermenterFuel(compost, valueCompost, cyclesCompost));
		}	
		
	}
	
	public static void registerCarpenterRecipes()
	{
		for (VeganCrate crate : crates)
		{
			ItemStack crateStack = new ItemStack(crate);
			ItemStack uncrated = crate.getContained();
			if (!uncrated.isEmpty()) 
			{
				addCrating(crateStack, uncrated);
				addUncrating(crateStack, uncrated);
			}
		}
		
		//Carpenter recipes
		RecipeManagers.carpenterManager.addRecipe(10, new FluidStack(VegetableOil.fluidVegetableOil, Fluid.BUCKET_VOLUME), ItemStack.EMPTY, new ItemStack(Ink.blackVegetableOilInk), 
				" - ",
				" # ",
				" = ",
				'#', new ItemStack(Resin.rosin),
				'-', ContentHelper.blackPigmentOreDict, //new ItemStack(Items.COAL, 1, 1),
				'=', new ItemStack(Items.GLASS_BOTTLE));
		RecipeManagers.carpenterManager.addRecipe(10, new FluidStack(VegetableOil.fluidVegetableOil, Fluid.BUCKET_VOLUME), ItemStack.EMPTY, new ItemStack(Ink.whiteVegetableOilInk),
				" - ",
				" # ",
				" = ",
				'#', new ItemStack(Resin.rosin),
				'-', ContentHelper.whitePigmentOreDict,
				'=', new ItemStack(Items.GLASS_BOTTLE));
		RecipeManagers.carpenterManager.addRecipe(10, new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME), ItemStack.EMPTY, new ItemStack(MobHeads.papierMache), 
				" + ",
				"+-+",
				" + ",
				'+', ContentHelper.paperOreDict,
				'-', ContentHelper.starchOreDict); //new ItemStack(Egg.potatoStarch));
		RecipeManagers.carpenterManager.addRecipe(10, new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME), ItemStack.EMPTY, new ItemStack(FrozenBubble.soapSolution), 
				" + ",
				" - ",
				" = ",
				'+', ContentHelper.soapOreDict,
				'-', new ItemStack(Items.SUGAR),
				'=', new ItemStack(Items.GLASS_BOTTLE));
		RecipeManagers.carpenterManager.addRecipe(10, new FluidStack(Soap.fluidLyeWater, Fluid.BUCKET_VOLUME), ItemStack.EMPTY, new ItemStack(Soap.soap), 
				"   ",
				" - ",
				" + ",
				'+', ContentHelper.waxOreDict,
				'-', new ItemStack(Resin.rosin));
		RecipeManagers.carpenterManager.addRecipe(10, new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME * 6), ItemStack.EMPTY, new ItemStack(Seitan.washableWheat, 1, ItemWashableWheat.META_RAW), 
				"   ",
				" = ",
				"   ",
				'=', new ItemStack(Seitan.washableWheat, 1, ItemWashableWheat.META_FLOUR));

		//add generic crate
		RecipeManagers.carpenterManager.addRecipe(20, new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME/4), ItemStack.EMPTY, new ItemStack(crate, 8),
				" # ", "# #", " # ", '#', "plankWood");
		//veg wax from veg oil and coal hydrogenation - 
		RecipeManagers.carpenterManager.addRecipe(10, new FluidStack(VegetableOil.fluidVegetableOil, Fluid.BUCKET_VOLUME*2), ItemStack.EMPTY, new ItemStack(Ink.waxVegetable, 6), 
				"   ",
				" # ",
				"   ",
				'#', new ItemStack(Items.COAL));
		RecipeManagers.carpenterManager.addRecipe(10, new FluidStack(FluidRegistry.LAVA, Fluid.BUCKET_VOLUME), ItemStack.EMPTY, new ItemStack(Items.BLAZE_ROD),
				"#-=",
				"   ",
				"   ",
				'-', ContentHelper.waxOreDict,
				'#', new ItemStack(Bioplastic.plasticRod),
				'=', Resin.rosin);
		RecipeManagers.carpenterManager.addRecipe(10, new FluidStack(Soap.fluidLyeWater, Fluid.BUCKET_VOLUME), ItemStack.EMPTY, PluginApiculture.getItems().beeComb.get(EnumHoneyComb.DRIPPING, 5).copy(), 
				" + ",
				" * ",
				"=- ",
				'+', ContentHelper.waxOreDict,
				'-', new ItemStack(Bioplastic.bioplastic),
				'=', new ItemStack(Bioplastic.plasticRod),
				'*', new ItemStack(Items.SUGAR));
	}
	
	//!!!!crashes - disabled
	public static void registerSqueezerRecipes()
	{
		RecipeManagers.squeezerManager.addRecipe(10, new ItemStack(Jute.juteSeeds), new FluidStack(VegetableOil.fluidVegetableOil, 200));
		RecipeManagers.squeezerManager.addRecipe(10, new ItemStack(VegetableOil.seedSunflower), new FluidStack(VegetableOil.fluidVegetableOil, 200));
		if(Fluids.SEED_OIL.getFluid() != null)
			RecipeManagers.squeezerManager.addRecipe(10, new ItemStack(Items.WHEAT_SEEDS), new FluidStack(Fluids.SEED_OIL.getFluid(), 25)); //Fluids.SEED_OIL.getFluid(1000));
		FluidHandlerItemForestry can = new FluidHandlerItemForestry(new ItemStack(PluginFluids.getItems().canEmpty), EnumContainerType.CAN);
		can.fill(new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME), true);
		NonNullList stackList = NonNullList.withSize(2, Arrays.asList(new ItemStack(Items.COAL), can.getContainer()));
		RecipeManagers.squeezerManager.addRecipe(20, stackList, new FluidStack(Soap.fluidLyeWater, Fluid.BUCKET_VOLUME), new ItemStack(PluginFluids.getItems().canEmpty), 100);
	
	}
	
	//Moistener
	public static void registerMoistenerRecipes()
	{
		if(rottenPlants != null)
			RecipeManagers.moistenerManager.addRecipe(rottenPlants, falseMorel.copy(), 10000);
		if(potatoStarch != null)
			RecipeManagers.moistenerManager.addRecipe(potatoStarch, new ItemStack(Blocks.MYCELIUM), 6000);
		if(juteStalk != null)
			RecipeManagers.moistenerManager.addRecipe(juteStalk, new ItemStack(Blocks.SPONGE), 7000);
		if(wheatFlourStack != null)
			RecipeManagers.moistenerManager.addRecipe(wheatFlourStack, new ItemStack(Blocks.MYCELIUM), 8000);
		if(wheatDoughStack != null)
			RecipeManagers.moistenerManager.addRecipe(wheatDoughStack, new ItemStack(Blocks.MYCELIUM), 6000);
		if(seitanUnwashedStack != null)
			RecipeManagers.moistenerManager.addRecipe(seitanUnwashedStack, new ItemStack(Blocks.MYCELIUM), 5000);
		if(appleSauce != null)
			RecipeManagers.moistenerManager.addRecipe(appleSauce, new ItemStack(Blocks.MYCELIUM), 7000);
		if(seitanRawStack != null)
			RecipeManagers.moistenerManager.addRecipe(seitanRawStack, falseMorel.copy(), 6000);
		
	}
	
	public static class FermentedLiquid
	{
		public FluidStack liquidStack;
		public float value;
		FermentedLiquid(FluidStack stack, float modifier) {liquidStack = stack; value = modifier;}
	}
	
	//Fermenter
	public static void registerFermenterRecipes()
	{
		LinkedHashSet <FermentedLiquid> fermentedLiquids = new LinkedHashSet<FermentedLiquid>();
		fermentedLiquids.add(new FermentedLiquid(new FluidStack(FluidRegistry.WATER, 1), 1.0f));
		if(Fluids.JUICE.getFluid(1) != null)
			fermentedLiquids.add(new FermentedLiquid(Fluids.JUICE.getFluid(1), 1.5f));
		if(Fluids.FOR_HONEY.getFluid(1) != null)
			fermentedLiquids.add(new FermentedLiquid(Fluids.FOR_HONEY.getFluid(1), 2.0f));
		fermentedLiquids.add(new FermentedLiquid(new FluidStack(PlantMilk.fluidPlantMilk, 1), 2.5f));
		fermentedLiquids.add(new FermentedLiquid(new FluidStack(VegetableOil.fluidVegetableOil, 1), 3.5f));
		if(ImmersiveEngineering.creosote != null)
			fermentedLiquids.add(new FermentedLiquid(new FluidStack(ImmersiveEngineering.creosote, 1), 3.0f));
		fermentedLiquids.add(new FermentedLiquid(new FluidStack(Ender.fluidRawEnder, 1), 6.0f));
		int fermentValue = ForestryAPI.activeMode.getIntegerSetting("fermenter.yield.wheat");
		
		for(FermentedLiquid fermentedLiquid : fermentedLiquids)
		{
			RecipeManagers.fermenterManager.addRecipe(VeganFermenter, fermentValue * 9, fermentedLiquid.value, Fluids.BIOMASS.getFluid(1), fermentedLiquid.liquidStack);
			RecipeManagers.fermenterManager.addRecipe(OreDictUtil.TREE_SAPLING, fermentValue * 9, fermentedLiquid.value, Fluids.BIOMASS.getFluid(1), fermentedLiquid.liquidStack);
			RecipeManagers.fermenterManager.addRecipe(OreDictUtil.TREE_LEAVES, fermentValue * 9, fermentedLiquid.value, Fluids.BIOMASS.getFluid(1), fermentedLiquid.liquidStack);	
		}
		
	}
	
	//crates
	public static void registerCrates() 
	{
		for(int i = 0; i < itemList.length; i++)
		{
			if (itemList[i] != null)
			{
				registerCrate(itemList[i]);
				ModelManager.getInstance().registerItemClient(itemList[i].getItem());
			}
			
		}
		for(VeganCrate crate : crates)
			crate.registerModel(crate, null);
	}

	private static void addCrating(ItemStack crateStack, ItemStack uncrated) 
	{
		FluidStack water = new FluidStack(FluidRegistry.WATER, Constants.CARPENTER_CRATING_LIQUID_QUANTITY);
		ItemStack box = new ItemStack(crate);
		RecipeManagers.carpenterManager.addRecipe(Constants.CARPENTER_CRATING_CYCLES, water, box, crateStack, "###", "###", "###", '#', uncrated);
	}

	private static void addUncrating(ItemStack crateStack, ItemStack uncrated) 
	{
		ItemStack product = new ItemStack(uncrated.getItem(), 9, uncrated.getItemDamage());
		RecipeManagers.carpenterManager.addRecipe(Constants.CARPENTER_UNCRATING_CYCLES, ItemStack.EMPTY, product, "#", '#', crateStack);
	}
	
	
	public static void registerCrate(ItemStack itemToCrate) 
	{
		if (itemToCrate.isEmpty()) {
			Log.error("Tried to make a crate without an item");
			return;
		}

		String crateName;
		String stringForItemStack = ItemStackUtil.getStringForItemStack(itemToCrate);
		if (stringForItemStack == null) 
		{
			Log.error("Could not get string name for itemStack {}", itemToCrate);
			return;
		}
		
		if(itemToCrate.getItem() instanceof ItemWashableWheat)
			crateName = "crated." + ItemWashableWheat.getName(itemToCrate);
		else
			crateName = "crated." + itemToCrate.getItem().getRegistryName().getResourcePath();
		
		VeganCrate crate = new VeganCrate(itemToCrate, crateName);

		crate.setUnlocalizedName(ModInfo.MODID + "." + crateName);
		//crate.setUnlocalizedName(crateName);
		crate.setRegistryName(crateName);

		MigrationHelper.addItemName(crateName);
		GameRegistry.register(crate);
		Proxies.common.registerItem(crate);
		crates.add(crate);
	}
	
	@SideOnly(Side.CLIENT)
	private static class ColoredItemItemColor implements IItemColor
	{
		public static final ColoredItemItemColor INSTANCE = new ColoredItemItemColor();

		private ColoredItemItemColor() {

		}

		@Override
		public int getColorFromItemstack(ItemStack stack, int tintIndex) 
		{
			Item item = stack.getItem();
			if (item instanceof IColoredItem) {
				return ((IColoredItem) item).getColorFromItemstack(stack, tintIndex);
			}
			return 0xffffff;
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onBakeModel(ModelBakeEvent event) 
	{
		ModelCrate.onModelBake(event);
	}
	
	public void registerCrateModel() 
	{
		ModelResourceLocation modelLocation = new ModelResourceLocation("veganoption:crate-filled", "crate-filled");
		ModelEntry modelEntry = new ModelEntry(modelLocation, new ModelCrate());
		ModelManager.getInstance().registerCustomModel(modelEntry);
	}
	
	private static ItemStack getItemStack(String itemName, int meta, String modID) 
	{
		ResourceLocation key = new ResourceLocation(modID, itemName);
		if (ForgeRegistries.ITEMS.containsKey(key)) 
			return new ItemStack(ForgeRegistries.ITEMS.getValue(key), 1, meta);
		else 
			return ItemStack.EMPTY;
	}
	
	public static void registerVegetableBeeWaxRecipes()
	{
		ItemRegistryFluids fluidItems = PluginFluids.getItems();
		ItemRegistryCore coreItems = PluginCore.getItems();
		ItemRegistryApiculture items = PluginApiculture.getItems();
		BlockRegistryApiculture blocks = PluginApiculture.getBlocks();
		
		int outputCapsuleAmount = ForestryAPI.activeMode.getIntegerSetting("recipe.output.capsule");
		if (outputCapsuleAmount > 0) 
		{
			ItemStack capsule = fluidItems.waxCapsuleEmpty.getItemStack(outputCapsuleAmount);
			RecipeUtil.addRecipe(capsule, "###", '#', waxVegetable);
		}

		int outputRefractoryAmount = ForestryAPI.activeMode.getIntegerSetting("recipe.output.refractory");
		if (outputRefractoryAmount > 0) 
		{
			ItemStack capsule = fluidItems.refractoryEmpty.getItemStack(outputRefractoryAmount);
			RecipeUtil.addRecipe(capsule, "###", '#', waxVegetable);
		}
		RecipeUtil.addRecipe(new ItemStack(Blocks.TORCH, 3),
				" # ", " # ", " Y ",
				'#', waxVegetable,
				'Y', OreDictUtil.STICK_WOOD);
		RecipeUtil.addRecipe(items.waxCast,
				"###",
				"# #",
				"###",
				'#', waxVegetable);
		RecipeManagers.carpenterManager.addRecipe(50, Fluids.FOR_HONEY.getFluid(500), ItemStack.EMPTY, coreItems.craftingMaterial.getScentedPaneling(),
				" J ", "###", "WPW",
				'#', OreDictUtil.PLANK_WOOD,
				'J', items.royalJelly,
				'W', waxVegetable,
				'P', items.pollenCluster.get(EnumPollenCluster.NORMAL, 1));

		RecipeManagers.carpenterManager.addRecipe(30, new FluidStack(FluidRegistry.WATER, 600), ItemStack.EMPTY, blocks.candle.getUnlitCandle(24),
				" X ",
				"###",
				"###",
				'#', waxVegetable,
				'X', Items.STRING);
		RecipeManagers.carpenterManager.addRecipe(10, new FluidStack(FluidRegistry.WATER, 200), ItemStack.EMPTY, blocks.candle.getUnlitCandle(6),
				"#X#",
				'#', waxVegetable,
				'X', coreItems.craftingMaterial.getSilkWisp());
	}

	public static void getForestrySaplings(EntityPlayer player, ItemStack heldItemStack, int ordinal) 
	{
		NonNullList<ItemStack> saplingList = NonNullList.create();
		PluginArboriculture.getItems().sapling.addCreativeItems(saplingList, false);
		ContainerBackpack backpackContainer = (ContainerBackpack) ((ItemBackpack)heldItemStack.getItem()).getContainer(player, heldItemStack, ordinal);
		
		for(int j = 0; j < saplingList.size(); j++)
		{
			backpackContainer.inventory.setInventorySlotContents(j, saplingList.get(j));
			//Log.error("--------------------------------");
			//Log.error(treeList.get(j).getDisplayName());
			//Log.error("--------------------------------");
		}
		
		/*
		  int slot = 0;
		 WoodAccess woodAccess = WoodAccess.getInstance();
		for(IWoodType woodType : woodAccess.getRegisteredWoodTypes())
		{
			backpackContainer.inventory.setInventorySlotContents(slot,  woodAccess.getStack(woodType, WoodBlockKind.LOG, false));
			slot++;
		}*/
		//int seedCount = 2;
		//for (ItemStack treeStack : treeList) 
		//{
		///	GameRegistry.addShapelessRecipe(treeStack, new ItemStack(Items.WHEAT_SEEDS, seedCount++));
		//}
		
	}
}


