package cyano.steamadvantage.init;

import java.util.HashSet;
import java.util.Set;

import cyano.steamadvantage.enchanments.HighExplosiveEnchantment;
import cyano.steamadvantage.enchanments.PowderlessEnchantment;
import cyano.steamadvantage.enchanments.RapidReloadEnchantment;
import cyano.steamadvantage.enchanments.RecoilEnchantment;
import net.minecraft.enchantment.Enchantment;
import net.minecraftforge.fml.common.FMLLog;

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
	
	private static final Set<Integer> reservedIDs = new HashSet<>();
	private static int getNextEnchantmentID(){
		for(int i = 0; i < 255; i++){
			if(Enchantment.getEnchantmentById(i) == null && reservedIDs.contains(i) == false){
				reservedIDs.add(i);
				return i;
			}
		}
		FMLLog.severe("Failed to find free enchantment ID!");
		return 255;
	}
}
