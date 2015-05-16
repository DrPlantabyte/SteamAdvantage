package cyano.steamadvantage.init;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import cyano.basemetals.registry.CrusherRecipeRegistry;
import cyano.poweradvantage.PowerAdvantage;
import cyano.poweradvantage.RecipeMode;

public class Recipes {

	private static boolean initDone = false;
	public static void init(){
		if(initDone) return;
		
		Blocks.init();
		Items.init();
		
		
		
		RecipeMode recipeMode = PowerAdvantage.recipeMode;
		OreDictionary.registerOre("stick", net.minecraft.init.Items.stick);
		
		if(recipeMode == RecipeMode.TECH_PROGRESSION){
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Items.steam_governor,1)," t ","srs","btb",'t',"sprocket",'s',"barsSteel",'r',"barsBrass",'b',"ingotBrass"));
		} else if(recipeMode == RecipeMode.APOCALYPTIC){
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Items.steam_governor,1)," t ","srs","btb",'t',"sprocket",'s',"stick",'r',"barsBrass",'b',"ingotBrass"));
			CrusherRecipeRegistry.addNewCrusherRecipe(Items.steam_governor, new ItemStack(cyano.poweradvantage.init.Items.sprocket,2));
		} else {
			// normal
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Items.steam_governor,1)," t ","srs","btb",'t',"nuggetIron",'s',"stick",'r',"stick",'b',"ingotBrass"));
		}
		
		initDone = true;
	}
}
