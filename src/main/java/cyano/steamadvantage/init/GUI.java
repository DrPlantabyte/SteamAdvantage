package cyano.steamadvantage.init;

import cyano.poweradvantage.PowerAdvantage;
import cyano.poweradvantage.registry.MachineGUIRegistry;
import cyano.steamadvantage.gui.CoalBoilerGUI;

public class GUI {

	private static boolean initDone = false;
	public static void init(){
		if(initDone) return;
		
		Blocks.init();
		Entities.init();
		
		Blocks.steam_boiler_coal.setGuiID(MachineGUIRegistry.addGUI(new CoalBoilerGUI()),PowerAdvantage.getInstance());
		
		
		initDone = true;
	}
}
