package cyano.steamadvantage.blocks;

import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.FMLLog;

public class DrillBitTileEntity extends TileEntity implements IUpdatePlayerListBox{

	public final static float ROTATION_PER_TICK = 360f / 20f; // in degrees
	public final static float TWOPI = (float)(Math.PI * 2 - 0.001);
	public float rotation = 0;
	
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
}
