package cyano.steamadvantage.machines;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cyano.poweradvantage.api.ConduitType;
import cyano.poweradvantage.api.PowerRequest;
import cyano.poweradvantage.api.fluid.FluidRequest;
import cyano.poweradvantage.init.Fluids;
import cyano.steamadvantage.init.Power;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidContainerRegistry.FluidContainerData;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class OilBoilerTileEntity extends cyano.poweradvantage.api.simple.TileEntitySimplePowerSource implements IFluidHandler{


	public static final int FLUID_BURN_ALIQUOT = FluidContainerRegistry.BUCKET_VOLUME / 10; // number of fluid units burned at a time

	private final FluidTank waterTank;
	private final FluidTank fuelTank;
	private static final Map<Fluid,List<FluidContainerData>> bucketCache = new HashMap<>();
	private static final Map<Fluid,Integer> burnCache = new HashMap<>();


	private int burnTime = 0;
	private int totalBurnTime = 0;

	private final int[] dataSyncArray = new int[6];

	public OilBoilerTileEntity() {
		super(Power.steam_power, 1000, OilBoilerTileEntity.class.getSimpleName());
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
					getWorld().playSoundEffect(getPos().getX()+0.5, getPos().getY()+0.5, getPos().getZ()+0.5, "random.fizz", 0.5f, 1f);
				}
				if(timeSinceSound > 200){
					if(getWaterTank().getFluidAmount() > 0){
						getWorld().playSoundEffect(getPos().getX()+0.5, getPos().getY()+0.5, getPos().getZ()+0.5, "liquid.lava", 0.3f, 1f);
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
		if(getEnergy() > 0){
			subtractEnergy(Power.ENERGY_LOST_PER_TICK,Power.steam_power);
		}
	}


	private int getFuelBurnTime(Fluid fluid, int amount) {
		return getBurnTimePerBucketFor(fluid) * amount / FluidContainerRegistry.BUCKET_VOLUME;
	}

	private static int getBurnTimePerBucketFor(Fluid fluid){
		if(fluid == null) return 0;
		if(burnCache.containsKey(fluid)){
			return burnCache.get(fluid);
		} else {
			if(bucketCache.isEmpty()){
				FluidContainerData[] data = FluidContainerRegistry.getRegisteredFluidContainerData();
				for(FluidContainerData d : data){
					if(d == null) continue;
					Fluid f = d.fluid.getFluid();
					List<FluidContainerData> list = bucketCache.computeIfAbsent(f, (Fluid flu)->new ArrayList<FluidContainerData>());
					list.add(d);
				}
			}
			if(bucketCache.containsKey(fluid)){
				List<FluidContainerData> data = bucketCache.get(fluid);
				for(int i = 0; i < data.size(); i++){
					FluidContainerData datum = data.get(i);
					int burn = TileEntityFurnace.getItemBurnTime( datum.filledContainer);
					if(burn > 0){
						int volume = FluidContainerRegistry.getFluidForFilledItem(datum.filledContainer).amount;
						int burnPerBucket = FluidContainerRegistry.BUCKET_VOLUME * burn / volume;
						burnCache.put(fluid, burnPerBucket);
						return burnPerBucket;
					}
				}
				// no burnables found
				burnCache.put(fluid, 0);
				return 0;
			} else {
				// no containers (buckets) for this fluid
				return 0;
			}
		}
	}


	private void decrementFuel(int vol) {
		this.getFuelTank().drain(vol, true);
	}


	private void boilWater() {
		if(getWaterTank().getFluidAmount() >= 1 && (getEnergyCapacity() - getEnergy()) >= 1){
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

		if(oldEnergy != getEnergy()){
			oldEnergy = getEnergy();
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
		return this.getEnergy() / this.getEnergyCapacity();
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
		dataSyncArray[0] = Float.floatToRawIntBits(this.getEnergy());
		dataSyncArray[1] = this.getWaterTank().getFluidAmount();
		dataSyncArray[2] = this.burnTime;
		dataSyncArray[3] = this.totalBurnTime;
		dataSyncArray[4] = this.getFuelTank().getFluidAmount();
		dataSyncArray[5] = (getFuelTank().getFluid() != null && getFuelTank().getFluid().getFluid() != null 
				? getFuelTank().getFluid().getFluid().getID() 
						: FluidRegistry.WATER.getID());
	}

	@Override
	public void onDataFieldUpdate() {
		this.setEnergy(Float.intBitsToFloat(dataSyncArray[0]), this.getType());
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
	public boolean isPowerSink(){
		return true;
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
		if(Fluids.isFluidType(type)){
			// do nothing, use getTank() methods instead
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


	/**
	 * Determines whether this conduit is compatible with a type of energy through any side
	 * @param type The type of energy in the conduit
	 * @return true if this conduit can flow the given energy type through one or more of its block 
	 * faces, false otherwise
	 */
	@Override
	public boolean canAcceptType(ConduitType type){
		return ConduitType.areSameType(getType(), type) || ConduitType.areSameType(Fluids.fluidConduit_general, type);
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
