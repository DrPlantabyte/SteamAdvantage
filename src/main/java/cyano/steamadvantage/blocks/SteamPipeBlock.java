package cyano.steamadvantage.blocks;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cyano.poweradvantage.api.ConduitType;
import cyano.steamadvantage.init.Power;

public class SteamPipeBlock extends cyano.poweradvantage.api.simple.BlockSimplePowerConduit{

	public SteamPipeBlock() {
		super(Material.piston, 0.75f, 2f/16f, Power.steam_power);
	}
	
}
