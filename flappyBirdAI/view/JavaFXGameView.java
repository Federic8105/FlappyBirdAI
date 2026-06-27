/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.view;

import flappyBirdAI.controller.GameController;
import flappyBirdAI.controller.GameClock;
import flappyBirdAI.controller.GameStats;
import flappyBirdAI.model.AbstractGameObject;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Set;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class JavaFXGameView implements GameView {

    // Window and UI Constants
    private static final String WINDOW_TITLE = "Flappy Bird AI";
    private static final String ICON_PATH = "/res/FB_ICON.png";
    private static final String GAME_BACKGROUND_IMAGE_PATH = "/res/BACKGROUND.png";
    
    // Panel Minimum Dimensions Constants
    private static final int MIN_STATS_PANEL_WIDTH = 1000;
    private static final int MIN_STATS_PANEL_HEIGHT = 40;
    private static final int MIN_CONTROLS_PANEL_HEIGHT = 150;
    private static final int MIN_IMPORT_EXPORT_PANEL_WIDTH = 250;
    private static final int MIN_CHRONOMETER_PANEL_WIDTH = MIN_IMPORT_EXPORT_PANEL_WIDTH;
    private static final int MIN_CHRONOMETER_PANEL_HEIGHT = MIN_CONTROLS_PANEL_HEIGHT;
    private static final int MIN_GAME_PANEL_WIDTH = MIN_STATS_PANEL_WIDTH;
    private static final int MIN_GAME_PANEL_HEIGHT = 500;
    private static final int MIN_IMPORT_EXPORT_PANEL_HEIGHT = MIN_GAME_PANEL_HEIGHT + MIN_STATS_PANEL_HEIGHT;
    private static final int MIN_WINDOW_WIDTH = MIN_STATS_PANEL_WIDTH + MIN_IMPORT_EXPORT_PANEL_WIDTH;
    private static final int MIN_WINDOW_HEIGHT = MIN_GAME_PANEL_HEIGHT + MIN_STATS_PANEL_HEIGHT + MIN_CONTROLS_PANEL_HEIGHT;
    
    // Colors (convertiti da java.awt.Color a javafx.scene.paint.Color)
    private static final Color GAME_BACKGROUND_COLOR = Color.CYAN;
    private static final Color STATS_BACKGROUND_COLOR = Color.DARKGRAY;
    private static final Color CONTROLS_BACKGROUND_COLOR = Color.web("#800020");
    private static final Color IMPORT_EXPORT_BACKGROUND_COLOR = Color.LIGHTGRAY;
    private static final Color CHRONOMETER_BACKGROUND_COLOR = Color.web("#F0E68C");
    private static final Color PAUSE_OVERLAY_COLOR = Color.rgb(0, 0, 0, 0.6);
    private static final Color PAUSE_SYMBOL_COLOR = Color.rgb(150, 150, 150);
    
    // Pause Symbol Ratios
    private static final double BAR_WIDTH_RATIO = 1.0 / 4.5;
    private static final double BAR_HEIGHT_RATIO = 0.8;
    private static final double BAR_GAP_RATIO = 1.0 / 3.0;
    
    // Initial Window Dimensions
    private final int initialWidth, initialHeight;
    
    // Caching Ultimi Valori di Statistica
    private int lastGen = -1;
    private boolean lastAutoSaveStatus = false;
    private double lastBestLifeTime = -1.0;
    
    // Controller Reference
    private GameController gameController;
    
    // Game Objects for Rendering
    private Set<AbstractGameObject> currentVGameObj;
    
    // JavaFX Components
    private Stage primaryStage;
    private Canvas gameCanvas;
    private GraphicsContext gc;
    private Image backgroundImage;
    
    // UI Components - Statistiche
    private Label lFPS, lCurrLifeTime, lBestLifeTime, lNGen, lNBirds, lNTubePassed, lMaxTubePassed, lAutoSave;
    private PauseTransition autoSaveMessageTimer;
    
    // UI Components - Controls
    private Slider velocitySlider;
    
    // UI Components - Import/Export
    private Button bSaveBrain, bLoadBrain;
    private CheckBox cbAutoSaveOnGen, cbAutoSaveOnBLT, cbAutoSaveOnMaxTubePassed;
    private Spinner<Integer> autoSaveGenThresholdSpinner, autoSaveBLThresholdSpinner, autoSaveMaxTubePassedThresholdSpinner;
    
    // UI Components - Chronometer
    private Label lTime, lTimeValue;
    
    public JavaFXGameView(int width, int height) {
        this.initialWidth = Math.max(width, MIN_WINDOW_WIDTH);
        this.initialHeight = Math.max(height, MIN_WINDOW_HEIGHT);
        
        // Inizializzare JavaFX Application se non già inizializzata
        Platform.startup(() -> {});
        
        Platform.runLater(() -> {
            primaryStage = new Stage();
            //initWindow();
        });
    }
    
}