package cyano.steamadvantage.machines;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import cyano.steamadvantage.blocks.DrillBitTileEntity;
import cyano.steamadvantage.init.Blocks;
import cyano.steamadvantage.init.Power;

public class SteamDrillTileEntity extends cyano.poweradvantage.api.simple.TileEntitySimplePowerConsumer{
	
	public static final int MAX_RANGE = 64;
	public static final float ENERGY_COST_DRILLBIT = 2.5f;
	public static final float ENERGY_COST_PROGRESS_TICK = 1f;
	
	private final ItemStack[] inventory = new ItemStack[5];
	private final int[] dataSyncArray = new int[3];
	private int progress = 0;
	private int progressGoal = 0;
	private BlockPos targetBlockCoord = null;
	private Block targetBlock = null;
	private List<ItemStack> targetBlockItems = null;
			
	public SteamDrillTileEntity() {
		super(Power.steam_power, 50, RockCrusherTileEntity.class.getName());
	}

	// TODO: place drillbits when running
	// TODO: remove drill bits when not running
	// TODO: replaced broken drillbits
	// TODO: mining

	private boolean redstone = true;
	@Override
	public void tickUpdate(boolean isServerWorld) {
		// TODO Auto-generated method stub
		if(isServerWorld){
			// disabled by redstone
			if(redstone){
				if (progress > 0){
					progress = 0;
				}
			} else {
				if(targetBlockCoord != null){
					// mining time
					if(getEnergy() > ENERGY_COST_PROGRESS_TICK && hasSpaceForItems(targetBlockItems)){
						this.subtractEnergy(ENERGY_COST_PROGRESS_TICK, Power.steam_power);
						progress++;
						if(progress >= progressGoal){
							// Mined it!
							getWorld().setBlockToAir(targetBlockCoord);
							for(ItemStack item : targetBlockItems){
								addItem(item);
							}
							untargetBlock();
						}
					}
				}
				
			}
			// lost steam
			energyDecay();
		}
		
	}
	
	private boolean hasSpaceForItems(List<ItemStack> items) {
		if(items == null || items.isEmpty()) return true;
		boolean success = true;
		for(int i = 0; i < items.size(); i++){
			boolean canFit = false;
			for(int n = 0; n < inventory.length; n++){
				if(canStack(items.get(i),inventory[n])) {
					canFit = true;
					break;
				}
			}
			success = success && canFit;
			
		}
		return success;
	}

	private boolean canStack(ItemStack newItem, ItemStack slot) {
		if(slot == null || newItem == null) return true;
		return ItemStack.areItemsEqual(newItem, slot) 
				&& ItemStack.areItemStackTagsEqual(newItem, slot) 
				&& (newItem.stackSize + slot.stackSize <= slot.getItem().getItemStackLimit(slot));
	}
	
	public ItemStack addItem(ItemStack in){
		for(int i = 0; i < inventory.length; i++){
			if(inventory[i] == null){
				inventory[i] = in;
				return null;
			} else if(ItemStack.areItemsEqual(in, inventory[i]) 
					&& ItemStack.areItemStackTagsEqual(in, inventory[i])){
				// are stackable
				if(inventory[i].stackSize >= inventory[i].getItem().getItemStackLimit(inventory[i])){
					continue;
				} else if(in.stackSize + inventory[i].stackSize <= inventory[i].getItem().getItemStackLimit(inventory[i])){
					inventory[i].stackSize += in.stackSize;
					return null;
				} else {
					int delta = inventory[i].getItem().getItemStackLimit(inventory[i]) - inventory[i].stackSize;
					inventory[i].stackSize += delta;
					in.stackSize -= delta;
				}
			}
		}
		// return the remainder, if any
		return in;
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
		dataSyncArray[1] = progress;
		dataSyncArray[2] = progressGoal;
	}

	@Override
	public void onDataFieldUpdate() {
		this.setEnergy(Float.intBitsToFloat(dataSyncArray[0]), this.getType());
		this.progress = dataSyncArray[1];
		progressGoal = dataSyncArray[2];
	}
	
