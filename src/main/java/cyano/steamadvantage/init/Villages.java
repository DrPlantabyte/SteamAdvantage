package cyano.steamadvantage.init;

import cyano.basemetals.BaseMetals;
import cyano.basemetals.entities.EntityBetterVillager;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.item.Item;

public abstract class Villages {
	// TODO: add machinist villager

	private static boolean initDone = false;
	public static void init(){
		if(initDone) return;
		
		Entities.init();
		
		if(BaseMetals.enableBetterVillagers){
			EntityBetterVillager.addVillagerTrades(3, 3, 1, new EntityVillager.ListItemForEmeralds(Items.steam_governor, new EntityVillager.PriceInfo(-4,-1)));
			EntityBetterVillager.addVillagerTrades(3, 2, 1, new EntityVillager.ListItemForEmeralds(Items.steam_governor, new EntityVillager.PriceInfo(-4,-1)));
			EntityBetterVillager.addVillagerTrades(3, 1, 1, new EntityVillager.ListItemForEmeralds(Items.steam_governor, new EntityVillager.PriceInfo(-4,-1)));
			EntityBetterVillager.addVillagerTrades(1, 1, 1, new EntityVillager.ListItemForEmeralds(Items.steam_governor, new EntityVillager.PriceInfo(-4,-1)));
			EntityBetterVillager.addVillagerTrades(1, 1, 1, new EntityVillager.ListItemForEmeralds(Item.getItemFromBlock(Blocks.steam_pipe), new EntityVillager.PriceInfo(-8,-4)));
			EntityBetterVillager.addVillagerTrades(3, 2, 1, new EntityVillager.ListItemForEmeralds(Items.blackpowder_cartridge, new EntityVillager.PriceInfo(-7,-5)));
			EntityBetterVillager.addVillagerTrades(3, 2, 1, new EntityVillager.ListItemForEmeralds(Items.blackpowder_musket, new EntityVillager.PriceInfo(10,15)));
			EntityBetterVillager.addVillagerTrades(3, 3, 1, new EntityVillager.ListItemForEmeralds(Items.blackpowder_cartridge, new EntityVillager.PriceInfo(-7,-5)));
			EntityBetterVillager.addVillagerTrades(3, 3, 1, new EntityVillager.ListItemForEmeralds(Items.blackpowder_musket, new EntityVillager.PriceInfo(10,15)));
			EntityBetterVillager.addVillagerTrades(2, 1, 3, 
					new EntityVillager.ListEnchantedItemForEmeralds(Items.blackpowder_musket, new EntityVillager.PriceInfo(15, 25)));
		}
		
		initDone = true;
	}
}
