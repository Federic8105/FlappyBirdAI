/**
 * @author Federico Sabbatani
 */

package flappyBirdAI;

import flappyBirdAI.view.GameMenu;
import javafx.application.Application;

//TODO: javadocs, uml, javaFX

public final class Launcher {
	public static void main(String[] args) {
		Application.launch(GameMenu.class, args);
    }
}