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

//TODO: opzioni di autosave con BLT e nMaxTubePassed, javaFX, javadocs e organizzazione metodi, migliori interfacce con più metodi
//TODO: private, tubes ridisegnati fino a fine schermo di gioco se ridimensiono schermo
//TODO: + threads, panel import export non ricalcola altezza, timer si blocca a fine gen

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

	//TODO implementa un modo per fermare il gioco
	private void startGame() {
		int nBirdsRegen = nBirdsXGen * 4 / 5;
		
		//TODO qui o in gameController?
		// Solo per la prima generazione
		gameController.addBirds(createRandomBirds(nBirdsXGen));
		
		while (true) {
			gameController.startGame();
			
			gameController.addBirds(createBirds(nBirdsRegen, gameController.getBestBirdBrain()));
			gameController.addBirds(createRandomBirds(nBirdsXGen - nBirdsRegen));
		}
	}

}