	@Override public void powerUpdate(){
		super.powerUpdate();
		
		redstone = hasRedstoneSignal();
		
		EnumFacing f = getFacing();
		BlockPos n = getPos().offset(f);
		
		// manage drill bits and find next block
		if(this.getEnergy() <= 0){
			// no power, destroy drill
			while(getWorld().getBlockState(n).getBlock() == cyano.steamadvantage.init.Blocks.drillbit){
				getWorld().setBlockToAir(n);
				n = n.offset(f);
			}
		} else {
			if(redstone) return; // disabled by redstone
			// drill baby drill!
			if(targetBlockCoord == null ){
				// find new block
				for(int i = 0; i < MAX_RANGE; i++){
					if(getWorld().getBlockState(n).getBlock() != Blocks.drillbit){
						if(getWorld().isAirBlock(n) || getWorld().getBlockState(n).getBlock().isReplaceable(getWorld(), n)){
							// this block is not worth mining, replace it
							DrillBitTileEntity.createDrillBitBlock(getWorld(), n, f);
							this.subtractEnergy(ENERGY_COST_DRILLBIT, Power.steam_power);
							return;
						} else {
							// found a block!
							targetBlock(n);
							
							return;
						}
					}
					n = n.offset(f);
				}
				// hit end of range
				// do nothing
				return;
			} else {
				// currently drilling a block
				// block validation
				if(getWorld().isAirBlock(targetBlockCoord) || getWorld().getBlockState(targetBlockCoord).getBlock() != targetBlock){
					// Block changed! invalidate!
					untargetBlock();
				}
			}
		}
	}
	
	private void targetBlock (BlockPos n){
		progress = 0;
		targetBlockCoord = n;
		progressGoal = this.getBlockStrength(n);
		targetBlock = getWorld().getBlockState(n).getBlock();
		targetBlockItems = targetBlock.getDrops(getWorld(), n, getWorld().getBlockState(n), 0);
	}
	
	private void untargetBlock(){
		progress = 0;
		progressGoal = 0;
		targetBlockCoord = null;
		targetBlock = null;
		targetBlockItems = null;
	}
	
	private EnumFacing getFacing(){
		return (EnumFacing)worldObj.getBlockState(getPos()).getValue(SteamDrillBlock.FACING);
	}
	
	private void energyDecay() {
		if(getEnergy() > 0){
			subtractEnergy(Power.ENERGY_LOST_PER_TICK,Power.steam_power);
		}
	}
	
	private boolean hasRedstoneSignal() {
		for(int i = 0; i < EnumFacing.values().length; i++){
			if(getWorld().getRedstonePower(getPos(), EnumFacing.values()[i]) > 0) return true;
		}
		return false;
	}
	
	private int getBlockStrength(BlockPos coord){
		if(worldObj.isAirBlock(coord)){
			return 0;
		}
		return (int)(getWorld().getBlockState(getPos()).getBlock().getBlockHardness(getWorld(), coord) * 30);
	}
	

	@Override
	public void writeToNBT(NBTTagCompound tagRoot){
		super.writeToNBT(tagRoot);
		tagRoot.setShort("progress",(short)progress);
		if(targetBlockCoord != null){
			tagRoot.setInteger("targetX", targetBlockCoord.getX());
			tagRoot.setInteger("targetY", targetBlockCoord.getY());
			tagRoot.setInteger("targetZ", targetBlockCoord.getZ());
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tagRoot){
		super.readFromNBT(tagRoot);
		if(tagRoot.hasKey("progress")){
			progress = tagRoot.getShort("progress");
		}
		if(tagRoot.hasKey("targetX") && tagRoot.hasKey("targetY") && tagRoot.hasKey("targetZ")){
			int x = tagRoot.getInteger("targetX");
			int y = tagRoot.getInteger("targetY");
			int z = tagRoot.getInteger("targetZ");
			this.targetBlock(new BlockPos(x,y,z));
		}
	}


	
	public float getProgressLevel(){
		if(progressGoal > 0) 
			return (float)progress / (float)progressGoal;
		return 0;
	}
	
	public float getSteamLevel(){
		return this.getEnergy() / this.getEnergyCapacity();
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
}
