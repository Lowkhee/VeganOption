package squeek.veganoption.blocks;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoAccessor;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.IGrowable;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ColorizerGrass;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeColorHelper;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import squeek.veganoption.content.modules.Jute;
import squeek.veganoption.helpers.LangHelper;
import squeek.veganoption.integration.compat.JutePlantIntegration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.List;
import java.util.Random;

@Optional.Interface(iface = "mcjty.theoneprobe.api.IProbeInfoAccessor", modid = "theoneprobe")
public class BlockJutePlant extends BlockBush implements IGrowable, IProbeInfoAccessor
{
	public static final int NUM_BOTTOM_STAGES = 6;
	public static final int NUM_TOP_STAGES = 5;
	public static final int NUM_GROWTH_STAGES = NUM_BOTTOM_STAGES + NUM_TOP_STAGES;
	public static final int BOTTOM_META_FULL = NUM_BOTTOM_STAGES;
	public static final int TOP_META_START = BOTTOM_META_FULL + 1;
	public static final int META_MAX = TOP_META_START + NUM_TOP_STAGES;
	public static final float GROWTH_CHANCE_PER_UPDATETICK = 0.20f;

	public static final PropertyBool HAS_TOP = PropertyBool.create("has_top");
	public static final PropertyEnum<BlockDoublePlant.EnumBlockHalf> HALF = BlockDoublePlant.HALF;
	public static final PropertyInteger GROWTH_STAGE = PropertyInteger.create("growth", 0, NUM_GROWTH_STAGES);

	public BlockJutePlant()
	{
		super();
		setDefaultState(blockState.getBaseState()
							.withProperty(HALF, BlockDoublePlant.EnumBlockHalf.LOWER)
							.withProperty(GROWTH_STAGE, 0)
							.withProperty(HAS_TOP, false));
	}

