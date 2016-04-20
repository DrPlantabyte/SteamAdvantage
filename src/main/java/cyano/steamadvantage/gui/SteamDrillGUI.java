package cyano.steamadvantage.gui;

import net.minecraft.util.ResourceLocation;
import cyano.poweradvantage.api.simple.SimpleMachineGUI;
import cyano.poweradvantage.math.Integer2D;
import cyano.steamadvantage.SteamAdvantage;
import cyano.steamadvantage.machines.SteamDrillTileEntity;

public class SteamDrillGUI extends SimpleMachineGUI{

	public SteamDrillGUI() {
		super(
				new ResourceLocation(SteamAdvantage.MODID+":textures/gui/container/steam_drill.png"), 
				Integer2D.fromCoordinates(44,88, 62,88, 80,88, 98,88, 116,88)
		);
	}
	
	
	private static final float maxNeedleSpeed = 0.015625f;
	
	private float oldSteam = 0;
	

	private long lastUpdate = 0;
	/**
	 * Override this method to draw on the GUI window.
	 * <br><br>
	 * This method is invoked when drawing the GUI so that you can draw 
	 * animations and other foreground decorations to the GUI.
	 * @param srcEntity This is the TileEntity (or potentially a LivingEntity) 
	 * for whom we are drawing this interface
	 * @param guiContainer This is the instance of GUIContainer that is drawing 
	 * the GUI. You need to use it to draw on the screen. For example:<br>
	   <pre>
guiContainer.mc.renderEngine.bindTexture(arrowTexture);
guiContainer.drawTexturedModalRect(x+79, y+35, 0, 0, arrowLength, 17); // x, y, textureOffsetX, textureOffsetY, width, height)
	   </pre>
	 * @param x This is the x coordinate (in pixels) from the top-left corner of 
	 * the GUI
	 * @param y This is the y coordinate (in pixels) from the top-left corner of 
	 * the GUI
	 * @param z This is the z coordinate (no units) into the depth of the screen
	 */
	@Override
	public void drawGUIDecorations(Object srcEntity, GUIContainer guiContainer, int x, int y, float  z){
		
		if(srcEntity.getClass() == SteamDrillTileEntity.class){
			SteamDrillTileEntity target = (SteamDrillTileEntity)srcEntity;

			
			float steamPivotX = 132f;
			float steamPivotY = 40f;
			
			
			float steam = target.getSteamLevel(); 
			float progress = target.getProgressLevel(); 

			long t = System.currentTimeMillis();
			if((t - lastUpdate) < 1000L){
				// slow the needles if it has been less than 1 second since last invocation
				steam = GUIHelper.maxDelta(steam, oldSteam, maxNeedleSpeed);
			}
			oldSteam = steam;
			lastUpdate = t;
			
			GUIHelper.drawNeedle(x+steamPivotX, y+steamPivotY, z, steam);
			GUIHelper.drawDownArrowProgress(x+44,y+46,progress,guiContainer);
			
		}
		
	}


	

}
