/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.controller;

import flappyBirdAI.model.FlappyBird;
import flappyBirdAI.model.Tube;
import flappyBirdAI.view.JavaFXGameView;
import flappyBirdAI.view.SwingGameView;

//TODO: javaFX, javadocs e organizzazione metodi, migliori interfacce con più metodi, uso tube solo a coppie
//TODO: + threads anche per gestire fine pausa senza attesa e times e per timer

public class FlappyBirdAI {

	public static void main(String[] args) {
		int w = 1250, h = 750, nBirdsXGen = 1000;
		boolean useJavaFX = false;
		new FlappyBirdAI(w, h, nBirdsXGen, useJavaFX);
	}

	private static final double BIRDS_REGEN_PERC = 0.8;
	
	private final GameController gameController;

    public FlappyBirdAI(int w, int h, int nBirdsXGen, boolean useJavaFX) {
    	int nBirdsRegen = (int) (nBirdsXGen * BIRDS_REGEN_PERC);
    	
    	Tube.loadImages();
    	FlappyBird.loadImages();
    	
		gameController = new GameController(useJavaFX ? new JavaFXGameView(w, h) : new SwingGameView(w, h), nBirdsXGen, nBirdsRegen);

		startGame();
	}

	private void startGame() {
		while (true) {
			gameController.startOneGen();
		}
	}

}