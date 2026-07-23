/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.controller;

import flappyBirdAI.ai.BirdBrain;
import flappyBirdAI.model.AbstractGameObject;
import flappyBirdAI.model.entities.FlappyBird;
import flappyBirdAI.model.entities.TubePair;
import flappyBirdAI.view.GameView;
import javafx.application.Platform;
import flappyBirdAI.persistence.BadFileFormatException;
import flappyBirdAI.persistence.BirdBrainFileStorage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.Optional;
import java.util.Map;
import java.util.Objects;
import java.util.HashMap;
import java.util.HashSet;
import java.awt.Rectangle;

public final class GameController {
	
	public static final int MIN_N_BIRDS_X_GEN = 1;
	public static final int MAX_N_BIRDS_X_GEN = 100000;
    
    private final GameView gameView;
    private final Set<AbstractGameObject> vGameObj;
    private final Map<String, Double> brainInputMap = new HashMap<>(BirdBrain.NUM_INPUT);
    
    // Game Engine Variables
    
    // Oggetto di Lock per la Sincronizzazione di Accesso a Variabili Condivise tra Thread (Game Thread e GUI Thread)
    private final Object lock = new Object();
    
    // Oggetto di Lock per Gestire la Pausa in Modo Thread-Safe
    // Usato come Monitor per wait() e notify() per la pausa del gioco
    private final Object pauseLock = new Object();
    
    // Game Statistics
    private final GameStats gameStats = new GameStats();
    
    // Game Clock
    private final GameClock gameClock = new GameClock();
    
    private final int nBirdsXGen, nBirdsRegen;
    private int lastGameHeight;
    private Optional<BirdBrain> bestBirdBrainOpt = Optional.empty();
    
    // Flag per Richiesta di Reset del Gioco per Caricamento Cervello da File
    private boolean brainLoadRequest = false;

	public GameController(GameView gameView, int nBirdsXGen, int birdsRegenPerc) throws NullPointerException, IllegalArgumentException {
		this.gameView = Objects.requireNonNull(gameView, "GameView Cannot be Null");
		if (nBirdsXGen < MIN_N_BIRDS_X_GEN) {
			throw new IllegalArgumentException("Number of Birds per Generation Must Be Greater than 0");
		}
		if (birdsRegenPerc < 0 || birdsRegenPerc > 100) {
	        throw new IllegalArgumentException("Birds Regeneration Percentage Must Be Between 0 and 100");
	    }
		
		this.nBirdsXGen = nBirdsXGen;
		this.nBirdsRegen = (int) (nBirdsXGen * (birdsRegenPerc / 100.0));
		
		vGameObj = new HashSet<>(nBirdsXGen + 15); // Capacità Iniziale Stimata (nBirds + TubePairs)
		gameView.setController(this);
		gameClock.start();
	}
	
	// Game Logic Methods
	
