package cyano.steamadvantage.init;

import net.minecraft.enchantment.Enchantment;
import net.minecraftforge.fml.common.FMLLog;
import cyano.steamadvantage.enchanments.*;

public class Enchantments {

	public static Enchantment rapid_reload;
	public static Enchantment powderless;
	public static Enchantment high_explosive;
	public static Enchantment recoil;
	
	private static boolean initDone = false;
	public static void init(){
		if(initDone) return;
		
		rapid_reload = addEnchantment(new RapidReloadEnchantment(getNextEnchantmentID(), 2));
		powderless = addEnchantment(new PowderlessEnchantment(getNextEnchantmentID(), 2));
		high_explosive = addEnchantment(new HighExplosiveEnchantment(getNextEnchantmentID(), 4));
		recoil = addEnchantment(new RecoilEnchantment(getNextEnchantmentID(), 4));
		
		initDone = true;
	}
	
	private static Enchantment addEnchantment(Enchantment e){
		Enchantment.addToBookList(e);
		return e;
	}
	
	private static int getNextEnchantmentID(){
		return Enchantment.enchantmentsBookList.length;
	}
}
