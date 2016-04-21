package cyano.steamadvantage.machines;

import cyano.basemetals.init.Items;
import cyano.poweradvantage.api.ConduitType;
import cyano.poweradvantage.api.PowerRequest;
import cyano.poweradvantage.api.fluid.FluidRequest;
import cyano.poweradvantage.init.Fluids;
import cyano.poweradvantage.registry.FuelRegistry;
import cyano.steamadvantage.SteamAdvantage;
import cyano.steamadvantage.init.Power;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fluids.*;

import java.util.HashMap;
import java.util.Map;

import static cyano.steamadvantage.util.SoundHelper.playSoundAtTileEntity;

public class OilBoilerTileEntity extends cyano.poweradvantage.api.simple.TileEntitySimplePowerMachine implements IFluidHandler{


	public static final int FLUID_BURN_ALIQUOT = FluidContainerRegistry.BUCKET_VOLUME / 10; // number of fluid units burned at a time

	private final FluidTank waterTank;
	private final FluidTank fuelTank;
	private static final Map<Fluid,Float> flammibilityCache = new HashMap<>();
	private static final Map<Fluid,Integer> burnCache = new HashMap<>();


	private int burnTime = 0;
	private int totalBurnTime = 0;

	private final int[] dataSyncArray = new int[6];

