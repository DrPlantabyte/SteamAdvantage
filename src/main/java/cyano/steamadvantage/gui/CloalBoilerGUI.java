package cyano.steamadvantage.gui;

import net.minecraft.util.ResourceLocation;
import cyano.poweradvantage.api.simple.SimpleMachineGUI;
import cyano.poweradvantage.math.Integer2D;
import cyano.steamadvantage.SteamAdvantage;

public class CloalBoilerGUI extends SimpleMachineGUI{

	public CloalBoilerGUI() {
		super(
				new ResourceLocation(SteamAdvantage.MODID+":textures/gui/container/coal_boiler"), 
				new Integer2D[] {new Integer2D(80,80)}
		);
	}
	

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
	public void drawGUIDecorations(Object srcEntity, GUIContainer guiContainer, int x, int y, float  z){
		float pressure = 0f; // TODO: set to number from 0 to 1
		float temperature = 0f; // TODO: set to number from 0 to 1
		float burnTime = 0f; // TODO: set to number from 0 to 1
		float pressurePivotX = 43.5f;
		float pressurePivotY = 39.5f;
		
		GUIHelper.drawNeedle(pressurePivotX, pressurePivotY, z, pressure);
	}


	

}
