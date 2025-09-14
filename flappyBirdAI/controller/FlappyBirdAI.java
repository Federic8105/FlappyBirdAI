/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.controller;

import flappyBirdAI.ai.BirdBrain;
import flappyBirdAI.model.AbstractGameObject;
import flappyBirdAI.model.FlappyBird;
import flappyBirdAI.model.Tube;
import flappyBirdAI.view.SwingGameView;
import java.util.List;
import java.util.ArrayList;

//TODO: opzioni di autosave con BLT e nMaxTubePassed, javaFX, javadocs e organizzazione metodi, migliori interfacce con più metodi
//TODO: private, tubes ridisegnati fino a fine schermo di gioco se ridimensiono schermo
//TODO: + threads anche per gestire fine pausa senza attesa e times, timer si blocca a fine gen, richiest focus dopo input import/export

public class FlappyBirdAI {

	public static void main(String[] args) {
		int w = 1250, h = 750, nBirdsXGen = 1000;
		boolean useJavaFX = false;
		new FlappyBirdAI(w, h, nBirdsXGen, useJavaFX);
	}

	private static final double BIRDS_REGEN_PERC = 0.8;
	
	private final int nBirdsXGen, nBirdsRegen;
	private final GameController gameController;

    public FlappyBirdAI(int w, int h, int nBirdsXGen, boolean useJavaFX) {
    	this.nBirdsXGen = nBirdsXGen;
    	this.nBirdsRegen = (int) (nBirdsXGen * BIRDS_REGEN_PERC);
    	
    	Tube.loadImages();
    	FlappyBird.loadImages();
    	
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
			((FlappyBird) vBirds.get(i)).getBrain().updateWeights();
		}
		
		return vBirds;
	}
	
	private void addNewGenBirds() {
		gameController.addBirds(createBirds(nBirdsRegen, gameController.getBestBirdBrain()));
		gameController.addBirds(createRandomBirds(nBirdsXGen - nBirdsRegen));
	}

	private void startGame() {
		
		// Solo per la prima generazione
		gameController.addBirds(createRandomBirds(nBirdsXGen));
		
		while (true) {
			gameController.startGame();
			addNewGenBirds();
		}
	}

}