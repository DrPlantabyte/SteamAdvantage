package cyano.steamadvantage.machines;

import cyano.poweradvantage.api.ConduitType;
import cyano.poweradvantage.api.PowerRequest;
import cyano.steamadvantage.init.Power;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;

public class SteamTankTileEntity  extends cyano.poweradvantage.api.simple.TileEntitySimplePowerMachine {

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
		
		if(oldSteam != this.getEnergy(Power.steam_power)){
			this.sync();
			oldSteam = this.getEnergy(Power.steam_power);
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
		return getWorld().isBlockPowered(getPos());
	}
	
	
	private void energyDecay() {
		if(getEnergy(Power.steam_power) > 0){
			subtractEnergy(Power.ENERGY_LOST_PER_TICK,Power.steam_power);
		}
	}

	@Override
	public boolean isPowerSink(ConduitType offer){
		return true;
	}
	@Override
	public boolean isPowerSource(ConduitType offer){
		return true;
	}
	
	@Override
	public PowerRequest getPowerRequest(ConduitType offer) {
		if(ConduitType.areSameType(Power.steam_power, offer)){
			PowerRequest request = new PowerRequest(PowerRequest.BACKUP_PRIORITY,
					(this.getEnergyCapacity(Power.steam_power) - this.getEnergy(Power.steam_power)),
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
		dataSyncArray[0] = Float.floatToIntBits(this.getEnergy(Power.steam_power));
	}

	@Override
	public void onDataFieldUpdate() {
		this.setEnergy(Float.intBitsToFloat(dataSyncArray[0]), Power.steam_power);
	}

	public float getSteamLevel(){
		return this.getEnergy(Power.steam_power) / this.getEnergyCapacity(Power.steam_power);
	}
}
