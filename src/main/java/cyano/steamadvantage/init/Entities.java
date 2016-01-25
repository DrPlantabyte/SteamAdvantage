package cyano.steamadvantage.init;

import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import cyano.steamadvantage.SteamAdvantage;
import cyano.steamadvantage.blocks.DrillBitTileEntity;
import cyano.steamadvantage.machines.*;

public class Entities {

	private static boolean initDone = false;
	public static void init(){
		if(initDone) return;
		
		Blocks.init();
		

		GameRegistry.registerTileEntity(CoalBoilerTileEntity.class, SteamAdvantage.MODID+"."+"steam_boiler_coal");
		GameRegistry.registerTileEntity(ElectricBoilerTileEntity.class, SteamAdvantage.MODID+"."+"steam_boiler_electric");
		GameRegistry.registerTileEntity(GeothermalBoilerTileEntity.class, SteamAdvantage.MODID+"."+"steam_boiler_geothermal");
		GameRegistry.registerTileEntity(SteamTankTileEntity.class, SteamAdvantage.MODID+"."+"steam_tank");
		GameRegistry.registerTileEntity(BlastFurnaceTileEntity.class, SteamAdvantage.MODID+"."+"steam_furnace");
		GameRegistry.registerTileEntity(RockCrusherTileEntity.class, SteamAdvantage.MODID+"."+"steam_crusher");
		GameRegistry.registerTileEntity(SteamDrillTileEntity.class, SteamAdvantage.MODID+"."+"steam_drill");
		GameRegistry.registerTileEntity(SteamElevatorTileEntity.class, SteamAdvantage.MODID+"."+"steam_elevator");
		GameRegistry.registerTileEntity(DrillBitTileEntity.class, SteamAdvantage.MODID+"."+"drillbit");
		GameRegistry.registerTileEntity(SteamStillTileEntity.class, SteamAdvantage.MODID+"."+"steam_still");
		GameRegistry.registerTileEntity(SteamPumpTileEntity.class, SteamAdvantage.MODID+"."+"steam_pump");
		GameRegistry.registerTileEntity(OilBoilerTileEntity.class, SteamAdvantage.MODID+"."+"steam_boiler_oil");
		
		
		initDone = true;
	}
	
	@SideOnly(Side.CLIENT)
	public static void registerRenderers(){
		ClientRegistry.bindTileEntitySpecialRenderer(DrillBitTileEntity.class, new cyano.steamadvantage.graphics.DrillBitRenderer());
	}
}
