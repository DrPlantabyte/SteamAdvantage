package cyano.steamadvantage.enchanments;

import cyano.steamadvantage.init.Items;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

public class PowderlessEnchantment extends net.minecraft.enchantment.Enchantment{

	public PowderlessEnchantment() {
		super(Rarity.VERY_RARE,EnumEnchantmentType.ALL,new EntityEquipmentSlot[]{EntityEquipmentSlot.MAINHAND});
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
