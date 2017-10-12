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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import squeek.veganoption.content.modules.VegetableOil;
import squeek.veganoption.helpers.LangHelper;
import squeek.veganoption.helpers.RandomHelper;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.List;
import java.util.Random;

@Optional.Interface(iface = "mcjty.theoneprobe.api.IProbeInfoAccessor", modid = "theoneprobe")
public class BlockHelianthus  extends BlockBush implements IGrowable, IProbeInfoAccessor
{
	public static final int NUM_BOTTOM_STAGES = 5;
	public static final int NUM_TOP_STAGES = 4;
	public static final int MIN_GROWTH_STAGE = 1;
	public static final int MAX_GROWTH_STAGE = NUM_BOTTOM_STAGES + NUM_TOP_STAGES;
	public static final int BOTTOM_META_FULL = NUM_BOTTOM_STAGES;
	public static final float GROWTH_CHANCE_PER_UPDATETICK = 0.20f;
	private static final int MIN_DROPS = 0;
	private static final int MAX_DROPS = 4;
	private static final double FORTUNE_MULTIPLIER = 1.3; //rounding (0.1 to 2.7)*FORTUNE_MULTIPLIER = fortune drops added

	public static final PropertyBool HAS_TOP = PropertyBool.create("has_top");
	public static final PropertyEnum<BlockDoublePlant.EnumBlockHalf> HALF = BlockDoublePlant.HALF;
	public static final PropertyInteger GROWTH_STAGE = PropertyInteger.create("growth", MIN_GROWTH_STAGE, MAX_GROWTH_STAGE);

	public BlockHelianthus()
	{
		super();
		setDefaultState(blockState.getBaseState()
							.withProperty(HALF, BlockDoublePlant.EnumBlockHalf.LOWER)
							.withProperty(GROWTH_STAGE, MIN_GROWTH_STAGE)
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
		boolean isUpper = isUpper(state);
		int max = isUpper ? NUM_TOP_STAGES : NUM_BOTTOM_STAGES;
		int stage = state.getValue(GROWTH_STAGE);
		int individualStage = isUpper ?  (stage - NUM_BOTTOM_STAGES) / 2  : stage; //TOP_META_START - stage : stage;
		float growthPercent = (float) individualStage / max;
		return new AxisAlignedBB(0.15F, 0.0F, 0.15F, 0.85F, 0.25f + growthPercent * 0.75f, 0.85F);
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		return state.getValue(GROWTH_STAGE);
	}

	@Nonnull
	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		return getDefaultState()
			.withProperty(GROWTH_STAGE, meta)
			.withProperty(HALF, meta > NUM_BOTTOM_STAGES ? BlockDoublePlant.EnumBlockHalf.UPPER : BlockDoublePlant.EnumBlockHalf.LOWER)
			.withProperty(HAS_TOP, meta == NUM_BOTTOM_STAGES ? true : false);
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
		//1123581321
		fortune = (int) Math.round((Math.random() * fortune * FORTUNE_MULTIPLIER));
		return state.getValue(GROWTH_STAGE) == MAX_GROWTH_STAGE ?  RandomHelper.getRandomIntFromRange(random, MIN_DROPS, MAX_DROPS + fortune) : RandomHelper.getRandomIntFromRange(random, 0, 1);
	}

	@Nonnull
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune)
	{
		return VegetableOil.seedSunflower;
	}
	
	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
    {
		return super.getDrops(world, pos, state, fortune);
    }

	public void GrowthUpdate(World world, BlockPos pos, IBlockState state, int stageChange)
	{
		if (state.getBlock() != this || stageChange <= 0 )
			return;
		if(state.getValue(HAS_TOP))
			return;

		int currentGrowthStage = state.getValue(GROWTH_STAGE);
		if (isFullyGrown(currentGrowthStage))
			return;	
		
		
		if(currentGrowthStage == NUM_BOTTOM_STAGES && world.isAirBlock(pos.up()))
		{
			stageChange = currentGrowthStage + stageChange >  MAX_GROWTH_STAGE ? MAX_GROWTH_STAGE : stageChange + currentGrowthStage;
			world.setBlockState(pos, state.withProperty(HAS_TOP, true), 3);
			world.setBlockState(pos.up(), getDefaultState().withProperty(GROWTH_STAGE, stageChange).withProperty(HALF, BlockDoublePlant.EnumBlockHalf.UPPER).withProperty(HAS_TOP, false), 3);
		}
		else if(currentGrowthStage < NUM_BOTTOM_STAGES)
		{
			stageChange = currentGrowthStage + stageChange >  NUM_BOTTOM_STAGES ? NUM_BOTTOM_STAGES : stageChange + currentGrowthStage;
			world.setBlockState(pos, state.withProperty(GROWTH_STAGE, stageChange), 3);
		}
		else
		{
			stageChange = currentGrowthStage + stageChange > MAX_GROWTH_STAGE ? MAX_GROWTH_STAGE : stageChange + currentGrowthStage;
			world.setBlockState(pos, state.withProperty(GROWTH_STAGE, stageChange), 3);
		}
	}

	public float getGrowthPercent(IBlockAccess world, BlockPos pos, IBlockState state)
	{
		if (world.getBlockState(pos.up()).getBlock() == this && hasTop(world, pos))
			return getGrowthPercent(world, pos.up(), world.getBlockState(pos.up()));

		return (float) state.getValue(GROWTH_STAGE) / MAX_GROWTH_STAGE;
	}

	public static boolean isFullyGrown(int growthStage)
	{
		return growthStage >= MAX_GROWTH_STAGE;
	}

	public static boolean isFullyGrown(IBlockState state)
	{
		return isFullyGrown(state.getValue(GROWTH_STAGE));
	}

	public static boolean isUpper(IBlockState state)
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
		if (shouldGrow)
			GrowthUpdate(world, pos, state, 1);
	}

	@Override
	public boolean canBlockStay(World world, BlockPos pos, IBlockState state)
	{
		if (state.getBlock() != this)
			return super.canBlockStay(world, pos, state);

		if (hasTop(world, pos))
			return world.getBlockState(pos.up()).getBlock() == this;
		if (isUpper(state))
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
		int deltaGrowth = RandomHelper.getRandomIntFromRange(random, 2, 5);
		GrowthUpdate(world, pos, state, deltaGrowth);
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
}