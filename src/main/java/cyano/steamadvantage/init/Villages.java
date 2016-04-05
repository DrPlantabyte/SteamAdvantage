package cyano.steamadvantage.init;

import cyano.basemetals.util.VillagerTradeHelper;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.FMLLog;
import org.apache.logging.log4j.Level;

public abstract class Villages {
	// TODO: add machinist villager

	private static boolean initDone = false;
	public static void init(){
		if(initDone) return;
		
		Entities.init();

		try {
			VillagerTradeHelper.insertTrades(3, 3, 1, new EntityVillager.ListItemForEmeralds(Items.steam_governor, new EntityVillager.PriceInfo(-4,-1)));
		VillagerTradeHelper.insertTrades(3, 2, 1, new EntityVillager.ListItemForEmeralds(Items.steam_governor, new EntityVillager.PriceInfo(-4,-1)));
			VillagerTradeHelper.insertTrades(3, 1, 1, new EntityVillager.ListItemForEmeralds(Items.steam_governor, new EntityVillager.PriceInfo(-4,-1)));
			VillagerTradeHelper.insertTrades(1, 1, 1, new EntityVillager.ListItemForEmeralds(Items.steam_governor, new EntityVillager.PriceInfo(-4,-1)));
			VillagerTradeHelper.insertTrades(1, 1, 1, new EntityVillager.ListItemForEmeralds(Item.getItemFromBlock(Blocks.steam_pipe), new EntityVillager.PriceInfo(-8,-4)));
			VillagerTradeHelper.insertTrades(3, 2, 1, new EntityVillager.ListItemForEmeralds(Items.blackpowder_cartridge, new EntityVillager.PriceInfo(-7,-5)));
			VillagerTradeHelper.insertTrades(3, 2, 1, new EntityVillager.ListItemForEmeralds(Items.blackpowder_musket, new EntityVillager.PriceInfo(10,15)));
			VillagerTradeHelper.insertTrades(3, 3, 1, new EntityVillager.ListItemForEmeralds(Items.blackpowder_cartridge, new EntityVillager.PriceInfo(-7,-5)));
			VillagerTradeHelper.insertTrades(3, 3, 1, new EntityVillager.ListItemForEmeralds(Items.blackpowder_musket, new EntityVillager.PriceInfo(10,15)));
			VillagerTradeHelper.insertTrades(2, 1, 3, 
					new EntityVillager.ListEnchantedItemForEmeralds(Items.blackpowder_musket, new EntityVillager.PriceInfo(15, 25)));
		} catch (NoSuchFieldException | IllegalAccessException e) {
			FMLLog.log(Level.ERROR, e, "Failed to add trades to villagers");
		}
		
		initDone = true;
	}
}
