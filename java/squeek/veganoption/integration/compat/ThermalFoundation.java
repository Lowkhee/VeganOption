package squeek.veganoption.integration.compat;

import squeek.veganoption.VeganOption;
import squeek.veganoption.content.Modifiers;
import squeek.veganoption.content.modules.Ender;
import squeek.veganoption.content.modules.ProofOfSuffering;
import squeek.veganoption.content.recipes.ShapelessMatchingOreRecipe;
import squeek.veganoption.integration.IntegrationHandler;
import squeek.veganoption.integration.IntegratorBase;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cofh.thermalfoundation.init.TFFluids;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.UniversalBucket;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class ThermalFoundation extends IntegratorBase
{
	protected static final Logger Log = LogManager.getLogger(ImmersiveEngineering.class.getCanonicalName());
	public static ItemStack bucketMana;
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
		FluidRegistry.addBucketForFluid(TFFluids.fluidMana);
		
		UniversalBucket bucket = ForgeModContainer.getInstance().universalBucket;
		//bucketRawEnder = new ItemStack(bucket);
		bucketMana = UniversalBucket.getFilledBucket(bucket, TFFluids.fluidMana);
		//bucket.fill(bucketRawEnder, new FluidStack(fluidRawEnder, Fluid.BUCKET_VOLUME), true);
		bucketMana.copy().getItem().setCreativeTab(VeganOption.creativeTab);
		

		GameRegistry.addRecipe(new ShapelessOreRecipe(bucketMana.copy(), Ender.bucketRawEnder.copy(), new ItemStack(ProofOfSuffering.proofOfSuffering), new ItemStack(ProofOfSuffering.proofOfSuffering)));
		Modifiers.crafting.addInputsToRemoveForOutput(bucketMana.copy(), Ender.bucketRawEnder.copy(), new ItemStack(ProofOfSuffering.proofOfSuffering), new ItemStack(ProofOfSuffering.proofOfSuffering)); //
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
