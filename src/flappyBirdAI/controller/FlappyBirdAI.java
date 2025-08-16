/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.controller;

import flappyBirdAI.ai.BirdBrain;
import flappyBirdAI.model.FlappyBird;
import flappyBirdAI.model.GameObject;
import flappyBirdAI.view.FlappyBirdPanel;
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
	private final FlappyBirdPanel p;

    public FlappyBirdAI(int w, int h, int nBirdsXGen) {
        setSize(w, h + 37);
		setTitle("Flappy Bird AI");
		setIconImage(new ImageIcon(getClass().getResource("/res/FB_ICON.png")).getImage());
		getContentPane().setBackground(Color.white);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setFocusable(false);
		setLayout(null);
		this.nBirdsXGen = nBirdsXGen;

		FlappyBirdPanel.width = w;
		FlappyBirdPanel.height = h;
		p = new FlappyBirdPanel();
		add(p);
	
		setVisible(true);

		startGame();
	}

	private List<GameObject> createRandomBirds(int nBirds) {
		List<GameObject> vBirds = new ArrayList<>();
		
		for (int i = 0; i < nBirds; ++i) {
			vBirds.add(new FlappyBird(20, FlappyBirdPanel.gameScreenH / 2 - FlappyBird.height / 2, new BirdBrain()));
		}
		
		return vBirds;
	}

	private List<GameObject> createBirds(int nBirds, BirdBrain bestBirdBrain) {
		List<GameObject> vBirds = new ArrayList<>();
		
		for (int i = 0; i < nBirds; ++i) {
			vBirds.add(new FlappyBird(20, FlappyBirdPanel.gameScreenH / 2 - FlappyBird.height / 2, bestBirdBrain));
			((FlappyBird) vBirds.get(i)).brain.updateWeights();
		}
		
		return vBirds;
	}

	private void startGame() {
		boolean isFirstGen = true;
		int nBirdsRegen = nBirdsXGen * 4 / 5;

		while (true) {

			if (isFirstGen) {
				p.addBirds(createRandomBirds(nBirdsXGen));
				isFirstGen = false;
			} else {
				p.addBirds(createBirds(nBirdsRegen, p.bestBirdBrain));
				p.addBirds(createRandomBirds(nBirdsXGen - nBirdsRegen));
			}

			p.startMotion();

			p.reset();
		}
	}

}