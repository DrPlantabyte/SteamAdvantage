package cyano.steamadvantage.gui;

import net.minecraft.util.ResourceLocation;
import cyano.poweradvantage.api.simple.SimpleMachineGUI;
import cyano.poweradvantage.math.Integer2D;
import cyano.steamadvantage.SteamAdvantage;
import cyano.steamadvantage.machines.CoalBoilerTileEntity;

public class CoalBoilerGUI extends SimpleMachineGUI{

	public CoalBoilerGUI() {
		super(
				new ResourceLocation(SteamAdvantage.MODID+":textures/gui/container/coal_boiler.png"), 
				new Integer2D[] {new Integer2D(80,80)}
		);
	}
	
	
	private static final float maxNeedleSpeed = 0.015625f;
	
	private float oldSteam = 0;
	private float oldWater = 0;
	

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
		
		if(srcEntity.getClass() == CoalBoilerTileEntity.class){
			CoalBoilerTileEntity target = (CoalBoilerTileEntity)srcEntity;

			
			float steamPivotX = 44f;
			float steamPivotY = 40f;
			
			float waterPivotX = 132f;
			float waterPivotY = 40f;
			
			float steam = target.getSteamLevel(); 
			float water = target.getWaterLevel(); 
			float burn = target.getBurnLevel();

			long t = System.currentTimeMillis();
			if((t - lastUpdate) < 1000L){
				// slow the needles if it has been less than 1 second since last invocation
				steam = GUIHelper.maxDelta(steam, oldSteam, maxNeedleSpeed);
				water = GUIHelper.maxDelta(water, oldWater, maxNeedleSpeed);
			}
			oldSteam = steam;
			oldWater = water;
			lastUpdate = t;
			
			GUIHelper.drawNeedle(x+steamPivotX, y+steamPivotY, z, steam);
			GUIHelper.drawNeedle(x+waterPivotX, y+waterPivotY, z, water);
			GUIHelper.drawFlameProgress(x+80,y+58,1f-burn,guiContainer);
			
		}
		
	}


	

}
