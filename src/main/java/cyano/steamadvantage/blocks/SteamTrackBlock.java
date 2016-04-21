package cyano.steamadvantage.blocks;

import cyano.steamadvantage.init.Power;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class SteamTrackBlock extends cyano.poweradvantage.api.simple.BlockSimplePowerConduit{

	public SteamTrackBlock() {
		super(Material.PISTON, 0.75f, 2f/16f, Power.steam_power);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return FULL_BLOCK_AABB;
	}
	@Override
	public void addCollisionBoxToList(final IBlockState bs, final World world, final BlockPos coord,
			final AxisAlignedBB box, final List collisionBoxList,
			final Entity entity) {
		super.addCollisionBoxToList(coord, box, collisionBoxList, FULL_BLOCK_AABB);
	}
	
	@Override
	public boolean isFullCube(IBlockState bs) {
		return false;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState bs) {
		return false;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}


}
