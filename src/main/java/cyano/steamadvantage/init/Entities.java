package cyano.steamadvantage.init;

import net.minecraftforge.fml.common.registry.GameRegistry;
import cyano.steamadvantage.SteamAdvantage;
import cyano.steamadvantage.machines.*;

public class Entities {

	private static boolean initDone = false;
	public static void init(){
		if(initDone) return;
		
		Blocks.init();
		

		GameRegistry.registerTileEntity(CoalBoilerTileEntity.class, SteamAdvantage.MODID+"."+"steam_boiler_coal");
		
		initDone = true;
	}
}
