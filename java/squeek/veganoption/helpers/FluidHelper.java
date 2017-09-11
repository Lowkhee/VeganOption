package squeek.veganoption.helpers;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import squeek.veganoption.content.modules.Ender;
import squeek.veganoption.content.modules.Ink;
import squeek.veganoption.content.modules.PlantMilk;
import squeek.veganoption.content.modules.Soap;
import squeek.veganoption.content.modules.VegetableOil;

public class FluidHelper
{
	public static final int FINITE_FLUID_MB_PER_META = (int) (0.125f * Fluid.BUCKET_VOLUME);
	
	/*
	 * Give Fluids a Stability property for fluid source blocks - Can be used with BlockBasin (Basin) - Only effects spillage and indirect fluid consumption(ie.. source blocks)
	 */
	public static enum STABILITY {STABLE, HIGHER, HIGH, TOLERANT, LOW , LOWER, UNSTABLE}; //STABILITY.TOLERANT.ordinal() + 1;
	
	/*
	 * fluid stability registry - fluids not registered are considered STABILITY.TOLERANT
	 */
	private static LinkedHashMap<Fluid, STABILITY> fluidStabilityRegistry = new LinkedHashMap<Fluid, STABILITY>();
	
	static
	{
		registerFluidStability(FluidRegistry.WATER, STABILITY.HIGH);
		registerFluidStability(FluidRegistry.LAVA, STABILITY.LOWER);
		registerFluidStability(Ink.whiteInkFluid, FluidHelper.STABILITY.TOLERANT);
		registerFluidStability(Ink.blackInkFluid, FluidHelper.STABILITY.TOLERANT);
		registerFluidStability(PlantMilk.fluidPlantMilk, FluidHelper.STABILITY.HIGHER);
		registerFluidStability(Soap.fluidLyeWater, FluidHelper.STABILITY.LOWER);
		registerFluidStability(VegetableOil.fluidVegetableOil, FluidHelper.STABILITY.HIGHER);
		registerFluidStability(Ender.fluidRawEnder, FluidHelper.STABILITY.UNSTABLE);
	}
	
	/*
	 * register a fluid with a stability - return false if fluid already registered
	 */
	public static boolean registerFluidStability(Fluid fluid, STABILITY stability)
	{
		if(fluidStabilityRegistry.containsKey(fluid))
			return false;
		fluidStabilityRegistry.put(fluid, stability);
		return true;
	} 
	
	/*
	 * get the Stability of a fluid
	 */
	public static STABILITY getFluidStability(Fluid fluid)
	{
		return fluidStabilityRegistry.containsKey(fluid) ? fluidStabilityRegistry.get(fluid) : STABILITY.TOLERANT;
	}
	
	/*
	 * get all fluids with Stability
	 */
	public static HashSet<Fluid> getFluidsWithStabilty(STABILITY stability)
	{
		HashSet<Fluid> fluids = new HashSet<Fluid>();
		for(Map.Entry<Fluid, STABILITY> entry : fluidStabilityRegistry.entrySet())
			if(entry.getValue() == stability)
				fluids.add(entry.getKey());
		return fluids;
	}
	
	/*
	 * set the stability of a fluid - false if fluid does not exist
	 */
	public static boolean setFluidStability(Fluid fluid, STABILITY newStability)
	{
		if(!fluidStabilityRegistry.containsKey(fluid))
			return false;
		fluidStabilityRegistry.replace(fluid, newStability);
		return true;
	}
	
	public static ItemStack toItemStack(Fluid fluid)
	{
		if (fluid == null || fluid.getBlock() == null)
			return null;

		return new ItemStack(fluid.getBlock());
	}

	public static ItemStack toItemStack(FluidStack fluidStack)
	{
		if (fluidStack == null)
			return null;

		return FluidHelper.toItemStack(fluidStack.getFluid());
	}

	public static FluidStack fromItemStack(ItemStack itemStack)
	{
		if (itemStack.isEmpty() || Block.getBlockFromItem(itemStack.getItem()) == Blocks.AIR)
			return null;

		Block block = Block.getBlockFromItem(itemStack.getItem());
		Fluid fluid = getFluidTypeOfBlock(block.getDefaultState());

		return fluid != null ? new FluidStack(fluid, Fluid.BUCKET_VOLUME) : null;
	}

	public static boolean isBlockMaterialWater(IBlockState state)
	{
		return state.getBlock() != null && state.getMaterial() == Material.WATER;
	}

	public static boolean isBlockMaterialLava(IBlockState state)
	{
		return state.getBlock() != null && state.getMaterial() == Material.LAVA;
	}

	public static Fluid getFluidTypeOfBlock(IBlockState state)
	{
		Fluid fluid = FluidRegistry.lookupFluidForBlock(state.getBlock());

		if (fluid != null)
			return fluid;
		else if (isBlockMaterialWater(state))
			return FluidRegistry.WATER;
		else if (isBlockMaterialLava(state))
			return FluidRegistry.LAVA;

		return null;
	}

	public static int getStillFluidLevel(Fluid fluid)
	{
		if (fluid != null)
			return getStillFluidLevel(fluid.getBlock());
		else
			return 0;
	}

	public static int getStillFluidLevel(Block block)
	{
		return block instanceof BlockFluidBase ? ((BlockFluidBase) block).getMaxRenderHeightMeta() : 0;
	}

	public static int getFluidLevel(World world, BlockPos pos)
	{
		return getFluidLevel(world.getBlockState(pos));
	}

	public static int getFluidLevel(IBlockState state)
	{
		if (state.getBlock() instanceof BlockFluidBase)
			return state.getValue(BlockFluidBase.LEVEL);
		else if (state.getBlock() instanceof BlockLiquid)
			return state.getValue(BlockLiquid.LEVEL);
		else
			return 0;
	}

