package cyano.steamadvantage.blocks;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cyano.steamadvantage.init.Blocks;
import cyano.steamadvantage.init.Power;

public class DrillBitBlock extends Block implements ITileEntityProvider {

	public static final PropertyDirection FACING = PropertyDirection.create("facing");
	
	public DrillBitBlock() {
		super(Material.iron);
		this.setHardness(5.0F).setResistance(2000.0F);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.DOWN));
	}

	@Override
	public TileEntity createNewTileEntity(World arg0, int arg1) {
		return new DrillBitTileEntity();
	}

	@Override
	public Item getItemDropped(IBlockState bs, Random rand, int fortune){
		return null;
	}
	//This will tell minecraft not to render any side of our cube.
	@Override
	public int getRenderType()
	{
		return 3;
	}

	//And this tell it that you can see through this block, and neighbor blocks should be rendered.
	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}
	
	@Override
	public boolean isFullCube(){
		return false;
	}
	
	@Override
	public void addCollisionBoxesToList(final World w, final BlockPos coord, final IBlockState bs, 
			final AxisAlignedBB bb, final List list, final Entity e) {
		
		this.setBlockBounds(0.25f, 0.0f, 0.25f, 0.75f, 1.0f, 0.75f);
		super.addCollisionBoxesToList(w, coord, bs, bb, list, e);
	}

	@Override
	public void setBlockBoundsBasedOnState(final IBlockAccess bs, final BlockPos coord) {
		this.setBlockBounds(0.25f, 0.0f, 0.25f, 0.75f, 1.0f, 0.75f);
	}


	@Override
	public void onEntityCollidedWithBlock(final World world, final BlockPos coord, final IBlockState bs, 
			final Entity victim) {
		victim.attackEntityFrom(Power.machine_damage, 2.0f);
	}
	
	@Override
	public void onBlockDestroyedByPlayer(World w, BlockPos coord, IBlockState state){
		super.onBlockDestroyedByPlayer(w, coord, state);
		destroyNeighbors(w,coord,state);
	}
	
	private void destroyNeighbors(World w, BlockPos coord, IBlockState state) {
		// destroy connected drill bits
		if(state.getProperties().containsKey(FACING)){
			EnumFacing dir = (EnumFacing)state.getValue(FACING);
			EnumFacing dir2 = dir.getOpposite();
			BlockPos p = coord.offset(dir);
			while(w.getBlockState(p).getBlock() == Blocks.drillbit){
				w.setBlockToAir(p);
				p = p.offset(dir);
			}
			p = coord.offset(dir2);
			while(w.getBlockState(p).getBlock() == Blocks.drillbit){
				w.setBlockToAir(p);
				p = p.offset(dir2);
			}
		}
	}

	@Override
	public void onBlockDestroyedByExplosion(World w, BlockPos coord, Explosion boom){
		destroyNeighbors(w,coord,w.getBlockState(coord));
		super.onBlockDestroyedByExplosion(w, coord, boom);
	}
	
	
}
