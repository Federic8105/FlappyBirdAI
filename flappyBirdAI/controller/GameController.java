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
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Random;
import java.util.Optional;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.awt.Rectangle;

public final class GameController {
	
	private static final int MAX_FPS = 90;
	private static final long TARGET_FRAME_TIME_NS = 1_000_000_000L / MAX_FPS;
	private static final int PAUSE_SLEEP_MS = 100;
	private static final Path AUTOSAVE_DIR = Path.of("autosaves");
	
	// Template per i nomi dei file da salvare
	private static final String AUTO_SAVE_FILENAME_TEMPLATE = "autosave_gen_%d_score_%d_time_%s.json";
	private static final String MANUAL_SAVE_FILENAME_TEMPLATE = "brain_gen_%d_score_%d_time_%s.json";
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final Random rand = new Random();
    
    private final GameView gameView;
    private final List<AbstractGameObject> vGameObj = new ArrayList<>();
    private final Map<String, Double> brainInputMap = new HashMap<>();
    
    // Game Engine Variables
    
    // Game Statistics
    private final GameStats gameStats = new GameStats();
    
    // Game Clock
    private final GameClock gameClock = new GameClock();
    
    private int lastGameHeight;
    private BirdBrain bestBirdBrain;

	public GameController(GameView gameView) throws NullPointerException {
		this.gameView = Objects.requireNonNull(gameView, "GameView Cannot be Null");
		gameView.setController(this);
		gameClock.start();
		newTubePair();
	}
	
	// Game Logic Methods
	
	public void addBirds(List<AbstractGameObject> vBirds) throws NullPointerException {
		Objects.requireNonNull(vBirds, "Birds List Cannot be Null");
		
		vGameObj.addAll((Collection<AbstractGameObject>) vBirds);
		gameStats.nBirds += vBirds.size();
	}
	
