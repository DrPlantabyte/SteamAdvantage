package cyano.steamadvantage.util;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketCustomSound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class SoundHelper {

	public static void playSoundAtTileEntity(SoundEvent sound, SoundCategory soundType, float volume, float pitch, TileEntity te){
		final BlockPos pos = te.getPos();
		final Vec3d center = new Vec3d(pos.getX()+0.5,pos.getY()+0.5,pos.getZ()+0.5);
		playSoundAtPosition(center.xCoord, center.yCoord, center.zCoord, sound, soundType, volume, pitch, te.getWorld());
	}

	public static void playSoundAtPosition(double x, double y, double z, SoundEvent sound, SoundCategory soundType, float volume, float pitch, World serverWorld){
		if(serverWorld.isRemote) return;
		final double range = 16;
		List<EntityPlayerMP> players = serverWorld.getEntitiesWithinAABB(EntityPlayerMP.class, new AxisAlignedBB(
				x - range, y - range, z - range,
				x + range, y + range, z + range));
		for(EntityPlayerMP player : players){
			player.playerNetServerHandler.sendPacket(new SPacketCustomSound(sound.getRegistryName().toString(), soundType,
					x, y, z, (float)volume, (float)pitch));
		}
	}
}
