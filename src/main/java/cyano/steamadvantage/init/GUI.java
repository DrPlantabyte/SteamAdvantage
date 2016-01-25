package cyano.steamadvantage.init;

import cyano.poweradvantage.PowerAdvantage;
import cyano.poweradvantage.registry.MachineGUIRegistry;
import cyano.steamadvantage.gui.*;

public class GUI {

	private static boolean initDone = false;
	public static void init(){
		if(initDone) return;
		
		Blocks.init();
		Entities.init();

		Blocks.steam_boiler_coal.setGuiID(MachineGUIRegistry.addGUI(new CoalBoilerGUI()),PowerAdvantage.getInstance());
		Blocks.steam_tank.setGuiID(MachineGUIRegistry.addGUI(new SteamTankGUI()),PowerAdvantage.getInstance());
		Blocks.steam_furnace.setGuiID(MachineGUIRegistry.addGUI(new BlastFurnaceGUI()),PowerAdvantage.getInstance());
		Blocks.steam_crusher.setGuiID(MachineGUIRegistry.addGUI(new RockCrusherGUI()),PowerAdvantage.getInstance());
		Blocks.steam_drill.setGuiID(MachineGUIRegistry.addGUI(new SteamDrillGUI()),PowerAdvantage.getInstance());
		Blocks.steam_boiler_electric.setGuiID(MachineGUIRegistry.addGUI(new ElectricBoilerGUI()),PowerAdvantage.getInstance());
		Blocks.steam_boiler_geothermal.setGuiID(MachineGUIRegistry.addGUI(new GeothermalBoilerGUI()),PowerAdvantage.getInstance());
		Blocks.steam_boiler_oil.setGuiID(MachineGUIRegistry.addGUI(new OilBoilerGUI()),PowerAdvantage.getInstance());
		Blocks.steam_still.setGuiID(MachineGUIRegistry.addGUI(new SteamStillGUI()),PowerAdvantage.getInstance());
		Blocks.steam_pump.setGuiID(MachineGUIRegistry.addGUI(new SteamPumpGUI()),PowerAdvantage.getInstance());
		
		
		initDone = true;
	}
}
