/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.view.fx;

import flappyBirdAI.controller.GameController;
import flappyBirdAI.controller.GameStats;
import flappyBirdAI.model.AbstractGameObject;
import flappyBirdAI.view.GameRenderer;
import flappyBirdAI.view.GameView;
import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.Set;

public class FxGameView implements GameView {
	
	// --- Costanti di Colori ---
	
    private static final Color GAME_BACKGROUND_COLOR = Color.CYAN;
    private static final Color STATS_BACKGROUND_COLOR = Color.DARKGRAY;
    private static final Color CONTROLS_BACKGROUND_COLOR = Color.rgb(128, 0, 32);
    private static final Color IMPORT_EXPORT_BACKGROUND_COLOR = Color.LIGHTGRAY;
    private static final Color CHRONOMETER_BACKGROUND_COLOR = Color.rgb(240, 230, 140);
    private static final Color PAUSE_OVERLAY_COLOR = Color.rgb(0, 0, 0, 150.0 / 255.0);
    private static final Color PAUSE_SYMBOL_COLOR = Color.rgb(150, 150, 150);
	
	// --- Riferimenti a Componenti Esterne ---
    
	private GameController gameController;
	private final GameRenderer<GraphicsContext, Image> spriteRenderer = new FxGameRenderer();
	
	// --- Campi per Caching delle Statistiche ---
	
    private int lastGen = -1, lastNBirds = -1, lastTubePassed = -1, lastMaxTubePassed = -1;
    private boolean lastAutoSaveStatus = false;
    private double lastBestLifeTime = -1.0;
    
    // --- Campi di Stato ---
    
  	private final int initWidth, initHeight;
  	private final boolean isFullScreen;

    private Set<AbstractGameObject> currentVGameObj;
    
    // --- Timers ---
    
    private Timeline animationTimer;
    private AnimationTimer chronometerTimer;
    private PauseTransition autoCloseAutoSaveDialogTimer;
    
    // --- Componenti UI ---
    
    private final Stage stage;
    private final StackPane rootPane;
	
	// --- Costruttori ---

	public FxGameView(int width, int height, boolean isFullScreen) {
		this.isFullScreen = isFullScreen;
		initWidth = Math.max(width, MIN_WINDOW_WIDTH);
		initHeight = Math.max(height, MIN_WINDOW_HEIGHT);
		
		stage = createStage();
		
		rootPane = new StackPane();
		
		initTimers();
		
		Scene scene = new Scene(rootPane);
		stage.setScene(scene);
		stage.show();
	}
	
	// --- Creazione Finestra di Gioco ---
	
	private Stage createStage() {
		Stage stage = new Stage();
		
		stage.setTitle(GAME_WINDOW_TITLE);
		stage.getIcons().add(new Image(getClass().getResourceAsStream(GAME_ICON_PATH)));
		stage.setMinWidth(MIN_WINDOW_WIDTH);
		stage.setMinHeight(MIN_WINDOW_HEIGHT);
		stage.setMaxWidth(MAX_WINDOW_WIDTH);
		stage.setMaxHeight(MAX_WINDOW_HEIGHT);
		
		return stage;
	}
	
	// --- Inizializzazione Timers ---
	
	private void initTimers() {
		chronometerTimer = new AnimationTimer() {
			// "now" è timestamp corrente in nanosecondi fornito da JavaFX AnimationTimer
	        @Override
	        public void handle(long now) {
	        	updateChronometerLabel();
	        }
	    };
	    							  // KeyFrame specifica intervallo di tempo e azione da eseguire
		animationTimer = new Timeline(new KeyFrame(Duration.millis(ANIMATION_REFRESH_MS), _ -> updateAnimations()));
		
		// timer con durata indefinita per aggiornare le animazioni dei GameObject
		animationTimer.setCycleCount(Animation.INDEFINITE);
		animationTimer.play();
	}
	
	// --- Gestione Ciclo di Vita ---
	
	@Override
	public void setController(GameController controller) {
		this.gameController = controller;
	}
	
	@Override
	public void startChronometerTimer() {
		chronometerTimer.start();
	}
	
	@Override
	public void close() {
		
		
	}
	
	// Eseguito su un thread separato per non bloccare JavaFX Application Thread durante l'attesa di terminazione dei thread del gioco (eventuali salvataggi)
	@Override
	public void exitGame() {
		new Thread(gameController::exitApplication, "safe-shutdown").start();
	}
	
	// --- Aggiornamento UI ---
	
	@Override
	public void updateGameStatsAndRepaint(GameStats stats) throws NullPointerException {
		Objects.requireNonNull(stats, "Game Stats Cannot be Null");
		
	}
	
	@Override
	public void updateDisplayAndRepaint(GameStats stats, Set<AbstractGameObject> vGameObj) throws NullPointerException {
		Objects.requireNonNull(stats, "Game Stats Cannot be Null");
		Objects.requireNonNull(vGameObj, "Game Objects List Cannot be Null");
		
	}

	@Override
	public void repaintGame() {
		
		
	}
	
	private void updateChronometerLabel() {
		if (gameController.isGameRunning()) {
	        //lTimeValue.setText(gameController.getFormattedGameTimeElapsed());
	    }
	}
	
	// --- Rendering e Animazioni ---
	
	@Override
	public void preloadSprites(Set<AbstractGameObject> vGameObj) {
		
		
	}
	
	@Override
	public void updateAnimations() {
		
		
	}
	
	// --- Gestione Pausa ---
	
	@Override
	public void togglePause() {
		gameController.togglePause();
        
        if (gameController.isGameRunning()) {
			chronometerTimer.start();
		} else {
			chronometerTimer.stop();
		}
        
        updatePauseOverlay();
	}
	
	@Override
    public void updatePauseOverlay() {
		
	}
	
	// --- Gestione Messaggi e Notifiche ---
	
	@Override
	public void showBlockingWarning(String headerText, String detailText) {
		
		
	}

	@Override
	public void showAutoSaveMessage(boolean success, String headerText, String errorDetail) {
		
		
	}
	
	// --- Getters per Dimensioni Pannello di Gioco ---

	@Override
	public int getGameWidth() {
		
		return 0;
	}

	@Override
	public int getGameHeight() {
		
		return 0;
	}

}