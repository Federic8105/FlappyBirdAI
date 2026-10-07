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
import javafx.application.Platform;
import javafx.util.Duration;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
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
    private final BorderPane rootPane;
	
	// --- Costruttori ---

	public FxGameView(int width, int height, boolean isFullScreen) {
		this.isFullScreen = isFullScreen;
		initWidth = Math.max(width, MIN_WINDOW_WIDTH);
		initHeight = Math.max(height, MIN_WINDOW_HEIGHT);
		
		stage = createStage();
		rootPane = new BorderPane();
		initUI();
		Scene scene = new Scene(rootPane);
		setupWindowKeyEventHandlers(scene);
		initTimers();
		
		stage.setScene(scene);
		// adatta la finestra alle dimensioni dei componenti
		stage.sizeToScene();
		stage.show();
	}
	
	// --- Creazione Finestra di Gioco ---
	
	private Stage createStage() {
		Stage stage = new Stage();
		
		if (isFullScreen) {
			stage.setFullScreen(true);
		    stage.setFullScreenExitHint("Press ESC to Exit the Game");
		    stage.setFullScreenExitKeyCombination(KeyCombination.valueOf("ESC"));
		} else {
		    stage.setWidth(initWidth);
		    stage.setHeight(initHeight);
		    stage.setMinWidth(MIN_WINDOW_WIDTH);
		    stage.setMinHeight(MIN_WINDOW_HEIGHT);
		}
		
		stage.setTitle(GAME_WINDOW_TITLE);
		stage.getIcons().add(new Image(getClass().getResourceAsStream(GAME_ICON_PATH)));
		stage.setMinWidth(MIN_WINDOW_WIDTH);
		stage.setMinHeight(MIN_WINDOW_HEIGHT);
		stage.setMaxWidth(MAX_WINDOW_WIDTH);
		stage.setMaxHeight(MAX_WINDOW_HEIGHT);
		
		return stage;
	}
	
	private void initUI() {
	    rootPane.setLeft(createLeftPanes());
	    rootPane.setCenter(createCentralPanes());
	}
	
	private BorderPane createLeftPanes() {
	    BorderPane leftPane = new BorderPane();
	    
	    // Calcolare la larghezza come percentuale della larghezza totale, con controllo sulla larghezza minima
	    int paneWidth = Math.max((int) (initWidth * 0.2f), MIN_IMPORT_EXPORT_PANEL_WIDTH);
	    leftPane.setPrefWidth(paneWidth);
	    leftPane.setMinWidth(MIN_IMPORT_EXPORT_PANEL_WIDTH);
	    leftPane.setCenter(createImportExportPanes(paneWidth));
	    leftPane.setBottom(createChronometerPanes(paneWidth));
	    
	    return leftPane;
	}
	
	private BorderPane createCentralPanes() {
	    BorderPane centralPane = new BorderPane();

	    centralPane.setPrefSize(initWidth, initHeight);
	    centralPane.setTop(createStatsPanes());
	    centralPane.setCenter(createGamePanes());
	    centralPane.setBottom(createControlsPanes());
	    
	    return centralPane;
	}
	
	// --- Inizializzazione Pannelli Principali ---
	
	// --- Inizializzazione Listeners e Timers ---
	
	private void setupWindowKeyEventHandlers(Scene scene) {
		scene.setOnKeyPressed(event -> {
			switch (event.getCode()) {
				case KeyCode.SPACE -> togglePause();
				case KeyCode.ESCAPE -> {
					if (isFullScreen) {
						handleExitRequest();
					}
				}
				default -> {}
			}
		});
	}
	
	private void handleExitRequest() {
		Alert alert = new Alert(AlertType.CONFIRMATION, "Do you want to Quit the Game?", ButtonType.YES, ButtonType.NO);
	    alert.setTitle("Confirm Exit");
	    alert.setHeaderText(null);
	    alert.initOwner(stage);

	    alert.showAndWait();
	    
	    if (alert.getResult() == ButtonType.YES) {
	    	exitGame();
	    }
	}
	
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
	
	// --- Rendering Pannello di Gioco ---
	//TODO
	private void drawPauseOverlay() {
	    GraphicsContext gc = pauseCanvas.getGraphicsContext2D();
	    double w = pauseCanvas.getWidth(), h = pauseCanvas.getHeight();
	    gc.clearRect(0, 0, w, h);
	    gc.setFill(PAUSE_OVERLAY_COLOR);
	    gc.fillRect(0, 0, w, h);

	    double symbolSize = Math.min(w, h) / 6;
	    double symbolX = (w - symbolSize) / 2, symbolY = (h - symbolSize) / 2;
	    double barW = symbolSize * BAR_WIDTH_RATIO, barH = symbolSize * BAR_HEIGHT_RATIO;
	    double gap = symbolSize * BAR_GAP_RATIO;
	    double barY = symbolY + (symbolSize - barH) / 2;
	    double bar1X = symbolX + (symbolSize - 2 * barW - gap) / 2;
	    double bar2X = bar1X + barW + gap;

	    gc.setFill(PAUSE_SYMBOL_COLOR);
	    gc.fillRoundRect(bar1X, barY, barW, barH, 5, 5);
	    gc.fillRoundRect(bar2X, barY, barW, barH, 5, 5);
	    gc.setLineWidth(1.7);
	    gc.setStroke(Color.BLACK);
	    gc.strokeRoundRect(bar1X, barY, barW, barH, 5, 5);
	    gc.strokeRoundRect(bar2X, barY, barW, barH, 5, 5);

	    // Calcolare posizione del testo sotto il simbolo di pausa
	    double fontSize = symbolSize / 4;
	    double textY = symbolY + symbolSize + fontSize * 1.2;
	    gc.setFont(Font.font("Arial", FontWeight.BOLD, FontPosture.ITALIC, fontSize));
	    gc.setTextAlign(TextAlignment.CENTER);
	    
	    // Ombra del testo (testo nero leggermente spostato)
	    gc.setFill(Color.BLACK);
	    gc.fillText("PAUSED", w / 2 + 3, textY + 3);
	    
	    // Testo principale bianco
	    gc.setFill(Color.WHITE);
	    gc.fillText("PAUSED", w / 2, textY);
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
		// Chiudere la finestra in modo thread-safe quando viene chiamato da un thread diverso dal thread dell'UI di JavaFX
		// Non termina nessun thread, solo la finestra di gioco
		// Platform.runLater accoda le operazioni da eseguire sul JavaFX Application Thread quando la funzione è chiamata da un altro thread, garantendo che l'aggiornamento della GUI avvenga in modo sicuro
		// JavaFX Application Thread è l'unico che può modificare lo scene graph attivo, cioè nodi in una Scene mostrata nello stage
		Platform.runLater(() -> {
			chronometerTimer.stop();
			animationTimer.stop();
			stage.close();
		});
	}
	
	// Eseguito su un thread separato per non bloccare JavaFX Application Thread durante l'attesa di terminazione dei thread del gioco (eventuali salvataggi)
	@Override
	public void exitGame() {
		new Thread(gameController::exitApplication, "safe-shutdown").start();
	}
	
	// --- Aggiornamento UI ---
	
	@Override
	public void updateGameStats(GameStats stats) throws NullPointerException {
		Objects.requireNonNull(stats, "Game Stats Cannot be Null");
		
	}
	
	@Override
	public void updateDisplay(GameStats stats, Set<AbstractGameObject> vGameObj) throws NullPointerException {
		Objects.requireNonNull(stats, "Game Stats Cannot be Null");
		Objects.requireNonNull(vGameObj, "Game Objects List Cannot be Null");
		
	}

	@Override
	public void renderGameArea() {
		
		
	}
	
	private void updateChronometerLabel() {
		if (gameController.isGameRunning()) {
	        //lTimeValue.setText(gameController.getFormattedGameTimeElapsed());
	    }
	}
	
	// --- Rendering e Animazioni ---
	
	@Override
	public void preloadSprites(Set<AbstractGameObject> vGameObj) {
		if (vGameObj != null && !vGameObj.isEmpty()) {
    		// Precaricare le immagini dei GameObject per evitare ritardi durante il rendering
    		spriteRenderer.preloadSprites(vGameObj);
		}
	}
	
	@Override
	public void updateAnimations() {
		if (currentVGameObj == null || currentVGameObj.isEmpty()) {
            return;
        }
    	
    	for (AbstractGameObject obj : currentVGameObj) {
			if (obj.isAlive() && obj.isShowSprite() && obj.isAnimated()) {
				obj.updateFrameIndex();
			}
		}
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
	    pauseHolder.setVisible(!gameController.isGameRunning());
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