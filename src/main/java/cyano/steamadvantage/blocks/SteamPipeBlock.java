package cyano.steamadvantage.blocks;

import cyano.steamadvantage.init.Power;
import net.minecraft.block.material.Material;

public class SteamPipeBlock extends cyano.poweradvantage.api.simple.BlockSimplePowerConduit{

	public SteamPipeBlock() {
		super(Material.piston, 0.75f, 2f/16f, Power.steam_power);
	}
	
}
