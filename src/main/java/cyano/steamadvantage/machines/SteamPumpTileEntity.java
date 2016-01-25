package cyano.steamadvantage.machines;

import java.util.HashSet;
import java.util.Set;

import cyano.poweradvantage.api.ConduitType;
import cyano.poweradvantage.api.PowerRequest;
import cyano.poweradvantage.init.Fluids;
import cyano.steamadvantage.init.Power;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fml.common.FMLLog;

public class SteamPumpTileEntity extends cyano.poweradvantage.api.simple.TileEntitySimplePowerSource implements IFluidHandler{



	private final FluidTank tank;
	public static final float ENERGY_COST_PIPE = 5f;
	public static final float ENERGY_COST_VERTICAL = 1f;
	public static final float ENERGY_COST_PUMP = 32f;
	public static final int VOLUME_PER_PUMP = FluidContainerRegistry.BUCKET_VOLUME;
	public static final byte PUMP_INTERVAL = 32;
	public static final byte PIPE_INTERVAL = 11;
	private static final int limit = 5 * 5 * 5; // do not search more than this many block at a time



	private final int[] dataSyncArray = new int[4];
	private byte timeUntilNextPump = PUMP_INTERVAL;

	public SteamPumpTileEntity() {
		super(Power.steam_power, 300f, SteamPumpTileEntity.class.getSimpleName());
		tank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME);
	}

	private boolean redstone = true;

	private int timeToSound = 0;

	@Override
	public void tickUpdate(boolean isServerWorld) {
		if(isServerWorld){
			// server-side logic
			net.minecraft.tileentity.TileEntityPiston k;
			if(!redstone){
				if(timeUntilNextPump > 0) timeUntilNextPump--;
				if(timeUntilNextPump == 0 && getTank().getFluidAmount() <= 0){
					World w = getWorld();
					boolean success = false;
					BlockPos target = this.getPos().down();
					while(target.getY() > 0 && w.getBlockState(target).getBlock() == cyano.steamadvantage.init.Blocks.pump_pipe_steam){
						target = target.down();
					}
					if(target.getY() > 0 && w.isAirBlock(target) && getEnergy() >= ENERGY_COST_PIPE){
						// place pipe
						w.setBlockState(target, cyano.steamadvantage.init.Blocks.pump_pipe_steam.getDefaultState());
						this.subtractEnergy(ENERGY_COST_PIPE, Power.steam_power);
						timeUntilNextPump = PIPE_INTERVAL;
						getWorld().playSoundEffect(target.getX()+0.5, target.getY()+0.5, target.getZ()+0.5, "step.stone", 0.3f, 1f);
					} else {
						// pump fluids
						BlockPos fluidSource = null;
						Set<BlockPos> searchSpace = new HashSet<>(limit + 1); // pre-allocate max memory usage
						fluidSource = scan(searchSpace,target.down(), limit);
						if(fluidSource == null) fluidSource = scan(searchSpace,target.north(), limit);
						if(fluidSource == null) fluidSource = scan(searchSpace,target.west(), limit);
						if(fluidSource == null) fluidSource = scan(searchSpace,target.south(), limit);
						if(fluidSource == null) fluidSource = scan(searchSpace,target.east(), limit);
						if(fluidSource != null){
							float cost = ENERGY_COST_PUMP+ENERGY_COST_VERTICAL*(getPos().getY()-fluidSource.getY());
							// Found a fluid to suck-up!
							IBlockState blockstate = w.getBlockState(fluidSource);
							Fluid f = getFluid(blockstate);
							if(f != null && getEnergy() >= cost){
								this.getTank().fill(new FluidStack(f,FluidContainerRegistry.BUCKET_VOLUME), true);
								this.subtractEnergy(cost, Power.steam_power);
								w.setBlockToAir(fluidSource);
								success = true;
							}
						}
						timeUntilNextPump = PUMP_INTERVAL;
					}
					if(success){
						getWorld().playSoundEffect(getPos().getX()+0.5, getPos().getY()+0.5, getPos().getZ()+0.5, "random.fizz", 0.5f, 1f);
						getWorld().playSoundEffect(getPos().getX()+0.5, getPos().getY()+0.5, getPos().getZ()+0.5, "tile.piston.out", 0.3f, 1f);
						timeToSound = 14;
					}
				}
			}
			if(timeToSound == 1){
				getWorld().playSoundEffect(getPos().getX()+0.5, getPos().getY()+0.5, getPos().getZ()+0.5, "tile.piston.in", 0.3f, 1f);
			}
			if(timeToSound > 0) timeToSound--;
			energyDecay();
		}
	}



	private Fluid getFluid(IBlockState blockstate) {
		return FluidRegistry.lookupFluidForBlock(blockstate.getBlock());
	}



	private BlockPos scan(Set<BlockPos> searchSpace, BlockPos coord, int limit) {
		if(isFluidBlock(coord) == false) return null;
		do{
			if(isSourceBlock(coord)){
				return coord;
			} else {
				searchSpace.add(coord);
				BlockPos next;
				boolean nomore = true;
				for(EnumFacing dir : EnumFacing.VALUES){
					next = coord.offset(dir);
					if(!searchSpace.contains(next) && isFluidBlock(next)){
						coord = next;
						nomore = false;
						break;
					}
				}
				if(nomore){
					return null;
				}
			}
		} while(searchSpace.size() < limit);
		return null;
	}
	
	private boolean isSourceBlock( BlockPos coord){
		IBlockState bs = getWorld().getBlockState(coord);
		if(bs.getBlock() instanceof BlockLiquid){
			return (Integer)bs.getValue(BlockDynamicLiquid.LEVEL) == 0;
		} else if(bs.getBlock() instanceof BlockFluidClassic){
			return ((BlockFluidClassic)bs.getBlock()).isSourceBlock(getWorld(), coord);
		}else{
			return false;
		}
	}



	private boolean isFluidBlock(BlockPos coord) {
		World w = getWorld();
		if(w.isBlockLoaded(coord) == false) return false;
		Block b = w.getBlockState(coord).getBlock();
		return (b instanceof BlockLiquid || b instanceof IFluidBlock);
	}



	private boolean hasRedstoneSignal() {
		return getWorld().isBlockPowered(getPos());
	}


	private void energyDecay() {
		if(getEnergy() > 0){
			subtractEnergy(Power.ENERGY_LOST_PER_TICK,Power.steam_power);
		}
	}



	private float oldEnergy = 0;
	private int oldFluid = 0;
	@Override
	public void powerUpdate(){
		// deliberately NOT calling super.powerUpdate()
		if(this.getTank().getFluidAmount() > 0){
			ConduitType type = Fluids.fluidToConduitType(getTank().getFluid().getFluid());
			float availableAmount = getTank().getFluidAmount();
			float delta = this.transmitPowerToConsumers(availableAmount, type, getMinimumSinkPriority());
			if(delta > 0){
				getTank().drain(Math.max((int)delta,1),true); // no free energy!
			}
		}
		// powerUpdate occurs once every 8 world ticks and is scheduled such that neighboring 
		// machines don't powerUpdate in the same world tick. To reduce network congestion, 
		// I'm doing the synchonization logic here instead of in the tickUpdate method
		boolean updateFlag = false;

		if(oldEnergy != getEnergy()){
			oldEnergy = getEnergy();
			updateFlag = true;
		}
		if(oldFluid != getTank().getFluidAmount()){
			oldFluid = getTank().getFluidAmount();
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


	public FluidTank getTank(){
		return tank;
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
		dataSyncArray[1] = this.getTank().getFluidAmount();
		dataSyncArray[2] = (this.getTank().getFluidAmount() > 0 ? this.getTank().getFluid().getFluid().getID() : FluidRegistry.WATER.getID());
		dataSyncArray[3] = this.timeUntilNextPump;
	}

	@Override
	public void onDataFieldUpdate() {
		this.setEnergy(Float.intBitsToFloat(dataSyncArray[0]), Power.steam_power);
		this.getTank().setFluid(new FluidStack(FluidRegistry.getFluid(dataSyncArray[2]),dataSyncArray[1]));
		this.timeUntilNextPump = (byte)dataSyncArray[3];
	}


	/**
	 * Handles data saving and loading
	 * @param tagRoot An NBT tag
	 */
	@Override
	public void writeToNBT(final NBTTagCompound tagRoot) {
		super.writeToNBT(tagRoot);
		NBTTagCompound tankTag = new NBTTagCompound();
		this.getTank().writeToNBT(tankTag);
		tagRoot.setTag("Tank", tankTag);
		tagRoot.setByte("NextPump", (byte)this.timeUntilNextPump);
	}
	/**
	 * Handles data saving and loading
	 * @param tagRoot An NBT tag
	 */
	@Override
	public void readFromNBT(final NBTTagCompound tagRoot) {
		super.readFromNBT(tagRoot);
		if (tagRoot.hasKey("Tank")) {
			NBTTagCompound tankTag = tagRoot.getCompoundTag("TankOut");
			getTank().readFromNBT(tankTag);
			if(tankTag.hasKey("Empty")){
				// empty the tank if NBT says its empty (not default behavior of Tank.readFromNBT(...) )
				getTank().setFluid(null);
			}
		}

		if (tagRoot.hasKey("NextPump")) {
			this.timeUntilNextPump = tagRoot.getByte("NextPump");
		}
	}

	public int getComparatorOutput() {
		return 15 * getTank().getFluidAmount() / getTank().getCapacity();
	}

	///// Overrides to make this a multi-type block /////
	/**
	 * Determines whether this block/entity should receive energy 
	 * @return true if this block/entity should receive energy
	 */
	@Override
	public boolean isPowerSink(){
		return true;
	}
	/**
	 * Determines whether this block/entity can provide energy 
	 * @return true if this block/entity can provide energy
	 */
	@Override
	public boolean isPowerSource(){
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
			if(amount > 0 && this.canFill(null, Fluids.conduitTypeToFluid(type))){
				return this.fill(null, new FluidStack(Fluids.conduitTypeToFluid(type),(int)amount), true);
			} else if (amount < 0 && this.canDrain(null, Fluids.conduitTypeToFluid(type))){
				return -1 * this.drain(null, (int)amount, true).amount;
			}
		}
		return super.addEnergy(amount, type);
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
		return addEnergy(-1*amount,type);
	}

	@Override
	public PowerRequest getPowerRequest(ConduitType offer) {
		if(redstone) return PowerRequest.REQUEST_NOTHING;
		if(Fluids.isFluidType(offer)){
			return PowerRequest.REQUEST_NOTHING;
		} else  if(ConduitType.areSameType(offer, Power.steam_power)){
			return new PowerRequest(PowerRequest.MEDIUM_PRIORITY,this.getEnergyCapacity() - this.getEnergy(), this);
		}
		return PowerRequest.REQUEST_NOTHING;
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
		if(getTank().getFluidAmount() <= 0 || getTank().getFluid().getFluid().equals(fluid.getFluid())){
			return getTank().fill(fluid, forReal);
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
		if(getTank().getFluidAmount() > 0 && getTank().getFluid().getFluid().equals(fluid.getFluid())){
			return getTank().drain(fluid.amount,forReal);
		} else {
			return new FluidStack(getTank().getFluid().getFluid(),0);
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
		if(getTank().getFluidAmount() > 0 ){
			return getTank().drain(amount,forReal);
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
		if(fluid == null) return false;
		if(getTank().getFluidAmount() <= 0) return true;
		return getTank().getFluidAmount() <= getTank().getCapacity() && fluid.equals(getTank().getFluid().getFluid());
	}
	/**
	 * Implementation of IFluidHandler
	 * @param face Face of the block being polled
	 * @param fluid The fluid being added/removed
	 */
	@Override
	public boolean canDrain(EnumFacing face, Fluid fluid) {
		if(fluid == null) return false;
		return getTank().getFluidAmount() > 0 && fluid.equals(getTank().getFluid().getFluid());
	}

	/**
	 * Implementation of IFluidHandler
	 * @param face Face of the block being polled
	 * @return array of FluidTankInfo describing all of the FluidTanks
	 */
	@Override
	public FluidTankInfo[] getTankInfo(EnumFacing face) {
		FluidTankInfo[] arr = new FluidTankInfo[1];
		arr[0] = getTank().getInfo();
		return arr;
	}

	///// end of IFluidHandler methods /////

	@Override
	public boolean isItemValidForSlot(final int slot, final ItemStack item) {
		return false; // no inventory
	}
}
