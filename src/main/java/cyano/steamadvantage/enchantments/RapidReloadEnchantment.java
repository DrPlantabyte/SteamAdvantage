package cyano.steamadvantage.enchantments;

import cyano.steamadvantage.init.Items;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

public class RapidReloadEnchantment extends net.minecraft.enchantment.Enchantment{

	public RapidReloadEnchantment() {
		super(Rarity.RARE,EnumEnchantmentType.ALL,new EntityEquipmentSlot[]{EntityEquipmentSlot.MAINHAND});
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
