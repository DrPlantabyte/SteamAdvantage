package cyano.steamadvantage.items;

import java.util.List;

import com.google.common.collect.Multimap;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import cyano.steamadvantage.SteamAdvantage;

public class MusketItem extends net.minecraft.item.Item{
	
	public static final String NBT_DATA_KEY_LOADED = "loaded";
	
	public MusketItem(){
		super();
		this.setMaxStackSize(1);
		this.setMaxDamage(250);
	}
	

	public static float getShotDamage(){
		return SteamAdvantage.MUSKET_DAMAGE;
	}
	public static float getMeleeDamage(){
		return 4;
	}
	
	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker)
	{
		stack.damageItem(1, attacker);
		return true;
	}
	
	/** plays a sound at the player location */
	protected void playSound(String soundID, World world, EntityPlayer playerEntity){
		if (!world.isRemote)
		{
			world.playSoundAtEntity(playerEntity, soundID, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
		}
	}
	
	public static void load(ItemStack musket){
		NBTTagCompound data = new NBTTagCompound();
		data.setBoolean(NBT_DATA_KEY_LOADED, true);
	}
	
	public static boolean isLoaded(ItemStack musket){
		NBTTagCompound data = musket.getTagCompound();
		if(data == null) return false;
		return data.hasKey(NBT_DATA_KEY_LOADED) && data.getBoolean(NBT_DATA_KEY_LOADED);
	}
	
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean b){
		super.addInformation(stack,player,list,b);
		list.add(StatCollector.translateToLocal("tooltip.musket.damage").replace("%x", String.valueOf((int)this.getShotDamage())));
		if(isLoaded(stack)){
			list.add(StatCollector.translateToLocal("tooltip.musket.loaded"));
		} else {
			list.add(StatCollector.translateToLocal("tooltip.musket.unloaded"));
		}
	}

	/** Sets melee attack damage */
	public Multimap getItemAttributeModifiers()
	{
		Multimap multimap = super.getItemAttributeModifiers();
		multimap.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(), new AttributeModifier(itemModifierUUID, "Weapon modifier", (double)getMeleeDamage(), 0));
		return multimap;
	}
}
