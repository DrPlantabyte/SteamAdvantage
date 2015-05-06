package cyano.steamadvantage.init;

public class GUI {

	private static boolean initDone = false;
	public static void init(){
		if(initDone) return;
		
		Entities.init();
		
		initDone = true;
	}
}
