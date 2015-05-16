package cyano.steamadvantage.machines;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import cyano.poweradvantage.api.ConduitType;
import cyano.steamadvantage.init.Power;

public class RockCrusherTileEntity extends cyano.poweradvantage.api.simple.TileEntitySimplePowerConsumer{

	public static final float STEAM_PER_PROGRESS_TICK = 0.5f;
	public static final int TICKS_PER_ACTION = 400;

	private final ItemStack[] inventory = new ItemStack[6]; // slot 0 is input, other slots are output
	private final int[] dataSyncArray = new int[2];
	
	
	private int progress = 0;
	
	public RockCrusherTileEntity() {
		super(Power.steam_power, 200, RockCrusherTileEntity.class.getName());
	}

	private boolean redstone = true;
	

	@Override
	public void tickUpdate(boolean isServerWorld) {
		// TODO Auto-generated method stub
		// TODO: reset progress when item in slot changes
	}
	// TODO: item handler input (to input slot) and output (from output slots)

	private float oldSteam = 0;
	private int oldProgress = 0;
	
	@Override
	public void powerUpdate(){
		super.powerUpdate();
		
		redstone = hasRedstoneSignal();
		
		if(oldSteam != this.getEnergy() || progress != oldProgress){
			this.sync();
			oldSteam = this.getEnergy();
			oldProgress = progress;
		}
	}

	private boolean hasRedstoneSignal() {
		for(int i = 0; i < EnumFacing.values().length; i++){
			if(getWorld().getRedstonePower(getPos(), EnumFacing.values()[i]) > 0) return true;
		}
		return false;
	}
	
	@Override
	protected ItemStack[] getInventory() {
		return inventory;
	}

	@Override
	public int[] getDataFieldArray() {
		return dataSyncArray;
	}

	@Override
	public void prepareDataFieldsForSync() {
		dataSyncArray[0] = Float.floatToRawIntBits(this.getEnergy());
		dataSyncArray[1] = this.progress;
	}

	@Override
	public void onDataFieldUpdate() {
		this.setEnergy(Float.intBitsToFloat(dataSyncArray[0]), this.getType());
		this.progress = dataSyncArray[1];
	}


}
