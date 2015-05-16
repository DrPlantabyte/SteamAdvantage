package cyano.steamadvantage.blocks;

import net.minecraft.block.material.Material;
import cyano.poweradvantage.api.ConduitType;
import cyano.steamadvantage.init.Power;

public class SteamPipeBlock extends cyano.poweradvantage.api.simple.BlockSimplePowerConduit{

	public SteamPipeBlock() {
		super(Material.piston, 0.75f, 2f/16f, Power.steam_power);
	}

}
