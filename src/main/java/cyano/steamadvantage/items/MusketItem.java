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
		FMLLog.info("onItemRightClick, using for "+ this.getMaxItemUseDuration(srcItemStack)+" ticks"); // TODO: remove debug code
		playerEntity.setItemInUse(srcItemStack, this.getMaxItemUseDuration(srcItemStack));
		return srcItemStack;
	}
	
	@Override
	public void onUsingTick(ItemStack stack, EntityPlayer player, int count){
		if(player.worldObj.isRemote && count % 11 == 3 && isNotLoaded(stack)){
			// indicator to player that the gun is loading
			player.playSound("step.wood", 0.5f, 1.0f);
		}
	}
	/**
	 * This method is invoked after the item has been used for an amount of time equal to the duration 
	 * provided to the EntityPlayer.setItemInUse(stack, duration).
	 */
	@Override
	public ItemStack onItemUseFinish (ItemStack srcItemStack, World world, EntityPlayer playerEntity)
	{ // 
		FMLLog.info("onItemUseFinish"); // TODO: remove debug code
		if(isNotLoaded(srcItemStack) && hasAmmo(playerEntity,srcItemStack)){
			FMLLog.info("doing reload"); // TODO: remove debug code
			decrementAmmo(playerEntity);
			startLoad(srcItemStack);
			playSound("random.click",world,playerEntity);
		}
		return srcItemStack;
	}
	
	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityPlayer playerIn, int timeLeft) {
		FMLLog.info("onPlayerStoppedUsing"); // TODO: remove debug code
		if(isAlmostLoaded(stack)){
			finishLoad(stack);
		} else if(isLoaded(stack)){
			fire(stack,playerIn,worldIn);
		}
	}
	
	protected void fire(ItemStack srcItemStack, EntityPlayer playerEntity,
			World world) {
		FMLLog.info("FIRE!!!"); // TODO: remove debug code
		unload(srcItemStack);
		Vec3 lookVector = playerEntity.getLookVec();
		spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, world,
				playerEntity.posX+lookVector.xCoord, playerEntity.posY+playerEntity.getEyeHeight()+lookVector.yCoord, playerEntity.posZ + lookVector.zCoord);
		if(world.isRemote){
			playerEntity.rotationPitch -= 15;
			return;
		}
		playSound("fire.ignite",world,playerEntity);
		FMLLog.info("ka-pow!"); // TODO: remove debug code
		world.playSoundEffect(playerEntity.posX,playerEntity.posY,playerEntity.posZ,"fireworks.blast",2F,0.5F);
		
		Vec3 start = new Vec3(playerEntity.posX, playerEntity.posY+playerEntity.getEyeHeight(),playerEntity.posZ);
		Vec3 end = start.addVector(MAX_RANGE * lookVector.xCoord, MAX_RANGE * lookVector.yCoord, MAX_RANGE * lookVector.zCoord);
		MovingObjectPosition rayTrace = rayTraceBlocksAndEntities(world,MAX_RANGE,playerEntity);
		if(rayTrace == null){
			// no collisions
			FMLLog.info("no collision detected");
			return;
		}
		FMLLog.info("ray traced. typeOfHit = "+rayTrace.typeOfHit+", hitVec = "+rayTrace.hitVec+", getBlockPos() = "+rayTrace.getBlockPos()+", rayTrace.entityHit = "+rayTrace.entityHit); // TODO: remove debug code
		if(rayTrace.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && rayTrace.entityHit != null){
			Entity e = rayTrace.entityHit;
			e.attackEntityFrom(Power.musket_damage, getShotDamage());
			FMLLog.info("hit a "+e.getClass()); // TODO: remove debug code
		} else if(rayTrace.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK){
			world.playSoundEffect(rayTrace.hitVec.xCoord, rayTrace.hitVec.yCoord, rayTrace.hitVec.zCoord, 
					"dig.snow", 1f, 1f);
			BlockPos coord = rayTrace.getBlockPos();
			if(coord.getY()>= 0 && coord.getY() <= 255 && !world.isAirBlock(coord)){
				FMLLog.info("hit block of "+world.getBlockState(coord).getBlock().getUnlocalizedName()); // TODO: remove debug code
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
			FMLLog.info("hit block at "+coord); // TODO: remove debug code
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
				FMLLog.info("Entity "+e.getName()+" box: "+box); // TODO: remove debug code
				if(rayIntersectsBoundingBox(rayOrigin,rayDirection, box)){
					// e is in cross-hairs
					FMLLog.info("Entity "+e.getName()+" was in the line of fire at a distance of "+(float)Math.sqrt(distSqr)); // TODO: remove debug code
					if(distSqr < closestDistSqr){
						// e is closest entity in line of fire
						closestDistSqr = distSqr;
						closestEntity = e;
						FMLLog.info("Targeted "+e.getName()); // TODO: remove debug code
					}
				}
			}
		}
		FMLLog.info("Shot entity "+closestEntity); // TODO: remove debug code
		if(closestEntity == null) {
			FMLLog.info("Block trace"); // TODO: remove debug code
			return w.rayTraceBlocks(rayOrigin, rayOrigin.add(mul(rayDirection, maxRange)), true, false, false);
		} else {
			FMLLog.info("Entity trace"); // TODO: remove debug code
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

		FMLLog.info("tmin="+tmin+"; tmax="+tmax); // TODO: remove debug code
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
//		Vec3 lowerCorner = new Vec3(box.minX, box.minY, box.minZ);
//		Vec3 upperCorner = new Vec3(box.maxX, box.maxY, box.maxZ);
//		Vec3 V0, V1, V2, V3, V4, V5, V6, V7;
//		V0 = lowerCorner;
//		V1 = new Vec3(upperCorner.xCoord, lowerCorner.yCoord, lowerCorner.zCoord);
//		V2 = new Vec3(upperCorner.xCoord, lowerCorner.yCoord, upperCorner.zCoord);
//		V3 = new Vec3(lowerCorner.xCoord, lowerCorner.yCoord, upperCorner.zCoord);
//		V4 = new Vec3(lowerCorner.xCoord, upperCorner.yCoord, lowerCorner.zCoord);
//		V5 = new Vec3(upperCorner.xCoord, upperCorner.yCoord, lowerCorner.zCoord);
//		V6 = new Vec3(lowerCorner.xCoord, upperCorner.yCoord, upperCorner.zCoord);
//		V7 = upperCorner;
//		return rayIntersectsRectangle(rayOrigin, rayDirection, V0, V1, V2, V3)
//				|| rayIntersectsRectangle(rayOrigin, rayDirection, V0, V3, V6, V4)
//				|| rayIntersectsRectangle(rayOrigin, rayDirection, V0, V1, V5, V4)
//				|| rayIntersectsRectangle(rayOrigin, rayDirection, V1, V2, V7, V5)
//				|| rayIntersectsRectangle(rayOrigin, rayDirection, V2, V3, V6, V7)
//				|| rayIntersectsRectangle(rayOrigin, rayDirection, V4, V5, V6, V7);
	}
	public static boolean rayIntersectsRectangle(Vec3 rayOrigin, Vec3 rayDirection, Vec3 V0, Vec3 V1, Vec3 V2, Vec3 V3){
		return rayIntersectsTriangle(rayOrigin,rayDirection,V0,V1,V2) || rayIntersectsTriangle(rayOrigin,rayDirection,V2,V3,V0); 
	}
	public static boolean rayIntersectsTriangle(Vec3 rayOrigin, Vec3 rayDirection, Vec3 V0, Vec3 V1, Vec3 V2){
		// Algorithm from http://geomalgorithms.com/a06-_intersect-2.html#intersect3D_RayTriangle%28%29
		Vec3    u, v, n;              // triangle vectors
		Vec3    dir, w0, w;           // ray vectors
		double     r, a, b;              // params to calc ray-plane intersect

		// get triangle edge vectors and plane normal
		u = V1.subtract(V0);
		v = V2.subtract(V0);
		n = u.crossProduct(v);              // cross product
		if (n.xCoord == 0 && n.yCoord == 0 && n.zCoord == 0)             // triangle is degenerate
			return false;                  // do not deal with this case

		dir = rayDirection;              // ray direction vector
		w0 = rayOrigin.subtract(V0);
		a = -dot(n,w0);
		b = dot(n,dir);
		if (Math.abs(b) < Float.MIN_VALUE) {     // ray is  parallel to triangle plane
			if (a == 0)                 // ray lies in triangle plane
				return true;
			else return false;              // ray disjoint from plane
		}

		// get intersect point of ray with triangle plane
		r = a / b;
		if (r < 0.0)                    // ray goes away from triangle
			return false;                   // => no intersect
		// for a segment, also test if (r > 1.0) => no intersect

		Vec3 I = rayOrigin.add(mul(dir,r));            // intersect point of ray and plane

		// is I inside T?
		double    uu, uv, vv, wu, wv, D;
		uu = dot(u,u);
		uv = dot(u,v);
		vv = dot(v,v);
		w = I.subtract(V0);
		wu = dot(w,u);
		wv = dot(w,v);
		D = uv * uv - uu * vv;

		// get and test parametric coords
		double s, t;
		s = (uv * wv - vv * wu) / D;
		if (s < 0.0 || s > 1.0)         // I is outside T
			return true;
		t = (uv * wu - uu * wv) / D;
		if (t < 0.0 || (s + t) > 1.0)  // I is outside T
			return true;

		return true;                       // I is in T
	}
	
	private static double dot(Vec3 a, Vec3 b){
		return a.dotProduct(b);
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
