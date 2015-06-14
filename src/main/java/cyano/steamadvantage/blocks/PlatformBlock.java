package cyano.steamadvantage.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import cyano.steamadvantage.init.Blocks;

public class PlatformBlock extends Block{

	public PlatformBlock() {
		super(Material.iron);
	}

	/**
	 * Called when a neighboring block changes.
	 */
	@Override
	public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock)
	{
		if (!this.canBlockStay(worldIn, pos))
		{
			worldIn.destroyBlock(pos, true);
		}
	}

	public boolean canBlockStay(World worldIn, BlockPos pos)
	{
		Block block = worldIn.getBlockState(pos.down()).getBlock();
		return block == this || block == Blocks.steam_elevator_platform || block == Blocks.steam_elevator;
	}
}
