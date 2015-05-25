package cyano.steamadvantage.blocks;

import cyano.steamadvantage.init.Blocks;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.FMLLog;

public class DrillBitTileEntity extends TileEntity implements IUpdatePlayerListBox{

	public final static float ROTATION_PER_TICK = 360f / 20f; // in degrees
	public final static float TWOPI = (float)(Math.PI * 2 - 0.001);
	public float rotation = 0;
	private EnumFacing direction = EnumFacing.DOWN;
	
	public DrillBitTileEntity(){
		super();
	}
	
	@Override
	public void update(){
		if(getWorld().isRemote){
			rotation += ROTATION_PER_TICK;
			if(rotation >= 360) rotation = 0;
			
		}
	}
	/**
	 * Destroys all drillbits connected to this one
	 */
	public void destroyLine(){
		this.destroyUp();
		this.destroyDown();
		FMLLog.info("destroy line");// TODO: remove debug code
		getWorld().setBlockToAir(getPos()); // redundant because this is being called on block destruction
	}
	/**
	 * destroys upstream drillbit
	 */
	private void destroyUp(){
		FMLLog.info("destroy up");// TODO: remove debug code
		BlockPos coord = this.getPos().offset(this.direction.getOpposite());
		TileEntity n = getWorld().getTileEntity(coord);
		if(n instanceof DrillBitTileEntity){
			((DrillBitTileEntity)n).destroyUp();
		}
		if(getWorld().getBlockState(coord).getBlock() == Blocks.drillbit){
			getWorld().setBlockToAir(coord);
		}
	}
	/**
	 * destroys downstream drillbit
	 */
	private void destroyDown(){
		FMLLog.info("destroy down");// TODO: remove debug code
		BlockPos coord = this.getPos().offset(this.direction);
		TileEntity n = getWorld().getTileEntity(coord);
		if(n instanceof DrillBitTileEntity){
			((DrillBitTileEntity)n).destroyUp();
		}
		if(getWorld().getBlockState(coord).getBlock() == Blocks.drillbit){
			getWorld().setBlockToAir(coord);
		}
	}
}
