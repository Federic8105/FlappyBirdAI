/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.controller;

import flappyBirdAI.ai.BirdBrain;
import flappyBirdAI.model.FlappyBird;
import flappyBirdAI.model.GameObject;
import flappyBirdAI.view.SwingGameView;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FlappyBirdAI extends JFrame {

	public static void main(String[] args) {
		int w = 1050, h = 750, nBirdsXGen = 1000;
		new FlappyBirdAI(w, h, nBirdsXGen);
	}
	
	private static final long serialVersionUID = 1L;

	private final int nBirdsXGen;
	private final SwingGameView gameView;
	private final GameController gameController;

    public FlappyBirdAI(int w, int h, int nBirdsXGen) {
    	this.nBirdsXGen = nBirdsXGen;
    	
        setSize(w, h + 37);
		setTitle("Flappy Bird AI");
		setIconImage(new ImageIcon(getClass().getResource("/res/FB_ICON.png")).getImage());
		getContentPane().setBackground(Color.white);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setFocusable(false);
		setLayout(null);
		
		gameView = new SwingGameView(w, h);
		gameController = new GameController(gameView);
		add(gameView);
	
		setVisible(true);

		startGame();
	}

	private List<GameObject> createRandomBirds(int nBirds) {
		List<GameObject> vBirds = new ArrayList<>();
		
		for (int i = 0; i < nBirds; ++i) {
			vBirds.add(new FlappyBird(20, GameController.GAME_SCREEN_HEIGHT / 2 - FlappyBird.height / 2, new BirdBrain()));
		}
		
		return vBirds;
	}

	private List<GameObject> createBirds(int nBirds, BirdBrain bestBirdBrain) {
		List<GameObject> vBirds = new ArrayList<>();
		
		for (int i = 0; i < nBirds; ++i) {
			vBirds.add(new FlappyBird(20, GameController.GAME_SCREEN_HEIGHT / 2 - FlappyBird.height / 2, bestBirdBrain));
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