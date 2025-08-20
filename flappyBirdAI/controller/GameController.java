/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.controller;

import flappyBirdAI.ai.BirdBrain;
import flappyBirdAI.model.AbstractGameObject;
import flappyBirdAI.model.FlappyBird;
import flappyBirdAI.model.GameObject;
import flappyBirdAI.model.Tube;
import flappyBirdAI.view.GameView;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.awt.Rectangle;

public class GameController {
	
	public static final int SLIDER_HEIGHT = 150, MAX_FPS = 80;
	private static final Path AUTOSAVE_DIR = Path.of("autosaves");
	
	// Template per i nomi dei file da salvare
	public static final String AUTO_SAVE_FILENAME_TEMPLATE = "autosave_gen_%d_score_%d_time_%s.json";
	public static final String MANUAL_SAVE_FILENAME_TEMPLATE = "brain_gen_%d_score_%d_time_%s.json";
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final Random rand = new Random();
    
    private final GameView gameView;
    private final List<AbstractGameObject> vGameObj = new ArrayList<>();
    private final int sleepMs = Math.round(1000 / (float) MAX_FPS), tubeHoleOffsetAbsValue = 180;
    
    // Game State
    private BirdBrain bestBirdBrain;
    private int nBirds = 0, nGen = 1, nTubePassed = 0, nMaxTubePassed = 0, autoSaveThreshold = 50;
    private double dtMultiplier = 1.0, bestLifeTime = 0;
    private boolean isGameRunning = false, isAutoSaveEnabled = true;
    
    // Game Statistics
    private GameStats currentGameStats = new GameStats();

	public GameController(GameView gameView) {
		this.gameView = gameView;
		this.gameView.setController(this);
        newTubes();
	}
	
	// Game Logic Methods
	
	public void addBirds(List<AbstractGameObject> vBirds) {
		vGameObj.addAll((Collection<AbstractGameObject>) vBirds);
		nBirds += vBirds.size();
		updateGameStats();
	}
	
	public void startMotion() {
		isGameRunning = true;
		List<Rectangle> vTubeHitBox;
		double dt, time, lastDt = System.nanoTime();

		FlappyBird randBird = getRandomBird();
		Tube previousFirstTopTube = getFirstTopTube(randBird);

		do {
			time = System.nanoTime();

			// Calcolo del Tempo trascorso in Secondi tra Frames
			dt = (time - lastDt) / 1e9 * dtMultiplier;
			lastDt = time;

			randBird = getRandomBird();
			
			// Ottenere Primo Tube Superiore a Destra
			Tube firstTopTube = getFirstTopTube(randBird);
			if (firstTopTube != null && !firstTopTube.equals(previousFirstTopTube)) {
				++nTubePassed;
			}
			previousFirstTopTube = firstTopTube;

			// Creazione vettore HitBox di Tube
			vTubeHitBox = new ArrayList<>();
			for (GameObject tempObj : vGameObj) {
				if (tempObj instanceof Tube currTube) {
					vTubeHitBox.add(currTube.getHitBox());
				}
			}
			
			// Aggiornare Oggetti di Gioco
            updateGameObjects(dt, vTubeHitBox, firstTopTube);

			checkNewTube();
			deleteDeadObjects();
			
			// Aggiornare Statistiche e UI
			currentGameStats.fps = (int) (1 / dt * dtMultiplier);
			updateGameStats();
			
			// Aggiornare la Vista di Gioco
			// Nota: Si passa una Copia della Lista per Evitare ConcurrentModificationException (Thread-Safe)
            gameView.updateDisplay(currentGameStats, new ArrayList<>(vGameObj));

			try {
				Thread.sleep(sleepMs);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}

        } while (nBirds > 0 && isGameRunning);

		if (nTubePassed > nMaxTubePassed) {
			nMaxTubePassed = nTubePassed;
		}
		
		nextGeneration();
        isGameRunning = false;
	}
	
