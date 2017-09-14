package squeek.veganoption;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

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

@Mod(
		modid = ModInfo.MODID, 
		name = "The Vegan Option",
		version = "1.0.0",
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
		IntegrationHandler.preInit();
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event)
	{
		FluidContainerHelper.init();
		GuiHelper.init();
		TooltipHelper.init();
		NetworkHandler.init();
		ContentModuleHandler.init();
		IntegrationHandler.init();
		PistonCraftingHandler.init();
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		ContentModuleHandler.postInit();
		IntegrationHandler.postInit();
		Modifiers.recipes.replaceRecipes();
	}
}