	public void startGame() throws NullPointerException, RuntimeException {
		FlappyBird randBird = Objects.requireNonNull(getRandomBird(), "No Alive Birds to Start the Game, There Must Be at Least One Alive Bird");
		
		int gameHeight;
		double dt;
		List<Rectangle> vTubeHitBox;
		Tube previousFirstTopTube = getFirstTopTube(randBird);
		
		lastGameHeight = getGameHeight();
		
		// Avviare una nuova sessione a inizio gioco (prima generazione)
		if (isFirstGen()) {
			gameClock.startSession();
		}
		
		gameClock.setLastTimeNow();

		while (gameStats.nBirds > 0) {
			//TODO
			long frameStartTime = System.nanoTime();
			
			if (!gameClock.isGameRunning()) {
				
				// Sleep per Ridurre l'Utilizzo della CPU Durante la Pausa
	            try {
	                Thread.sleep(PAUSE_SLEEP_MS);
	            } catch (InterruptedException e) {
	                throw new RuntimeException(e);
	            }
	            
	            // Aggiornare la vista per mostrare lo stato di pausa e animazioni
	            gameView.updateDisplay(gameClock, gameStats, new ArrayList<>(vGameObj));
	            continue;
	        }

			// Calcolo del Tempo trascorso in Secondi tra Frames
			dt = gameClock.getDeltaTime();
			
			// Controllo se l'Altezza della Finestra di Gioco è Cambiata
			if (lastGameHeight != (gameHeight = getGameHeight())) {
				// Ricreare tutti i Tube con la Nuova Altezza
				recreateTubes();
				lastGameHeight = gameHeight;
			}
			
			// Ottenere Primo Tube Superiore a Destra
			Tube firstTopTube = getFirstTopTube(getRandomBird());
			if (firstTopTube != null && !firstTopTube.equals(previousFirstTopTube)) {
				++gameStats.nTubePassed;
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
			gameStats.fps = updateFPS(dt);
			
			//TODO non aggiorna timer a fine gen
			// Aggiornare la Vista di Gioco
			// Nota: Si passa una Copia della Lista per Evitare ConcurrentModificationException (Thread-Safe)
            gameView.updateDisplay(gameClock, gameStats, new ArrayList<>(vGameObj));
            
            //TODO
            long frameEnd = System.nanoTime();
            long frameDuration = frameEnd - frameStartTime;
            long sleepTime = TARGET_FRAME_TIME_NS - frameDuration;
            
            //TODO
            if (sleepTime > 0) {
				try {
					
					long sleepTimeMs = sleepTime / 1_000_000L;
					int sleepTimeNs = (int) (sleepTime % 1_000_000L);
					
					if (sleepTimeMs > 0 || sleepTimeNs > 0) {
						Thread.sleep(sleepTimeMs, sleepTimeNs);
					} else {
						// Yield per frame troppo lunghi
						Thread.yield();
					}
					
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
        }

		if (gameStats.nTubePassed > gameStats.nMaxTubePassed) {
			gameStats.nMaxTubePassed = gameStats.nTubePassed;
		}
		
		try {
			prepareNextGen();
		} catch (IOException e) {
			System.err.println("Error Starting Next Generation: " + e.getMessage());
		}
	}
	
	private void recreateTubes() {
		List<Tube> newTubes = new ArrayList<>();
		
		for (AbstractGameObject obj : vGameObj) {
			if (obj instanceof Tube currTube && currTube.isAlive && currTube.isSuperior()) {
				newTubes.addAll(Tube.createTubePair(currTube.x, getGameHeight(), rand));
			}
		}
		
		// Rimuovere tutti i Tube esistenti e aggiungere i nuovi Tube
		vGameObj.removeIf(obj -> obj instanceof Tube);
		vGameObj.addAll(newTubes);
	}
	
	private void updateGameObjects(double dt, List<Rectangle> tubeHitBoxes, Tube firstTopTube) {
		
        for (GameObject obj : new ArrayList<>(vGameObj)) {
        	
            if (obj instanceof FlappyBird currBird && currBird.isAlive) {
            	
            	if (currBird.lifeTime > gameStats.currLifeTime) {
                	gameStats.currLifeTime = currBird.lifeTime;
                	
                	// Nuovo Record di Vita
                	if (gameStats.currLifeTime > gameStats.bestLifeTime) {
						gameStats.bestLifeTime = currBird.lifeTime;
						bestBirdBrain = currBird.getBrain();
					}
                }
                
            	// Controllo Collisioni e Limiti Schermo - Flappy Bird Morto
                if (currBird.checkCollision(tubeHitBoxes.toArray(new Rectangle[0])) || currBird.y + currBird.h < 0 || currBird.y > getGameHeight()) {
                    
                    currBird.isAlive = false;
                    --gameStats.nBirds;
                    continue;
                    
                // AI Decision
                } else if (firstTopTube != null) {
                	
                	brainInputMap.put("yBird", (double) currBird.y);
                	brainInputMap.put("vyBird", currBird.vy);
                	brainInputMap.put("yCenterTubeHole", (double) (firstTopTube.h + Tube.DIST_Y_BETWEEN_TUBES / 2));
                	brainInputMap.put("xDistBirdTube", (double) firstTopTube.x - currBird.x);

                	currBird.getBrain().setInputs(brainInputMap);
                    
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
	
	private int updateFPS(double dt) {
		return (int) (1 / dt * gameClock.getDtMultiplier());
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
				if (firstTopTube == null || ( currTube.isAlive && currTube.isSuperior() && currTube.x < firstTopTube.x && currTube.x >= currBird.x )) {
					firstTopTube = currTube;
				}
			}
		}
		
		return firstTopTube;
	}

	private void checkNewTube() {
		Tube lastTube = null;
        for (AbstractGameObject obj : vGameObj) {
        	// Controllo anche se il tubo è il superiore per non pendere stessa coppia 2 volte perchè hanno stessa x
            if (obj instanceof Tube && obj.isAlive && ((Tube) obj).isSuperior()) {
                lastTube = (Tube) obj;
            }
        }

		if (lastTube != null && lastTube.x + Tube.WIDTH <= getGameWidth() - Tube.DIST_X_BETWEEN_TUBES) {
			newTubePair();
		} else if (lastTube == null) {
			newTubePair();
		}
	}
	
	private void newTubePair() {
		List<Tube> newTubePair = Tube.createTubePair(getGameWidth(), getGameHeight(), rand);
	    vGameObj.addAll(newTubePair);
	}
	
	private void prepareNextGen() throws IOException {
		checkAndAutoSave();
        resetForNewGen();
    }
	
	private void checkAndAutoSave() throws IOException {
		if (gameStats.isAutoSaveEnabled && bestBirdBrain != null && gameStats.nGen % gameStats.autoSaveThreshold == 0) {
			
        	// Creare la DIR Se Non Esiste
            try {
                Files.createDirectories(AUTOSAVE_DIR);
            } catch (IOException e) {
            	throw new IOException("Error Creating Autosaves Directory: " + e.getMessage(), e);
            }
        	
            String fileName = generateAutoSaveFileName();
            Path fullPath = AUTOSAVE_DIR.resolve(fileName);
            
            try {
            	saveBestBrain(fullPath);
                gameView.showAutoSaveMessage("AUTO-SAVED!");
            } catch (IOException | NullPointerException e) {
                gameView.showAutoSaveMessage("AUTO-SAVE FAILED!");
                System.err.println("Error in Automatic Brain Save: " + e.getMessage());
            }
        }
	}

	private void resetForNewGen() {
		++gameStats.nGen;
		gameStats.nBirds = 0;
		gameStats.nTubePassed = 0;
		gameStats.currLifeTime = 0;
		vGameObj.clear();
		newTubePair();
	}
	
	private void resetToFirstGen() {
		gameStats.resetToFirstGen();
		gameClock.reset();
		
		vGameObj.clear();
		bestBirdBrain = null;
		
		newTubePair();
	}
	
	// Import/Export Methods
	
	private String generateAutoSaveFileName() {
	    String timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);
	    return String.format(AUTO_SAVE_FILENAME_TEMPLATE, gameStats.nGen, gameStats.nMaxTubePassed, timestamp);
	}
	
	public String generateManualSaveFileName() {
	    String timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);
	    return String.format(MANUAL_SAVE_FILENAME_TEMPLATE, gameStats.nGen, gameStats.nMaxTubePassed, timestamp);
	}
	
	public void saveBestBrain(Path file) throws NullPointerException, IOException {
		Objects.requireNonNull(bestBirdBrain, "No Best Bird Brain Available for Saving");
		
		bestBirdBrain.saveToFile(file);
	}
	
	public void loadBrain(String filePath) throws NullPointerException, IOException, IllegalArgumentException, InvalidPathException {
		Objects.requireNonNull(filePath, "File Path Cannot be Null");
		
		try {
			bestBirdBrain = BirdBrain.loadFromFile(Path.of(filePath));
			resetToFirstGen();
		} catch (IOException e) {
			throw e;
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
    
    public BirdBrain getBestBirdBrain() {
        return bestBirdBrain;
    }
    
    public void setBestBirdBrain(BirdBrain brain) {
        bestBirdBrain = brain;
    }
    
    public boolean isAutoSaveEnabled() {
        return gameStats.isAutoSaveEnabled;
    }
    
    public void toggleAutoSave() {
    	gameStats.isAutoSaveEnabled = !gameStats.isAutoSaveEnabled;
    }
    
    public int getAutoSaveThreshold() {
        return gameStats.autoSaveThreshold;
    }
    
    public void setAutoSaveThreshold(int threshold) {
        if (threshold > 0) {
        	gameStats.autoSaveThreshold = threshold;
        }
    }
    
    public boolean isFirstGen() {
		return gameStats.isFirstGen();
	}
    
    public boolean isGameRunning() {
		return gameClock.isGameRunning();
    }
    
    public void setDtMultiplier(double multiplier) {
		gameClock.setDtMultiplier(multiplier);
	}
    
    //TODO sbloccare subito sleep di pausa
    public void togglePause() {
        if (gameClock.isGameRunning()) {
        	gameClock.pause();
        } else {
        	gameClock.resume();
        }
        
        // Forzare l'aggiornamento del display per feedback visivo istantaneo
        gameView.repaintGame();
    }
    
}