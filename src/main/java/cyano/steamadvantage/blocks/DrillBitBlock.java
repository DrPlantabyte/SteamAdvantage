package cyano.steamadvantage.blocks;

import cyano.steamadvantage.init.Power;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class DrillBitBlock extends Block implements ITileEntityProvider {

	
	public DrillBitBlock() {
		super(Material.IRON);
		this.setHardness(5.0F).setResistance(2000.0F);
	}

	@Override
	public TileEntity createNewTileEntity(World arg0, int arg1) {
		return new DrillBitTileEntity();
	}

	@Override
	public Item getItemDropped(IBlockState bs, Random rand, int fortune){
		return null;
	}
	
	@Override 
	public int quantityDropped(IBlockState state, int fortune, Random random){
		return 0;
	}
	
	@Override public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune){
		return Collections.EMPTY_LIST;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	//And this tell it that you can see through this block, and neighbor blocks should be rendered.
	@Override
	public boolean isOpaqueCube(IBlockState state)
	{
		return false;
	}
	
	@Override
	public boolean isFullCube(IBlockState state){
		return false;
	}

	private static final AxisAlignedBB blockBounds = new AxisAlignedBB(0.25f, 0.25f, 0.25f, 0.75f, 0.75f, 0.75f);
	@Override
	public void addCollisionBoxToList(final IBlockState state, final World w, final BlockPos coord, final AxisAlignedBB bb, final List<AxisAlignedBB> list, final Entity e) {
		super.addCollisionBoxToList(coord, bb, list, blockBounds);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return blockBounds;
	}


	@Override
	public void onEntityCollidedWithBlock(final World world, final BlockPos coord, final IBlockState bs, 
			final Entity victim) {
		victim.attackEntityFrom(Power.machine_damage, 2.0f);
	}
	
	@Override
	public void onBlockDestroyedByPlayer(World w, BlockPos coord, IBlockState state){
		destroyNeighbors(w,coord,w.getBlockState(coord));
		super.onBlockDestroyedByPlayer(w, coord, state);
		w.removeTileEntity(coord);
	}

	@Override
	public void onBlockDestroyedByExplosion(World w, BlockPos coord, Explosion boom){
		destroyNeighbors(w,coord,w.getBlockState(coord));
		super.onBlockDestroyedByExplosion(w, coord, boom);
		w.removeTileEntity(coord);
	}

	
	private void destroyNeighbors(World w, BlockPos coord, IBlockState state) {
		if(w.isRemote) return;
		// destroy connected drill bits
		for(int i = 0; i < 6; i++){
			BlockPos pos = coord.offset(EnumFacing.getFront(i));
			if(w.getTileEntity(pos) instanceof DrillBitTileEntity){
				DrillBitTileEntity te = (DrillBitTileEntity)w.getTileEntity(pos);
				te.destroyLine();
			}
		}
	}
	
}
