package cyano.steamadvantage.blocks;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cyano.poweradvantage.api.ConduitType;
import cyano.steamadvantage.init.Power;

public class SteamTrackBlock extends cyano.poweradvantage.api.simple.BlockSimplePowerConduit{

	public SteamTrackBlock() {
		super(Material.piston, 0.75f, 2f/16f, Power.steam_power);
	}
	
	@Override
	public void setBlockBoundsBasedOnState(final IBlockAccess world, final BlockPos coord) {
		this.setBlockBounds(0f, 0f, 0f, 1f, 1f, 1f);
	}
	@Override
	public void addCollisionBoxesToList(final World world, final BlockPos coord, 
			final IBlockState bs, final AxisAlignedBB box, final List collisionBoxList, 
			final Entity entity) {
		this.setBlockBoundsBasedOnState(world,coord);
		AxisAlignedBB aabb = this.getCollisionBoundingBox(world, coord, bs);
		if (aabb != null && box.intersectsWith(aabb)) {
			collisionBoxList.add(aabb);
		}
	}
	
	@Override
	public boolean isFullCube() {
		return false;
	}
	
	@Override
	public boolean isOpaqueCube() {
		return false;
	}
	@Override
	public net.minecraft.util.EnumWorldBlockLayer getBlockLayer(){
		return net.minecraft.util.EnumWorldBlockLayer.CUTOUT;
	}

}
