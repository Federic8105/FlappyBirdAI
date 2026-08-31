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
import flappyBirdAI.persistence.BirdBrainFileStorage;
import flappyBirdAI.persistence.BirdBrainFileStorage.ShutdownResult;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.Optional;
import java.util.Map;
import java.util.Objects;
import java.util.HashMap;
import java.util.HashSet;
import java.awt.Rectangle;

public final class GameController {
	
	// --- Costanti di Configurazione ---
	
	public static final int MIN_N_BIRDS_X_GEN = 1, MAX_N_BIRDS_X_GEN = 100000;
	
	// --- Riferimenti a Componenti Esterne ---
	
	private final GameView gameView;
	private final GameStats gameStats = new GameStats();
	private final GameClock gameClock = new GameClock();
    
	// --- Campi di Stato ---
	
    private final Set<AbstractGameObject> vGameObj;
    private final Map<String, Double> brainInputMap = new HashMap<>(BirdBrain.NUM_INPUT);
    private final int nBirdsXGen, nBirdsRegen;
    
    private int lastGameHeight;
    private Optional<BirdBrain> bestBirdBrainOpt = Optional.empty();
    
    // Flag per Richiesta di Reset del Gioco per Caricamento Cervello da File
    private boolean brainLoadRequest = false;
    
    // --- Lock Objects per la Sincronizzazione tra Thread ---
    
    // Oggetto di Lock per la Sincronizzazione di Accesso a Variabili Condivise tra Thread (Game Thread e GUI Thread)
    private final Object lock = new Object();
    
    // Oggetto di Lock per Gestire la Pausa in Modo Thread-Safe
    // Usato come Monitor per wait() e notify() per la pausa del gioco
    private final Object pauseLock = new Object();
    
    // -- - Costruttori ---

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
	
	// -- Ciclo di Gioco ---
	