	private void updateGameObjects(double dt, List<Rectangle> tubeHitBoxes, Tube firstTopTube) {
		
        for (GameObject obj : new ArrayList<>(vGameObj)) {
        	
        	// Stampa per Debug
        	//System.out.println(obj + System.lineSeparator());
        	
            if (obj instanceof FlappyBird currBird && currBird.isAlive) {
                
            	// Controllo Collisioni
                if (currBird.checkCollision(tubeHitBoxes.toArray(new Rectangle[0])) || currBird.y + FlappyBird.height < 0 || currBird.y > getGameHeight()) {
                    
                    // Nuovo Record
                    if (currBird.lifeTime > bestLifeTime) {
                        bestLifeTime = currBird.lifeTime;
                        bestBirdBrain = currBird.brain;
                    }
                    
                    currBird.isAlive = false;
                    --nBirds;
                    continue;
                    
                // AI Decision
                } else if (firstTopTube != null) {
                    currBird.brain.setInputs(new HashMap<String, Double>() {
                        private static final long serialVersionUID = 1L;
                        {
                            put("yBird", (double) currBird.y);
                            put("vyBird", currBird.vy);
                            put("yCenterTubeHole", (double) (firstTopTube.h + Tube.distYBetweenTubes / 2));
                            put("xDistBirdTube", (double) firstTopTube.x - currBird.x);
                        }
                    });
                    
                    if (currBird.think()) {
                        currBird.jump();
                    }
                }
                
                currBird.updateXY(dt);
                
            } else if (obj instanceof Tube currTube && currTube.isAlive) {
                if (currTube.x + currTube.w < 0) {
                    currTube.isAlive = false;
                } else {
                    currTube.updateXY(dt);
                }
            }
        }
    }
	
	private void deleteDeadObjects() {
        vGameObj.removeIf(obj -> !obj.isAlive);
	}
	
	private FlappyBird getRandomBird() {
        for (GameObject obj : vGameObj) {
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
		for (GameObject motObj : vGameObj) {
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
        for (AbstractGameObject obj : vGameObj) {
            if (obj instanceof Tube && obj.isAlive && ((Tube) obj).isSuperior) {
                lastTube = (Tube) obj;
            }
        }

		if (lastTube != null && lastTube.x + Tube.width <= gameView.getGameWidth() - Tube.distXBetweenTubes) {
			newTubes();
		} else if (lastTube == null) {
			newTubes();
		}
	}
	
	private void newTubes() {
		int tubeHoleOffset = rand.nextInt(-tubeHoleOffsetAbsValue, tubeHoleOffsetAbsValue + 1);
		int yTubeHoleCenter = (getGameHeight() / 2) + tubeHoleOffset;
		int upperTubeHeight = yTubeHoleCenter - Tube.distYBetweenTubes / 2;

		vGameObj.add(new Tube(gameView.getGameWidth(), 0, upperTubeHeight, true));
		vGameObj.add(new Tube(gameView.getGameWidth(), upperTubeHeight + Tube.distYBetweenTubes, getGameHeight() - upperTubeHeight - Tube.distYBetweenTubes, false));
	}
	
	private void nextGeneration() {
        ++nGen;
        
        // Salvataggio automatico
        if (isAutoSaveEnabled && bestBirdBrain != null && nGen % autoSaveThreshold == 0) {
        	// Creare la DIR Se Non Esiste
            try {
                Files.createDirectories(AUTOSAVE_DIR);
            } catch (IOException e) {
                System.err.println("Errore nella creazione della cartella autosaves: " + e.getMessage());
            }
        	
            String fileName = generateAutoSaveFileName();
            Path fullPath = AUTOSAVE_DIR.resolve(fileName);
            
            if (saveBestBrain(fullPath)) {
                gameView.showAutoSaveMessage("AUTO-SAVED!");
                System.out.println("Brain salvato automaticamente: " + fullPath);
            } else {
                gameView.showAutoSaveMessage("AUTO-SAVE FAILED!");
                System.err.println("Errore nel salvataggio automatico del cervello");
            }
        }
    }

	public void reset() {
		isGameRunning = false;
		nBirds = 0;
		nTubePassed = 0;
		vGameObj.clear();
		newTubes();
		updateGameStats();
	}
	
	public void resetToFirstGeneration() {
		isGameRunning = false;
		nBirds = 0;
		nTubePassed = 0;
		vGameObj.clear();
		
		nGen = 1;
		bestLifeTime = 0;
		nMaxTubePassed = 0;
		bestBirdBrain = null;
		newTubes();
		updateGameStats();
	}
	
	private void updateGameStats() {
        currentGameStats.nBirds = nBirds;
        currentGameStats.nGen = nGen;
        currentGameStats.bestLifeTime = bestLifeTime;
        currentGameStats.nTubePassed = nTubePassed;
        currentGameStats.nMaxTubePassed = nMaxTubePassed;
        currentGameStats.autoSaveThreshold = autoSaveThreshold;
        currentGameStats.isAutoSaveEnabled = isAutoSaveEnabled;
    }
	
	// Import/Export Methods
	
	private String generateAutoSaveFileName() {
	    String timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);
	    return String.format(AUTO_SAVE_FILENAME_TEMPLATE, nGen, nMaxTubePassed, timestamp);
	}
	
	public String generateManualSaveFileName() {
	    String timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);
	    return String.format(MANUAL_SAVE_FILENAME_TEMPLATE, nGen, nMaxTubePassed, timestamp);
	}
	
