package cyano.steamadvantage;

import cyano.steamadvantage.init.*;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;

/* TODO list
 * + Coal-Fired Steam Boiler
 * + Boiler Tank
 * + Steam Conduit
 * + Steam Powered Rock Crusher
 * + Steam Powered Blast Furnace (expanded furnace 2x2)
 * --- push version 1.0 ---
 * + Steam Powered Drill
 * --- push version 1.1 ---
 * - pressure cooker (steam-only furnace that only cooks food)
 * + Musket (slow-loading ranged weapon)
 * + Bullets (smelt lead nuggets into grape-shot, loading a gun consumes 1 grape-shot and 1 gunpowder)
 * - Steam Powered Machine Shop (automatable crafter)
 * - Steam Powered Defense Cannon (manual aiming and requires redstone trigger)
 * --- push version 1.2 ---
 * - Steam Powered Lift (pushes up special lift blocks, like an extendable piston)
 * --- push version 1.3 ---
 * - Oil-Burning Steam Boiler
 * - Bioreactor (slowly makes liquid fuel from organic matter)
 */
@Mod(modid = SteamAdvantage.MODID, version = SteamAdvantage.VERSION, name=SteamAdvantage.NAME, 
		dependencies = "required-after:poweradvantage;required-after:basemetals")
public class SteamAdvantage
{/** The identifier for this mod */
	public static final String MODID = "steamadvantage";
	/** The display name for this mod */
	public static final String NAME = "Steam Advantage";
	/** The version of this mod, in the format major.minor.update */
	public static final String VERSION = "2.0.3";

	public static float MUSKET_DAMAGE = 20;
	public static int MUSKET_RELOAD = 20*5;
	public static boolean MUSKET_ENABLE = true;
	public static Map<String,Float> fluidBurnValues = new HashMap<>();

	/**
	 * Pre-initialization step. Used for initializing objects and reading the 
	 * config file
	 * @param event FML event object
	 */
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();

		MUSKET_DAMAGE = config.getFloat("musket_damage", "options", MUSKET_DAMAGE, 0, 100, 
				"This is the amount of damage dealt by a shot from a blackpowder musket");
		MUSKET_RELOAD = config.getInt("musket_reload_time", "options", MUSKET_RELOAD, 20, 15*20, 
				"This is the amount of time (in game update ticks) that it takes to reload a musket.\n"+
				"Note that 20 ticks is 1 second of real time");
		MUSKET_ENABLE = config.getBoolean("musket_allowed", "options", MUSKET_ENABLE, 
				"If true, then the musket gun will be craftable.");

		String[] strs = config.getString("fluid_fuel_values","options","oil=5000;fuel=25000",
				"A semi-colon delimited list of fuel fluids (by name) and the burn time (in game ticks) \n"
			+   "for a bucket (1000 units) of the given fluid. Specified in the following format: \n"
			+   "fluid name=burn time").split(";");
		for(String e : strs){
			if(e.contains("=")){
				String[] comps = e.split("=");
				String fluidName = comps[0].trim();
				Float value = 0f;
				try{
					value = Float.parseFloat(comps[1].trim());
				}catch(NumberFormatException ex){
					FMLLog.severe("Fluid fuel descriptor '%s' is not valid!",e);
				}
				FMLLog.info("%s: adding fluid fuel override for fluid '%s' with a burn time of %s game ticks per bucket",MODID,fluidName,value);
			} else {
				FMLLog.severe("Fluid fuel descriptor '%s' is not valid!",e);
			}
		}

		config.save();

		Blocks.init();
		Items.init();
		TreasureChests.init(config.getConfigFile().toPath().getParent());


		if(event.getSide() == Side.CLIENT){
			clientPreInit(event);
		}
		if(event.getSide() == Side.SERVER){
			serverPreInit(event);
		}
	}

	@SideOnly(Side.CLIENT)
	private void clientPreInit(FMLPreInitializationEvent event){
		// client-only code
	}
	@SideOnly(Side.SERVER)
	private void serverPreInit(FMLPreInitializationEvent event){
		// client-only code
	}
	/**
	 * Initialization step. Used for adding renderers and most content to the 
	 * game
	 * @param event FML event object
	 */
	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		Recipes.init();
		Entities.init();
		GUI.init();
		Enchantments.init();
		Villages.init();

		if(event.getSide() == Side.CLIENT){
			clientInit(event);
		}
		if(event.getSide() == Side.SERVER){
			serverInit(event);
		}
	}
	

	@SideOnly(Side.CLIENT)
	private void clientInit(FMLInitializationEvent event){
		// client-only code
		Items.registerItemRenders(event);
		Blocks.registerItemRenders(event);
		Entities.registerRenderers();
		
	}
	@SideOnly(Side.SERVER)
	private void serverInit(FMLInitializationEvent event){
		// client-only code
	}

	/**
	 * Post-initialization step. Used for cross-mod options
	 * @param event FML event object
	 */
	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{

	}


	@SideOnly(Side.CLIENT)
	private void clientPostInit(FMLPostInitializationEvent event){
		// client-only code
	}
	@SideOnly(Side.SERVER)
	private void serverPostInit(FMLPostInitializationEvent event){
		// client-only code
	}

}