	public OilBoilerTileEntity() {
		super(new ConduitType[]{Power.steam_power, Fluids.fluidConduit_general}, new float[]{1000,1000}, OilBoilerTileEntity.class.getSimpleName());
		waterTank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME * 4);
		fuelTank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME * 4);
	}

	private boolean redstone = true;

	private int timeSinceSound = 0;

	@Override
	public void tickUpdate(boolean isServerWorld) {
		if(isServerWorld){
			// server-side logic
			if(burnTime > 0){
				burnTime--;
				boilWater();
				// play steam sounds occasionally
				if(getWorld().rand.nextInt(100) == 0){
					playSoundAtTileEntity( SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.AMBIENT, 0.5f, 1f, this);
				}
				if(timeSinceSound > 200){
					if(getWaterTank().getFluidAmount() > 0){
						playSoundAtTileEntity( SoundEvents.BLOCK_LAVA_AMBIENT, SoundCategory.AMBIENT, 0.3f, 1f, this);
					}
					timeSinceSound = 0;
				}
				timeSinceSound++;
			} else {
				if(!hasRedstoneSignal() && getFuelTank().getFluidAmount() > 0 && getWaterTank().getFluidAmount() > 0){
					int volume = Math.min(FLUID_BURN_ALIQUOT, getFuelTank().getFluidAmount());
					int fuel = getFuelBurnTime(getFuelTank().getFluid().getFluid(), volume);
					if( fuel > 0 && this.getWaterTank().getFluidAmount() > 0){
						burnTime = fuel;
						totalBurnTime = fuel;
						decrementFuel(volume);
					}
				}
				energyDecay();
			}
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


	private int getFuelBurnTime(Fluid fluid, int amount) {
		return (int)(getBurnTimePerBucketFor(fluid) * amount / FluidContainerRegistry.BUCKET_VOLUME);
	}

	private static Float getBurnTimePerBucketFor(Fluid fluid){
		if(fluid == null) return 0f;
		if(flammibilityCache.containsKey(fluid)){
			return flammibilityCache.get(fluid)*1000;
		} else {
			// first, check configured overrides
			if(SteamAdvantage.fluidBurnValues.containsKey(fluid.getUnlocalizedName())){
				Float fuelPerBucket = SteamAdvantage.fluidBurnValues.get(fluid.getUnlocalizedName());
				flammibilityCache.put(fluid,0.001F*fuelPerBucket);
				return fuelPerBucket;
			}
			// second, check universal bucket fuel registry
			ItemStack bucket = new ItemStack(Items.universal_bucket);
			int vol = Items.universal_bucket.getCapacity(bucket);
			Items.universal_bucket.fill(bucket,new FluidStack(fluid,vol),true);
			Float burnTicksPerAmount = (float) FuelRegistry.getActualBurntimeForItem(bucket) / (float) vol;
			if(burnTicksPerAmount > 0){
				flammibilityCache.put(fluid,burnTicksPerAmount);
				return 1000*burnTicksPerAmount;
			}

			// third, deprecated fluid container registry
			FluidContainerRegistry.FluidContainerData[] registry = FluidContainerRegistry.getRegisteredFluidContainerData();
			for(FluidContainerRegistry.FluidContainerData datum : registry){
				if(datum.fluid.getFluid() == fluid){
					if(FuelRegistry.getActualBurntimeForItem(datum.filledContainer) > 0){
						vol = FluidContainerRegistry.getContainerCapacity(datum.filledContainer);
						Float fuelPerVolume = (float)FuelRegistry.getActualBurntimeForItem(datum.filledContainer) / (float)vol;
						flammibilityCache.put(fluid,fuelPerVolume);
						return 1000*fuelPerVolume;
					}
				}
			}

			// it just isn't a fuel
			flammibilityCache.put(fluid,0f);
		}
		// no containers (buckets) for this fluid
		return 0f;


	}


	private void decrementFuel(int vol) {
		this.getFuelTank().drain(vol, true);
	}


	private void boilWater() {
		if(getWaterTank().getFluidAmount() >= 1 && (getEnergyCapacity(Power.steam_power) - getEnergy(Power.steam_power)) >= 1){
			getWaterTank().drain(1, true);
			addEnergy(1.25f,Power.steam_power); // oil-burning furnace is 25% more efficient than normal furnace
		}
	}

	private float oldEnergy = 0;
	private int oldBurnTime = 0;
	private int oldWater = 0;
	@Override
	public void powerUpdate(){
		super.powerUpdate();
		// powerUpdate occurs once every 8 world ticks and is scheduled such that neighboring 
		// machines don't powerUpdate in the same world tick. To reduce network congestion, 
		// I'm doing the synchonization logic here instead of in the tickUpdate method
		boolean updateFlag = false;

		if(oldEnergy != getEnergy(Power.steam_power)){
			oldEnergy = getEnergy(Power.steam_power);
			updateFlag = true;
		}
		if(oldBurnTime != burnTime){
			oldBurnTime = burnTime;
			updateFlag = true;
		}
		if(oldWater != getWaterTank().getFluidAmount()){
			oldWater = getWaterTank().getFluidAmount();
			updateFlag = true;
		}

		redstone = hasRedstoneSignal();

		if(updateFlag){
			super.sync();
		}
	}


	public float getSteamLevel(){
		return this.getEnergy(Power.steam_power) / this.getEnergyCapacity(Power.steam_power);
	}

	public float getBurnLevel(){
		if(burnTime == 0){
			return 0;
		} else if (totalBurnTime == 0){
			return 1;
		}
		return Math.max(0,Math.min(1,((float)burnTime)/((float)totalBurnTime)));
	}

	public FluidTank getWaterTank(){
		return waterTank;
	}
	public FluidTank getFuelTank(){
		return fuelTank;
	}

	@Override
	protected ItemStack[] getInventory() {
		return null;
	}

	@Override
	public int[] getDataFieldArray() {
		return dataSyncArray;
	}

	@Override
	public void prepareDataFieldsForSync() {
		dataSyncArray[0] = Float.floatToRawIntBits(this.getEnergy(Power.steam_power));
		dataSyncArray[1] = this.getWaterTank().getFluidAmount();
		dataSyncArray[2] = this.burnTime;
		dataSyncArray[3] = this.totalBurnTime;
		dataSyncArray[4] = this.getFuelTank().getFluidAmount();
		dataSyncArray[5] = (getFuelTank().getFluid() != null && getFuelTank().getFluid().getFluid() != null 
				? FluidRegistry.getFluidID(getFuelTank().getFluid().getFluid())
						: FluidRegistry.getFluidID(FluidRegistry.WATER));
	}

	@Override
	public void onDataFieldUpdate() {
		this.setEnergy(Float.intBitsToFloat(dataSyncArray[0]), Power.steam_power);
		this.getWaterTank().setFluid(new FluidStack(FluidRegistry.WATER,dataSyncArray[1]));
		this.burnTime = dataSyncArray[2];
		this.totalBurnTime = dataSyncArray[3];
		this.getFuelTank().setFluid(new FluidStack(FluidRegistry.getFluid(dataSyncArray[5]),dataSyncArray[4]));
	}


	/**
	 * Handles data saving and loading
	 * @param tagRoot An NBT tag
	 */
	@Override
	public void writeToNBT(final NBTTagCompound tagRoot) {
		super.writeToNBT(tagRoot);
		NBTTagCompound tankTag = new NBTTagCompound();
		this.getWaterTank().writeToNBT(tankTag);
		tagRoot.setTag("WaterTank", tankTag);
		NBTTagCompound tankTag2 = new NBTTagCompound();
		this.getFuelTank().writeToNBT(tankTag2);
		tagRoot.setTag("FuelTank", tankTag2);
		if(this.burnTime > 0)tagRoot.setInteger("BurnTime", this.burnTime);
		if(this.totalBurnTime > 0)tagRoot.setInteger("BurnTimeTotal", this.totalBurnTime);
	}
	/**
	 * Handles data saving and loading
	 * @param tagRoot An NBT tag
	 */
	@Override
	public void readFromNBT(final NBTTagCompound tagRoot) {
		super.readFromNBT(tagRoot);
		if (tagRoot.hasKey("WaterTank")) {
			NBTTagCompound tankTag = tagRoot.getCompoundTag("WaterTank");
			getWaterTank().readFromNBT(tankTag);
			if(tankTag.hasKey("Empty")){
				// empty the tank if NBT says its empty (not default behavior of Tank.readFromNBT(...) )
				getWaterTank().setFluid(null);
			}
		}
		if (tagRoot.hasKey("FuelTank")) {
			NBTTagCompound tankTag = tagRoot.getCompoundTag("FuelTank");
			getFuelTank().readFromNBT(tankTag);
			if(tankTag.hasKey("Empty")){
				// empty the tank if NBT says its empty (not default behavior of Tank.readFromNBT(...) )
				getFuelTank().setFluid(null);
			}
		}
		if(tagRoot.hasKey("BurnTime")){
			this.burnTime = tagRoot.getInteger("BurnTime");
		} else {
			this.burnTime = 0;
		}
		if(tagRoot.hasKey("BurnTimeTotal")){
			this.totalBurnTime = tagRoot.getInteger("BurnTimeTotal");
		} else {
			this.totalBurnTime = 0;
		}
	}

	public int getComparatorOutput() {
		return 15 * this.getFuelTank().getFluidAmount() / this.getFuelTank().getCapacity();
	}

	///// Overrides to make this a multi-type block /////

	@Override
	public boolean isPowerSink(ConduitType type){
		return !ConduitType.areSameType(Power.steam_power, type);
	}

	@Override
	public boolean isPowerSource(ConduitType type){
		return ConduitType.areSameType(Power.steam_power, type);
	}

	/**
	 * Adds "energy" as a fluid to the FluidTank returned by getTank(). This implementation ignores 
	 * all non-fluid energy types.
	 * @param amount amount of energy/fluid to add
	 * @param type the type of energy/fluid being added.
	 * @return The amount that was actually added
	 */
	@Override
	public float addEnergy(float amount, ConduitType type){
		if(Fluids.isFluidType(type)){
			if(this.canFill(null, Fluids.conduitTypeToFluid(type))){
				return this.fill(null, new FluidStack(Fluids.conduitTypeToFluid(type),(int)amount), true);
			} else {
				return 0;
			}
		}else{
			return super.addEnergy(amount, type);
		}
	}
	/**
	 * Sets the tank contents using the energy API method
	 * @param amount amount of energy/fluid to add
	 * @param type the type of energy/fluid being added.
	 */
	@Override
	public void setEnergy(float amount,ConduitType type) {
		if(Fluids.isFluidType(type) && !ConduitType.areSameType(Fluids.fluidConduit_general,type)){
			Fluid f = Fluids.conduitTypeToFluid(type);
			if(f == FluidRegistry.WATER){
				this.getWaterTank().setFluid(new FluidStack(f,(int)amount));
			}else{
				this.getFuelTank().setFluid(new FluidStack(f,(int)amount));
			}
		}else{
			super.setEnergy(amount, type);
		}
	}
	/**
	 * Subtracts "energy" as a fluid to the FluidTank returned by getTank(). This implementation 
	 * ignores all non-fluid energy types.
	 * @param amount amount of energy/fluid to add
	 * @param type the type of energy/fluid being added.
	 * @return The amount that was actually added
	 */
	@Override
	public float subtractEnergy(float amount, ConduitType type){
		if(Fluids.isFluidType(type)){
			if(this.canDrain(null, Fluids.conduitTypeToFluid(type))){
				return -1 * this.drain(null, new FluidStack(Fluids.conduitTypeToFluid(type),(int)amount), true).amount;
			} else {
				return 0;
			}
		}else{
			return super.subtractEnergy(amount, type);
		}
	}

	@Override
	public PowerRequest getPowerRequest(ConduitType offer) {
		if(this.redstone) return PowerRequest.REQUEST_NOTHING;
		if(Fluids.conduitTypeToFluid(offer) == FluidRegistry.WATER){
			PowerRequest request = new FluidRequest(FluidRequest.MEDIUM_PRIORITY+1,
					(getWaterTank().getCapacity() - getWaterTank().getFluidAmount()),
					this);
			return request;
		} else if(getBurnTimePerBucketFor(Fluids.conduitTypeToFluid(offer)) > 0 
				&& (getFuelTank().getFluidAmount() <= 0 || getFuelTank().getFluid().getFluid().equals(Fluids.conduitTypeToFluid(offer)))){
			PowerRequest request = new FluidRequest(FluidRequest.MEDIUM_PRIORITY+1,
					(getFuelTank().getCapacity() - getFuelTank().getFluidAmount()),
					this);
			return request;
		} else {
			return PowerRequest.REQUEST_NOTHING;
		}
	}

	///// end multi-type overrides /////



	///// IFluidHandler /////

	/**
	 * Implementation of IFluidHandler
	 * @param face Face of the block being polled
	 * @param fluid The fluid being added/removed
	 * @param forReal if true, then the fluid in the tank will change
	 */
	@Override
	public int fill(EnumFacing face, FluidStack fluid, boolean forReal) {
		if(fluid == null) return 0;
		if(fluid.getFluid() == FluidRegistry.WATER){
			if(getWaterTank().getFluidAmount() <= 0 || getWaterTank().getFluid().getFluid().equals(fluid.getFluid())){
				return getWaterTank().fill(fluid, forReal);
			}
		} else if(getBurnTimePerBucketFor(fluid.getFluid()) > 0){
			if(getFuelTank().getFluidAmount() <= 0 || getFuelTank().getFluid().getFluid().equals(fluid.getFluid())){
				return getFuelTank().fill(fluid, forReal);
			}
		}
		return 0;
	}
	/**
	 * Implementation of IFluidHandler
	 * @param face Face of the block being polled
	 * @param fluid The fluid being added/removed
	 * @param forReal if true, then the fluid in the tank will change
	 */
	@Override
	public FluidStack drain(EnumFacing face, FluidStack fluid, boolean forReal) {
		if(getWaterTank().getFluidAmount() > 0 && getWaterTank().getFluid().getFluid().equals(FluidRegistry.WATER)){
			return getWaterTank().drain(fluid.amount,forReal);
		} else if(getFuelTank().getFluidAmount() > 0 && getFuelTank().getFluid().getFluid().equals(fluid.getFluid())){
			return getFuelTank().drain(fluid.amount,forReal);
		} else {
			return new FluidStack(getWaterTank().getFluid().getFluid(),0);
		}
	}
	/**
	 * Implementation of IFluidHandler
	 * @param face Face of the block being polled
	 * @param amount The amount of fluid being added/removed
	 * @param forReal if true, then the fluid in the tank will change
	 */
	@Override
	public FluidStack drain(EnumFacing face, int amount, boolean forReal) {
		if(getWaterTank().getFluidAmount() > 0 ){
			return getWaterTank().drain(amount,forReal);
		} else if(getFuelTank().getFluidAmount() > 0 ){
			return getFuelTank().drain(amount,forReal);
		} else {
			return null;
		}
	}
	/**
	 * Implementation of IFluidHandler
	 * @param face Face of the block being polled
	 * @param fluid The fluid being added/removed
	 */
	@Override
	public boolean canFill(EnumFacing face, Fluid fluid) {
		if(fluid == FluidRegistry.WATER) {
			if(getWaterTank().getFluidAmount() <= 0) return true;
			return getWaterTank().getFluidAmount() <= getWaterTank().getCapacity() && fluid.equals(getWaterTank().getFluid().getFluid());
		} else if(getBurnTimePerBucketFor(fluid) > 0){
			if(getFuelTank().getFluidAmount() <= 0) return true;
			return getFuelTank().getFluidAmount() <= getFuelTank().getCapacity() && fluid.equals(getFuelTank().getFluid().getFluid());
		}
		return false;
	}
	/**
	 * Implementation of IFluidHandler
	 * @param face Face of the block being polled
	 * @param fluid The fluid being added/removed
	 */
	@Override
	public boolean canDrain(EnumFacing face, Fluid fluid) {
		if(fluid == null) return false;
		if(fluid == FluidRegistry.WATER) {
			return getWaterTank().getFluidAmount() > 0 && fluid.equals(getWaterTank().getFluid().getFluid());
		} else {
			return getFuelTank().getFluidAmount() > 0 && fluid.equals(getFuelTank().getFluid().getFluid());
		}
	}

	/**
	 * Implementation of IFluidHandler
	 * @param face Face of the block being polled
	 * @return array of FluidTankInfo describing all of the FluidTanks
	 */
	@Override
	public FluidTankInfo[] getTankInfo(EnumFacing face) {
		FluidTankInfo[] arr = new FluidTankInfo[2];
		arr[0] = getWaterTank().getInfo();
		arr[1] = getFuelTank().getInfo();
		return arr;
	}

	///// end of IFluidHandler methods /////

	@Override
	public boolean isItemValidForSlot(final int slot, final ItemStack item) {
		return false; // no inventory
	}
}
