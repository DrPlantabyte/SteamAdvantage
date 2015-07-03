package cyano.steamadvantage.machines;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import cyano.basemetals.registry.CrusherRecipeRegistry;
import cyano.basemetals.registry.recipe.ICrusherRecipe;
import cyano.steamadvantage.init.Power;

public class RockCrusherTileEntity extends cyano.poweradvantage.api.simple.TileEntitySimplePowerConsumer{

	public static final float STEAM_PER_PROGRESS_TICK = 0.5f;
	public static final int TICKS_PER_ACTION = 400;

	private final ItemStack[] inventory = new ItemStack[6]; // slot 0 is input, other slots are output
	private final int[] allSlots;
	private final int[] dataSyncArray = new int[2];
	
	
	private int progress = 0;
	
	public RockCrusherTileEntity() {
		super(Power.steam_power, 200, RockCrusherTileEntity.class.getName());
		allSlots = new int[inventory.length];
		for(int i = 0; i < allSlots.length; i++){
			allSlots[i] = i;
		}
	}

	private boolean redstone = true;
	

	private int timeSinceLastSteamBurst = 0;
	
	private ItemStack itemCheck = null;
	@Override
	public void tickUpdate(boolean isServerWorld) {
		if(isServerWorld){
			// reset progress when item in slot changes
			ItemStack input = inventory[0];
			if(itemCheck != null && ItemStack.areItemsEqual(itemCheck, input) == false){
				progress = 0;
			}
			itemCheck  = input;
			
			// disabled by redstone
			if(redstone){
				if (progress > 0){
					progress--;
				}
			} else {
				ICrusherRecipe recipe = getCrusherRecipe(inventory[0]);
				// reset progress if item is not crushable
				if(progress > 0 && recipe == null){
					progress = 0;
				}
				
				if(recipe != null){
					ItemStack output = recipe.getOutput(); 
					// check if there's a place to put the output item
					int availableSlot = canAddToOutputInventory(output); // returns -1 if no slot is available
					if(availableSlot >= 0){
						// crusher progress
						if(this.getEnergy() >= STEAM_PER_PROGRESS_TICK){
							progress++;
							this.subtractEnergy(STEAM_PER_PROGRESS_TICK, getType());
							// play steam sounds occasionally
							timeSinceLastSteamBurst++;
							if(timeSinceLastSteamBurst > 50){
								getWorld().playSoundEffect(getPos().getX()+0.5, getPos().getY()+0.5, getPos().getZ()+0.5, "random.fizz", 0.5f, 1f);
								timeSinceLastSteamBurst = 0;
							}
						} else if (progress > 0){
							// cannot crush, undo progress
							progress--;
						}
						if(progress >= TICKS_PER_ACTION){
							// add product to output
							if(inventory[availableSlot] == null){
								inventory[availableSlot] = output.copy();
							} else {
								inventory[availableSlot].stackSize += output.stackSize;
							}
							if(--inventory[0].stackSize <= 0){inventory[0] = null;} // decrement the input slot
							progress = 0;
							getWorld().playSoundEffect(getPos().getX()+0.5, getPos().getY()+0.5, getPos().getZ()+0.5, "dig.gravel", 0.5f, 0.2f);
						}
					} else if (progress > 0){
						// cannot crush, undo progress
						progress--;
					}
				}
			}
			energyDecay();
		}
	}


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

	

	private void energyDecay() {
		if(getEnergy() > 0){
			subtractEnergy(Power.ENERGY_LOST_PER_TICK,Power.steam_power);
		}
	}
	
	private ICrusherRecipe getCrusherRecipe(ItemStack item) {
		if(item == null) return null;
		return CrusherRecipeRegistry.getInstance().getRecipeForInputItem(item);
	}

	private boolean hasRedstoneSignal() {
		return getWorld().isBlockPowered(getPos());
	}
	
	public float getProgressLevel(){
		return (float)progress / (float)TICKS_PER_ACTION;
	}
	
	public float getSteamLevel(){
		return this.getEnergy() / this.getEnergyCapacity();
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

	

	private int canAddToOutputInventory(ItemStack output) {
		for(int i = 1; i < inventory.length; i++){
			if(inventory[i] == null) {return i;}
			if(ItemStack.areItemsEqual(output, inventory[i]) && ItemStack.areItemStackTagsEqual(output, inventory[i])
					&& (inventory[i].stackSize + output.stackSize) <= inventory[i].getMaxStackSize()){
				return i;
			}
		}
		return -1;
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tagRoot){
		super.writeToNBT(tagRoot);
		tagRoot.setShort("progress",(short)progress);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tagRoot){
		super.readFromNBT(tagRoot);
		if(tagRoot.hasKey("progress")){
			progress = tagRoot.getShort("progress");
		}
	}



	public int getComparatorOutput() {
		int sum = 0;
		for(int n = 1; n < inventory.length; n++){
			if(inventory[n] != null){
				sum += inventory[n].stackSize * 64 / inventory[n].getMaxStackSize();
			}
		}
		if(sum == 0) return 0;
		return Math.min(Math.max(15 * sum / (64 * (inventory.length - 1)),1),15);
	}
	
///// Item Handling (for hoppers) /////

	@Override
	public int[] getSlotsForFace(EnumFacing side) {
		return allSlots;
	}
	
	@Override
	public boolean canExtractItem(final int slot, final ItemStack targetItem, final EnumFacing side) {
		return slot > 0 && slot < this.getInventory().length;
	}

	@Override
	public boolean canInsertItem(final int slot, final ItemStack srcItem, final EnumFacing side) {
		return this.isItemValidForSlot(slot, srcItem);
	}
	
	@Override
	public boolean isItemValidForSlot(final int slot, final ItemStack item) {
		switch(slot){
		case 0:
			return CrusherRecipeRegistry.getInstance().getRecipeForInputItem(item) != null;
		case 1:
		case 2:
		case 3:
		case 4:
		case 5:
			return true;
		default:
			return false;
		}
	}

}
