package cyano.steamadvantage.machines;

import cyano.poweradvantage.api.ConduitType;
import cyano.steamadvantage.init.Blocks;
import cyano.steamadvantage.init.Power;
import net.minecraft.entity.Entity;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.List;

import static cyano.steamadvantage.util.SoundHelper.playSoundAtTileEntity;

public class SteamElevatorTileEntity extends cyano.poweradvantage.api.simple.TileEntitySimplePowerMachine{

	public static final float STEAM_PER_ELEVATOR_MOVE = 32f;
	public static final int MAX_RANGE = 16;
	private static final double smallAmount = 0.00390625;

	private final ItemStack[] inventory = new ItemStack[0]; // slot 0 is input, other slots are output
	private final int[] dataSyncArray = new int[1];
	
	
	private boolean up = false;
	
	public SteamElevatorTileEntity() {
		super(Power.steam_power, STEAM_PER_ELEVATOR_MOVE*2, SteamElevatorTileEntity.class.getName());
	}

	private boolean redstone = true;
	

	private int timeSinceLastSteamBurst = 0;
	
	private ItemStack itemCheck = null;
	@Override
	public void tickUpdate(boolean isServerWorld) {
		if(isServerWorld){
			
			// controlled by redstone
			if(redstone != up){
				if(redstone) {
					up = moveUp();
				} else {
					up = !moveDown();
				}
			}
		}
	}

	private boolean moveUp(){
		if(this.getEnergy(Power.steam_power) < STEAM_PER_ELEVATOR_MOVE) return false;
		this.subtractEnergy(STEAM_PER_ELEVATOR_MOVE, Power.steam_power);
		playSoundAtTileEntity( SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.AMBIENT, 0.5f, 1f, this);
		playSoundAtTileEntity( SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.BLOCKS, 0.5f, 1f, this);
		// scan up to find solid block, then move piston up to 2 less than that
		final int maxTop = MAX_RANGE + 2;
		int dist = 0;
		BlockPos p = this.getPos().up();
		while(p.getY() < 256 && dist < MAX_RANGE && getWorld().isAirBlock(p)){
			dist++;
			p = p.up();
		}
		BlockPos topPos = p.down(2);
		p = p.down(3); // make room for character to stand in
		boolean top = true;
		for(; p.getY() > getPos().getY(); p = p.down()){
			if(top){
				// platform top
				getWorld().setBlockState(p, Blocks.steam_elevator_platforms[4]);
				top = false;
			} else {
				// shaft
				getWorld().setBlockState(p, Blocks.steam_elevator_platforms[0]);
			}
		}
		// move people
		List<Entity> passengers = getWorld().getEntitiesWithinAABB(Entity.class, 
				new AxisAlignedBB(getPos().getX(),getPos().getY()+1,getPos().getZ(),
						getPos().getX()+1, topPos.getY(), getPos().getZ()+1));

		for(Entity e : passengers){
			e.setPositionAndUpdate(e.posX,topPos.getY()+smallAmount, e.posZ);
			e.fallDistance = 0;
		}
		return true;
	}

	private boolean moveDown(){
		if(this.getEnergy(Power.steam_power) < STEAM_PER_ELEVATOR_MOVE) return false;
		this.subtractEnergy(STEAM_PER_ELEVATOR_MOVE, Power.steam_power);
		playSoundAtTileEntity( SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.AMBIENT, 0.5f, 1f, this);
		playSoundAtTileEntity( SoundEvents.BLOCK_PISTON_CONTRACT, SoundCategory.BLOCKS, 0.5f, 1f, this);
		// scan up the piston and clear it
		BlockPos destination = this.getPos().up();
		BlockPos p = destination;
		while(getWorld().getBlockState(p).getBlock() == Blocks.steam_elevator_platform){
			p = p.up();
		}
		// remove piston
		BlockPos i = p.down();
		for(; i.getY() > getPos().getY(); i = i.down()){
			getWorld().setBlockToAir(i);
		}
		// move people
		List<Entity> passengers = getWorld().getEntitiesWithinAABB(Entity.class, 
				new AxisAlignedBB(p.getX(),p.getY(),p.getZ(), p.getX()+1, p.getY()+2, p.getZ()+1));
		
		for(Entity e : passengers){
			e.setPositionAndUpdate(e.posX, destination.getY()+smallAmount,e.posZ);
			e.fallDistance = 0;
		}
		return true;
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

	

	
	private boolean hasRedstoneSignal() {
		return getWorld().isBlockPowered(getPos());
	}
	
	
	public float getSteamLevel(){
		return this.getEnergy(Power.steam_power) / this.getEnergyCapacity(Power.steam_power);
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
		if(up){
			dataSyncArray[0] = 1;
		} else {
			dataSyncArray[0] = 0;
		}
	}

	@Override
	public void onDataFieldUpdate() {
		this.up = dataSyncArray[0] != 0;
	}

	

	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tagRoot){
		super.writeToNBT(tagRoot);
		tagRoot.setBoolean("up",up);
		return tagRoot;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tagRoot){
		super.readFromNBT(tagRoot);
		if(tagRoot.hasKey("up")){
			up = tagRoot.getBoolean("up");
		}
	}



	public int getComparatorOutput() {
		if(up) return 15;
		return 0;
	}


	@Override
	public boolean isPowerSink(ConduitType conduitType) {
		return true;
	}

	@Override
	public boolean isPowerSource(ConduitType conduitType) {
		return false;
	}
}
