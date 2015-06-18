package cyano.steamadvantage.blocks;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import cyano.steamadvantage.init.Blocks;

public class PlatformBlock extends Block{


	public static final net.minecraft.block.properties.PropertyInteger HEIGHT = PropertyInteger.create("height", 0, 4);
	public PlatformBlock() {
		super(Material.iron);
		this.setHardness(5.0F).setResistance(2000.0F);
		this.setDefaultState(this.blockState.getBaseState().withProperty(HEIGHT, 1));
		this.setBlockBoundsForItemRender();
	}

	@Override
	protected BlockState createBlockState()
	{
		return new BlockState(this, new IProperty[]{HEIGHT});
	}

	@Override
	public void setBlockBoundsBasedOnState(final IBlockAccess world, final BlockPos coord) {
		Integer height = ((Integer)world.getBlockState(coord).getValue(HEIGHT));
		if(height == 0) {
			this.setBlockBounds(0.25f, 0f, 0.25f, 0.75f, 1f, 0.75f);
			return;
		}
		this.setBlockBounds(0, 0, 0, 1, 0.25f*height, 1);
	}

	@Override
	public void addCollisionBoxesToList(final World world, final BlockPos coord, 
			final IBlockState bs, final AxisAlignedBB box, final List collisionBoxList, 
			final Entity entity) {
		Integer height = ((Integer)world.getBlockState(coord).getValue(HEIGHT));
		if(height == 0) {
			// shaft only
			this.setBlockBounds(0.25f, 0f, 0.25f, 0.75f, 1f, 0.75f);
			super.addCollisionBoxesToList(world, coord, bs, box, collisionBoxList, entity);
			return;
		}
		if(height > 1){
			this.setBlockBounds(0.25f, 0f, 0.25f, 0.75f, 0.25f*(height - 1), 0.75f);
			super.addCollisionBoxesToList(world, coord, bs, box, collisionBoxList, entity);
		}
		this.setBlockBounds(0f, 0.25f*(height - 1), 0f, 1f, 0.25f*height, 1f);
		super.addCollisionBoxesToList(world, coord, bs, box, collisionBoxList, entity);

	}

	/**
	 * Override of default block behavior
	 */
	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	/**
	 * Override of default block behavior
	 */
	@Override
	public boolean isFullCube() {
		return false;
	}

	/**
	 * Override of default block behavior
	 */
	@Override
	public boolean isPassable(final IBlockAccess world, final BlockPos coord) {
		return false;
	}

	@Override
	public int getMetaFromState(final IBlockState bs) {
		return ((Integer)bs.getValue(HEIGHT)).intValue();
	}

	@Override
	public IBlockState getStateFromMeta(final int bs) {
		return this.getDefaultState().withProperty(HEIGHT, bs);
	}

	/**
	 * Called when a neighboring block changes.
	 */
	@Override
	public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock)
	{
		if (!this.canBlockStay(world, pos))
		{
			world.destroyBlock(pos, true);
		}
	}

	public boolean canBlockStay(World worldIn, BlockPos pos)
	{
		Block block = worldIn.getBlockState(pos.down()).getBlock();
		return block == this || block == Blocks.steam_elevator_platform || block == Blocks.steam_elevator;
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

	@SideOnly(Side.CLIENT)
	@Override
	public boolean shouldSideBeRendered(final IBlockAccess world, final BlockPos coord, final EnumFacing face) {
		return true;
	}
}
