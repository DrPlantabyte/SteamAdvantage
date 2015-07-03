package cyano.steamadvantage.init;

import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import cyano.steamadvantage.SteamAdvantage;
import cyano.steamadvantage.blocks.DrillBitTileEntity;
import cyano.steamadvantage.machines.BlastFurnaceTileEntity;
import cyano.steamadvantage.machines.CoalBoilerTileEntity;
import cyano.steamadvantage.machines.RockCrusherTileEntity;
import cyano.steamadvantage.machines.SteamDrillTileEntity;
import cyano.steamadvantage.machines.SteamElevatorTileEntity;
import cyano.steamadvantage.machines.SteamTankTileEntity;

public class Entities {

	private static boolean initDone = false;
	public static void init(){
		if(initDone) return;
		
		Blocks.init();
		

		GameRegistry.registerTileEntity(CoalBoilerTileEntity.class, SteamAdvantage.MODID+"."+"steam_boiler_coal");
		GameRegistry.registerTileEntity(SteamTankTileEntity.class, SteamAdvantage.MODID+"."+"steam_tank");
		GameRegistry.registerTileEntity(BlastFurnaceTileEntity.class, SteamAdvantage.MODID+"."+"steam_furnace");
		GameRegistry.registerTileEntity(RockCrusherTileEntity.class, SteamAdvantage.MODID+"."+"steam_crusher");
		GameRegistry.registerTileEntity(SteamDrillTileEntity.class, SteamAdvantage.MODID+"."+"steam_drill");
		GameRegistry.registerTileEntity(SteamElevatorTileEntity.class, SteamAdvantage.MODID+"."+"steam_elevator");
		GameRegistry.registerTileEntity(DrillBitTileEntity.class, SteamAdvantage.MODID+"."+"drillbit");
		
		
		initDone = true;
	}
	
	@SideOnly(Side.CLIENT)
	public static void registerRenderers(){
		ClientRegistry.bindTileEntitySpecialRenderer(DrillBitTileEntity.class, new cyano.steamadvantage.graphics.DrillBitRenderer());
	}
}
