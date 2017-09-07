package Assignment2;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class BilliardsLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

		config.title = "Billiards"; // or whatever you like
		config.width = 800;  //experiment with
		config.height = 600;  //the window size
		
		new LwjglApplication(new BilliardsGame(), config);
	}
}