	public void playOneGen() throws RuntimeException {		
		int gameHeight;
		// Delta Time del Gioco - Influenzato dal Dt Multiplier
		double dt;
		long sleepTime;
		boolean isGameRunning;
		Optional<TubePair> firstTubePairOpt;
		TubePair previousFirstTubePair = null, currTargetTubePair;
		Optional<FlappyBird> randBirdOpt;
		FlappyBird randBird;
		// Copia Snapshot per Thread-Safety
		Set<AbstractGameObject> vGameObjSnapshot;
		
		lastGameHeight = getGameHeight();
		
		synchronized (lock) {
			// Avviare una nuova sessione a inizio gioco (prima generazione)
			if (isFirstGen()) {
				gameClock.startSession();
				// Aggiungere Uccelli alla Prima Generazione
				addFirstGenBirds();
			}
			addNewTubePair();
			vGameObjSnapshot = new HashSet<>(vGameObj);
			gameClock.setLastUpdateTimeNow();
		}
		
		// Precaricare le Sprite per Migliorare le Prestazioni di Rendering
		// Fuori da synchronized per Evitare di Bloccare il Thread di Gioco durante il Preload di Sprite I/O
		gameView.preloadSprites(vGameObjSnapshot);

		while (true) {
			synchronized (lock) {
				// Controllo di Uscita dal Ciclo di Gioco
				if (gameStats.nBirds == 0 || brainLoadRequest) {
					break;
				}
				
				gameClock.setFrameStartTime();
				isGameRunning = gameClock.isGameRunning();
			}
			
			if (!isGameRunning) {
				
				// Acquisizione del Lock per la Pausa
				synchronized (pauseLock) {
					
					synchronized (lock) {
						// Aggiornare la vista per mostrare lo stato di pausa e animazioni
			            gameView.updateDisplay(gameClock, gameStats, new HashSet<>(vGameObj));
					}
					
					// Sleep per Ridurre l'Utilizzo della CPU Durante la Pausa
		            try {
		            	// Rilascia Momentaneamente il Lock per Permettere la Notifica di Ripresa
		            	// Thread si Sospende Qui Fino a Notifica o Timeout (dopo sleep di PAUSE_SLEEP_MS)
		            	pauseLock.wait(GameClock.PAUSE_SLEEP_MS);
		            } catch (InterruptedException e) {
		                throw new RuntimeException("Game Thread Interrupted During Pause: " + e.getMessage(), e);
		            }
				}
	            
	            continue;
	        }
			
			synchronized (lock) {

				// Calcolo del Tempo trascorso in Secondi tra Frames (Delta Time del Gioco - Influenzato dal Dt Multiplier)
				dt = gameClock.getDeltaTime();
				
				// Controllo se l'Altezza della Finestra di Gioco è Cambiata
				if (lastGameHeight != (gameHeight = getGameHeight())) {
					// Ricreare tutti i Tube con la Nuova Altezza
					recreateTubePairs();
					lastGameHeight = gameHeight;
				}
				
				randBirdOpt = getRandomBird();
				
				// Aggiornare Statistica Tempo di Vita Attuale, Migliore e Cervello del Miglior Uccello
	        	if (randBirdOpt.isPresent() && (randBird = randBirdOpt.get()).lifeTime > gameStats.currLifeTime) {
	        		gameStats.currLifeTime = randBird.lifeTime;
	            	
	            	// Nuovo Record di Vita
	            	if (gameStats.currLifeTime > gameStats.bestLifeTime) {
						gameStats.bestLifeTime = randBird.lifeTime;
						bestBirdBrainOpt = Optional.of(randBird.getBrain());
					}
	            }
	        	
	        	firstTubePairOpt = getFirstTubePair(randBirdOpt);
				if (firstTubePairOpt.isPresent()) {
					currTargetTubePair = firstTubePairOpt.get();
					
					if (previousFirstTubePair == null) {
						previousFirstTubePair = currTargetTubePair;
					} else if (!currTargetTubePair.equals(previousFirstTubePair)) {
						++gameStats.nTubePassed;
						
						if (gameStats.nTubePassed > gameStats.maxTubePassed) {
				            gameStats.maxTubePassed = gameStats.nTubePassed;
				        }
						
				        previousFirstTubePair = currTargetTubePair;
					}
				}
				
				// Aggiornare Oggetti di Gioco
	            updateGameObjects(dt, getTubeHitBoxes(firstTubePairOpt), firstTubePairOpt);
	            deleteDeadGameObjects();
				checkNewTube();
				
				sleepTime = gameClock.setFrameEndTime();
				
				// Aggiornare Statistica FPS
				gameStats.fps = gameClock.getEMAFPS();
				
				// Aggiornare la Vista di Gioco
				// Nota: Si passa una Copia della Lista per Evitare ConcurrentModificationException (Thread-Safe)
	            gameView.updateDisplay(gameClock, gameStats, new HashSet<>(vGameObj));
	            
				checkAndAutoSaveInGen();
			}
            
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
		
		synchronized (lock) {
			if (brainLoadRequest) {
				brainLoadRequest = false;
			    prepareForLoadedBrain();
			} else {
				checkAndAutoSaveOnEndGen();
				prepareForNewGen();
			}
		}
	}
	
	public void resetGame() {	
		synchronized (lock) {
	        gameStats.resetToFirstGen();
	        gameClock.reset();
	        
	        vGameObj.clear();
	        bestBirdBrainOpt = Optional.empty();
	        
	        addNewTubePair();
	    }
	}
	
	private Rectangle[] getTubeHitBoxes(Optional<TubePair> firstTubePairOpt) {
		return firstTubePairOpt.isPresent() ? firstTubePairOpt.get().getHitBox() : new Rectangle[0];
	}
	
	private void recreateTubePairs() {
		Set<TubePair> newTubePairs = new HashSet<>(15);
		double holeRatio;
		for (AbstractGameObject obj : vGameObj) {
			if (obj instanceof TubePair currTubePair && currTubePair.isAlive()) {
				// Mantenere la posizione relativa del buco rispetto alla vecchia altezza
				holeRatio = (double) currTubePair.getYTubeHoleCenter() / lastGameHeight;
				newTubePairs.add(new TubePair(currTubePair.x, getGameHeight(), holeRatio));
			}
		}
		
		// Rimuovere tutti i TubePair esistenti e aggiungere i nuovi
		vGameObj.removeIf(obj -> obj instanceof TubePair);
		vGameObj.addAll(newTubePairs);
	}

	private void updateGameObjects(double dt, Rectangle[] tubeHitBoxes, Optional<TubePair> firstTubePairOpt) {
        for (AbstractGameObject obj : vGameObj) {
        	
            if (obj instanceof FlappyBird currBird && currBird.isAlive()) {
                
            	// Controllo Collisioni e Limiti Schermo - Flappy Bird Morto
                if (currBird.checkCollision(tubeHitBoxes) || currBird.isOutOfScreen(getGameWidth(), getGameHeight())) {
                    currBird.setAlive(false);
        			--gameStats.nBirds;
                    continue;
                    
                // AI Decision
                } else if (firstTubePairOpt.isPresent()) {
                	//Tube firstTopTube = firstTopTubeOpt.get();
                	
                	TubePair firstTubePair = firstTubePairOpt.get();
                	
                	brainInputMap.put("yBird", (double) currBird.y);
                	brainInputMap.put("vyBird", currBird.vy);
                	brainInputMap.put("yCenterTubeHole", (double) firstTubePair.getYTubeHoleCenter());
                	brainInputMap.put("xDistBirdTube", (double) firstTubePair.x - currBird.x);

                	currBird.getBrain().setInputs(brainInputMap);
                    
                    if (currBird.think()) {
                        currBird.jump();
                    }
                }
                
                currBird.updateXY(dt);
                
            } else if (obj instanceof TubePair currTubePair && currTubePair.isAlive()) {
            	// Rimuovere i Tube che sono usciti dallo schermo           
                if (currTubePair.isOutOfScreen(getGameWidth(), getGameHeight())) {
                    currTubePair.setAlive(false);
                } else {
                    currTubePair.updateXY(dt);
                }
            }
        }
    }
	
	private void deleteDeadGameObjects() {
		vGameObj.removeIf(obj -> !obj.isAlive());
	}
	
	private Optional<FlappyBird> getRandomBird() {
        for (AbstractGameObject obj : vGameObj) {
            if (obj instanceof FlappyBird currBird && currBird.isAlive()) {
                return Optional.of(currBird);
            }
        }
        return Optional.empty();
    }
	
	private Optional<TubePair> getFirstTubePair(Optional<FlappyBird> birdOpt) {
		if (birdOpt.isEmpty()) {
			return Optional.empty();
		}
		
		FlappyBird bird = birdOpt.get();
		TubePair firstTubePair = null;
		
		for (AbstractGameObject obj : vGameObj) {
			if (obj instanceof TubePair currTubePair && currTubePair.isAlive()) {
				if (firstTubePair == null || (currTubePair.x < firstTubePair.x && (currTubePair.x + TubePair.WIDTH) >= bird.x)) {
					firstTubePair = currTubePair;
				}
			}
		}
		
		return Optional.ofNullable(firstTubePair);
	}

	private void checkNewTube() {
		TubePair lastTubePair = null;
		
        for (AbstractGameObject obj : vGameObj) {
            if (obj instanceof TubePair currTubePair && currTubePair.isAlive()) {
            	// Tenere il TubePair con la x più grande (il più a destra)
            	if (lastTubePair == null || currTubePair.x > lastTubePair.x) {
            		lastTubePair = currTubePair;
            	}
            }
        }

		if (lastTubePair != null && lastTubePair.x + TubePair.WIDTH <= getGameWidth() - TubePair.DIST_X_BETWEEN_TUBES) {
			addNewTubePair();
		} else if (lastTubePair == null) {
			addNewTubePair();
		}
	}
	
	private void addBirds(Set<AbstractGameObject> vBirds) {
		vGameObj.addAll(vBirds);
		gameStats.nBirds += vBirds.size();
	}
	
	private Set<AbstractGameObject> createRandomBirds(int nBirds) {
		Set<AbstractGameObject> vBirds = new HashSet<>(nBirds);
		int startY = getGameHeight() / 2 - FlappyBird.HEIGHT / 2;
		
		for (int i = 0; i < nBirds; ++i) {
			vBirds.add(new FlappyBird(20, startY, new BirdBrain()));
		}
		
		return vBirds;
	}

	private Set<AbstractGameObject> createBrainedBirds(int nBirds, Optional<BirdBrain> bestBirdBrainOpt) {
		if (bestBirdBrainOpt.isEmpty()) {
			return createRandomBirds(nBirds);
		}
		
		BirdBrain bestBirdBrain = bestBirdBrainOpt.get();
		Set<AbstractGameObject> vBirds = new HashSet<>(nBirds);
		int startY = getGameHeight() / 2 - FlappyBird.HEIGHT / 2;
		FlappyBird bird;
		
		for (int i = 0; i < nBirds; ++i) {
			bird = new FlappyBird(20, startY, bestBirdBrain);
			bird.getBrain().updateWeights();
			vBirds.add(bird);
		}
		
		return vBirds;
	}
	
	// Creazione Uccelli per la Prima Generazione, 2 casi: con bestBirdBrainOpt vuoto o non vuoto (in caso di caricamento cervello da file)
	private void addFirstGenBirds() {
		if (bestBirdBrainOpt.isPresent()) {
			addBirds(createBrainedBirds(nBirdsXGen, bestBirdBrainOpt));
		} else {
			addBirds(createRandomBirds(nBirdsXGen));
		}
	}
	
	// Creazione Nuovi Uccelli per la Nuova Generazione Dopo la Prima (una parte con bestBirdBrain e una parte casuali)
	private void addNewGenBirds() {
		addBirds(createBrainedBirds(nBirdsRegen, getBestBirdBrain()));
		addBirds(createRandomBirds(nBirdsXGen - nBirdsRegen));
	}
	
	private void addNewTubePair() {
		//Set<Tube> newTubePair = Tube.newTubePair(getGameWidth(), getGameHeight());
	    //vGameObj.addAll(newTubePair);
		vGameObj.add(new TubePair(getGameWidth(), getGameHeight()));
	}
	
	// Controllo autosave a fine generazione (On Gen)
	private void checkAndAutoSaveOnEndGen() {
		// Controllo autosave per generazione
    	if (gameStats.isAutoSaveOnGenEnabled && gameStats.nGen % gameStats.getAutoSaveGenThreshold() == 0) {
			createAutoSaveFile();
    	}
	}
	
	// Controllo autosave durante la generazione attuale (On BLT e On Max Tube Passed)
	private void checkAndAutoSaveInGen() {
		if (bestBirdBrainOpt.isEmpty()) {
			return;
		}
		
		// Controllo autosave per Best Life Time
    	if (gameStats.isAutoSaveOnBLTEnabled && gameStats.bestLifeTime > 0 && Math.floor(gameStats.bestLifeTime) != gameStats.getLastSavedBLT() && Math.floor(gameStats.bestLifeTime) % gameStats.getAutoSaveBLTThreshold() == 0) {
    		gameStats.setLastSavedBLT((int) Math.floor(gameStats.bestLifeTime));
    		createAutoSaveFile();
    		// Evitare salvataggi multipli per stesso Frame
    		return;
    	}
    	
    	// Controllo autosave per Max Tube Passed
    	if (gameStats.isAutoSaveOnMaxTubePassedEnabled && gameStats.maxTubePassed > 0 && gameStats.maxTubePassed != gameStats.getLastSavedMaxTubePassed() && gameStats.maxTubePassed % gameStats.getAutoSaveMaxTubePassedThreshold() == 0) {
			gameStats.setLastSavedMaxTubePassed(gameStats.maxTubePassed);
    		createAutoSaveFile();
    	}
	}
	
	private void createAutoSaveFile() {
	    try {
	    	BirdBrainFileStorage.save(bestBirdBrainOpt.get(), gameStats);
	        gameView.showAutoSaveMessage("AUTO-SAVED!");
	    } catch (IOException | NullPointerException e) {
	        gameView.showAutoSaveMessage("AUTO-SAVE FAILED!");
	        System.err.println("Error in Automatic Brain Save: " + e.getMessage());
	    }
	}
	
	// Riavvio da Gen 1 dopo il caricamento di un cervello da file
	private void prepareForLoadedBrain() {
	    gameStats.resetToFirstGen();
	    gameClock.reset();
	    vGameObj.clear();
	}
	
	// Transizione naturale alla generazione successiva
	private void prepareForNewGen() {
		++gameStats.nGen;
		gameStats.nBirds = 0;
		gameStats.nTubePassed = 0;
		gameStats.currLifeTime = 0;
		vGameObj.clear();
		addNewGenBirds();
	}
	
	private int getGameHeight() {
		return gameView.getGameHeight();
	}
	
	private int getGameWidth() {
		return gameView.getGameWidth();
	}
	
	// Import/Export Methods
	// Chiamati dal Thread Grafico
	
	public String createManualSaveFileName() {
		synchronized (lock) { return BirdBrainFileStorage.createManualSaveFileName(gameStats); }
	} 
	
	public void saveBestBrain(Path file) throws NullPointerException, IOException {
		Optional<BirdBrain> brainOpt;
		synchronized (lock) { brainOpt = bestBirdBrainOpt; }
		
		if (brainOpt.isEmpty()) {
			throw new NullPointerException("No Best Bird Brain to Save");
		}
		
		BirdBrainFileStorage.save(bestBirdBrainOpt.get(), file);
	}
	
	public void loadBrain(String filePath) throws NullPointerException, IOException, BadFileFormatException {
		Objects.requireNonNull(filePath, "File Path Cannot be Null");
		// Caricamento I/O del cervello da file può richiedere tempo
		BirdBrain loadedBrain = BirdBrainFileStorage.load(Path.of(filePath));
		synchronized (lock) {
	        bestBirdBrainOpt = Optional.of(loadedBrain);
	        brainLoadRequest = true;
	    }
	}
	
	// API Methods
	// Chiamati dal Thread Grafico
	
	public void exitApplication() {
		gameView.close();
		BirdBrainFileStorage.shutdownWrites();
	    // Chiusura pulita del thread JavaFX (menu e FxGameView se in uso)
	    Platform.exit();
	    // Chiusura del processo JVM per terminare altri thread (game-thread e SwingGameView se in uso)
	    System.exit(0);
	}
	
	// Getter and Setter Methods
    
    public Optional<BirdBrain> getBestBirdBrain() {
    	synchronized (lock) { return bestBirdBrainOpt; }
    }
    
    public void setBestBirdBrain(BirdBrain brain) {
    	synchronized (lock) { bestBirdBrainOpt = Optional.of(brain); }
    }
    
    public boolean isAutoSaveEnabled() {
    	synchronized (lock) { return gameStats.isAutoSaveEnabled(); }
    }
    
    public void setAutoSaveOnGenEnabled(boolean enabled) {
		synchronized (lock) { gameStats.isAutoSaveOnGenEnabled = enabled; }
	}
    
    public void setAutoSaveGenThreshold(int threshold) {
    	synchronized (lock) { gameStats.setAutoSaveGenThreshold(threshold); }
    }
    
    public void setAutoSaveOnBLTEnabled(boolean enabled) {
    	synchronized (lock) { gameStats.isAutoSaveOnBLTEnabled = enabled; }
    }
    
    public void setAutoSaveBLTThreshold(int threshold) {
    	synchronized (lock) { gameStats.setAutoSaveBLTThreshold(threshold); }
	}
    
    public void setAutoSaveOnMaxTubePassedEnabled(boolean enabled) {
    	synchronized (lock) { gameStats.isAutoSaveOnMaxTubePassedEnabled = enabled; }
	}
    
    public void setAutoSaveMaxTubePassedThreshold(int threshold) {
    	synchronized (lock) { gameStats.setAutoSaveMaxTubePassedThreshold(threshold); }
	}
    
    public boolean isFirstGen() {
    	synchronized (lock) { return gameStats.isFirstGen(); }
	}
    
    public boolean isGameRunning() {
    	synchronized (lock) { return gameClock.isGameRunning(); }
    }
    
    public void setDtMultiplier(double multiplier) {
    	synchronized (lock) { gameClock.setDtMultiplier(multiplier); }
	}
    
    public void togglePause() {
    	boolean nowRunning;
    	
    	synchronized (lock) {
    		 if (gameClock.isGameRunning()) {
    			 gameClock.pause();
    			 nowRunning = false;
    		 } else {
    			 gameClock.resume();
    			 nowRunning = true;
    		 }
    		
    	}
    	
        if (nowRunning) {
        	// Sbloccare subito il thread di gioco se in attesa senza aspettare il prossimo ciclo di sleep
        	synchronized (pauseLock) {
				pauseLock.notifyAll();
        	}
        }
      
        // Forzare l'aggiornamento del display per feedback visivo istantaneo
        // Fuori dal blocco synchronized per evitare di bloccare il thread di gioco durante il repaint sul thread grafico
        gameView.repaintGame();
    }
    
}