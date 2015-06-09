package cyano.steamadvantage.items;

import java.util.List;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

import com.google.common.collect.Multimap;

import cyano.steamadvantage.SteamAdvantage;

public class MusketItem extends net.minecraft.item.Item{
	
	public static final String NBT_DATA_KEY_LOADED = "loaded";

	private static final int maxBowUseTime = 72000;
	private static final int minAimTime = 8;
	
	public MusketItem(){
		super();
		this.setMaxStackSize(1);
		this.setMaxDamage(250);
	}
	

	public static int getReloadTime(){
		return SteamAdvantage.MUSKET_RELOAD;
	}
	public static float getShotDamage(){
		return SteamAdvantage.MUSKET_DAMAGE;
	}
	public static float getMeleeDamage(){
		return 4;
	}
	
	@Override
	public EnumAction getItemUseAction(ItemStack stack){
		if(isLoaded(stack)){
			return EnumAction.BOW;
		} else {
			return EnumAction.EAT;
		}
	}
	
	@Override
	public ItemStack onItemRightClick(ItemStack srcItemStack, World world, EntityPlayer playerEntity){
		if(isLoaded(srcItemStack)){
			// use like a bow
			playerEntity.setItemInUse(srcItemStack, maxBowUseTime);
		} else if(hasAmmo(playerEntity,srcItemStack)){
			// reload time
			playerEntity.setItemInUse(srcItemStack, getReloadTime());
		}
		return srcItemStack;
	}
	
	/**
	 * This method is invoked after the item has been used for an amount of time equal to the duration 
	 * provided to the EntityPlayer.setItemInUse(stack, duration).
	 */
	@Override
	public ItemStack onItemUseFinish (ItemStack srcItemStack, World world, EntityPlayer playerEntity)
	{ // 
		if(!isLoaded(srcItemStack) && hasAmmo(playerEntity,srcItemStack)){
			decrementAmmo(playerEntity);
			playSound("random.click",world,playerEntity);
			playerEntity.stopUsingItem();
			load(srcItemStack);
		} else if(isLoaded(srcItemStack)){
			fire(srcItemStack,playerEntity,world);
		}
		
		return srcItemStack;
	}
	
	private void fire(ItemStack srcItemStack, EntityPlayer playerEntity,
			World world) {
		NBTTagCompound data = srcItemStack.getTagCompound();
		if(data != null){
			data.setBoolean(NBT_DATA_KEY_LOADED, false);
		}
		// TODO shoot bullet
		if(world.isRemote)playerEntity.setAngles(playerEntity.rotationYaw, Math.min(90f,playerEntity.rotationPitch+15));
		playSound("fire.ignite",world,playerEntity);
		playSound("ambient.weather.thunder",world,playerEntity);
	}


	/**
	 * Called when the player stops using an Item (stops holding the right mouse button).
	 *  
	 * @param timeLeft The amount of ticks left before the using would have been complete
	 */
	public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityPlayer playerIn, int timeLeft)
	{
		int timeUsed = this.getMaxItemUseDuration(stack) - timeLeft;
		if(isLoaded(stack)){
			if(timeUsed > minAimTime){
				fire(stack,playerIn,worldIn);
			}
		}
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


	
	public static boolean hasAmmo(EntityPlayer playerEntity, ItemStack musket) {
		if(playerEntity.capabilities.isCreativeMode 
				|| EnchantmentHelper.getEnchantmentLevel(Enchantment.infinity.effectId, musket) > 0) return true;;
		List<ItemStack> ammoItems = OreDictionary.getOres("ammoBlackpowder");
		for(int i = 0; i < playerEntity.inventory.mainInventory.length; i++){
			if(playerEntity.inventory.mainInventory[i] == null) continue;
			for(int n = 0; n < ammoItems.size(); n++){
				if(OreDictionary.itemMatches(ammoItems.get(n), playerEntity.inventory.mainInventory[i], false)){
					return true;
				}
			}
		}
		return false;
	}
	

	public static void decrementAmmo(EntityPlayer playerEntity) {
		if (playerEntity.capabilities.isCreativeMode) return;
		List<ItemStack> ammoItems = OreDictionary.getOres("ammoBlackpowder");
		for(int i = 0; i < playerEntity.inventory.mainInventory.length; i++){
			if(playerEntity.inventory.mainInventory[i] == null) continue;
			for(int n = 0; n < ammoItems.size(); n++){
				if(OreDictionary.itemMatches(ammoItems.get(n), playerEntity.inventory.mainInventory[i], false)){
					playerEntity.inventory.mainInventory[i].stackSize--;
					if(playerEntity.inventory.mainInventory[i].stackSize <= 0){
						playerEntity.inventory.mainInventory[i] = null;
					}
					return;
				}
			}
		}
	}
	
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean b){
		super.addInformation(stack,player,list,b);
		list.add(StatCollector.translateToLocal("tooltip.musket.damage").replace("%x", String.valueOf((int)this.getShotDamage())));
		if(isLoaded(stack)){
			list.add(StatCollector.translateToLocal("tooltip.musket.loaded"));
		} else {
			list.add(StatCollector.translateToLocal("tooltip.musket.unloaded"));
			list.add(StatCollector.translateToLocal("tooltip.musket.ammo").replace("%x",StatCollector.translateToLocal("item.steamadvantage.blackpowder_cartridge.name")));
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
