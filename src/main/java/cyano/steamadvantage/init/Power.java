package cyano.steamadvantage.init;

import net.minecraft.util.DamageSource;
import cyano.poweradvantage.api.ConduitType;

public abstract class Power {

	public static final ConduitType steam_power = new ConduitType("steam");

	public static float ENERGY_LOST_PER_TICK = 0.0625f;
	

	public static final DamageSource machine_damage = new DamageSource("steam_machine");
	public static final DamageSource musket_damage = new DamageSource("musket");
}
