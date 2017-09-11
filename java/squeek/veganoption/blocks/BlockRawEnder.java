package squeek.veganoption.blocks;

import java.util.Random;

import javax.annotation.Nonnull;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.BlockFluidFinite;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import squeek.veganoption.content.modules.Ender;
import squeek.veganoption.helpers.BlockHelper;
import squeek.veganoption.helpers.FluidHelper;
import squeek.veganoption.helpers.RandomHelper;
import net.minecraft.entity.player.EntityPlayer;

public class BlockRawEnder extends BlockFluidFinite
{
	public static Material materialRawEnder = Material.WATER;

	public BlockRawEnder(Fluid fluid)
	{
		super(fluid, materialRawEnder);
		this.setDefaultState(blockState.getBaseState().withProperty(LEVEL, 7));
	}

	@Override
	public boolean canDisplace(IBlockAccess world, BlockPos pos)
	{
		//if (world.getBlock(x, y, z).getMaterial().isLiquid())
		//	return false;
		return super.canDisplace(world, pos);
	}

	@Override
	public boolean displaceIfPossible(World world, BlockPos pos)
	{
		//if (world.getBlock(x, y, z).getMaterial().isLiquid())
		//	return false;
		return super.displaceIfPossible(world, pos);
	}
	
	@Override
	public int tryToFlowVerticallyInto(World world, BlockPos pos, int amtToInput)
	{
		return super.tryToFlowVerticallyInto(world, pos, amtToInput);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand)
    {
		//if(state.getBlock() instanceof BlockRawEnder && state.getValue(LEVEL) <= 1)
            BlockHelper.spawnParticles(world, pos, EnumParticleTypes.PORTAL, 0, -1.0, 0, 0, -1.5, 0);
        super.randomDisplayTick(state, world, pos, rand);
    }
	
	//In order to dissovle the rawender, set the blocks to air when quantity doesnt change for # ticks. The super will loop when quantity reaches 1  
	@Override
    public void updateTick(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Random rand)
    {	
      
		int stability = FluidHelper.getFluidStability(FluidRegistry.lookupFluidForBlock(state.getBlock())).ordinal();
		if(state.getBlock() instanceof BlockRawEnder && world.getBlockState(pos).getValue(LEVEL) <= 1)//state. --- && RandomHelper.getRandomIntFromRange(0, stability) <= Math.ceil((float)stability/2))
		{
			world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2); //BLOCK_FIRE_EXTINGUISH ENTITY_ENDERMEN_TELEPORT BLOCK_PORTAL_AMBIENT
			world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
		}
		else
			super.updateTick(world, pos, state, rand);
    }
		
}
