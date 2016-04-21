package cyano.steamadvantage.machines;

import cyano.poweradvantage.api.*;
import cyano.poweradvantage.api.simple.TileEntitySimplePowerMachine;
import cyano.poweradvantage.conduitnetwork.ConduitRegistry;
import cyano.steamadvantage.blocks.DrillBitTileEntity;
import cyano.steamadvantage.init.Power;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import java.util.Random;

public class SteamDrillBlock extends GUIBlock implements ITypedConduit {
	private final ConduitType[] type = {Power.steam_power};
	/**
	 * Blockstate property
	 */
	public static final PropertyDirection FACING = PropertyDirection.create("facing");

	public SteamDrillBlock(){
		super(Material.PISTON);
		super.setHardness(0.75f);
	}

	/**
	 * Override of default block behavior
	 */
	@Override
	public void onBlockAdded(final World world, final BlockPos coord, final IBlockState state) {
		super.onBlockAdded(world, coord, state);
		ConduitRegistry.getInstance().conduitBlockPlacedEvent(world, world.provider.getDimension(), coord, Power.steam_power);
	}
	/**
	 * This method is called when the block is removed from the world by an entity.
	 */
	@Override
	public void onBlockDestroyedByPlayer(World w, BlockPos coord, IBlockState state){
		super.onBlockDestroyedByPlayer(w, coord, state);
		destroyNeighbors(w,coord,w.getBlockState(coord));
		ConduitRegistry.getInstance().conduitBlockRemovedEvent(w, w.provider.getDimension(), coord, Power.steam_power);
	}
	/**
	 * This method is called when the block is destroyed by an explosion.
	 */
	@Override
	public void onBlockDestroyedByExplosion(World w, BlockPos coord, Explosion boom){
		super.onBlockDestroyedByExplosion(w, coord, boom);
		destroyNeighbors(w,coord,w.getBlockState(coord));
		ConduitRegistry.getInstance().conduitBlockRemovedEvent(w, w.provider.getDimension(), coord, Power.steam_power);
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
	/**
	 * Creates a TileEntity for this block when the block is placed into the 
	 * world.
	 * @return A new TileEntity instance, probably one that extends 
	 * <b>TileEntitySimplePowerConsumer</b>.
	 */
	@Override
	public PoweredEntity createNewTileEntity(final World world, final int metaDataValue){
		return new SteamDrillTileEntity();
	}

	/**
	 * Used to decides whether or not a conduit should connect to this block 
	 * based on its energy type.
	 * @return The type of energy for this block 
	 */
	@Override
	public ConduitType[] getTypes() {
		return type;
	}


	@Override
	public boolean canAcceptConnection(PowerConnectorContext connection){
		return ConduitType.areSameType(Power.steam_power, connection.powerType);
	}
@Override
	public boolean isPowerSink(ConduitType t){
		return true;
	}
	@Override
	public boolean isPowerSource(ConduitType t){
		return false;
	}

	/**
	 * Override of default block behavior
	 */
	@Override
	public Item getItemDropped(final IBlockState state, final Random prng, final int i3) {
		return Item.getItemFromBlock(this);
	}


	/**
	 * Creates the blockstate of this block when it is placed in the world
	 */
	@Override
	public IBlockState onBlockPlaced(final World world, final BlockPos coord, final EnumFacing facing, 
			final float f1, final float f2, final float f3, 
			final int meta, final EntityLivingBase player) {
		return this.getDefaultState().withProperty( FACING, facing.getOpposite());
	}

	/**
	 * Creates the blockstate of this block when it is placed in the world
	 */
	@Override
	public void onBlockPlacedBy(final World world, final BlockPos coord, final IBlockState bs, 
			final EntityLivingBase placer, final ItemStack srcItemStack) {
		if (srcItemStack.hasDisplayName()) {
			final TileEntity tileEntity = world.getTileEntity(coord);
			if (tileEntity instanceof PoweredEntity){
				((PoweredEntity)tileEntity).setCustomInventoryName(srcItemStack.getDisplayName());
			}
		}
	}





	/**
	 * Destroys the TileEntity associated with this block when this block 
	 * breaks.
	 */
	@Override
	public void breakBlock(final World world, final BlockPos coord, final IBlockState bs) {
		final TileEntity tileEntity = world.getTileEntity(coord);
		if (tileEntity instanceof TileEntitySimplePowerMachine) {
			InventoryHelper.dropInventoryItems(world, coord, (IInventory)tileEntity);
			world.updateComparatorOutputLevel(coord, this);
		}
		super.breakBlock(world, coord, bs);
	}

	
	/**
	 * This method tells Minecraft whether this block has a signal that can be 
	 * measured by a redstone comparator.
	 * <br><br>
	 * You are encouraged to integrate redstone control into your machines. 
	 * This means outputin a redstone signal with the 
	 * <code>getComparatorInputOverride(...)</code> method and (in your 
	 * TileEntity class) reading the redstone input with the 
	 * <code>World.isBlockPowered(...)</code> method.<br><br>
	 * Typically, machines output a redstone value proportional to the amount of 
	 * stuff in their inventory and are disabled when they receive a redstone 
	 * signal.
	 * @return true if this block can be measured by a redstone comparator, 
	 * false otherwise
	 */
	@Override
	public boolean hasComparatorInputOverride(IBlockState bs){
		return true;
	}

	/**
	 * This method gets the output for a redstone comparator placed against this 
	 * block.
	 * <br><br>
	 * You are encouraged to integrate redstone control into your machines. 
	 * This means outputin a redstone signal with the 
	 * <code>getComparatorInputOverride(...)</code> method and (in your 
	 * TileEntity class) reading the redstone input with the 
	 * <code>World.isBlockPowered(...)</code> method.<br><br>
	 * Typically, machines output a redstone value proportional to the amount of 
	 * stuff in their inventory and are disabled when they receive a redstone 
	 * signal.
	 * @param world World object
	 * @param coord Coordinates of this block
	 * @return a number from 0 to 15
	 */
	@Override
	public int getComparatorInputOverride(IBlockState bs, final World world, final BlockPos coord){
		TileEntity te = world.getTileEntity(coord);
		if(te instanceof SteamDrillTileEntity){
			return ((SteamDrillTileEntity)te).getComparatorOutput();
		} else {
			return 0;
		}
	}

	/**
	 * Converts metadata into blockstate
	 */
	@Override
	public IBlockState getStateFromMeta(final int metaValue) {
		EnumFacing enumFacing = EnumFacing.values()[metaValue % EnumFacing.values().length];
		return this.getDefaultState().withProperty( FACING, enumFacing);
	}

	/**
	 * Converts blockstate into metadata
	 */
	@Override
	public int getMetaFromState(final IBlockState bs) {
		return ((EnumFacing)bs.getValue( FACING)).getIndex();
	}

	/**
	 * Creates a blockstate
	 */
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] {  FACING });
	}


}
