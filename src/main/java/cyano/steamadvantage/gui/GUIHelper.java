package cyano.steamadvantage.gui;

public abstract class GUIHelper {

	
	public static void drawNeedle(float pivotX, float pivotY, float zLevel,
			float value) {
		
		final float pixelSize = 1.0f / 256; // conversion factor
		
		final float PI = 3.141592654f;
		final float needleX = 176 * pixelSize;
		final float needleW = 7 * pixelSize;
		final float needleY = 0 * pixelSize;
		final float needleH = 35 * pixelSize;
		final float needleL = needleH * 0.5f;
		final float needleR = needleW * 0.5f;
		
		float angle = (value - 0.5f) * PI;
		float offsetX = pivotX * pixelSize;
		float offsetY = pivotY * pixelSize;
		net.minecraft.client.renderer.WorldRenderer renderer = net.minecraft.client.renderer.Tessellator.getInstance().getWorldRenderer();

		float sin = net.minecraft.util.MathHelper.sin(angle);
		float cos = net.minecraft.util.MathHelper.cos(angle);
		
		renderer.startDrawingQuads();

		// (x, y, z, u, v)
		renderer.addVertexWithUV( sin * needleL - cos * needleR + offsetX, cos * needleL - sin * needleR + offsetY, zLevel, needleX, needleY); // top-left corner 
		renderer.addVertexWithUV(-sin * needleL - cos * needleR + offsetX,-cos * needleL - sin * needleR + offsetY, zLevel, needleX, needleY + needleH); // bottom-left corner
		renderer.addVertexWithUV(-sin * needleL + cos * needleR + offsetX,-cos * needleL + sin * needleR + offsetY, zLevel, needleX + needleW, needleY + needleH); // bottom-right corner
		renderer.addVertexWithUV( sin * needleL + cos * needleR + offsetX, cos * needleL + sin * needleR + offsetY, zLevel, needleX + needleW, needleY); // top-right corner
		
		net.minecraft.client.renderer.Tessellator.getInstance().draw();
	}
}
