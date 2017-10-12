package squeek.veganoption.helpers;

import javax.annotation.Nullable;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class TileEntityHelper 
{
	//if chunk, seems to return empty default tiles
	@Nullable
	public static TileEntity getTile(World world, BlockPos pos)
	{
		return getTile((IBlockAccess) world, pos);
	}
	@Nullable
	public static TileEntity getTile(IBlockAccess world, BlockPos pos) 
	{
		if (world instanceof ChunkCache) 
		{
			ChunkCache chunk = (ChunkCache) world;
			return chunk.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
		} 
		else
			return world.getTileEntity(pos);
	}
}
