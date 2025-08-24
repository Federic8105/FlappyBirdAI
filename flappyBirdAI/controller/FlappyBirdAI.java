/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.controller;

import flappyBirdAI.ai.BirdBrain;
import flappyBirdAI.model.AbstractGameObject;
import flappyBirdAI.model.FlappyBird;
import flappyBirdAI.view.GameView;
import flappyBirdAI.view.SwingGameView;
import java.util.List;
import java.util.ArrayList;

//TODO: messaggi in inglese, opzioni di autosave con BLT e nMaxTubePassed, javaFX, uso requireNonNull
//TODO: impostare w e h min di ogni pannello, javadocs, sfondo con nuvole

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
    	
    	GameView gameView;
    	if (useJavaFX) {
    		gameView = new SwingGameView(w, h);
		} else {
			gameView = new SwingGameView(w, h);
		}
    	
		gameController = new GameController(gameView);

		startGame();
	}

	private List<AbstractGameObject> createRandomBirds(int nBirds) {
		List<AbstractGameObject> vBirds = new ArrayList<>();
		int startY;
		
		for (int i = 0; i < nBirds; ++i) {
			startY = gameController.getGameHeight() / 2 - FlappyBird.height / 2;
			vBirds.add(new FlappyBird(20, startY, new BirdBrain()));
		}
		
		return vBirds;
	}

	private List<AbstractGameObject> createBirds(int nBirds, BirdBrain bestBirdBrain) {
		List<AbstractGameObject> vBirds = new ArrayList<>();
		int startY;
		
		for (int i = 0; i < nBirds; ++i) {
			startY = gameController.getGameHeight() / 2 - FlappyBird.height / 2;
			vBirds.add(new FlappyBird(20, startY, bestBirdBrain));
			((FlappyBird) vBirds.get(i)).brain.updateWeights();
		}
		
		return vBirds;
	}

	private void startGame() {
		boolean isFirstGen = true;
		int nBirdsRegen = nBirdsXGen * 4 / 5;

		while (true) {

			if (isFirstGen) {
				gameController.addBirds(createRandomBirds(nBirdsXGen));
				isFirstGen = false;
			} else {
				gameController.addBirds(createBirds(nBirdsRegen, gameController.getBestBirdBrain()));
				gameController.addBirds(createRandomBirds(nBirdsXGen - nBirdsRegen));
			}

			gameController.startMotion();
			gameController.reset();
		}
	}

}