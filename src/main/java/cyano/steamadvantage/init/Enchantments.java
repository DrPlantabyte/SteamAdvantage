package cyano.steamadvantage.init;

import cyano.steamadvantage.SteamAdvantage;
import cyano.steamadvantage.enchantments.HighExplosiveEnchantment;
import cyano.steamadvantage.enchantments.PowderlessEnchantment;
import cyano.steamadvantage.enchantments.RapidReloadEnchantment;
import cyano.steamadvantage.enchantments.RecoilEnchantment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLLog;

import java.util.HashSet;
import java.util.Set;

public class Enchantments {

	public static Enchantment rapid_reload;
	public static Enchantment powderless;
	public static Enchantment high_explosive;
	public static Enchantment recoil;
	
	private static boolean initDone = false;
	public static void init(){
		if(initDone) return;

		Enchantment.REGISTRY.register(getNextEnchantmentID(),
				new ResourceLocation(SteamAdvantage.MODID+":"+"high_explosive"),
				new HighExplosiveEnchantment());
		Enchantment.REGISTRY.register(getNextEnchantmentID(),
				new ResourceLocation(SteamAdvantage.MODID+":"+"powderless"),
				new PowderlessEnchantment());
		Enchantment.REGISTRY.register(getNextEnchantmentID(),
				new ResourceLocation(SteamAdvantage.MODID+":"+"rapid_reload"),
				new RapidReloadEnchantment());
		Enchantment.REGISTRY.register(getNextEnchantmentID(),
				new ResourceLocation(SteamAdvantage.MODID+":"+"recoil"),
				new RecoilEnchantment());

		
		initDone = true;
	}

	
	private static final Set<Integer> reservedIDs = new HashSet<>();
	private static int getNextEnchantmentID(){
		for(int i = 0; i < 255; i++){
			if(Enchantment.REGISTRY.getObjectById(i) == null && reservedIDs.contains(i) == false){
				reservedIDs.add(i);
				return i;
			}
		}
		FMLLog.severe("Failed to find free enchantment ID!");
		return 255;
	}
}
