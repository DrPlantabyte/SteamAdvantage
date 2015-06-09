package cyano.steamadvantage.items;

import java.util.List;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.oredict.OreDictionary;

import com.google.common.collect.Multimap;

import cyano.steamadvantage.SteamAdvantage;
import cyano.steamadvantage.init.Power;

public class MusketItem extends net.minecraft.item.Item{
	
	public static final String NBT_DATA_KEY_LOADED = "loaded";
	public static final double MAX_RANGE = 64;
	
	
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
		if(isNotLoaded(stack)){
			return EnumAction.BLOCK;
		} else {
			return EnumAction.NONE;
		}
	}
	
	@Override
	public ItemStack onItemRightClick(ItemStack srcItemStack, World world, EntityPlayer playerEntity){
		if(isLoaded(srcItemStack)){
			// use normal item
			FMLLog.info("pulled the trigger"); // TODO: remove debug code
			fire(srcItemStack,playerEntity,world);
		} else if(hasAmmo(playerEntity,srcItemStack)){
			// reload time
			playerEntity.setItemInUse(srcItemStack, getReloadTime());
			FMLLog.info("using like something else"); // TODO: remove debug code
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
		FMLLog.info("item use finish"); // TODO: remove debug code
		if(!isLoaded(srcItemStack) && hasAmmo(playerEntity,srcItemStack)){
			FMLLog.info("doing reload"); // TODO: remove debug code
			decrementAmmo(playerEntity);
			playSound("random.click",world,playerEntity);
			startLoad(srcItemStack);
		}
		return srcItemStack;
	}
	
	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityPlayer playerIn, int timeLeft) {
		FMLLog.info("player stopped using"); // TODO: remove debug code
		if(isAlmostLoaded(stack)){
			finishLoad(stack);
		}
	}
	
	protected void fire(ItemStack srcItemStack, EntityPlayer playerEntity,
			World world) {
		unload(srcItemStack);
		if(world.isRemote){
			playerEntity.setAngles(playerEntity.rotationYaw, Math.min(90f,playerEntity.rotationPitch+15));
			return;
		}
		Vec3 start = new Vec3(playerEntity.posX, playerEntity.posY+playerEntity.getEyeHeight(),playerEntity.posZ);
		Vec3 lookVector = playerEntity.getLookVec();
		Vec3 end = start.addVector(MAX_RANGE * lookVector.xCoord, MAX_RANGE * lookVector.yCoord, MAX_RANGE * lookVector.zCoord);
		MovingObjectPosition rayTrace = world.rayTraceBlocks(start, end,true,true,false);
		if(rayTrace.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && rayTrace.entityHit instanceof EntityLivingBase){
			EntityLivingBase e = (EntityLivingBase)rayTrace.entityHit;
			e.attackEntityFrom(Power.musket_damage, getShotDamage());
			FMLLog.info("hit a "+e.getClass()); // TODO: remove debug code
		} else if(rayTrace.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK){
			world.playSoundEffect(rayTrace.hitVec.xCoord, rayTrace.hitVec.yCoord, rayTrace.hitVec.zCoord, 
					"dig.snow", 0.5f, 1f);
			FMLLog.info("hit block at "+rayTrace.getBlockPos()); // TODO: remove debug code
		}
		if(rayTrace.typeOfHit != MovingObjectPosition.MovingObjectType.MISS && rayTrace.hitVec != null){
			world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, 
					rayTrace.hitVec.xCoord, rayTrace.hitVec.yCoord, rayTrace.hitVec.zCoord,
					0, 0, 0);
		}
		playSound("fire.ignite",world,playerEntity);
		FMLLog.info("ka-pow!"); // TODO: remove debug code
		playSound("ambient.weather.thunder",world,playerEntity);
		world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, playerEntity.posX, playerEntity.posY+1.2, playerEntity.posZ,
				0, 0, 0);
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
	
	public static void startLoad(ItemStack musket){
		NBTTagCompound data;
		if(musket.hasTagCompound()){
			data = musket.getTagCompound();
		} else {
			data = new NBTTagCompound();
		}
		data.setByte(NBT_DATA_KEY_LOADED, (byte)2);
		musket.setTagCompound(data);
		FMLLog.info("gun partially loaded"); // TODO: remove debug code
	}
	
	public static void finishLoad(ItemStack musket){
		NBTTagCompound data;
		if(musket.hasTagCompound()){
			data = musket.getTagCompound();
		} else {
			data = new NBTTagCompound();
		}
		data.setByte(NBT_DATA_KEY_LOADED, (byte)1);
		musket.setTagCompound(data);
		FMLLog.info("gun fully loaded"); // TODO: remove debug code
	}
	
	public static void unload(ItemStack musket){
		NBTTagCompound data;
		if(musket.hasTagCompound()){
			data = musket.getTagCompound();
		} else {
			data = new NBTTagCompound();
		}
		data.setByte(NBT_DATA_KEY_LOADED, (byte)0);
		musket.setTagCompound(data);
		FMLLog.info("gun unloaded"); // TODO: remove debug code
	}
	
	public static boolean isLoaded(ItemStack musket){
		NBTTagCompound data = musket.getTagCompound();
		if(data == null) return false;
		return data.hasKey(NBT_DATA_KEY_LOADED) && data.getByte(NBT_DATA_KEY_LOADED) == 1;
	}
	
	public static boolean isNotLoaded(ItemStack musket){
		NBTTagCompound data = musket.getTagCompound();
		if(data == null) return false;
		return data.hasKey(NBT_DATA_KEY_LOADED) && data.getByte(NBT_DATA_KEY_LOADED) == 0;
	}
	
	public static boolean isAlmostLoaded(ItemStack musket){
		NBTTagCompound data = musket.getTagCompound();
		if(data == null) return false;
		return data.hasKey(NBT_DATA_KEY_LOADED) && data.getByte(NBT_DATA_KEY_LOADED) == 2;
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
		FMLLog.info("subtracting ammo"); // TODO: remove debug code
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
