package cyano.steamadvantage.gui;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraftforge.fml.common.FMLLog;

public abstract class GUIHelper {

	
	public static void drawNeedle(float pivotX, float pivotY, float zLevel,
			float value) {
		

		final float pixelSize = 1.0f / 256; // conversion factor
		
		final float PI = 3.141592654f;
		final float needleX = 176 ;
		final float needleW = 7 ;
		final float needleY = 0 ;
		final float needleH = 35;
		final float needleL = needleH * 0.5f;
		final float needleR = needleW * 0.5f;
		
		float angle = (1f - value) * PI;
		float offsetX = pivotX;
		float offsetY = pivotY;
		net.minecraft.client.renderer.WorldRenderer renderer = net.minecraft.client.renderer.Tessellator.getInstance().getWorldRenderer();

		float sin = net.minecraft.util.MathHelper.sin(angle);
		float cos = net.minecraft.util.MathHelper.cos(angle);

		float tipX =   cos * needleL + offsetX;
		float tipY =  -sin * needleL + offsetY;
		float baseX = -cos * needleL + offsetX;
		float baseY =  sin * needleL + offsetY;
		float dx = -sin * needleR;
		float dy = -cos * needleR;
		
		float[] x = new float[4];
		float[] y = new float[4];
		float[] u = new float[4];
		float[] v = new float[4];
		// top-left, bottom-left, bottom-right, top-right
		x[0] = tipX - dx;
		x[1] = baseX - dx;
		x[2] = baseX + dx;
		x[3] = tipX + dx;
		y[0] = tipY - dy;
		y[1] = baseY - dy;
		y[2] = baseY + dy;
		y[3] = tipY + dy;
		u[0] = pixelSize * needleX;
		u[1] = u[0];
		u[2] = u[0] + pixelSize * needleW;
		u[3] = u[2];
		v[0] = pixelSize * needleY;
		v[1] = v[0] + pixelSize * needleH;
		v[2] = v[1];		
		v[3] = v[0];
		
		renderer.startDrawingQuads();

		// (x, y, z, u, v)
		for(int i = 3; i >= 0; i--){
			renderer.addVertexWithUV(x[i], y[i], zLevel, u[i],v[i]); 
		}
		net.minecraft.client.renderer.Tessellator.getInstance().draw();
		
	}
	/*
	// from Forge source:
	public void drawTexturedModalRect(final float x, final float y, final int u, final int v, final int w, final int h) {
        final float n = 0.00390625f;
        final float n2 = 0.00390625f;
        final Tessellator instance = Tessellator.getInstance();
        final WorldRenderer worldRenderer = instance.getWorldRenderer();
        worldRenderer.startDrawingQuads();
        worldRenderer.addVertexWithUV(x + 0.0f, y + h, zLevel, (u + 0) * n, (v + h) * n2);
        worldRenderer.addVertexWithUV(x + w, y + h, zLevel, (u + w) * n, (v + h) * n2);
        worldRenderer.addVertexWithUV(x + w, y + 0.0f, zLevel, (u + w) * n, (v + 0) * n2);
        worldRenderer.addVertexWithUV(x + 0.0f, y + 0.0f, zLevel, (u + 0) * n, (v + 0) * n2);
        instance.draw();
    }
    */
}
