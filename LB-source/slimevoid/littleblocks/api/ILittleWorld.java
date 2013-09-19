package slimevoid.littleblocks.api;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public interface ILittleWorld extends IBlockAccess {

	public World getRealWorld();

	public void idModified(int lastId, int xCoord, int yCoord, int zCoord, int side,
			int littleX, int littleY, int littleZ, int id, int metadata);

	public void metadataModified(int xCoord, int yCoord, int zCoord, int side, int littleX,
			int littleY, int littleZ, int blockId, int metadata);

	public void setBlockTileEntity(int x, int y, int z,
			TileEntity tileentity);

	boolean isOutdated(World world);
}
