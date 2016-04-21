package cyano.steamadvantage.machines;

import cyano.poweradvantage.api.ConduitType;
import cyano.steamadvantage.init.Power;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;

import static cyano.steamadvantage.util.SoundHelper.playSoundAtTileEntity;

public class BlastFurnaceTileEntity extends cyano.poweradvantage.api.simple.TileEntitySimplePowerMachine{


	public static final float STEAM_PER_TICK = 0.5f;

	private final ItemStack[] inventory = new ItemStack[7]; // slot 0 is input, other slots are output
	private final int[] allSlots;
	private final int[] dataSyncArray = new int[6];// 3 progresses, burn time, total burn time, temperature
	
	private final float[] progress = new float[3];

	private int burnTime = 0;
	private int totalBurnTime = 0;
	private float temperature = 0;
	private static final float maxTemperature = 2000f;
	private static final float minSmeltingTemperature = 100f;
	
	private boolean redstone = true;
	
	public BlastFurnaceTileEntity() {
		super(Power.steam_power, 200, BlastFurnaceTileEntity.class.getName());
		allSlots = new int[inventory.length];
		for(int i = 0; i < allSlots.length; i++){
			allSlots[i] = i;
		}
	}
	
	@SideOnly(Side.CLIENT)
	public float[] getProgress(){
		float[] a = new float[progress.length];
		System.arraycopy(progress, 0, a, 0, progress.length);
		return a;
	}
	
	public float getBurnLevel(){
		if(totalBurnTime <= 0) return 0;
		return (float)burnTime/(float)totalBurnTime;
	}
	
	public float getTemperatureLevel(){
		return temperature / maxTemperature;
	}
	
	@Override
	public void tickUpdate(boolean isServerWorld) {
		if(isServerWorld){
			// fual input heat
			float e = 0;
			if(burnTime > 0){
				burnTime--;
				e = 2.5f;
			} else {
				int fuel = getFuelBurnTime(inventory[0]);
				if( fuel > 0 && (!redstone) && (canSmelt(1) || canSmelt(2) || canSmelt(3))){
					burnTime = fuel;
					totalBurnTime = fuel;
					decrementFuel();
					playSoundAtTileEntity( SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.AMBIENT, 0.5f, 1f, this);
				}
				energyDecay();
			}
			// steam bonus
			if(this.getEnergy(Power.steam_power) > STEAM_PER_TICK && burnTime > 0 && (!redstone)){
				e = 15;
				this.subtractEnergy(STEAM_PER_TICK, Power.steam_power);
			}
			temperature = iterateTemperature(temperature,e);
			if(temperature < minSmeltingTemperature){
				// too cold, fail all
				Arrays.fill(progress, 0f);
			} else {
				// do smelting
				boolean smeltSuccess = false;
				for(int i = 0; i < progress.length; i++){
					int slot = 1+i; // input slots are slots 1-3
					if(canSmelt(slot)){
						progress[i] += progressPerTickAtTemperature(temperature);
						if(progress[i] >= 1){
							progress[i] = 0;
							doSmelt(slot);
							if(--inventory[slot].stackSize <= 0){inventory[slot] = null;} // decrement the input slot
							if(!smeltSuccess){
								playSoundAtTileEntity(SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.AMBIENT, 0.5f, 1f, this);
							}
							smeltSuccess = true;
						}
					} else {
						progress[i] = 0;
					}
				}
			}
		}
		
		
	}
	
	

	private boolean canSmelt(int slot) {
		ItemStack input = inventory[slot];
		if(input == null) return false;
		ItemStack output = FurnaceRecipes.instance().getSmeltingResult(input);
		if(output == null) return false;
		ItemStack outputSlot = inventory[slot+3];
		if(outputSlot == null) return true;
		return (ItemStack.areItemsEqual(output, outputSlot) 
				&& ItemStack.areItemStackTagsEqual(output, outputSlot)
				&& (output.stackSize + outputSlot.stackSize) <= outputSlot.getMaxStackSize());
	}
	
	private void doSmelt(int slot) {
		ItemStack input = inventory[slot];
		ItemStack output = FurnaceRecipes.instance().getSmeltingResult(input);
		ItemStack outputSlot = inventory[slot+3];
		if(outputSlot == null){
			inventory[slot+3] = output.copy();
		} else {
			outputSlot.stackSize += output.stackSize;
		}
		
	}

	float oldSteam = 0;
	float oldTemp = 0;
	float[] oldProgress = new float[3];
	
	@Override
	public void powerUpdate(){
		super.powerUpdate();
		
		boolean updateFlag = false;
		
		if(oldSteam != this.getEnergy(Power.steam_power)){
			updateFlag = true;
			oldSteam = this.getEnergy(Power.steam_power);
		}
		if(oldTemp != temperature){
			updateFlag = true;
			oldTemp = temperature;
		}
		
		for(int i = 0; i < 3; i++){
			updateFlag = updateFlag || (oldProgress[i] != progress[i]);
		}
		System.arraycopy(progress, 0, oldProgress, 0, 3);
		
		
		if(updateFlag ){
			this.sync();
		}
		redstone = hasRedstoneSignal();
	}

