package cyano.steamadvantage.enchanments;

import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import cyano.steamadvantage.SteamAdvantage;
import cyano.steamadvantage.init.Items;

public class PowderlessEnchantment extends net.minecraft.enchantment.Enchantment{

	public PowderlessEnchantment(int enchID, 
			int enchWeight) {
		super(enchID, new ResourceLocation(SteamAdvantage.MODID+":"+"powderless"), enchWeight,EnumEnchantmentType.ALL);
		this.setName("powderless");
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
		return 20;
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
