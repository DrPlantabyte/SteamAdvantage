package cyano.steamadvantage.init;

public class Items {

	private static boolean initDone = false;
	public static void init(){
		if(initDone) return;
		initDone = true;
	}
}
