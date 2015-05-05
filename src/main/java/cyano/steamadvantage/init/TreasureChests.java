package cyano.steamadvantage.init;

public class TreasureChests {

	private static boolean initDone = false;
	public static void init(){
		if(initDone) return;
		initDone = true;
	}
}