	@Nonnull
	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, HALF, GROWTH_STAGE, HAS_TOP);
	}

	@Nonnull
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
	{
		boolean isTop = isTop(state);
		int max = isTop ? NUM_TOP_STAGES : NUM_BOTTOM_STAGES;
		int stage = state.getValue(GROWTH_STAGE);
		int individualStage = isTop ?  stage - TOP_META_START : stage; //TOP_META_START - stage : stage;
		float growthPercent = (float) individualStage / max;
		return new AxisAlignedBB(0.15F, 0.0F, 0.15F, 0.85F, 0.25f + growthPercent * 0.75f, 0.85F);
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		int growthStage = state.getValue(GROWTH_STAGE); 
		boolean hasTop = state.getValue(HAS_TOP);
		return hasTop && growthStage == NUM_BOTTOM_STAGES ? growthStage + NUM_BOTTOM_STAGES : growthStage;
	}

	@Nonnull
	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		boolean hasTop = false;
		if(meta > NUM_GROWTH_STAGES)
		{
			hasTop = true;
			meta = meta - NUM_BOTTOM_STAGES;
		}
		
		return getDefaultState()
			.withProperty(GROWTH_STAGE, meta)
			.withProperty(HALF, meta > NUM_BOTTOM_STAGES ? BlockDoublePlant.EnumBlockHalf.UPPER : BlockDoublePlant.EnumBlockHalf.LOWER)
			.withProperty(HAS_TOP, hasTop ? true : false);
	}

	@Nonnull
	@Override
	public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess block, BlockPos pos)
	{
		return block.getBlockState(pos);
	}

	@Override
	public int quantityDropped(IBlockState state, int fortune, @Nonnull Random random)
	{
		return isTop(state) ? 0 : 1;
	}

	@Nonnull
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune)
	{
		return Jute.juteSeeds;
	}
	
	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
    {
		List<ItemStack> listDrops = JutePlantIntegration.getIntegrationDrops(world, pos, state, fortune);
		if(listDrops != null)
			return listDrops;
		return super.getDrops(world, pos, state, fortune);
    }

	public void deltaGrowth(World world, BlockPos pos, IBlockState state, int delta)
	{
		if (state.getBlock() != this)
			return;

		int oldGrowthStage = state.getValue(GROWTH_STAGE);

		if (hasTop(world, pos)) //redundant, should never be called based on line 198
		{
			deltaGrowth(world, pos.up(), world.getBlockState(pos.up()), delta);
			return;
		}

		int newGrowthStage = oldGrowthStage + delta;

		if (isFullyGrown(newGrowthStage))
		{
			BlockPos position = state.getValue(HALF) == BlockDoublePlant.EnumBlockHalf.UPPER ? pos.down() : pos;
			//world.setBlockState(position, Blocks.AIR.getDefaultState());
			//world.setBlockState(position.up(), Blocks.AIR.getDefaultState());
			IBlockState bottomFern = Blocks.DOUBLE_PLANT.getDefaultState().withProperty(BlockDoublePlant.VARIANT, BlockDoublePlant.EnumPlantType.FERN).withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.LOWER);
			IBlockState topFern = Blocks.DOUBLE_PLANT.getDefaultState().withProperty(BlockDoublePlant.VARIANT, BlockDoublePlant.EnumPlantType.FERN).withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.UPPER);
			world.setBlockState(position, bottomFern, 3);
	        world.setBlockState(position.up(), topFern, 3);
		}
		else
		{			
			if (!isTop(state) && oldGrowthStage >= NUM_BOTTOM_STAGES && world.isAirBlock(pos.up()))
			{
				world.setBlockState(pos, state.withProperty(GROWTH_STAGE, NUM_BOTTOM_STAGES).withProperty(HAS_TOP, true));
				world.setBlockState(pos.up(), getDefaultState().withProperty(GROWTH_STAGE, newGrowthStage).withProperty(HALF, BlockDoublePlant.EnumBlockHalf.UPPER).withProperty(HAS_TOP, false));
			}
			else
			{
				world.setBlockState(pos, state.withProperty(GROWTH_STAGE, newGrowthStage), 3);
			}
		}
	}

	public float getGrowthPercent(IBlockAccess world, BlockPos pos, IBlockState state)
	{
		if (world.getBlockState(pos.up()).getBlock() == this && hasTop(world, pos))
			return getGrowthPercent(world, pos.up(), world.getBlockState(pos.up()));

		return (float) state.getValue(GROWTH_STAGE) / NUM_GROWTH_STAGES;
	}

	public static boolean isFullyGrown(int growthStage)
	{
		return growthStage >= NUM_GROWTH_STAGES;
	}

	public static boolean isFullyGrown(IBlockState state)
	{
		return isFullyGrown(state.getValue(GROWTH_STAGE));
	}

	public static boolean isTop(IBlockState state)
	{
		return state.getValue(HALF) == BlockDoublePlant.EnumBlockHalf.UPPER;
	}

	public static boolean hasTop(IBlockAccess world, BlockPos pos)
	{
		return world.getBlockState(pos).getActualState(world, pos).getValue(HAS_TOP);
	}

	@Override
	public void updateTick(@Nullable World world, @Nullable BlockPos pos, @Nullable IBlockState state, Random random)
	{
		super.updateTick(world, pos, state, random);

		boolean shouldGrow = random.nextFloat() < GROWTH_CHANCE_PER_UPDATETICK;
		if (shouldGrow && !hasTop(world, pos))
			deltaGrowth(world, pos, state, 1);
	}

	@Override
	public boolean canBlockStay(World world, BlockPos pos, IBlockState state)
	{
		if (state.getBlock() != this)
			return super.canBlockStay(world, pos, state);

		if (hasTop(world, pos))
			return world.getBlockState(pos.up()).getBlock() == this;
		if (isTop(state))
			return world.getBlockState(pos.down()).getBlock() == this;

		return super.canBlockStay(world, pos, state);
	}

	@Override
	public boolean canGrow(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, boolean isClient)
	{
		return true;
	}

	@Override
	public boolean canUseBonemeal(@Nonnull World world, @Nonnull Random random, @Nonnull BlockPos pos, @Nonnull IBlockState state)
	{
		return true;
	}

	@Override //never called?
	public void grow(@Nonnull World world, @Nonnull Random random, @Nonnull BlockPos pos, @Nonnull IBlockState state)
	{
		int deltaGrowth = MathHelper.getInt(random, 2, 5);
		deltaGrowth(world, pos, state, deltaGrowth);
	}

	@Override
	public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data)
	{
		float growthValue = getGrowthPercent(world, data.getPos(), blockState) * 100F;
		if (growthValue < 100)
			probeInfo.text(LangHelper.contextString("top", "growth", Math.round(growthValue)));
		else
			probeInfo.text(LangHelper.contextString("top", "growth.mature"));
	}

	public static class ColorHandler implements IBlockColor, IItemColor
	{
		@SideOnly(Side.CLIENT)
		@Override
		public int colorMultiplier(@Nonnull IBlockState state, @Nullable IBlockAccess world, @Nullable BlockPos pos, int tintIndex)
		{
			return world != null && pos != null ? BiomeColorHelper.getGrassColorAtPos(world, pos) : -1;
		}

		@Override
		public int getColorFromItemstack(@Nonnull ItemStack stack, int tintIndex)
		{
			return ColorizerGrass.getGrassColor(0.5D, 1.0D);
		}
	}
}
