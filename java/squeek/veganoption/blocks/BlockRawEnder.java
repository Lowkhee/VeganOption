package squeek.veganoption.blocks;

import java.util.Random;

import javax.annotation.Nonnull;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.BlockFluidFinite;
import net.minecraftforge.fluids.Fluid;
import squeek.veganoption.content.modules.Ender;

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
	
	//In order to dissovle the rawender, set the blocks to air when quantity doesnt change for # ticks. The super will loop when quantity reaches 1  
	@Override
    public void updateTick(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Random rand)
    {
		if(state.getBlock() == Ender.rawEnder && state.getValue(LEVEL) <= 1)
			world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
		else
			super.updateTick(world, pos, state, rand);
    }
	
	
}
