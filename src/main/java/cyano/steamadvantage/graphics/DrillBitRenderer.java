package cyano.steamadvantage.graphics;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import cyano.steamadvantage.SteamAdvantage;
import cyano.steamadvantage.blocks.DrillBitTileEntity;

@SideOnly(Side.CLIENT) // This is needed for classes that extend client-only classes
public class DrillBitRenderer extends TileEntitySpecialRenderer{

	
	private final ResourceLocation texture = new ResourceLocation(SteamAdvantage.MODID+":textures/entity/drill_bit.png");

	private static final float RADIANS_TO_DEGREES = (float)(180 / Math.PI);
	
	public DrillBitRenderer() {
		super();
	}

	
	@Override
	public void renderTileEntityAt(final TileEntity te, final double x, final double y, final double z, final float partialTick, int meta) {
		if(te instanceof DrillBitTileEntity){
			// partialTick is guaranteed to range from 0 to 1
			GlStateManager.pushMatrix();
			GlStateManager.translate((float)x, (float)y, (float)z);

			render((DrillBitTileEntity)te,te.getWorld(),te.getPos(),partialTick);
			
			GlStateManager.popMatrix();
		}
	}
	
	private void render(DrillBitTileEntity e, World world, BlockPos pos, float partialTick){
		this.bindTexture(texture);
		final Tessellator instance = Tessellator.getInstance();
		
		//This will make your block brightness dependent from surroundings lighting.
		instance.getWorldRenderer().setBrightness(world.getCombinedLight(pos, 0));
		instance.getWorldRenderer().setColorOpaque_F(1f, 1f, 1f);
		
		final WorldRenderer worldRenderer = instance.getWorldRenderer();
		final float sideU0 = 0;
		final float sideU1 = 0.5f;
		final float sideV0 = 0;
		final float sideV1 = 1f;
		final float endU0 = 0.5f;
		final float endU1 = 1;
		final float endV0 = 0;
		final float endV1 = 0.5f;
		final float radius = 0.25f;

		GlStateManager.translate(0.5f, 0.5f, 0.5f);		
		if(e.getDirection() != EnumFacing.Axis.Y){
			GlStateManager.rotate(90, 1.0f, 0.0f, 0.0f);
			if(e.getDirection() != EnumFacing.Axis.Z){
				GlStateManager.rotate(90, 0.0f, 0.0f, 1.0f);
			}
		}
		GlStateManager.rotate(e.rotation + DrillBitTileEntity.ROTATION_PER_TICK * partialTick, 0.0f, 1.0f, 0.0f);

		worldRenderer.startDrawingQuads();
		worldRenderer.setNormal(0.0f, 1.0f, 0.0f);
		worldRenderer.addVertexWithUV( radius, 0.5f, -radius, sideU0, sideV0);
		worldRenderer.addVertexWithUV( radius,-0.5f, -radius, sideU0, sideV1);
		worldRenderer.addVertexWithUV(-radius,-0.5f, -radius, sideU1, sideV1);
		worldRenderer.addVertexWithUV(-radius, 0.5f, -radius, sideU1, sideV0);

		worldRenderer.addVertexWithUV(-radius, 0.5f,  radius, sideU0, sideV0);
		worldRenderer.addVertexWithUV(-radius,-0.5f,  radius, sideU0, sideV1);
		worldRenderer.addVertexWithUV( radius,-0.5f,  radius, sideU1, sideV1);
		worldRenderer.addVertexWithUV( radius, 0.5f,  radius, sideU1, sideV0);

		worldRenderer.addVertexWithUV(-radius, 0.5f, -radius, sideU0, sideV0);
		worldRenderer.addVertexWithUV(-radius,-0.5f, -radius, sideU0, sideV1);
		worldRenderer.addVertexWithUV(-radius,-0.5f,  radius, sideU1, sideV1);
		worldRenderer.addVertexWithUV(-radius, 0.5f,  radius, sideU1, sideV0);

		worldRenderer.addVertexWithUV( radius, 0.5f,  radius, sideU0, sideV0);
		worldRenderer.addVertexWithUV( radius,-0.5f,  radius, sideU0, sideV1);
		worldRenderer.addVertexWithUV( radius,-0.5f, -radius, sideU1, sideV1);
		worldRenderer.addVertexWithUV( radius, 0.5f, -radius, sideU1, sideV0);

		worldRenderer.addVertexWithUV(-radius, 0.5f, -radius, endU0, endV0);
		worldRenderer.addVertexWithUV(-radius, 0.5f,  radius, endU0, endV1);
		worldRenderer.addVertexWithUV( radius, 0.5f,  radius, endU1, endV1);
		worldRenderer.addVertexWithUV( radius, 0.5f, -radius, endU1, endV0);

		worldRenderer.addVertexWithUV(-radius,-0.5f,  radius, endU0, endV0);
		worldRenderer.addVertexWithUV(-radius,-0.5f, -radius, endU0, endV1);
		worldRenderer.addVertexWithUV( radius,-0.5f, -radius, endU1, endV1);
		worldRenderer.addVertexWithUV( radius,-0.5f,  radius, endU1, endV0);
		
		instance.draw();
	}

	
}
