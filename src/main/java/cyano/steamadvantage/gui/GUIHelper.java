package cyano.steamadvantage.gui;

import cyano.poweradvantage.api.simple.SimpleMachineGUI.GUIContainer;
import cyano.poweradvantage.gui.FluidTankGUI;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fluids.FluidStack;

import java.util.HashMap;
import java.util.Map;

public abstract class GUIHelper {


	private static final Map<ResourceLocation,ResourceLocation> realTextureLocationCache = new HashMap<>();
	
	public static float maxDelta(float newValue,float oldValue,float maxChange){
		if((newValue - oldValue) > maxChange){
			return oldValue + maxChange;
		}
		if((oldValue - newValue) > maxChange){
			return oldValue - maxChange;
		}
		return newValue;
	}
	
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
		VertexBuffer renderer = Tessellator.getInstance().getBuffer();

		float sin = MathHelper.sin(angle);
		float cos = MathHelper.cos(angle);

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
		
		renderer.begin(7, DefaultVertexFormats.POSITION_TEX);

		// (x, y, z, u, v)
		for(int i = 3; i >= 0; i--){
			renderer.pos(x[i], y[i], zLevel).tex(u[i],v[i]).endVertex();; 
		}
		net.minecraft.client.renderer.Tessellator.getInstance().draw();
		
	}
	
	public static void drawFlameProgress(int x, int y, float flameHeight, GUIContainer gc){
		if(flameHeight <= 0) return;
		final int flameTexX = 200;
		final int flameTexY = 0;
		final int width = 14;
		final int height = 15;
		int h = (int)(flameHeight*height);
		if(h == 0 ) h = 1;
		if(h > 0){
			gc.drawTexturedModalRect(x, y+h, flameTexX, flameTexY+h, width, height - h);
		}
	}
	
	public static void drawFluidBar(FluidStack fs, float barHeight, int xPos, int yPos, 
			GUIContainer guiContainer, int x, int y, float z,
			ResourceLocation displayImage, int texOverlayX, int texOverlayY, int texOverlayW, int texOverlayH){
		final int texMarginW = (texOverlayW - 16) / 2;
		final int texMarginH = (texOverlayH - 60) / 2;
		final int w = 16;
		final int barSlotHeight = 60;
		final int h = (int)(barSlotHeight * barHeight);
		if(barHeight > 0){
			FluidTankGUI.drawFluidFilledRectangle(guiContainer,fs,x+xPos, y+yPos+barSlotHeight-h,w, h,z);
		}
			guiContainer.mc.renderEngine.bindTexture(displayImage);
		
		guiContainer.drawTexturedModalRect(x+xPos-texMarginW, y+yPos-texMarginH, texOverlayX, texOverlayY, texOverlayW, texOverlayH); // x, y, textureOffsetX, textureOffsetY, width, height)
	}
	


	public static void drawDownArrowProgress(int x, int y, float progress,
			GUIContainer gc) {
		if(progress <= 0) return;
		final int arrowTexX = 184;
		final int arrowTexY = 0;
		final int width = 16;
		final int height = 32;
		int h = (int)(progress*height);
		if(h == 0 ) h = 1;
		if(h > 0){
			gc.drawTexturedModalRect(x, y, arrowTexX, arrowTexY, width, h);
		}
	}
}
