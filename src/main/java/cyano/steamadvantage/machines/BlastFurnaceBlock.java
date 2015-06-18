package cyano.steamadvantage.machines;

import net.minecraft.block.material.Material;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import cyano.poweradvantage.api.PoweredEntity;
import cyano.steamadvantage.init.Power;


/**
 * @author DrCyano
 *
 */
public class BlastFurnaceBlock extends cyano.poweradvantage.api.simple.BlockSimplePowerConsumer{

	
	public BlastFurnaceBlock() {
		super(Material.piston, 0.75f, Power.steam_power);
	}

	@Override
	public PoweredEntity createNewTileEntity(World world, int metaDataValue) {
		return new BlastFurnaceTileEntity();
	}

	@Override
	public boolean hasComparatorInputOverride() {
		return true;
	}

	@Override
	public int getComparatorInputOverride(World world, BlockPos coord) {
		if(world.getTileEntity(coord) instanceof BlastFurnaceTileEntity){
			return ((BlastFurnaceTileEntity)world.getTileEntity(coord)).getComparatorOutput();
		}
		return 0;
	}

}
