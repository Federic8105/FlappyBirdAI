/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.view;

import flappyBirdAI.ai.BirdBrain;
import flappyBirdAI.model.AbstractGameObjec;
import flappyBirdAI.model.FlappyBird;
import flappyBirdAI.model.GameObject;
import flappyBirdAI.model.Tube;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class FlappyBirdPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private final Random rand = new Random();

	public static int width = 0, height = 0, sliderH = 150, gameScreenH;
	public static final int maxFPS = 80;

	public final List<AbstractGameObjec> vObj = new ArrayList<>();
	private final int sleep_ms = Math.round(1000 / (float) maxFPS), tubeHoleOffsetABSValue = 180;

	public double dtMultiplier = 1;
	public BirdBrain bestBirdBrain;

	private int nBirds = 0, nGen = 1, nMaxTubePassed = 0;
	private double bestLifeTime = 0;

	private JLabel lCurrFPS, lBestLifeTime, lNGen, lNBirds, lNTubePassed, lMaxTubePassed;
	private JSlider sl;

	public FlappyBirdPanel() {
		if (FlappyBirdPanel.width == 0 || FlappyBirdPanel.height == 0) {
			throw new RuntimeException("FlappyBirdPanel Width and Height Not Set");
		}
		gameScreenH = height - sliderH;

		setBounds(0,0,FlappyBirdPanel.width,FlappyBirdPanel.height);
		setBackground(Color.cyan);
		setLayout(null);

		addCurrFPSLabel();
		addBestLifeTimeLabel();
		addNGenLabel();
		addNBirdsLabel();
		addNTubePassedLabel();
		addMaxTubePassedLabel();
		addSlider();
		
		newTubes();
	}

	private void addCurrFPSLabel() {
		lCurrFPS = new JLabel("FPS: 0");
		lCurrFPS.setBounds(0, 0, 55, 20);
		lCurrFPS.setOpaque(true);
		lCurrFPS.setBackground(Color.green);
		lCurrFPS.setForeground(Color.red);
		lCurrFPS.setBorder(BorderFactory.createLineBorder(Color.red));
		add(lCurrFPS);
	}
	
	private void updateCurrFPS(double dt) {
		lCurrFPS.setText("FPS: " + (int) (1 / dt * dtMultiplier));
    }

	private void addBestLifeTimeLabel() {
		lBestLifeTime = new JLabel("BLT: 0.000s");
		lBestLifeTime.setBounds(0, 20, 90, 20);
		lBestLifeTime.setOpaque(true);
		lBestLifeTime.setBackground(Color.green);
		lBestLifeTime.setForeground(Color.red);
		lBestLifeTime.setBorder(BorderFactory.createLineBorder(Color.red));
		add(lBestLifeTime);
	}
	
	private void updateBestLifeTimeLabel() {
		lBestLifeTime.setText("BLT: " + String.format("%.3f", bestLifeTime) + "s");
	}

	private void addNGenLabel() {
		lNGen = new JLabel("Gen: " + nGen);
		lNGen.setBounds(width - 85, 0, 70, 20);
		lNGen.setOpaque(true);
		lNGen.setBackground(Color.green);
		lNGen.setForeground(Color.red);
		lNGen.setBorder(BorderFactory.createLineBorder(Color.red));
		add(lNGen);
	}
	
	private void updateNGen() {
		++nGen;
		lNGen.setText("Gen: " + nGen);
	}

	private void addNBirdsLabel() {
		lNBirds = new JLabel("Birds: " + nBirds);
		lNBirds.setBounds(width - 85, 20, 70, 20);
		lNBirds.setOpaque(true);
		lNBirds.setBackground(Color.green);
		lNBirds.setForeground(Color.red);
		lNBirds.setBorder(BorderFactory.createLineBorder(Color.red));
		add(lNBirds);
	}
	
	private void updateNBirdsLabel(int nBirdsLeft) {
		lNBirds.setText("Birds: " + nBirdsLeft);
	}

	private void addNTubePassedLabel() {
		lNTubePassed = new JLabel("Tubes: 0");
		lNTubePassed.setBounds(width - 85, 40, 70, 20);
		lNTubePassed.setOpaque(true);
		lNTubePassed.setBackground(Color.green);
		lNTubePassed.setForeground(Color.red);
		lNTubePassed.setBorder(BorderFactory.createLineBorder(Color.red));
		add(lNTubePassed);
	}
	
	private void updateNTubePassedLabel(int nTubePassed) {
		lNTubePassed.setText("Tubes: " + nTubePassed);
		
		// Aggiornamento del massimo numero di tubi passati se nTubePassed > nMaxTubePassed
		if (nTubePassed > nMaxTubePassed) {
			nMaxTubePassed = nTubePassed;
			updateMaxTubePassedLabel();
		}
	}

	private void addMaxTubePassedLabel() {
		lMaxTubePassed = new JLabel("Max Tubes: 0");
		lMaxTubePassed.setBounds(width - 115, 60, 100, 20);
		lMaxTubePassed.setOpaque(true);
		lMaxTubePassed.setBackground(Color.green);
		lMaxTubePassed.setForeground(Color.red);
		lMaxTubePassed.setBorder(BorderFactory.createLineBorder(Color.red));
		add(lMaxTubePassed);
	}
	
	private void updateMaxTubePassedLabel() {
		lMaxTubePassed.setText("Max Tubes: " + nMaxTubePassed);
	}
	
	private void addSlider() {
		sl = new JSlider(JSlider.HORIZONTAL, 1, 15, 1);
		sl.setBounds(0, height - sliderH, width - 14, sliderH);
        sl.setPaintTicks(true);
        sl.setPaintLabels(true);
        sl.setSnapToTicks(true);
		sl.setMinorTickSpacing(1);
        sl.setMajorTickSpacing(1);
		sl.setBackground(Color.decode("#800020"));
		sl.addChangeListener(_ -> this.dtMultiplier = sl.getValue());

		TitledBorder sliderTitle = BorderFactory.createTitledBorder("Velocity Multiplier");
		sliderTitle.setTitleJustification(TitledBorder.CENTER);
		sliderTitle.setTitlePosition(TitledBorder.TOP);
		sliderTitle.setTitleFont(new Font("Arial", Font.BOLD, 20));
		sl.setBorder(sliderTitle);

		add(sl);
	}

	private void deleteDeadObjects() {
        vObj.removeIf(obj -> !obj.isAlive);
	}

	private FlappyBird getRandomBird() {
		for (GameObject obj : vObj) {
			if (obj instanceof FlappyBird currBird && currBird.isAlive) {
				return currBird;
			}
		}
		
		return null;
	}

	private Tube getFirstTopTube(FlappyBird currBird) {
		if (currBird == null) {
			return null;
		}

		Tube firstTopTube = null;
		for (GameObject motObj : vObj) {
			if (motObj instanceof Tube currTube) {
				if (firstTopTube == null || ( currTube.isAlive && currTube.isSuperior && currTube.x < firstTopTube.x && currTube.x >= currBird.x )) {
					firstTopTube = currTube;
				}
			}
		}
		
		return firstTopTube;
	}

	private void checkNewTube() {
		Tube lastTube = null;
        for (AbstractGameObjec obj : vObj) {
            if (obj instanceof Tube && obj.isAlive && ((Tube) obj).isSuperior) {
                lastTube = (Tube) obj;
            }
        }

		if (lastTube != null && lastTube.x + Tube.width <= getWidth() - Tube.distXBetweenTubes) {
			newTubes();
		} else if (lastTube == null) {
			newTubes();
		}
	}
	
	private void newTubes() {
		int tubeHoleOffset = rand.nextInt( - tubeHoleOffsetABSValue, tubeHoleOffsetABSValue + 1);
		int yTubeHoleCenter = (gameScreenH / 2) + tubeHoleOffset;
		int upperTubeHeight = yTubeHoleCenter - Tube.distYBetweenTubes / 2;

		vObj.add(new Tube(getWidth(), 0, upperTubeHeight, true));
		vObj.add(new Tube(getWidth(), upperTubeHeight + Tube.distYBetweenTubes, gameScreenH - upperTubeHeight - Tube.distYBetweenTubes, false));
	}
	
	@SuppressWarnings("unchecked")
	public void addBirds(List<GameObject> vBirds) {
		vObj.addAll((Collection<? extends AbstractGameObjec>) vBirds);
		nBirds += vBirds.size();
	}
	
	public void startMotion() {
		List<Rectangle> vTubeHitBox;
		double dt, time, last_dt = System.nanoTime();
		int nTubePassed = 0;

		FlappyBird randBird = getRandomBird();
		Tube previousFirstTopTube = getFirstTopTube(randBird);

		do {
			time = System.nanoTime();

			// Calcolo del Tempo trascorso in Secondi tra Frames
			dt = (time - last_dt) / 1e9 * dtMultiplier;
			last_dt = time;

			randBird = getRandomBird();
			// Ottenere Primo Tube Superiore a Destra
			Tube firstTopTube = getFirstTopTube(randBird);
			if (firstTopTube != null && !firstTopTube.equals(previousFirstTopTube)) {
				++nTubePassed;
			}
			previousFirstTopTube = firstTopTube;

			// Creazione vettore HitBox di Tube
			vTubeHitBox = new ArrayList<>();
			for (GameObject tempObj : vObj) {
				if (tempObj instanceof Tube currTube) {
					vTubeHitBox.add(currTube.getHitBox());
				}
			}

			for (GameObject obj : vObj) {
				
				//System.out.println(obj + System.lineSeparator());

				// Controllo Collisioni con Tube o Uscita da Schermo di FlappyBird
				if (obj instanceof FlappyBird currBird && currBird.isAlive) {

					if (currBird.checkCollision(vTubeHitBox.toArray(new Rectangle[0])) || currBird.y + FlappyBird.height < 0 || currBird.y > gameScreenH) {
						if (currBird.lifeTime > bestLifeTime) {
							bestLifeTime = currBird.lifeTime;
							bestBirdBrain = currBird.brain;
							updateBestLifeTimeLabel();
						}
						currBird.isAlive = false;
						--nBirds;
						continue;
					} else {
						currBird.brain.setInputs(new HashMap<>() {
							
							private static final long serialVersionUID = 1L;

						{
                            put("yBird", (double) currBird.y);
							put("vyBird", currBird.vy);
                            put("yCenterTubeHole", (double) (firstTopTube.h + Tube.distYBetweenTubes) - ((double) Tube.distYBetweenTubes / 2));
                            put("xDistBirdTube", (double) firstTopTube.x - currBird.x);
                        }});

						if (currBird.think()) {
							currBird.jump();
						}

						currBird.updateXY(dt);
					}

					continue;
				}

				// Controllo Tube uscito da schermo a sinistra altrimenti aggiornamento posizione
				if (obj instanceof Tube currTube && currTube.isAlive) {
					if (currTube.x + currTube.w < 0) {
						currTube.isAlive = false;
						continue;
					} else {
						currTube.updateXY(dt);
					}
				}
			}

			checkNewTube();

			deleteDeadObjects();

			updateCurrFPS(dt);
			updateNBirdsLabel(nBirds);
			updateNTubePassedLabel(nTubePassed);

			repaint();

			try {
				Thread.sleep(sleep_ms);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}

        } while (nBirds > 0);

		if (nTubePassed > nMaxTubePassed) {
			nMaxTubePassed = nTubePassed;
			updateMaxTubePassedLabel();
		}
		
		updateNGen();
	}

	public void reset() {
		nBirds = 0;
		vObj.clear();
		newTubes();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		//g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		List<GameObject> vObjCopy = new ArrayList<>(vObj);
		for (GameObject obj : vObjCopy) {
			obj.draw(g2d);
		}
	}
	
}