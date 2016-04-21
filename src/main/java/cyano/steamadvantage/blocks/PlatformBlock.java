package cyano.steamadvantage.blocks;

import cyano.steamadvantage.init.Blocks;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class PlatformBlock extends Block{


	public static final net.minecraft.block.properties.PropertyInteger HEIGHT = PropertyInteger.create("height", 0, 4);

	private static final AxisAlignedBB[] boxes = new AxisAlignedBB[5];
	static{
		for(int i = 0; i < boxes.length; i++){
			if(i == 0){
				boxes[i] = new AxisAlignedBB(0.25f, 0f, 0.25f, 0.75f, 1f, 0.75f);
				continue;
			}
			boxes[i] = new AxisAlignedBB(0, 0, 0, 1, 0.25f*i, 1);
		}
	}

	public PlatformBlock() {
		super(Material.IRON);
		this.setHardness(5.0F).setResistance(2000.0F);
		this.setDefaultState(this.blockState.getBaseState().withProperty(HEIGHT, 1));
	}

	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, new IProperty[]{HEIGHT});
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos coord) {
		Integer height = ((Integer)state.getValue(HEIGHT));
		return boxes[height];
	}

	@Override
	public void addCollisionBoxToList(final IBlockState bs, final World world, final BlockPos coord,
										final AxisAlignedBB box, final List collisionBoxList,
										final Entity entity) {
		Integer height = ((Integer)world.getBlockState(coord).getValue(HEIGHT));
		super.addCollisionBoxToList(coord, box, collisionBoxList, boxes[height]);
	}

	/**
	 * Override of default block behavior
	 */
	@Override
	public boolean isOpaqueCube(final IBlockState bs) {
		return false;
	}

	/**
	 * Override of default block behavior
	 */
	@Override
	public boolean isFullCube(final IBlockState bs) {
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
	public boolean shouldSideBeRendered(final IBlockState bs, final IBlockAccess world, final BlockPos coord, final EnumFacing face) {
		return true;
	}
}
