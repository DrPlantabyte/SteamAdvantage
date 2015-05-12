package cyano.steamadvantage.machines;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import cyano.poweradvantage.api.PoweredEntity;
import cyano.steamadvantage.init.Power;

public class SteamTankBlock extends cyano.poweradvantage.api.simple.BlockSimplePowerSource {

	public SteamTankBlock(){
		super(Material.piston, 0.75f, Power.steam_power);
	}

	@Override
	public PoweredEntity createNewTileEntity(World world, int metaDataValue) {
		return new SteamTankTileEntity();
	}

	@Override
	public boolean hasComparatorInputOverride() {
		return true;
	}

	@Override
	public int getComparatorInputOverride(World world, BlockPos coord) {
		TileEntity te = world.getTileEntity(coord);
		if(te instanceof SteamTankTileEntity){
			return (int)(15 * ((SteamTankTileEntity)te).getSteamLevel());
		} else{
			return 0;
		}
	}
}
