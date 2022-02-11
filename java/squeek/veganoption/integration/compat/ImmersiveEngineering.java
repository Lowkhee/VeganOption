package squeek.veganoption.integration.compat;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import blusunrize.immersiveengineering.api.crafting.RefineryRecipe;
import blusunrize.immersiveengineering.api.crafting.SqueezerRecipe;
import blusunrize.immersiveengineering.api.energy.DieselHandler;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockRefinery;
import forestry.core.fluids.Fluids;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import squeek.veganoption.content.modules.Burlap;
import squeek.veganoption.content.modules.Ender;
import squeek.veganoption.content.modules.Jute;
import squeek.veganoption.content.modules.VegetableOil;
import squeek.veganoption.integration.IntegrationHandler;
import squeek.veganoption.integration.IntegratorBase;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//import blusunrize.immersiveengineering.api.energy;

public class ImmersiveEngineering  extends IntegratorBase
{
	public static Fluid creosote = null;
	protected static final Logger Log = LogManager.getLogger(ImmersiveEngineering.class.getCanonicalName());
	
	@Override
	public void create()
	{
	}

	@Override
	public void oredict()
	{
		
	}

	@Override
	public void recipes()
	{
		creosote = FluidRegistry.getFluid("creosote");
		
		NonNullList<ItemStack> steelPlates = OreDictionary.getOres("plateSteel");
		if(!steelPlates.isEmpty())
		{
			for(ItemStack steelPlate : steelPlates)
				GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Items.ELYTRA, 1),
					"S S",
					"BSB",
					"S S",
					'B', Burlap.burlap,
					'S', steelPlate));
		}
		

		DieselHandler.registerFuel(Ender.fluidRawEnder, 1500);
		DieselHandler.registerDrillFuel(Ender.fluidRawEnder);
		DieselHandler.registerFuel(VegetableOil.fluidVegetableOil, 50);
		//Log.info("################Registered Liquids#############################");
		for(Fluid fluid : FluidRegistry.getBucketFluids())
		{
			//Log.info(fluid.getName());
			if(!DieselHandler.isValidFuel(fluid))
			{
				if(fluid.getName().contains("biomass"))
					DieselHandler.registerFuel(fluid, 75);
				if(fluid.getName().contains("fuel"))
					DieselHandler.registerFuel(fluid, 130);
				if(fluid.getName().contains("ethanol"))
					DieselHandler.registerFuel(fluid, 90);
				if(fluid.getName().contains("ic2uu_matter"))
					DieselHandler.registerFuel(fluid, 1000000);
			}
			if(!DieselHandler.isValidDrillFuel(fluid))
			{
				if(fluid.getName().contains("fuel"))
					DieselHandler.registerDrillFuel(fluid);
				if(fluid.getName().contains("ethanol"))
					DieselHandler.registerDrillFuel(fluid);
			}
			if(IntegrationHandler.integratorExists(MODID_FORESTRY))
			{
				if(fluid.getName().contains("lubricant"))
				{
					RefineryRecipe.addRecipe(new FluidStack(IEContent.fluidBiodiesel, 20), new FluidStack(fluid, 10), new FluidStack(forestry.core.fluids.Fluids.BIO_ETHANOL.getFluid(), 10), 90);
					RefineryRecipe.addRecipe(new FluidStack(forestry.core.fluids.Fluids.BIOMASS.getFluid(), 20), new FluidStack(fluid, 10), new FluidStack(forestry.core.fluids.Fluids.SEED_OIL.getFluid(), 10), 90);
					RefineryRecipe.addRecipe(new FluidStack(forestry.core.fluids.Fluids.BIOMASS.getFluid(), 20), new FluidStack(fluid, 10), new FluidStack(VegetableOil.fluidVegetableOil, 10), 90);
					RefineryRecipe.addRecipe(new FluidStack(forestry.core.fluids.Fluids.BIOMASS.getFluid(), 20), new FluidStack(fluid, 10), new FluidStack(IEContent.fluidPlantoil, 10), 90);
				}
			}
		}
		if(IntegrationHandler.integratorExists(MODID_FORESTRY))
		{
			RefineryRecipe.addRecipe(new FluidStack(IEContent.fluidBiodiesel, 20), new FluidStack(VegetableOil.fluidVegetableOil, 10), new FluidStack(forestry.core.fluids.Fluids.BIO_ETHANOL.getFluid(), 10), 90);
			RefineryRecipe.addRecipe(new FluidStack(IEContent.fluidBiodiesel, 20), new FluidStack(IEContent.fluidPlantoil, 10), new FluidStack(forestry.core.fluids.Fluids.BIO_ETHANOL.getFluid(), 10), 90);
			RefineryRecipe.addRecipe(new FluidStack(forestry.core.fluids.Fluids.BIOMASS.getFluid(), 20), new FluidStack(creosote, 10), new FluidStack(forestry.core.fluids.Fluids.SEED_OIL.getFluid(), 10), 90);
			RefineryRecipe.addRecipe(new FluidStack(forestry.core.fluids.Fluids.BIOMASS.getFluid(), 20), new FluidStack(creosote, 10), new FluidStack(VegetableOil.fluidVegetableOil, 10), 90);
			RefineryRecipe.addRecipe(new FluidStack(forestry.core.fluids.Fluids.BIOMASS.getFluid(), 20), new FluidStack(creosote, 10), new FluidStack(IEContent.fluidPlantoil, 10), 90);
			RefineryRecipe.addRecipe(new FluidStack(forestry.core.fluids.Fluids.BIO_ETHANOL.getFluid(), 10), new FluidStack(forestry.core.fluids.Fluids.BIOMASS.getFluid(), 10), new FluidStack(forestry.core.fluids.Fluids.BIOMASS.getFluid(), 10), 90);
		}
		//Log.info("################IM: Diesel Engine Fuels#############################");
		/*Map<String, Integer> fuels = DieselHandler.getFuelValuesSorted(false);
		Iterator<Entry<String, Integer>> iterator = fuels.entrySet().iterator();
		while(iterator.hasNext())
		{
			Entry<String, Integer> entry = iterator.next();
			Log.info(entry.getKey() + "---time:  " + entry.getValue());
		}*/
		
		SqueezerRecipe.addRecipe(new FluidStack(VegetableOil.fluidVegetableOil, Fluid.BUCKET_VOLUME), null, new ItemStack(VegetableOil.seedSunflower), 100);
		SqueezerRecipe.addRecipe(new FluidStack(VegetableOil.fluidVegetableOil, Fluid.BUCKET_VOLUME), null, new ItemStack(Jute.juteSeeds), 100);
		if(IntegrationHandler.integratorExists(MODID_FORESTRY))
			SqueezerRecipe.addRecipe(new FluidStack(Fluids.ICE.getFluid(), Fluid.BUCKET_VOLUME), null, new ItemStack(Blocks.PACKED_ICE, 10), 100);
		
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