	private boolean hasRedstoneSignal() {
		return getWorld().isBlockPowered(getPos());
	}


	private void energyDecay() {
		if(getEnergy(Power.steam_power) > 0){
			subtractEnergy(Power.ENERGY_LOST_PER_TICK,Power.steam_power);
		}
	}
	
	private static int getFuelBurnTime(ItemStack item) {
		if(item == null) return 0;
		return TileEntityFurnace.getItemBurnTime(item);
	}
	
	private void decrementFuel() {
		if(inventory[0].stackSize == 1 && inventory[0].getItem().getContainerItem(inventory[0]) != null){
			inventory[0] = inventory[0].getItem().getContainerItem(inventory[0]);
		} else {
			this.decrStackSize(0, 1);
		}
	}
	
	private static float progressPerTickAtTemperature(float temperature){
		return 3.5E-6f * temperature;
	}

	private static float iterateTemperature(float prevTemp, float energyInput){
		// energyInput is 2.5 without steam, 15 with steam
		final float timeFactor = 0.3f;
		final float energyInputFactor = 10.0f;
		final float energyLossFactor1 = 0.00003f;
		final float energyLossFactor2 = 0.03f;
		return constrain(
				prevTemp + timeFactor * (energyInputFactor * energyInput - energyLossFactor1 * (prevTemp * prevTemp) - energyLossFactor2 * prevTemp), 
				0, maxTemperature
		);
	}
	
	private static float constrain(float v, float min, float max){
		if(v < min) return min;
		if(v > max) return max;
		return v;
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
		dataSyncArray[0] = Float.floatToRawIntBits(progress[0]);
		dataSyncArray[1] = Float.floatToRawIntBits(progress[1]);
		dataSyncArray[2] = Float.floatToRawIntBits(progress[2]);
		dataSyncArray[3] = burnTime;
		dataSyncArray[4] = totalBurnTime;
		dataSyncArray[5] = Float.floatToRawIntBits(temperature);
	}

	@Override
	public void onDataFieldUpdate() {
		progress[0] = Float.intBitsToFloat(dataSyncArray[0]);
		progress[1] = Float.intBitsToFloat(dataSyncArray[1]);
		progress[2] = Float.intBitsToFloat(dataSyncArray[2]);
		burnTime = dataSyncArray[3];
		totalBurnTime = dataSyncArray[4];
		temperature = Float.intBitsToFloat(dataSyncArray[5]);
	}
	

	@Override
	public void writeToNBT(NBTTagCompound tagRoot){
		super.writeToNBT(tagRoot);
		prepareDataFieldsForSync();
		tagRoot.setIntArray("Data", getDataFieldArray());
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tagRoot){
		super.readFromNBT(tagRoot);
		if(tagRoot.hasKey("Data")){
			int[] data = tagRoot.getIntArray("Data");
			System.arraycopy(data, 0, this.getDataFieldArray(), 0, Math.min(data.length,this.getDataFieldArray().length));
			onDataFieldUpdate();
		}
	}

	public int getComparatorOutput() {
		int sum = 0;
		for(int n = 0; n < 3; n++){
			if(inventory[n] != null){
				sum += inventory[n].stackSize * 64 / inventory[n].getMaxStackSize();
			}
		}
		if(sum == 0) return 0;
		return Math.min(Math.max(15 * sum / (64 * 3),1),15);
	}
	
	
	///// Item Handling (for hoppers) /////

	@Override
	public int[] getSlotsForFace(EnumFacing side) {
		return allSlots;
	}
	
	@Override
	public boolean canExtractItem(final int slot, final ItemStack targetItem, final EnumFacing side) {
		return slot >= 4 && slot < this.getInventory().length;
	}

	@Override
	public boolean canInsertItem(final int slot, final ItemStack srcItem, final EnumFacing side) {
		return this.isItemValidForSlot(slot, srcItem) && slot <= 3;
	}
	
	@Override
	public boolean isItemValidForSlot(final int slot, final ItemStack item) {
		switch(slot){
		case 0:
			return this.getFuelBurnTime(item) > 0;
		case 1:
		case 2:
		case 3:
			return FurnaceRecipes.instance().getSmeltingResult(item) != null;
		case 4:
		case 5:
		case 6:
			return true;
		default:
			return false;
		}
	}


	@Override
	public boolean isPowerSink(ConduitType powerType) {
		return ConduitType.areSameType(Power.steam_power,powerType);
	}

	@Override
	public boolean isPowerSource(ConduitType powerType) {
		return false;
	}
}