	public static FluidStack getFluidStackFromBlock(World world, BlockPos pos)
	{
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() instanceof IFluidBlock)
		{
			return ((IFluidBlock) state.getBlock()).drain(world, pos, false);
		}
		return getFluidStackFromBlock(state);
	}

	public static FluidStack getFluidStackFromBlock(IBlockState state)
	{
		Fluid fluid = getFluidTypeOfBlock(state);
		if (fluid != null && getFluidLevel(state) == getStillFluidLevel(fluid))
		{
			return new FluidStack(fluid, Fluid.BUCKET_VOLUME);
		}
		return null;
	}

	public static FluidStack consumeExactFluid(World world, BlockPos posFrom, Fluid fluid, int amount)
	{
		return consumeFluid(world, null, posFrom, fluid, amount, amount);
	}

	public static FluidStack consumeFluid(World world, @Nullable BlockPos posTo, BlockPos posFrom, Fluid fluid, int maxAmount)
	{
		FluidStack consumed = consumeFluid(world, posTo, posFrom, fluid, 0, maxAmount);
		return consumed != null && consumed.amount > 0 ? consumed : null;
	}

	//
	public static FluidStack consumeFluid(World world, @Nullable BlockPos posTo, BlockPos posFrom, Fluid fluid, int minAmount, int maxAmount)
	{
		if (world.isRemote)
			return null;

		if (maxAmount < minAmount)
			return null;
	
		//this checks first block pos as a fluid handler, but what if we follow the stream to another fluidtank
		net.minecraftforge.fluids.capability.IFluidHandler fluidHandler = getFluidHandlerAt(world, posFrom, null);
		if (fluidHandler != null)
		{
			FluidStack stackDrained = fluidHandler.drain(new FluidStack(fluid, maxAmount), false);
			if (stackDrained != null && stackDrained.amount >= minAmount)
				return fluidHandler.drain(stackDrained, true);
		}

		BlockPos sourcePos = BlockHelper.followFluidStreamToSourceBlock(world, posTo, posFrom, fluid);
		
		if (sourcePos == null)
			return null;

		//this checks first block pos as a fluid handler, but what if we follow the stream to another fluidtank
		fluidHandler = getFluidHandlerAt(world, sourcePos, null);
		if (fluidHandler != null)
		{
			FluidStack stackDrained = fluidHandler.drain(new FluidStack(fluid, maxAmount), false);
			if (stackDrained != null && stackDrained.amount >= minAmount)
				return fluidHandler.drain(stackDrained, true);
		}

		FluidStack fluidToAdd = FluidHelper.getFluidStackFromBlock(world, sourcePos);

		if (fluidToAdd == null)
			return null;

		/*if (fluidToAdd.amount > maxAmount && world.getBlockState(sourcePos).getBlock() instanceof BlockFluidFinite)
		{
			fluidToAdd = consumePartialFiniteFluidBlock(world, sourcePos, fluidToAdd, maxAmount);
			return fluidToAdd;
		}*/

		if (fluidToAdd.amount >= minAmount)// && fluidToAdd.amount <= maxAmount)
		{
			world.setBlockToAir(sourcePos);
			return fluidToAdd;
		}

		return null;
	}

	public static FluidStack consumePartialFiniteFluidBlock(World world, BlockPos fluidBlockPos, int maxAmount)
	{
		return consumePartialFiniteFluidBlock(world, fluidBlockPos, FluidHelper.getFluidStackFromBlock(world, fluidBlockPos), maxAmount);
	}

	public static FluidStack consumePartialFiniteFluidBlock(World world, BlockPos fluidBlockPos, FluidStack fullFluidStack, int maxAmount)
	{
		if (world.isRemote)
			return null;

		int deltaMeta = -(maxAmount / FINITE_FLUID_MB_PER_META);
		int newMeta = getFluidLevel(world, fluidBlockPos) + deltaMeta;

		if (deltaMeta == 0)
			return null;

		FluidStack fluidConsumed = fullFluidStack.copy();
		fluidConsumed.amount = Math.abs(deltaMeta) * FINITE_FLUID_MB_PER_META;

		if (newMeta >= 0)
			world.setBlockState(fluidBlockPos, world.getBlockState(fluidBlockPos).withProperty(BlockFluidBase.LEVEL, newMeta), 2);
		else
			world.setBlockToAir(fluidBlockPos);

		return fluidConsumed;
	}
	
	public static IFluidHandler getFluidHandlerAt(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nullable EnumFacing facing)
	{
		if(facing == null)
		{
			List<EnumFacing> faces = BlockHelper.getBlockFaces(world, pos);
			facing = faces == null ? EnumFacing.UP : faces.get(0);
		}
		
		TileEntity tile = world.getTileEntity(pos);

		if (tile != null && tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing))
			return tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing);

		return null;
	}
	/*
	 * get block type of BlockPos of fluid, face is the side of the block the fluid is accessible and is nullable, return null if no fluid found
	 */
	@SuppressWarnings("unchecked")
	public static <T>T getTypeOfFluidSource(@Nonnull World world, @Nonnull BlockPos pos, @Nullable EnumFacing face)
	{
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		if (block instanceof IFluidBlock)
			return (T) IFluidBlock.class;
		else if (block instanceof BlockLiquid)
			return (T) BlockLiquid.class;
		else if(FluidRegistry.lookupFluidForBlock(block) != null)
			return (T) Fluid.class;
		else if(face != null)
			if(FluidUtil.getFluidHandler(world, pos, face) != null)
				return  (T)IFluidHandler.class;
		
		return null;
	}
}
