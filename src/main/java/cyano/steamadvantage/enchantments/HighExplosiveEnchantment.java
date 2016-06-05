package cyano.steamadvantage.enchantments;

import cyano.steamadvantage.init.Items;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

public class HighExplosiveEnchantment extends net.minecraft.enchantment.Enchantment{

	public HighExplosiveEnchantment() {
		super(Rarity.UNCOMMON,EnumEnchantmentType.ALL,new EntityEquipmentSlot[]{EntityEquipmentSlot.MAINHAND});
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
