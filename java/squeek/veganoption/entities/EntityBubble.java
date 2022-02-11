package squeek.veganoption.entities;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import squeek.veganoption.content.modules.FrozenBubble;
import squeek.veganoption.helpers.BlockHelper;
import squeek.veganoption.helpers.RandomHelper;
import squeek.veganoption.helpers.TemperatureHelper;
import squeek.veganoption.network.MessageFX;
import squeek.veganoption.network.NetworkHandler;

import javax.annotation.Nonnull;

public class EntityBubble extends EntityThrowable
{
	public static final int LIFETIME_BASE = 40;
	public static final int LIFETIME_MAX = 80;
	public static final float FREEZING_TEMPERATURE = -15;
	public static final int TEMPERATURE_CHECK_RADIUS = 3;

	public int lifetime = RandomHelper.getRandomIntFromRange(LIFETIME_BASE, LIFETIME_MAX);
	public float temperature;

	public EntityBubble(World world)
	{
		super(world);
	}

	public EntityBubble(World world, EntityLivingBase entity)
	{
		super(world, entity);
	}

	public EntityBubble(World world, double x, double y, double z)
	{
		super(world, x, y, z);
	}

	@Override
	protected void entityInit()
	{
		super.entityInit();
		this.temperature = TemperatureHelper.getBiomeTemperature(world, new BlockPos((int) posX, (int) posY, (int) posZ));
	}

	@Override
	public boolean attackEntityFrom(@Nonnull DamageSource source, float amount)
	{
		pop();
		return super.attackEntityFrom(source, amount);
	}

	@Override
	public boolean canBeCollidedWith()
	{
		return true;
	}

	@Override
	public void onUpdate()
	{
		if (!this.world.isRemote && (ticksExisted >= lifetime || this.isInWater()))
		{
			pop();
			return;
		}

		BlockPos freezePos =  new BlockPos(posX, posY, posZ);
		float surroundingTemp = getSurroundingAirTemperature(world, new BlockPos(posX, posY, posZ));
		temperature += (surroundingTemp - temperature) * 0.05f;

		if (!this.world.isRemote && temperature <= FREEZING_TEMPERATURE)
		{
			freeze(freezePos);
			return;
		}

		if (!this.world.isRemote && ticksExisted % 10 == 0)
		{
			this.motionX += 0.1F * (RandomHelper.random.nextFloat() - 0.5F);
			this.motionY += 0.1F * (RandomHelper.random.nextFloat() - 0.5F);
			this.motionZ += 0.1F * (RandomHelper.random.nextFloat() - 0.5F);
		}

		super.onUpdate();

		// this check helps avoid low velocities which minecraft seems to struggle with? (visually stutters)
		// have not looked into the cause; this is basically an ugly/fast workaround
		if (motionX * motionX + motionY * motionY + motionZ * motionZ > 0.025f)
		{
			this.motionX *= 0.9F;
			this.motionY *= 0.9F;
			this.motionZ *= 0.9F;
		}
	}

	@Override
	protected float getGravityVelocity()
	{
		return 0.0F;
	}

	public void pop()
	{
		if (!this.world.isRemote)
		{
			NetworkRegistry.TargetPoint target = new NetworkRegistry.TargetPoint(dimension, posX, posY, posZ, 80);
			NetworkHandler.channel.sendToAllAround(new MessageFX(posX, posY, posZ, MessageFX.FX.BUBBLE_POP), target);
			this.setDead();
		}
	}

	public void freeze(BlockPos posFreezePoint)
	{
		if (!this.world.isRemote)
		{
			EntityItem frozenBubble = new EntityItem(world, posX, posY, posZ, new ItemStack(FrozenBubble.frozenBubble));//posFreezePoint.getX(), posFreezePoint.getY(), posFreezePoint.getZ(), new ItemStack(FrozenBubble.frozenBubble));
			float xMove = -(float)(posX - posFreezePoint.getX());
			float zMove = -(float)(posZ - posFreezePoint.getZ());
			//frozenBubble.motionX = 0.0;
			
			//frozenBubble.noClip = true;
			frozenBubble.entityCollisionReduction = 0; 
			frozenBubble.moveRelative((float).5, xMove, (float)0);
			world.spawnEntity(frozenBubble);
			//frozenBubble.move(MoverType.SELF, posFreezePoint.getX(), posFreezePoint.getY(), posFreezePoint.getZ());
			//frozenBubble.moveRelative(strafe, forward, friction);
			this.setDead();
		}
	}

	@Override
	protected void onImpact(@Nonnull RayTraceResult rayTraceResult)
	{
		pop();
	}

	public static float getSurroundingAirTemperature(World world, BlockPos pos)
	{
		float airTemperature = TemperatureHelper.getBiomeTemperature(world, pos);

		BlockPos[] surroundingBlocks = BlockHelper.getBlocksInRadiusAround(pos, TEMPERATURE_CHECK_RADIUS);

		for (BlockPos blockPos : surroundingBlocks)
		{
			Block block = world.getBlockState(blockPos).getBlock();
			if (block == Blocks.AIR)
				airTemperature -= 0.01f;
			else if (block == Blocks.SNOW)
				airTemperature -= 0.1f;
			else if (block == Blocks.ICE)
				airTemperature -= 0.5f;
			else if (block == Blocks.PACKED_ICE)
				airTemperature -= 1.0f;
			else if (block == Blocks.LAVA)
				airTemperature += 3.0f;
			else if (block == Blocks.TORCH)
				airTemperature += 0.5f;
		}

		return airTemperature;
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound tag)
	{
		super.readEntityFromNBT(tag);

		temperature = tag.getFloat("temperature");
		lifetime = tag.getInteger("lifetime");
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound tag)
	{
		super.writeEntityToNBT(tag);

		tag.setFloat("temperature", temperature);
		tag.setInteger("lifetime", lifetime);
	}
	
}
