package cyano.steamadvantage.init;

import cyano.poweradvantage.api.ConduitType;

public abstract class Power {

	public static final ConduitType steam_power = new ConduitType("steam");

	public static float ENERGY_LOST_PER_TICK = 0.0625f;
	
}
