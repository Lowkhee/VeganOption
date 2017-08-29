package squeek.veganoption;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import squeek.veganoption.content.ContentModuleHandler;
import squeek.veganoption.content.Modifiers;
import squeek.veganoption.content.crafting.PistonCraftingHandler;
import squeek.veganoption.helpers.FluidContainerHelper;
import squeek.veganoption.helpers.GuiHelper;
import squeek.veganoption.helpers.TooltipHelper;
import squeek.veganoption.integration.IntegrationHandler;
import squeek.veganoption.network.NetworkHandler;


// dependency of after:* seems necessary to ensure that the RecipeModifier doesn't miss any recipes //still?
@Mod(
		modid = "veganoption", 
		name = "The Vegan Option",
		version = "0.1.0",
		acceptedMinecraftVersions = "1.11.2", 
		dependencies = "required-after:forge@[13.20.1.2386,)" 
	)
public class VeganOption
{
	private static final Logger Log = LogManager.getLogger(VeganOption.class.getCanonicalName());

	static 
	{
		FluidRegistry.enableUniversalBucket();
	}

	@Mod.Instance(ModInfo.MODID)
	public static VeganOption instance;

	// creative tab initialized in CreativeTabProxy#create
	public static CreativeTabs creativeTab;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		ContentModuleHandler.preInit();
		VeganOption.Log.log(squeek.veganoption.ModInfo.debugLevel, "pre-init Content complete");
		IntegrationHandler.preInit();
		VeganOption.Log.log(squeek.veganoption.ModInfo.debugLevel, "pre-init Integration complete");
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event)
	{
		FluidContainerHelper.init();
		VeganOption.Log.log(squeek.veganoption.ModInfo.debugLevel, "init fluid complete");
		GuiHelper.init();
		VeganOption.Log.log(squeek.veganoption.ModInfo.debugLevel, "init gui complete");
		TooltipHelper.init();
		VeganOption.Log.log(squeek.veganoption.ModInfo.debugLevel, "init tooltip complete");
		NetworkHandler.init();
		VeganOption.Log.log(squeek.veganoption.ModInfo.debugLevel, "init network complete");
		ContentModuleHandler.init();
		VeganOption.Log.log(squeek.veganoption.ModInfo.debugLevel, "init content complete");
		IntegrationHandler.init();
		VeganOption.Log.log(squeek.veganoption.ModInfo.debugLevel, "init integration complete");
		PistonCraftingHandler.init();
		VeganOption.Log.log(squeek.veganoption.ModInfo.debugLevel, "init piston complete");
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		ContentModuleHandler.postInit();
		VeganOption.Log.log(squeek.veganoption.ModInfo.debugLevel, "ContentModuleHandler.postInit() complete");
		IntegrationHandler.postInit();
		VeganOption.Log.log(squeek.veganoption.ModInfo.debugLevel, "IntegrationHandler.postInit() complete");
		Modifiers.recipes.replaceRecipes();
		VeganOption.Log.log(squeek.veganoption.ModInfo.debugLevel, "Modifiers.recipes.replaceRecipes() complete");
	}
}
