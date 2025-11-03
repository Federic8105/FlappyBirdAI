/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.controller;

import flappyBirdAI.ai.BirdBrain;
import flappyBirdAI.model.AbstractGameObject;
import flappyBirdAI.model.FlappyBird;
import flappyBirdAI.model.Tube;
import flappyBirdAI.view.JavaFXGameView;
import flappyBirdAI.view.SwingGameView;
import java.util.Set;
import java.util.HashSet;
import java.util.Optional;

//TODO: javaFX, javadocs e organizzazione metodi, migliori interfacce con più metodi, uso tube solo a coppie
//TODO: + threads anche per gestire fine pausa senza attesa e times e per timer, autosave solo 1 volta per ogni cambiamento di var o tempo

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
    	
		gameController = new GameController(useJavaFX ? new JavaFXGameView(w, h) : new SwingGameView(w, h), nBirdsXGen);

		startGame();
	}

	private Set<AbstractGameObject> createRandomBirds(int nBirds) {
		Set<AbstractGameObject> vBirds = new HashSet<>(nBirds);
		int startY = gameController.getGameHeight() / 2 - FlappyBird.HEIGHT / 2;
		
		for (int i = 0; i < nBirds; ++i) {
			vBirds.add(new FlappyBird(20, startY, new BirdBrain()));
		}
		
		return vBirds;
	}

	private Set<AbstractGameObject> createBirds(int nBirds, Optional<BirdBrain> bestBirdBrainOpt) {
		if (bestBirdBrainOpt.isEmpty()) {
			return createRandomBirds(nBirds);
		}
		
		BirdBrain bestBirdBrain = bestBirdBrainOpt.get();
		Set<AbstractGameObject> vBirds = new HashSet<>(nBirds);
		int startY = gameController.getGameHeight() / 2 - FlappyBird.HEIGHT / 2;
		FlappyBird bird;
		
		for (int i = 0; i < nBirds; ++i) {
			bird = new FlappyBird(20, startY, bestBirdBrain);
			bird.getBrain().updateWeights();
			vBirds.add(bird);
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