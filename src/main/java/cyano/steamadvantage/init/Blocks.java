package cyano.steamadvantage.init;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import cyano.poweradvantage.api.GUIBlock;
import cyano.steamadvantage.SteamAdvantage;
import cyano.steamadvantage.machines.CoalBoilerBlock;

public abstract class Blocks {
	private static final Map<String,Block> allBlocks = new HashMap<>();


	public static GUIBlock steam_boiler_coal;
	public static GUIBlock steam_crusher;
	public static GUIBlock steam_furnace;
	public static GUIBlock steam_tank;
	
	private static boolean initDone = false;
	public static void init(){
		if(initDone) return;
		
		steam_boiler_coal = (GUIBlock)addBlock(new CoalBoilerBlock(),"steam_boiler_coal");
		
		initDone = true;
	}
	

	private static Block addBlock(Block block, String name ){
		block.setUnlocalizedName(SteamAdvantage.MODID+"."+name);
		GameRegistry.registerBlock(block, name);
		block.setCreativeTab(cyano.poweradvantage.init.ItemGroups.tab_powerAdvantage);
		allBlocks.put(name, block);
		return block;
	}
	
	@SideOnly(Side.CLIENT)
	public static void registerItemRenders(FMLInitializationEvent event){
		for(Map.Entry<String, Block> e : allBlocks.entrySet()){
			String name = e.getKey();
			Block block = e.getValue();
			Minecraft.getMinecraft().getRenderItem().getItemModelMesher()
			.register(net.minecraft.item.Item.getItemFromBlock(block), 0, 
				new ModelResourceLocation(SteamAdvantage.MODID+":"+name, "inventory"));
		}
	}

	
}