	public void playOneGen() throws RuntimeException {
		// Copia per efficienza per evitare chiamate multiple a metodi sincronizzati
		int gameHeight, gameWidth;
		// Delta Time del Gioco - Influenzato dal Dt Multiplier
		double dt;
		long sleepTime;
		boolean isGameRunning;
		Optional<TubePair> firstTubePairOpt;
		TubePair previousFirstTubePair = null, currTargetTubePair;
		Optional<FlappyBird> randBirdOpt;
		Optional<BirdBrain> autoSaveInGenBrain, autoSaveEndGenBrain;
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
				gameView.startChronometerTimer();
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
			            gameView.updateDisplay(gameStats, new HashSet<>(vGameObj));
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
				
				// Aggiornare copia di gameHeight e gameWidth per evitare chiamate multiple a metodi sincronizzati
				gameHeight = getGameHeight();
				gameWidth = getGameWidth();
				
				// Controllo se l'Altezza della Finestra di Gioco è Cambiata
				if (lastGameHeight != gameHeight) {
					// Ricreare tutti i Tube con la Nuova Altezza
					recreateTubePairs(gameHeight);
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
	            updateGameObjects(dt, gameWidth, gameHeight, getTubeHitBoxes(firstTubePairOpt), firstTubePairOpt);
	            deleteDeadGameObjects();
				checkNewTube(gameWidth);
				
				sleepTime = gameClock.setFrameEndTime();
				
				// Aggiornare Statistica FPS
				gameStats.fps = gameClock.getEMAFPS();
				
				// Aggiornare la Vista di Gioco
				// TODO Si passa il riferimento a gameStats ma nel frattempo i suoi campi potrebbero essere modificati dal thread di gioco
				// Si passa una Copia della Lista per Evitare ConcurrentModificationException (Thread-Safe)
				// TODO Ma comunque rimane il problema che i singoli oggetti di gioco all'interno non sono una copia 
				// quindi rimangono mutabili e sono condivisi, rischio di inconsistenza visiva se il thread di gioco aggiorna un oggetto mentre il thread grafico lo sta disegnando
	            // TODO soluzioni:
				// 1) creare copie snapshot di gameStats e dei valori dei game objects usati per il rendering messi in records (x, y, ...) dentro un blocco synchronized e passarli al thread grafico fuori dal blocco synchronized (risolve problema di visibilità e consistenza ma ha un costo di allocazione a frame)
				// 2) rendere i campi usati nel rendereing volatile in gameStats e nei game objects (x, y, ...) per garantire la visibilità tra thread (risolve problema di visibilità ma non di consistenza)
				//    MA non risolve il problema di consistenza se il thread di gioco aggiorna un oggetto mentre il thread grafico lo sta disegnando
				// Criterio di scelta:
				// - gameStats: pochi campi primitivi -> costo trascurabile, usare Soluzione 1 (snapshot) sempre
				// - game objects (potenzialmente molti, es. migliaia di bird): 
				//   Soluzione 2 (volatile) se il difetto visivo di un frame "storto" è accettabile (costo ~0)
				//   Soluzione 1 (snapshot) se serve consistenza garantita, valutando il costo di allocazione a frame
				gameView.updateDisplay(gameStats, new HashSet<>(vGameObj));
	            
	            // Controllo se autosave durante la generazione è da fare e ritorna Optional<BirdBrain> con bestBirdBrain da salvare se è il momento di fare l'autosave, altrimenti Optional vuoto
	            autoSaveInGenBrain = checkAutoSaveInGen();
			}
			
			// Autosave fatta fuori da synchronized per evitare di bloccare il thread di gioco durante serializzazione JSON di brain
			autoSaveInGenBrain.ifPresent(this::createAutoSaveFile);
            
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
		
		// Optional del cervello da salvare se è il momento di fare l'autosave a fine generazione, altrimenti Optional vuoto
		autoSaveEndGenBrain = Optional.empty();
		
		synchronized (lock) {
			if (brainLoadRequest) {
				brainLoadRequest = false;
			    prepareForLoadedBrain();
			} else {
				// Controllo se autosave a fine generazione è da fare e ritorna Optional<BirdBrain> con bestBirdBrain da salvare se è il momento di fare l'autosave, altrimenti Optional vuoto
				autoSaveEndGenBrain = checkAutoSaveOnEndGen();
				prepareForNewGen();
			}
		}
		
		// Autosave fatta fuori da synchronized per evitare di bloccare il thread di gioco durante serializzazione JSON di brain
		autoSaveEndGenBrain.ifPresent(this::createAutoSaveFile);
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
	
	// --- Aggiornamento Oggetti di Gioco/Helper Methods ---
	
	private Rectangle[] getTubeHitBoxes(Optional<TubePair> firstTubePairOpt) {
		return firstTubePairOpt.isPresent() ? firstTubePairOpt.get().getHitBox() : new Rectangle[0];
	}
	
	private void recreateTubePairs(int gameHeight) {
		Set<TubePair> newTubePairs = new HashSet<>(15);
		double holeRatio;
		for (AbstractGameObject obj : vGameObj) {
			if (obj instanceof TubePair currTubePair && currTubePair.isAlive()) {
				// Mantenere la posizione relativa del buco rispetto alla vecchia altezza
				holeRatio = (double) currTubePair.getYTubeHoleCenter() / lastGameHeight;
				newTubePairs.add(new TubePair(currTubePair.x, gameHeight, holeRatio));
			}
		}
		
		// Rimuovere tutti i TubePair esistenti e aggiungere i nuovi
		vGameObj.removeIf(obj -> obj instanceof TubePair);
		vGameObj.addAll(newTubePairs);
	}

	private void updateGameObjects(double dt, int gameWidth, int gameHeight, Rectangle[] tubeHitBoxes, Optional<TubePair> firstTubePairOpt) {
        TubePair firstTubePair;
        
		for (AbstractGameObject obj : vGameObj) {
        	
            if (obj instanceof FlappyBird currBird && currBird.isAlive()) {
                
            	// Controllo Collisioni e Limiti Schermo - Flappy Bird Morto
                if (currBird.checkCollision(tubeHitBoxes) || currBird.isOutOfScreen(gameWidth, gameHeight)) {
                    currBird.setAlive(false);
        			--gameStats.nBirds;
                    continue;
                    
                // AI Decision
                } else if (firstTubePairOpt.isPresent()) {
                	
                	firstTubePair = firstTubePairOpt.get();
                	
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
                if (currTubePair.isOutOfScreen(gameWidth, gameHeight)) {
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

	private void checkNewTube(int gameWidth) {
		TubePair lastTubePair = null;
		
        for (AbstractGameObject obj : vGameObj) {
            if (obj instanceof TubePair currTubePair && currTubePair.isAlive()) {
            	// Tenere il TubePair con la x più grande (il più a destra)
            	if (lastTubePair == null || currTubePair.x > lastTubePair.x) {
            		lastTubePair = currTubePair;
            	}
            }
        }

		if (lastTubePair != null && lastTubePair.x + TubePair.WIDTH <= gameWidth - TubePair.DIST_X_BETWEEN_TUBES) {
			addNewTubePair();
		} else if (lastTubePair == null) {
			addNewTubePair();
		}
	}
	
	// --- Creazione e Aggiunta Bird e TubePair ---
	
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
	
	// --- Gestione Autosave ---
	
	// Controllo autosave a fine generazione (On Gen)
	// ritorna Optional<BirdBrain> con bestBirdBrain da salvare se è il momento di fare l'autosave, altrimenti Optional vuoto
	private Optional<BirdBrain> checkAutoSaveOnEndGen() {
		// Controllo autosave per generazione
    	if (gameStats.isAutoSaveOnGenEnabled && gameStats.nGen % gameStats.getAutoSaveGenThreshold() == 0) {
			return bestBirdBrainOpt;
    	}
    	
    	return Optional.empty();
	}
	
	// Controllo autosave durante la generazione attuale (On BLT e On Max Tube Passed)
	// ritorna Optional<BirdBrain> con bestBirdBrain da salvare se è il momento di fare l'autosave, altrimenti Optional vuoto
	private Optional<BirdBrain> checkAutoSaveInGen() {
		if (bestBirdBrainOpt.isEmpty()) {
			return Optional.empty();
		}
		
		// Controllo autosave per Best Life Time
    	if (gameStats.isAutoSaveOnBLTEnabled && gameStats.bestLifeTime > 0 && Math.floor(gameStats.bestLifeTime) != gameStats.getLastSavedBLT() && Math.floor(gameStats.bestLifeTime) % gameStats.getAutoSaveBLTThreshold() == 0) {
    		gameStats.setLastSavedBLT((int) Math.floor(gameStats.bestLifeTime));
    		// Evitare salvataggi multipli per stesso Frame
    		return bestBirdBrainOpt;
    	}
    	
    	// Controllo autosave per Max Tube Passed
    	if (gameStats.isAutoSaveOnMaxTubePassedEnabled && gameStats.maxTubePassed > 0 && gameStats.maxTubePassed != gameStats.getLastSavedMaxTubePassed() && gameStats.maxTubePassed % gameStats.getAutoSaveMaxTubePassedThreshold() == 0) {
			gameStats.setLastSavedMaxTubePassed(gameStats.maxTubePassed);
    		return bestBirdBrainOpt;
    	}
    	
    	return Optional.empty();
	}
	
	private void createAutoSaveFile(BirdBrain brain) {
		// no try-catch attorno a saveAsync perchè ritorna subito un CompletableFuture e non lancia eccezioni, eventuali eccezioni sono catturate e gestite nel whenComplete
		BirdBrainFileStorage.saveAsync(brain, gameStats)
		.whenComplete((_, ex) -> { // azione eseguita da thread di saveAsync quando il CompletableFuture non è subito pronto
            boolean success = (ex == null);
			String header = (success ? "AUTO-SAVED!" : "AUTO-SAVE FAILED!");
			String msg = (success ? "" : "Error: " + ex.getCause().getMessage());
            gameView.showAutoSaveMessage(success, header, msg);
        });
	}
	
	// --- Transizione di Stato tra Generazioni ---
	
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
	
	// --- Getter Interni per Dimensioni Area di Gioco ---
	
	private int getGameHeight() {
		return gameView.getGameHeight();
	}
	
	private int getGameWidth() {
		return gameView.getGameWidth();
	}
	
	// --- Gestione Import/Export ---
	// Chiamati dal Thread Grafico
	
	public String createManualSaveFileName() {
		synchronized (lock) { return BirdBrainFileStorage.createManualSaveFileName(gameStats); }
	} 
	
	public CompletableFuture<Void> saveBestBrainAsync(Path file) {
		Optional<BirdBrain> brainOpt;
		synchronized (lock) { brainOpt = bestBirdBrainOpt; }
		
		if (brainOpt.isEmpty()) {
			return CompletableFuture.failedFuture(new NullPointerException("No Best Bird Brain to Save"));
		}
		
		return BirdBrainFileStorage.saveAsync(brainOpt.get(), file);
	}
	
	public CompletableFuture<Void> loadBrainAsync(String filePath) {
		Objects.requireNonNull(filePath, "File Path Cannot be Null");
		
		return BirdBrainFileStorage.loadAsync(Path.of(filePath))
		        .thenAccept(loadedBrain -> {
		            synchronized (lock) {
		                bestBirdBrainOpt = Optional.of(loadedBrain);
		                brainLoadRequest = true;
		            }
		        });
	}
	
	// --- Gestione Pausa/Riprendi Gioco ---
	
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
	
	// --- Gestione Uscita Applicazione ---
	// Chiamato dal Thread Grafico
	
	public void exitApplication() {
		gameView.close();
		
		// Chiusura pulita del thread di persistenza per completare eventuali scritture in corso
		ShutdownResult result = BirdBrainFileStorage.shutdownAndAwaitCompletion();
		if (result != ShutdownResult.COMPLETED) {
	        String headerText = switch (result) {
	            case TIMED_OUT -> "The save took too long to complete";
	            case INTERRUPTED -> "Saving was interrupted";
	            default -> throw new IllegalArgumentException("Unexpected value: " + result);
	        };
	        gameView.showBlockingWarning(headerText, "Some progress may not have been saved.");
	    }
		
	    // Chiusura pulita del thread JavaFX (menu e FxGameView se in uso)
	    Platform.exit();
	    // Chiusura del processo JVM per terminare altri thread (game-thread e SwingGameView se in uso)
	    System.exit(0);
	}
	
	// --- Getters/Setters e Query di Stato ---

    public boolean isGameRunning() {
    	return gameClock.isGameRunning();
    }
    
    public boolean isFirstGen() {
    	synchronized (lock) { return gameStats.isFirstGen(); }
	}
    
    public boolean isAutoSaveEnabled() {
    	synchronized (lock) { return gameStats.isAutoSaveEnabled(); }
    }
    
    public Optional<BirdBrain> getBestBirdBrain() {
    	synchronized (lock) { return bestBirdBrainOpt; }
    }
    
    public void setBestBirdBrain(BirdBrain brain) {
    	synchronized (lock) { bestBirdBrainOpt = Optional.of(brain); }
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
    
    public String getFormattedGameTimeElapsed() {
        return gameClock.getFormattedGameTimeElapsed();
    }
    
    public void setDtMultiplier(double multiplier) {
    	synchronized (lock) { gameClock.setDtMultiplier(multiplier); }
	}
    
}