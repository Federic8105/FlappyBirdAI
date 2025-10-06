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
	
	private static final Path AUTOSAVE_DIR = Path.of("autosaves");
	
	// Template per i nomi dei file da salvare
	private static final String AUTO_SAVE_FILENAME_TEMPLATE = "autosave_gen_%d_maxTubePassed_%d_BLT_%.2f_time_%s.json";
	private static final String MANUAL_SAVE_FILENAME_TEMPLATE = "brain_gen_%d_maxTubePassed_%d_BLT_%.2f_time_%s.json";
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final Random rand = new Random();
    
    private final GameView gameView;
    private final List<AbstractGameObject> vGameObj = new ArrayList<>();
    private final Map<String, Double> brainInputMap = new HashMap<>();
    
    // Game Engine Variables
    
    // Oggetto di Lock (usato solo per Lock) per Gestire la Pausa in Modo Thread-Safe
    // Usato come Monitor per wait() e notify() per la pausa del gioco
    private final Object pauseLock = new Object();
    
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
		// Delta Time del Gioco - Influenzato dal Dt Multiplier
		double dt;
		long sleepTime;
		List<Rectangle> vTubeHitBox;
		Tube previousFirstTopTube = getFirstTopTube(randBird);
		
		lastGameHeight = getGameHeight();
		
		// Avviare una nuova sessione a inizio gioco (prima generazione)
		if (isFirstGen()) {
			gameClock.startSession();
		}
		
		gameClock.setLastUpdateTimeNow();

		while (gameStats.nBirds > 0) {
			gameClock.setFrameStartTime();
			
			if (!gameClock.isGameRunning()) {
				
				synchronized (pauseLock) {
				
					// Aggiornare la vista per mostrare lo stato di pausa e animazioni
		            gameView.updateDisplay(gameClock, gameStats, new ArrayList<>(vGameObj));
					
					// Sleep per Ridurre l'Utilizzo della CPU Durante la Pausa
		            try {
		            	// Rilascia Momentaneamente il Lock per Permettere la Notifica di Ripresa
		            	// Thread si Sospende Qui Fino a Notifica o Timeout (dopo sleep di PAUSE_SLEEP_MS)
		            	pauseLock.wait(GameClock.PAUSE_SLEEP_MS);
		            } catch (InterruptedException e) {
		                throw new RuntimeException(e);
		            }
				}
	            
	            continue;
	        }

			// Calcolo del Tempo trascorso in Secondi tra Frames (Delta Time del Gioco - Influenzato dal Dt Multiplier)
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
			
			sleepTime = gameClock.setFrameEndTime();
			
			// Aggiornare Statistica FPS
			gameStats.fps = gameClock.getEMAFPS();
			
			// Aggiornare la Vista di Gioco
			// Nota: Si passa una Copia della Lista per Evitare ConcurrentModificationException (Thread-Safe)
            gameView.updateDisplay(gameClock, gameStats, new ArrayList<>(vGameObj));
            
            // Se sleepTime < 0, significa che il frame è durato più del tempo target, quindi non dormire per recuperare il ritardo
            if (sleepTime > 0) {
            	long sleepTimeMs = sleepTime / 1_000_000L;
				int sleepTimeNs = (int) (sleepTime % 1_000_000L);
				
				try {
					Thread.sleep(sleepTimeMs, sleepTimeNs);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
        }

		if (gameStats.nTubePassed > gameStats.maxTubePassed) {
			gameStats.maxTubePassed = gameStats.nTubePassed;
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
		if (!gameStats.isAutoSaveEnabled || bestBirdBrain == null) {
    		return;
    	}
		
		// Controllo autosave per generazione
    	if (gameStats.isAutoSaveOnGenEnabled && gameStats.nGen % gameStats.getAutoSaveGenThreshold() == 0) {
    		createAutoSaveFile();
    		// Solo un autosave per ciclo di gioco (frame)
    		return;
    	}
    	
    	// Controllo autosave per Best Life Time
    	if (gameStats.isAutoSaveOnBLTEnabled && gameStats.bestLifeTime > 0 && Math.floor(gameStats.bestLifeTime) % gameStats.getAutoSaveBLTThreshold() == 0) {
    		createAutoSaveFile();
    		return;
    	}
    	
    	// Controllo autosave per Max Tube Passed
    	if (gameStats.isAutoSaveOnMaxTubePassedEnabled && gameStats.maxTubePassed > 0 && gameStats.maxTubePassed % gameStats.getAutoSaveMaxTubePassedThreshold() == 0) {
    		createAutoSaveFile();
    		return;
    	}
	}
	
	private void createAutoSaveFile() throws IOException {
		// Creare la DIR Se Non Esiste
        try {
            Files.createDirectories(AUTOSAVE_DIR);
        } catch (IOException e) {
        	throw new IOException("Error Creating Autosaves Directory: " + e.getMessage(), e);
        }
    	
        String fileName = createAutoSaveFileName();
        Path fullPath = AUTOSAVE_DIR.resolve(fileName);
        
        try {
        	saveBestBrain(fullPath);
            gameView.showAutoSaveMessage("AUTO-SAVED!");
        } catch (IOException | NullPointerException e) {
            gameView.showAutoSaveMessage("AUTO-SAVE FAILED!");
            System.err.println("Error in Automatic Brain Save: " + e.getMessage());
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
	
	private String createAutoSaveFileName() {
	    String timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);
	    return String.format(AUTO_SAVE_FILENAME_TEMPLATE, gameStats.nGen, gameStats.maxTubePassed, gameStats.bestLifeTime, timestamp);
	}
	
	public String createManualSaveFileName() {
	    String timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);
	    return String.format(MANUAL_SAVE_FILENAME_TEMPLATE, gameStats.nGen, gameStats.maxTubePassed, gameStats.bestLifeTime, timestamp);
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
    
    public void setAutoSaveOnGenEnabled(boolean enabled) {
		gameStats.isAutoSaveOnGenEnabled = enabled;
	}
    
    public void setAutoSaveGenThreshold(int threshold) {
        gameStats.setAutoSaveGenThreshold(threshold);
    }
    
    public void setAutoSaveOnBLTEnabled(boolean enabled) {
    	gameStats.isAutoSaveOnBLTEnabled = enabled;
    }
    
    public void setAutoSaveBLTThreshold(int threshold) {
		gameStats.setAutoSaveBLTThreshold(threshold);
	}
    
    public void setAutoSaveOnMaxTubePassedEnabled(boolean enabled) {
		gameStats.isAutoSaveOnMaxTubePassedEnabled = enabled;
	}
    
    public void setAutoSaveMaxTubePassedThreshold(int threshold) {
		gameStats.setAutoSaveMaxTubePassedThreshold(threshold);
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
    
    public void togglePause() {
        if (gameClock.isGameRunning()) {
        	gameClock.pause();
        } else {
        	gameClock.resume();
        	
        	// Sbloccare subito il thread di gioco se in attesa senza aspettare il prossimo ciclo di sleep
        	synchronized (pauseLock) {
				pauseLock.notifyAll();
        	}
        }
        
        // Forzare l'aggiornamento del display per feedback visivo istantaneo
        gameView.repaintGame();
    }
    
}