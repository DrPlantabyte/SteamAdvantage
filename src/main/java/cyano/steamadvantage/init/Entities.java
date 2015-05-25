package cyano.steamadvantage.init;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import cyano.steamadvantage.SteamAdvantage;
import cyano.steamadvantage.blocks.DrillBitTileEntity;
import cyano.steamadvantage.graphics.DrillBitRenderer;
import cyano.steamadvantage.machines.BlastFurnaceTileEntity;
import cyano.steamadvantage.machines.CoalBoilerTileEntity;
import cyano.steamadvantage.machines.RockCrusherTileEntity;
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
		GameRegistry.registerTileEntity(DrillBitTileEntity.class, SteamAdvantage.MODID+"."+"drillbit");
		
		
		initDone = true;
	}
	
	public static void registerRenderers(){
		RenderManager rm = Minecraft.getMinecraft().getRenderManager();
		ClientRegistry.bindTileEntitySpecialRenderer(DrillBitTileEntity.class, new DrillBitRenderer());
	}
}
