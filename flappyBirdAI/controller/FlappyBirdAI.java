/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.controller;

import flappyBirdAI.ai.BirdBrain;
import flappyBirdAI.model.AbstractGameObject;
import flappyBirdAI.model.FlappyBird;
import flappyBirdAI.view.SwingGameView;
import java.util.List;
import java.util.ArrayList;

//TODO: opzioni di autosave con BLT e nMaxTubePassed, javaFX, javadocs e organizzazione metodi, update label migliore, migliori interfacce con più metodi
//TODO: pausa gioco con spazio usando isGameRunning, private, labels centrate, tubes ridisegnati fine a fine schermo di gioco se ridimensiono schermo
//TODO: t da inizio gioco, + threads

public class FlappyBirdAI {

	public static void main(String[] args) {
		int w = 1250, h = 750, nBirdsXGen = 1000;
		boolean useJavaFX = false;
		new FlappyBirdAI(w, h, nBirdsXGen, useJavaFX);
	}

	private final int nBirdsXGen;
	private final GameController gameController;

    public FlappyBirdAI(int w, int h, int nBirdsXGen, boolean useJavaFX) {
    	this.nBirdsXGen = nBirdsXGen;
    	
		gameController = new GameController(useJavaFX ? new SwingGameView(w, h) : new SwingGameView(w, h));

		startGame();
	}

	private List<AbstractGameObject> createRandomBirds(int nBirds) {
		List<AbstractGameObject> vBirds = new ArrayList<>();
		int startY;
		
		for (int i = 0; i < nBirds; ++i) {
			startY = gameController.getGameHeight() / 2 - FlappyBird.HEIGHT / 2;
			vBirds.add(new FlappyBird(20, startY, new BirdBrain()));
		}
		
		return vBirds;
	}

	private List<AbstractGameObject> createBirds(int nBirds, BirdBrain bestBirdBrain) {
		List<AbstractGameObject> vBirds = new ArrayList<>();
		int startY;
		
		for (int i = 0; i < nBirds; ++i) {
			startY = gameController.getGameHeight() / 2 - FlappyBird.HEIGHT / 2;
			vBirds.add(new FlappyBird(20, startY, bestBirdBrain));
			((FlappyBird) vBirds.get(i)).brain.updateWeights();
		}
		
		return vBirds;
	}

	private void startGame() {
		int nBirdsRegen = nBirdsXGen * 4 / 5;

		while (true) {

			if (gameController.isFirstGen()) {
				gameController.addBirds(createRandomBirds(nBirdsXGen));
			} else {
				gameController.addBirds(createBirds(nBirdsRegen, gameController.getBestBirdBrain()));
				gameController.addBirds(createRandomBirds(nBirdsXGen - nBirdsRegen));
			}

			gameController.startGame();
			gameController.resetForNewGen();
		}
	}

}