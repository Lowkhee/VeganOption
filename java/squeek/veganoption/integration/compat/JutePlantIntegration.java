package squeek.veganoption.integration.compat;

import java.util.List; 

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.block.BlockDoublePlant;
import squeek.veganoption.blocks.BlockJutePlant;
import squeek.veganoption.content.modifiers.DropsModifier;
import squeek.veganoption.content.modules.Jute;
import squeek.veganoption.integration.IntegrationBase;

public class JutePlantIntegration extends BlockJutePlant
{

	public static List<ItemStack> getIntegrationDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
	{
		if(IntegrationBase.integrationExists(IntegrationBase.MODID_FORESTRY))
				return getDropsForForestry(world, pos, state, fortune);
		return null;
	}
	
	public static List<ItemStack> getDropsForForestry(IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
    {
		List<ItemStack> listDrops = new java.util.ArrayList<ItemStack>();
		if(state.getBlock() instanceof BlockJutePlant)
		{
			IBlockState stateBelowJute = world.getBlockState(pos.down().down());
			if(stateBelowJute != null && stateBelowJute.getBlock() == Blocks.FARMLAND)
			{
				if(state.getValue(BlockJutePlant.HALF) == BlockDoublePlant.EnumBlockHalf.UPPER && state.getValue(BlockJutePlant.GROWTH_STAGE) == 10)
					listDrops.addAll(new DropsModifier.DropSpecifier(new ItemStack(Jute.juteStalk), 1, 3).getDrops());
			}	
			else if(state.getValue(BlockJutePlant.HALF) == BlockDoublePlant.EnumBlockHalf.LOWER)
				listDrops.addAll(new DropsModifier.DropSpecifier(new ItemStack(Jute.juteSeeds), 0, 1).getDrops());
		}	

		return listDrops;
    }

}
