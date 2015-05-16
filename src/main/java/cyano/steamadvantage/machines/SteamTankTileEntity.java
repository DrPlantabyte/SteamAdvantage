package cyano.steamadvantage.machines;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.FMLLog;
import cyano.poweradvantage.api.ConduitType;
import cyano.poweradvantage.api.PowerRequest;
import cyano.poweradvantage.api.fluid.FluidRequest;
import cyano.poweradvantage.init.Fluids;
import cyano.steamadvantage.init.Power;

public class SteamTankTileEntity  extends cyano.poweradvantage.api.simple.TileEntitySimplePowerSource {

	private final ItemStack[] inventory = new ItemStack[0];
	private final int[] dataSyncArray = new int[1];
	
	public SteamTankTileEntity() {
		super(Power.steam_power, FluidContainerRegistry.BUCKET_VOLUME * 10, SteamTankTileEntity.class.getSimpleName());
	}
	
	private boolean redstone = true;
	

	@Override
	public void tickUpdate(boolean isServerWorld) {
		if(isServerWorld){
			// server-side
			energyDecay();
		}
	}
	
	private float oldSteam = 0;
	
	@Override
	public void powerUpdate(){
		super.powerUpdate();
		
		redstone = hasRedstoneSignal();
		
		if(oldSteam != this.getEnergy()){
			this.sync();
			oldSteam = this.getEnergy();
		}
	}
	
	@Override
	protected float transmitPowerToConsumers(float amount, ConduitType type, byte priority){
		if(redstone){
			// disabled by redstone signal
			return 0f;
		} else {
			return super.transmitPowerToConsumers(amount,type,priority);
		}
	}
	
	private boolean hasRedstoneSignal() {
		for(int i = 0; i < EnumFacing.values().length; i++){
			if(getWorld().getRedstonePower(getPos(), EnumFacing.values()[i]) > 0) return true;
		}
		return false;
	}
	
	
	private void energyDecay() {
		if(getEnergy() > 0){
			subtractEnergy(Power.ENERGY_LOST_PER_TICK,Power.steam_power);
		}
	}
	
	@Override
	public boolean isPowerSink(){
		return true;
	}
	
	@Override
	public PowerRequest getPowerRequest(ConduitType offer) {
		if(ConduitType.areSameType(Power.steam_power, offer)){
			PowerRequest request = new PowerRequest(PowerRequest.BACKUP_PRIORITY,
					(this.getEnergyCapacity() - this.getEnergy()),
					this);
			return request;
		} else {
			return PowerRequest.REQUEST_NOTHING;
		}
	}
	

	@Override
	protected byte getMinimumSinkPriority(){
		return PowerRequest.BACKUP_PRIORITY+1;
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
		dataSyncArray[0] = Float.floatToIntBits(this.getEnergy());
	}

	@Override
	public void onDataFieldUpdate() {
		this.setEnergy(Float.intBitsToFloat(dataSyncArray[0]), this.getType());
	}

	public float getSteamLevel(){
		return this.getEnergy() / this.getEnergyCapacity();
	}
}