	public boolean saveBestBrain(Path file) {
		if (bestBirdBrain == null) {
			System.err.println("Nessun cervello migliore disponibile per il salvataggio");
			return false;
		}
		
		return bestBirdBrain.saveToFile(file);
	}
	
	public boolean loadBrain(String filePath) {
		try {
			BirdBrain loadedBrain = BirdBrain.loadFromFile(Path.of(filePath));
			if (loadedBrain != null) {
				bestBirdBrain = loadedBrain;
				resetToFirstGeneration();
				System.out.println("Cervello caricato con successo da: " + filePath);
				return true;
			}
			return false;
		} catch (IOException e) {
			System.err.println("Errore nel caricamento del cervello: " + e.getMessage());
			return false;
		}
	}
	
	public Optional<String> exportBestBrainAsJson() {
		// Controllare se il cervello migliore è disponibile (!= null) e convertirlo in JSON
		return Optional.ofNullable(bestBirdBrain).map(BirdBrain::toJson);
	}
	
	// Getters and Setters - API Methods
	
	public int getGameHeight() {
		return gameView.getGameHeight();
	}
	
	public int getGameWidth() {
		return gameView.getGameWidth();
	}
	
	public void setDtMultiplier(double multiplier) {
        this.dtMultiplier = multiplier;
    }
    
    public double getDtMultiplier() {
        return dtMultiplier;
    }
    
    public BirdBrain getBestBirdBrain() {
        return bestBirdBrain;
    }
    
    public void setBestBirdBrain(BirdBrain brain) {
        this.bestBirdBrain = brain;
    }
    
    public void toggleAutoSave() {
    	isAutoSaveEnabled = !isAutoSaveEnabled;
        currentGameStats.isAutoSaveEnabled = isAutoSaveEnabled;
    }
    
    public void setAutoSaveThreshold(int threshold) {
        if (threshold > 0) {
            this.autoSaveThreshold = threshold;
        }
    }
    
    public boolean isAutoSaveEnabled() {
        return isAutoSaveEnabled;
    }
    
    public int getAutoSaveThreshold() {
        return autoSaveThreshold;
    }
    
    public void stopGame() {
        isGameRunning = false;
    }
    
    public GameStats getCurrentStats() {
        return currentGameStats;
    }
    
}