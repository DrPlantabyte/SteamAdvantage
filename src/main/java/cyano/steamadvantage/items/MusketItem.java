package cyano.steamadvantage.items;

import java.util.List;

import net.minecraft.block.BlockTNT;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
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
	
	// TODO: enchantments
	
	public static final String NBT_DATA_KEY_LOADED = "loaded";
	public static final double MAX_RANGE = 64;
	private static final int maxUseTime = 7200;
	
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
			return EnumAction.BLOCK;
		}
	}
	
	@Override
	public ItemStack onItemRightClick(ItemStack srcItemStack, World world, EntityPlayer playerEntity){
		playerEntity.setItemInUse(srcItemStack, this.getMaxItemUseDuration(srcItemStack));
		return srcItemStack;
	}
	
	@Override
	public void onUsingTick(ItemStack stack, EntityPlayer player, int count){
		if(player.worldObj.isRemote && count % 7 == 3 && isNotLoaded(stack)){
			// indicator to player that the gun is loading
			player.playSound("dig.stone", 0.5f, 1.0f);
		}
	}
	/**
	 * This method is invoked after the item has been used for an amount of time equal to the duration 
	 * provided to the EntityPlayer.setItemInUse(stack, duration).
	 */
	@Override
	public ItemStack onItemUseFinish (ItemStack srcItemStack, World world, EntityPlayer playerEntity)
	{ // 
		if(isNotLoaded(srcItemStack) && hasAmmo(playerEntity,srcItemStack)){
			decrementAmmo(playerEntity);
			startLoad(srcItemStack);
			playSound("random.click",world,playerEntity);
		}
		return srcItemStack;
	}
	
	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityPlayer playerIn, int timeLeft) {
		if(isAlmostLoaded(stack)){
			finishLoad(stack);
		} else if(isLoaded(stack)){
			fire(stack,playerIn,worldIn);
		}
	}
	
	protected void fire(ItemStack srcItemStack, EntityPlayer playerEntity,
			World world) {
		unload(srcItemStack);
		if(!playerEntity.capabilities.isCreativeMode){
			srcItemStack.damageItem(1, playerEntity);
		}
		Vec3 lookVector = playerEntity.getLookVec();
		spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, world,
				playerEntity.posX+lookVector.xCoord, playerEntity.posY+playerEntity.getEyeHeight()+lookVector.yCoord, playerEntity.posZ + lookVector.zCoord);
		if(world.isRemote){
			playerEntity.rotationPitch -= 15;
			return;
		}
		playSound("fire.ignite",world,playerEntity);
		world.playSoundEffect(playerEntity.posX,playerEntity.posY,playerEntity.posZ,"fireworks.blast",2F,0.5F);
		
		Vec3 start = new Vec3(playerEntity.posX, playerEntity.posY+playerEntity.getEyeHeight(),playerEntity.posZ);
		Vec3 end = start.addVector(MAX_RANGE * lookVector.xCoord, MAX_RANGE * lookVector.yCoord, MAX_RANGE * lookVector.zCoord);
		MovingObjectPosition rayTrace = rayTraceBlocksAndEntities(world,MAX_RANGE,playerEntity);
		if(rayTrace == null){
			// no collisions
			return;
		}
		if(rayTrace.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && rayTrace.entityHit != null){
			Entity e = rayTrace.entityHit;
			e.attackEntityFrom(Power.musket_damage, getShotDamage());
		} else if(rayTrace.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK){
			world.playSoundEffect(rayTrace.hitVec.xCoord, rayTrace.hitVec.yCoord, rayTrace.hitVec.zCoord, 
					"dig.snow", 1f, 1f);
			BlockPos coord = rayTrace.getBlockPos();
			if(coord.getY()>= 0 && coord.getY() <= 255 && !world.isAirBlock(coord)){
				Material blockMat = world.getBlockState(coord).getBlock().getMaterial();
				if(blockMat == net.minecraft.block.material.Material.glass
						|| blockMat == net.minecraft.block.material.Material.leaves
						|| blockMat == net.minecraft.block.material.Material.circuits
						|| blockMat == net.minecraft.block.material.Material.plants
						|| blockMat == net.minecraft.block.material.Material.vine
						|| blockMat == net.minecraft.block.material.Material.web){
					world.destroyBlock(coord, true);
				} else if (blockMat == net.minecraft.block.material.Material.tnt){
					if(world.getBlockState(coord).getBlock() instanceof BlockTNT){
						((BlockTNT)world.getBlockState(coord).getBlock()).explode(world, coord, world.getBlockState(coord).withProperty(BlockTNT.EXPLODE, (Boolean)true), playerEntity);
						world.setBlockToAir(coord);
					}
				}
			}
		}
		if(rayTrace.typeOfHit != MovingObjectPosition.MovingObjectType.MISS && rayTrace.hitVec != null){
			spawnParticle(EnumParticleTypes.SMOKE_LARGE, world,
					rayTrace.hitVec.xCoord, rayTrace.hitVec.yCoord, rayTrace.hitVec.zCoord);
			spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, world,
					rayTrace.hitVec.xCoord, rayTrace.hitVec.yCoord, rayTrace.hitVec.zCoord);
		}
	}


	private void spawnParticle(EnumParticleTypes p, World w, double x, double y, double z) {
		w.spawnParticle(p,
				x, y, z,
				0f, 0f, 0f,
				null);
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
			world.playSoundAtEntity(playerEntity, soundID, 1F, 1F);
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
	}
	
	public static boolean isLoaded(ItemStack musket){
		NBTTagCompound data = musket.getTagCompound();
		if(data == null) return false;
		return data.hasKey(NBT_DATA_KEY_LOADED) && data.getByte(NBT_DATA_KEY_LOADED) == 1;
	}
	
	public static boolean isNotLoaded(ItemStack musket){
		NBTTagCompound data = musket.getTagCompound();
		if(data == null) return true;
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
	
	
	public static MovingObjectPosition rayTraceBlocksAndEntities(World w, double maxRange, EntityLivingBase source){
		double rangeSqr = maxRange * maxRange;
		Vec3 rayOrigin = new Vec3(source.posX, source.posY + source.getEyeHeight(), source.posZ);
		Vec3 rayDirection = source.getLookVec();
		BlockPos srcPos = source.getPosition();
		AxisAlignedBB aoi = new AxisAlignedBB(srcPos.getX() - maxRange, srcPos.getY() - maxRange, srcPos.getZ() - maxRange,
				srcPos.getX() + maxRange, srcPos.getY() + maxRange, srcPos.getZ() + maxRange);
		List<Entity> entities = w.getEntitiesWithinAABBExcludingEntity(source, aoi);
		double closestDistSqr = Double.MAX_VALUE;
		Entity closestEntity = null;
		for(Entity e : entities){
			double distSqr = e.getDistanceSq(source.posX, source.posY, source.posZ);
			if(distSqr < rangeSqr){
				// e is within range
				AxisAlignedBB box = e.getEntityBoundingBox();
				if(rayIntersectsBoundingBox(rayOrigin,rayDirection, box)){
					// e is in cross-hairs
					if(distSqr < closestDistSqr){
						// e is closest entity in line of fire
						closestDistSqr = distSqr;
						closestEntity = e;
					}
				}
			}
		}
		if(closestEntity == null) {
			return w.rayTraceBlocks(rayOrigin, rayOrigin.add(mul(rayDirection, maxRange)), true, false, false);
		} else {
			Vec3 pos = new Vec3(closestEntity.posX, closestEntity.posY+closestEntity.getEyeHeight(), closestEntity.posZ);
			MovingObjectPosition entityCollision = new MovingObjectPosition(closestEntity, pos);
			return entityCollision;
		}
	}

	public static boolean rayIntersectsBoundingBox(Vec3 rayOrigin, Vec3 rayDirection, AxisAlignedBB box){
		if(box == null) return false;
		// algorithm from http://gamedev.stackexchange.com/questions/18436/most-efficient-aabb-vs-ray-collision-algorithms
		Vec3 inverse = new Vec3(1.0 / rayDirection.xCoord, 1.0 / rayDirection.yCoord, 1.0 / rayDirection.zCoord);
		double t1 = (box.minX - rayOrigin.xCoord)*inverse.xCoord;
		double t2 = (box.maxX- rayOrigin.xCoord)*inverse.xCoord;
		double t3 = (box.minY - rayOrigin.yCoord)*inverse.yCoord;
		double t4 = (box.maxY - rayOrigin.yCoord)*inverse.yCoord;
		double t5 = (box.minZ - rayOrigin.zCoord)*inverse.zCoord;
		double t6 = (box.maxZ - rayOrigin.zCoord)*inverse.zCoord;

		double tmin = max(max(min(t1, t2), min(t3, t4)), min(t5, t6));
		double tmax = min(min(max(t1, t2), max(t3, t4)), max(t5, t6));

		// if tmax < 0, ray (line) is intersecting AABB, but whole AABB is behing us
		if (tmax < 0)
		{
			return false;
		}

		// if tmin > tmax, ray doesn't intersect AABB
		if (tmin > tmax)
		{
			return false;
		}

		return true;
	}
	
	
	private static Vec3 mul(Vec3 a, double b){
		return new Vec3(a.xCoord * b, a.yCoord * b, a.zCoord * b);
	}
	private static double max(double a, double b){
		return Math.max(a, b);
	}
	private static double min(double a, double b){
		return Math.min(a, b);
	}
	
	/**
	 * Return whether this item is repairable in an anvil.
	 */
	@Override public boolean getIsRepairable(ItemStack srcItemStack, ItemStack rawMaterial)
	{
		// repair with steel ingots
		List<ItemStack> repairItems = OreDictionary.getOres("ingotSteel"); 
		for(int i = 0; i < repairItems.size(); i++){
			if(OreDictionary.itemMatches(repairItems.get(i),rawMaterial,false)) return true;
		}
		return false;
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
	
	@Override
	public int getMaxItemUseDuration(ItemStack stack)
	{
		if(isLoaded(stack)){
			return maxUseTime;
		} else {
			return getReloadTime();
		}
	}
}
