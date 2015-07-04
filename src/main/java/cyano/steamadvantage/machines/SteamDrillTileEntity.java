package cyano.steamadvantage.machines;

import java.util.Arrays;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLLog;
import cyano.poweradvantage.util.InventoryWrapper;
import cyano.steamadvantage.blocks.DrillBitTileEntity;
import cyano.steamadvantage.init.Blocks;
import cyano.steamadvantage.init.Power;

public class SteamDrillTileEntity extends cyano.poweradvantage.api.simple.TileEntitySimplePowerConsumer{
	
	public static final int MAX_RANGE = 64;
	public static final float ENERGY_COST_DRILLBIT = 2.5f;
	public static final float ENERGY_COST_MOVE = 10f;
	public static final float ENERGY_COST_PROGRESS_TICK = 1f;
	public static float MINING_TIME_FACTOR = 12.0f;
	
	private final ItemStack[] inventory = new ItemStack[5];
	private final int[] dataSyncArray = new int[3];
	private int progress = 0;
	private int progressGoal = 0;
	private BlockPos targetBlockCoord = null;
	private Block targetBlock = null;
	private List<ItemStack> targetBlockItems = null;
	private EnumFacing oldDir = null;

	
	private boolean deferred = false;
	
	public SteamDrillTileEntity() {
		super(Power.steam_power, 50, RockCrusherTileEntity.class.getName());
	}

	
	private boolean redstone = true;
	@Override
	public void tickUpdate(boolean isServerWorld) {
		if(isServerWorld){
			if(deferred){
				targetBlock(targetBlockCoord);
			}
			EnumFacing face = this.getFacing();
			if(oldDir == null){
				// oldDir becomes null on rotation and new TileEntity creation
				// destroy drillbits in all directions but the current one
				oldDir = face;
				for(EnumFacing d : EnumFacing.values()){
					if(d == face) continue;
					destroyDrillBit(d);
				}
			}
			
			// disabled by redstone
			if(redstone){
				if (progress > 0){
					progress = 0;
				}
			} else {
				if(targetBlockCoord != null){
					// mining time
					if(getEnergy() > ENERGY_COST_PROGRESS_TICK && hasSpaceForItems(targetBlockItems) && canMine(targetBlockCoord)){
						this.subtractEnergy(ENERGY_COST_PROGRESS_TICK, Power.steam_power);
						progress++;
						if(progress >= progressGoal){
							// Mined it!
							getWorld().playSoundEffect(targetBlockCoord.getX()+0.5, targetBlockCoord.getY()+0.5, targetBlockCoord.getZ()+0.5, targetBlock.stepSound.getBreakSound(), 0.5f, 1f);
							getWorld().playSoundEffect(getPos().getX()+0.5, getPos().getY()+0.5, getPos().getZ()+0.5, "random.fizz", 0.5f, 1f);
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

	private EnumFacing trackDirection(){
		for(EnumFacing dir : EnumFacing.values()){
			if(getWorld().getBlockState(getPos().offset(dir)).getBlock() == Blocks.steam_track){
				return dir;
			}
		}
		return null;
	}
	
	private boolean followTrack(){
		if(this.getEnergy() < ENERGY_COST_MOVE) return false;
		EnumFacing trackDir = trackDirection();
		if(trackDir == null) return false;
		
		// clone this block into neighboring block
		this.untargetBlock();
		World w = getWorld();
		BlockPos nextPos = getPos().offset(trackDir);
		w.setBlockState(nextPos, w.getBlockState(getPos()), 2);
		SteamDrillTileEntity te = (SteamDrillTileEntity)w.getTileEntity(nextPos);
		NBTTagCompound dataTransfer = new NBTTagCompound();
		this.writeToNBT(dataTransfer);
		te.readFromNBT(dataTransfer);
		te.setPos(nextPos);
		te.validate();
		te.markDirty();
		Arrays.fill(this.getInventory(), null);
		
		// replace this block with steam pipe
		destroyDrillBit(this.getFacing());
		w.setBlockState(getPos().offset(getFacing()), cyano.poweradvantage.init.Blocks.steel_frame.getDefaultState());
		w.setBlockState(getPos(), Blocks.steam_pipe.getDefaultState(), 2);
		
		
		this.subtractEnergy(ENERGY_COST_MOVE, getType());
		return true;
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
	private float oldEnergy = 0;
	private int oldProgress = 0;
	@Override public void powerUpdate(){
		if(deferred){
			targetBlock(targetBlockCoord);
		}
		super.powerUpdate();
		boolean flagSync = progress != oldProgress || oldEnergy != getEnergy();
		oldProgress = progress;
		oldEnergy = getEnergy();
		
		redstone = hasRedstoneSignal();
		
		
		EnumFacing f = getFacing();
		BlockPos n = getPos().offset(f);
		
		// manage drill bits and find next block
		if(redstone || this.getEnergy() <= 0){
			// no power, destroy drill
			destroyDrillBit(f);
			flagSync = true;
			this.untargetBlock();
		} else {
			// drill baby drill!
			if(targetBlockCoord == null ){
				// find new block
				boolean hitEnd = false;
				for(int i = 0; i <= MAX_RANGE ; i++){
					if(i == MAX_RANGE){
						hitEnd = true;
						break;
					}
					if(getWorld().getBlockState(n).getBlock() != Blocks.drillbit){
						if(getWorld().isAirBlock(n) || getWorld().getBlockState(n).getBlock().isReplaceable(getWorld(), n)){
							// this block is not worth mining, replace it
							DrillBitTileEntity.createDrillBitBlock(getWorld(), n, f);
							this.subtractEnergy(ENERGY_COST_DRILLBIT, Power.steam_power);
							flagSync = true;
							break;
						} else {
							// found a block!
							if(canMine(n)){
								targetBlock(n);
							} else {
								hitEnd = true;
							}
							flagSync = true;
							break;
						}
					}
					n = n.offset(f);
					if(n.getY() == 0 || n.getY() == 255){
						hitEnd = true;
						break;
					}
				}
				// if hit end of range, move along track
				if(hitEnd){
					boolean moved = followTrack();
					if(moved){
						cyano.poweradvantage.conduitnetwork.ConduitRegistry.getInstance()
								.conduitBlockRemovedEvent(getWorld(), getWorld().provider.getDimensionId(), getPos(), getType());
						return;
					}
				}
			} else {
				// currently drilling a block
				// block validation
				if(getWorld().isAirBlock(targetBlockCoord) || getWorld().getBlockState(targetBlockCoord).getBlock() != targetBlock){
					// Block changed! invalidate!
					untargetBlock();
					flagSync = true;
				}
			}
		}
		
		if(flagSync){
			this.sync();
		}
		

		// push inventory to adjacent chest
		BlockPos adj = getPos().offset(f.getOpposite());
		if(!redstone && !getWorld().isAirBlock(adj)){
			inventoryTransfer(adj,f);
		}
	}

	private void destroyDrillBit(EnumFacing f) {
		BlockPos n = getPos().offset(f);
		while(getWorld().getBlockState(n).getBlock() == cyano.steamadvantage.init.Blocks.drillbit){
			getWorld().setBlockToAir(n);
			n = n.offset(f);
		}
	}

	private void targetBlock (BlockPos n){
		progress = 0;
		targetBlockCoord = n;
		progressGoal = this.getBlockStrength(n);
		targetBlock = getWorld().getBlockState(n).getBlock();
		targetBlockItems = targetBlock.getDrops(getWorld(), n, getWorld().getBlockState(n), 0);
		deferred = false;
	}
	
	private void deferredTargetBlock (BlockPos n){
		targetBlockCoord = n;
		deferred = true;
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
		return getWorld().isBlockPowered(getPos());
	}
	
	private boolean canMine(BlockPos coord){
		Block b = getWorld().getBlockState(coord).getBlock();
		return !(b == net.minecraft.init.Blocks.bedrock || b == net.minecraft.init.Blocks.barrier);
	}
	
	private int getBlockStrength(BlockPos coord){
		if(getWorld().isAirBlock(coord)){
			return 0;
		}
		Block block = getWorld().getBlockState(coord).getBlock();
		return (int)(Math.max(MINING_TIME_FACTOR * block.getBlockHardness(getWorld(), coord),0.5f * MINING_TIME_FACTOR));
	}
	
	

	private void inventoryTransfer(BlockPos adj, EnumFacing otherFace) {
		TileEntity te = getWorld().getTileEntity(adj);
		if(te instanceof IInventory ){
			ISidedInventory inv = InventoryWrapper.wrap((IInventory)te);
			int[] accessibleSlots = inv.getSlotsForFace(otherFace);
			if(accessibleSlots.length == 0) return;
			for(int mySlot = 0; mySlot < this.inventory.length; mySlot++){
				if(this.inventory[mySlot] == null) continue;
				for(int i = 0; i < accessibleSlots.length; i++){
					int theirSlot = accessibleSlots[i];
					ItemStack theirItem = inv.getStackInSlot(theirSlot);
					if(inv.canInsertItem(theirSlot, inventory[mySlot], otherFace)){
						if(theirItem == null){
							ItemStack newItem = inventory[mySlot].copy();
							newItem.stackSize = 1;
							inv.setInventorySlotContents(theirSlot, newItem);
							inventory[mySlot].stackSize--;
							if(inventory[mySlot].stackSize <= 0) inventory[mySlot] = null;
							return;
						} else if(ItemStack.areItemsEqual(theirItem, inventory[mySlot]) 
								&& ItemStack.areItemStackTagsEqual(theirItem, inventory[mySlot])
								&& theirItem.stackSize < theirItem.getMaxStackSize()
								&& theirItem.stackSize < inv.getInventoryStackLimit()){
							theirItem.stackSize++;
							inventory[mySlot].stackSize--;
							if(inventory[mySlot].stackSize <= 0) inventory[mySlot] = null;
							return;
						}
					}
				}
			}
			
		}
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
			// Note: world object for tile entities is set AFTER loading them from NBT
			if(getWorld() == null){
				this.deferredTargetBlock(new BlockPos(x,y,z));
			} else {
				this.targetBlock(new BlockPos(x,y,z));
			}
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
