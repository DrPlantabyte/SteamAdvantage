package cyano.steamadvantage.machines;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.base.Predicate;

import cyano.poweradvantage.api.ConduitType;
import cyano.poweradvantage.api.GUIBlock;
import cyano.poweradvantage.api.ITypedConduit;
import cyano.poweradvantage.api.PoweredEntity;
import cyano.poweradvantage.api.simple.TileEntitySimplePowerConsumer;
import cyano.poweradvantage.conduitnetwork.ConduitRegistry;
import cyano.steamadvantage.blocks.DrillBitTileEntity;
import cyano.steamadvantage.init.Power;

public class SteamDrillBlock extends GUIBlock implements ITypedConduit {
	private final ConduitType type;
	/**
	 * Blockstate property
	 */
    public static final PropertyDirection FACING = PropertyDirection.create("facing");
    
	public SteamDrillBlock(){
		super(Material.piston);
		this.type = Power.steam_power;
    	super.setHardness(0.75f);
	}
	 
	/**
	 * Override of default block behavior
	 */
	@Override
	public void onBlockAdded(final World world, final BlockPos coord, final IBlockState state) {
		this.setDefaultFacing(world, coord, state);
		ConduitRegistry.getInstance().conduitBlockPlacedEvent(world, world.provider.getDimensionId(), coord, getType());
	}
	/**
	 * This method is called when the block is removed from the world by an entity.
	 */
	@Override
	public void onBlockDestroyedByPlayer(World w, BlockPos coord, IBlockState state){
		super.onBlockDestroyedByPlayer(w, coord, state);
		destroyNeighbors(w,coord,w.getBlockState(coord));
		ConduitRegistry.getInstance().conduitBlockRemovedEvent(w, w.provider.getDimensionId(), coord, getType());
	}
	/**
	 * This method is called when the block is destroyed by an explosion.
	 */
	@Override
	public void onBlockDestroyedByExplosion(World w, BlockPos coord, Explosion boom){
		super.onBlockDestroyedByExplosion(w, coord, boom);
		destroyNeighbors(w,coord,w.getBlockState(coord));
		ConduitRegistry.getInstance().conduitBlockRemovedEvent(w, w.provider.getDimensionId(), coord, getType());
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
	public ConduitType getType() {
		return type;
	}

	/**
	 * Determines whether this conduit is compatible with an adjacent one
	 * @param type The type of energy in the conduit
	 * @param blockFace The side through-which the energy is flowing
	 * @return true if this conduit can flow the given energy type through the given face, false 
	 * otherwise
	 */
	public boolean canAcceptType(ConduitType type, EnumFacing blockFace){
		return ConduitType.areSameType(getType(), type);
	}
	/**
	 * Determines whether this conduit is compatible with a type of energy through any side
	 * @param type The type of energy in the conduit
	 * @return true if this conduit can flow the given energy type through one or more of its block 
	 * faces, false otherwise
	 */
	public boolean canAcceptType(ConduitType type){
		return ConduitType.areSameType(getType(), type);
	}
	
	/**
	 * Determines whether this block/entity should receive energy 
	 * @return true if this block/entity should receive energy
	 */
	public boolean isPowerSink(){
		return true;
	}
	/**
	 * Determines whether this block/entity can provide energy 
	 * @return true if this block/entity can provide energy
	 */
	public boolean isPowerSource(){
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
        world.setBlockState(coord, bs.withProperty((IProperty) FACING, (Comparable)BlockPistonBase.getFacingFromEntity(world, coord, placer).getOpposite()), 2);
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
        if (tileEntity instanceof TileEntitySimplePowerConsumer) {
            InventoryHelper.dropInventoryItems(world, coord, (IInventory)tileEntity);
            world.updateComparatorOutputLevel(coord, this);
        }
        super.breakBlock(world, coord, bs);
    }
    
    /**
     * Sets the default blockstate
     * @param w World instance
     * @param coord Block coordinate
     * @param state Block state
     */
    protected void setDefaultFacing(final World w, final BlockPos coord, final IBlockState state) {
        if (w.isRemote) {
            return;
        }
        final Block block = w.getBlockState(coord.north()).getBlock();
        final Block block2 = w.getBlockState(coord.south()).getBlock();
        final Block block3 = w.getBlockState(coord.west()).getBlock();
        final Block block4 = w.getBlockState(coord.east()).getBlock();
        EnumFacing enumFacing = (EnumFacing)state.getValue(FACING);
        if (enumFacing == EnumFacing.NORTH && block.isFullBlock() && !block2.isFullBlock()) {
            enumFacing = EnumFacing.SOUTH;
        }
        else if (enumFacing == EnumFacing.SOUTH && block2.isFullBlock() && !block.isFullBlock()) {
            enumFacing = EnumFacing.NORTH;
        }
        else if (enumFacing == EnumFacing.WEST && block3.isFullBlock() && !block4.isFullBlock()) {
            enumFacing = EnumFacing.EAST;
        }
        else if (enumFacing == EnumFacing.EAST && block4.isFullBlock() && !block3.isFullBlock()) {
            enumFacing = EnumFacing.WEST;
        }
        w.setBlockState(coord, state.withProperty((IProperty) FACING, (Comparable)enumFacing), 2);
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
    public boolean hasComparatorInputOverride(){
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
    public int getComparatorInputOverride(final World world, final BlockPos coord){
    	TileEntity te = world.getTileEntity(coord);
    	if(te instanceof SteamDrillTileEntity){
    		return ((SteamDrillTileEntity)te).getComparatorOutput();
    	} else {
    		return 0;
    	}
    }
    
    /**
     * Override of default block behavior
     */
    @Override
    public int getRenderType() {
        return 3;
    }
    
    /**
     * Converts metadata into blockstate
     */
    @Override
    public IBlockState getStateFromMeta(final int metaValue) {
        EnumFacing enumFacing = EnumFacing.getFront(metaValue);
        if (enumFacing.getAxis() == EnumFacing.Axis.Y) {
            enumFacing = EnumFacing.NORTH;
        }
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
    protected BlockState createBlockState() {
        return new BlockState(this, new IProperty[] {  FACING });
    }
    
    ///// CLIENT-SIDE CODE /////

    /**
     * (Client-only) Gets the blockstate used for GUI and such.
     */
    @SideOnly(Side.CLIENT)
    @Override
    public IBlockState getStateForEntityRender(final IBlockState bs) {
        return this.getDefaultState().withProperty( FACING, EnumFacing.SOUTH);
    }
    
    /**
     * (Client-only) Override of default block behavior
     */
    @SideOnly(Side.CLIENT)
    @Override
    public Item getItem(final World world, final BlockPos coord) {
        return Item.getItemFromBlock(this);
    }
    

}
