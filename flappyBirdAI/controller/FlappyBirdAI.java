/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.controller;

import flappyBirdAI.view.JavaFXGameView;
import flappyBirdAI.view.SwingGameView;

//TODO: javaFX, javadocs e organizzazione metodi, menù iniziale con scelta grafica e se carica già cervello
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
    	
		gameController = new GameController(useJavaFX ? new JavaFXGameView(w, h) : new SwingGameView(w, h), nBirdsXGen, nBirdsRegen);

		startGame();
	}

	private void startGame() {
		while (true) {
			gameController.playOneGen();
		}
	}

}