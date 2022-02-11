package squeek.veganoption;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import forestry.core.PluginCore;
import squeek.veganoption.content.ContentModuleHandler;
import squeek.veganoption.content.Modifiers;
import squeek.veganoption.content.crafting.PistonCraftingHandler;
import squeek.veganoption.helpers.FluidContainerHelper;
import squeek.veganoption.helpers.GuiHelper;
import squeek.veganoption.helpers.InventoryHelper;
import squeek.veganoption.helpers.TooltipHelper;
import squeek.veganoption.integration.IntegrationHandler;
import squeek.veganoption.network.NetworkHandler;

@Mod(
		modid = ModInfo.MODID, 
		name = "The Vegan Option",
		version = "1.1.2",
		acceptedMinecraftVersions = "1.11.2", 
		dependencies = "required-after:forge@[13.20.1.2386,)" 
	)
public class VeganOption //13.20.0.2270 13.20.1.2386
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
		//List ores in log
		//Set<ResourceLocation> blocks = Block.REGISTRY.getKeys();
		//Map<String, Fluid> fluid = FluidRegistry.getRegisteredFluids();
				
		//int lineNumber = 0;
		//for(ItemStack stack :OreDictionary.getOres("log"))
		//for(ResourceLocation resLoc : blocks)
		//for(String name : fluid.keySet())
		//String blockV = (new ItemStack(Blocks.LOG, 1, lineNumber)).getDisplayName();
		//for(; lineNumber < 100 && blockV != null;)
		//{
			//lineNumber++;
			//if(resLoc.getResourcePath().contains("log2"))
			//OreDictionary.getOres("logwood");
			//Log.error("(" + lineNumber + ")     " + blockV);
			//blockV = (new ItemStack(, 1, .WILDCARD_VALUE)).getDisplayName();
			//Log.error("(" + lineNumber + ")     " + resLoc.getResourcePath());
			//Log.error("(" + lineNumber + ")     " + name);
		//}
		
		
		FluidContainerHelper.init();
		GuiHelper.init();
		TooltipHelper.init();
		InventoryHelper.init();
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
