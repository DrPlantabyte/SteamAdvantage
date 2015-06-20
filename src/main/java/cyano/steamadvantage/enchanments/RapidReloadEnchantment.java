package cyano.steamadvantage.enchanments;

import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import cyano.steamadvantage.SteamAdvantage;
import cyano.steamadvantage.init.Items;

public class RapidReloadEnchantment extends net.minecraft.enchantment.Enchantment{

	public RapidReloadEnchantment(int enchID, 
			int enchWeight) {
		super(enchID, new ResourceLocation(SteamAdvantage.MODID+":"+"rapid_reload"), enchWeight,EnumEnchantmentType.ALL);
		this.setName("rapid_reload");
	}

	
	@Override
	public boolean canApply(ItemStack item){
		return item.getItem() == Items.blackpowder_musket;
	}
	@Override
	public boolean canApplyAtEnchantingTable(ItemStack item){
		return canApply(item);
	}
	


	@Override
	public int getMinEnchantability(final int lvl) {
		return 15;
	}

	@Override
	public int getMaxEnchantability(final int lvl) {
		return 50;
	}

	@Override
	public int getMaxLevel() {
		return 1;
	}
}
