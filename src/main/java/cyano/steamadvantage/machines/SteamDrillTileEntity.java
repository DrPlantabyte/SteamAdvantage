package cyano.steamadvantage.machines;

import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import cyano.steamadvantage.init.Power;

public class SteamDrillTileEntity extends cyano.poweradvantage.api.simple.TileEntitySimplePowerConsumer{
	
	public static final int MAX_RANGE = 64;

	private final ItemStack[] inventory = new ItemStack[5];
	private final int[] dataSyncArray = new int[3];
	private int progress = 0;
	private int progressGoal = 0;
			
	public SteamDrillTileEntity() {
		super(Power.steam_power, 50, RockCrusherTileEntity.class.getName());
	}

	// TODO: place drillbits when running
	// TODO: remove drill bits when not running
	// TODO: replaced broken drillbits
	// TODO: mining
	
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

	@Override
	public void tickUpdate(boolean isServerWorld) {
		// TODO Auto-generated method stub
		
	}
	
	private EnumFacing getFacing(){
		return (EnumFacing)worldObj.getBlockState(getPos()).getValue(SteamDrillBlock.FACING);
	}
	
	private int getBlockStrength(){
		BlockPos coord = getDrillingBlock();
		if(worldObj.isAirBlock(coord)){
			// hit range limit
			return 0;
		}
		return (int)(getWorld().getBlockState(getPos()).getBlock().getBlockHardness(getWorld(), coord) * 30);
	}
	
	private BlockPos getDrillingBlock(){
		int dist = 0;
		EnumFacing dir = getFacing();
		BlockPos coord = this.getPos().offset(dir);
		while(dist < MAX_RANGE 
				&& coord.getY() > 0
				&& coord.getY() < 255
				&& worldObj.isAirBlock(coord)){
			coord = coord.offset(dir);
		}
		return coord;
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
