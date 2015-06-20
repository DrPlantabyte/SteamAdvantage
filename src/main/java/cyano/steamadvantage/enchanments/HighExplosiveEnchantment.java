package cyano.steamadvantage.enchanments;

import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import cyano.steamadvantage.SteamAdvantage;
import cyano.steamadvantage.init.Items;

public class HighExplosiveEnchantment extends net.minecraft.enchantment.Enchantment{

	public HighExplosiveEnchantment(int enchID, 
			int enchWeight) {
		super(enchID, new ResourceLocation(SteamAdvantage.MODID+":"+"high_explosive"), enchWeight,EnumEnchantmentType.ALL);
		this.setName("high_explosive");
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
		return lvl * 7;
	}

	@Override
	public int getMaxEnchantability(final int lvl) {
		return this.getMinEnchantability(lvl) + 10;
	}

	@Override
	public int getMaxLevel() {
		return 5;
	}
}
