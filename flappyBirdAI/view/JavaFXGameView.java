/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.view;

import flappyBirdAI.controller.GameController;
import flappyBirdAI.controller.GameClock;
import flappyBirdAI.controller.GameStats;
import flappyBirdAI.model.AbstractGameObject;
import flappyBirdAI.model.GameObject;
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
import java.util.List;
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
    private List<AbstractGameObject> currentVGameObj;
    
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
            initWindow();
        });
    }
    
    private void initWindow() {
        primaryStage.setTitle(WINDOW_TITLE);
        
        // Set icon
        try {
            InputStream iconStream = getClass().getResourceAsStream(ICON_PATH);
            if (iconStream != null) {
                primaryStage.getIcons().add(new Image(iconStream));
            }
        } catch (Exception e) {
            System.err.println("Error Loading Icon: " + e.getMessage());
        }
        
        // Load background image
        try {
            InputStream bgStream = getClass().getResourceAsStream(GAME_BACKGROUND_IMAGE_PATH);
            if (bgStream != null) {
                backgroundImage = new Image(bgStream);
            }
        } catch (Exception e) {
            System.err.println("Error Loading Background Image: " + e.getMessage());
        }
        
        BorderPane root = new BorderPane();
        
        // Create panels
        VBox leftPanel = createLeftPanel();
        VBox centralPanel = createCentralPanel();
        
        root.setLeft(leftPanel);
        root.setCenter(centralPanel);
        
        Scene scene = new Scene(root, initialWidth, initialHeight);
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SPACE) {
                gameController.togglePause();
            }
        });
        
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(MIN_WINDOW_WIDTH);
        primaryStage.setMinHeight(MIN_WINDOW_HEIGHT);
        primaryStage.show();
    }
    
    private VBox createLeftPanel() {
        VBox leftPanel = new VBox();
        int panelWidth = calcImportExportPanelWidth(0.2f);
        leftPanel.setPrefWidth(panelWidth);
        leftPanel.setMinWidth(MIN_IMPORT_EXPORT_PANEL_WIDTH);
        leftPanel.setMinHeight(MIN_IMPORT_EXPORT_PANEL_HEIGHT + MIN_CHRONOMETER_PANEL_HEIGHT);
        
        VBox importExportPanel = createImportExportPanel(panelWidth);
        VBox chronometerPanel = createChronometerPanel(panelWidth);
        
        VBox.setVgrow(importExportPanel, Priority.ALWAYS);
        
        leftPanel.getChildren().addAll(importExportPanel, chronometerPanel);
        
        return leftPanel;
    }
    
    private int calcImportExportPanelWidth(float percOfTotWidth) {
        return Math.max((int) (initialWidth * percOfTotWidth), MIN_IMPORT_EXPORT_PANEL_WIDTH);
    }
    
    private VBox createImportExportPanel(int panelWidth) {
        VBox panel = new VBox(10);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setBackground(new Background(new BackgroundFill(IMPORT_EXPORT_BACKGROUND_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));
        panel.setPrefWidth(panelWidth);
        panel.setMinWidth(MIN_IMPORT_EXPORT_PANEL_WIDTH);
        panel.setMinHeight(MIN_IMPORT_EXPORT_PANEL_HEIGHT);
        panel.setPadding(new Insets(15, 10, 15, 10));
        
        Label title = new Label("Bird Brain Import/Export");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        title.setStyle("-fx-border-color: black; -fx-border-width: 1; -fx-padding: 5;");
        
        int componentsWidth = panelWidth - 20;
        
        // Save Brain Button
        bSaveBrain = createImportExportButton("Save Brain to File", Color.GREEN, componentsWidth);
        bSaveBrain.setOnAction(_ -> handleSaveBrain());
        
        // Auto-Save Settings
        VBox autoSavePanel = new VBox(5);
        autoSavePanel.setAlignment(Pos.CENTER);
        autoSavePanel.setBackground(new Background(new BackgroundFill(IMPORT_EXPORT_BACKGROUND_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));
        autoSavePanel.setMaxWidth(componentsWidth);
        autoSavePanel.setStyle("-fx-border-color: darkgray; -fx-border-width: 1; -fx-padding: 10;");
        
        Label autoSaveTitle = new Label("Auto-Save Settings");
        autoSaveTitle.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        // Auto-Save on Generation
        cbAutoSaveOnGen = createAutoSaveCheckBox("On Generation", componentsWidth);
        cbAutoSaveOnGen.setSelected(GameStats.DEFAULT_IS_AUTOSAVE_ON_GEN_ENABLED);
        cbAutoSaveOnGen.setOnAction(_ -> {
            gameController.setAutoSaveOnGenEnabled(cbAutoSaveOnGen.isSelected());
            autoSaveGenThresholdSpinner.setDisable(!cbAutoSaveOnGen.isSelected());
        });
        
        autoSaveGenThresholdSpinner = createValidatedSpinner(
            GameStats.MIN_AUTOSAVE_GEN_THRESHOLD, 
            GameStats.MAX_AUTOSAVE_GEN_THRESHOLD, 
            GameStats.DEFAULT_AUTOSAVE_GEN_THRESHOLD,
            value -> gameController.setAutoSaveGenThreshold(value)
        );
        autoSaveGenThresholdSpinner.setDisable(!cbAutoSaveOnGen.isSelected());
        autoSaveGenThresholdSpinner.setMaxWidth(100);
        
        // Auto-Save on Best Life Time
        cbAutoSaveOnBLT = createAutoSaveCheckBox("On Best Life Time (s)", componentsWidth);
        cbAutoSaveOnBLT.setSelected(GameStats.DEFAULT_IS_AUTOSAVE_ON_BLT_ENABLED);
        cbAutoSaveOnBLT.setOnAction(_ -> {
            gameController.setAutoSaveOnBLTEnabled(cbAutoSaveOnBLT.isSelected());
            autoSaveBLThresholdSpinner.setDisable(!cbAutoSaveOnBLT.isSelected());
        });
        
        autoSaveBLThresholdSpinner = createValidatedSpinner(
            GameStats.MIN_AUTOSAVE_BLT_THRESHOLD,
            GameStats.MAX_AUTOSAVE_BLT_THRESHOLD,
            GameStats.DEFAULT_AUTOSAVE_BLT_THRESHOLD,
            value -> gameController.setAutoSaveBLTThreshold(value)
        );
        autoSaveBLThresholdSpinner.setDisable(!cbAutoSaveOnBLT.isSelected());
        autoSaveBLThresholdSpinner.setMaxWidth(100);
        
        // Auto-Save on Max Tubes Passed
        cbAutoSaveOnMaxTubePassed = createAutoSaveCheckBox("On Max Tubes Passed", componentsWidth);
        cbAutoSaveOnMaxTubePassed.setSelected(GameStats.DEFAULT_IS_AUTOSAVE_ON_MAX_TUBE_PASSED_ENABLED);
        cbAutoSaveOnMaxTubePassed.setOnAction(_ -> {
            gameController.setAutoSaveOnMaxTubePassedEnabled(cbAutoSaveOnMaxTubePassed.isSelected());
            autoSaveMaxTubePassedThresholdSpinner.setDisable(!cbAutoSaveOnMaxTubePassed.isSelected());
        });
        
        autoSaveMaxTubePassedThresholdSpinner = createValidatedSpinner(
            GameStats.MIN_AUTOSAVE_MAX_TUBE_PASSED_THRESHOLD,
            GameStats.MAX_AUTOSAVE_MAX_TUBE_PASSED_THRESHOLD,
            GameStats.DEFAULT_AUTOSAVE_MAX_TUBE_PASSED_THRESHOLD,
            value -> gameController.setAutoSaveMaxTubePassedThreshold(value)
        );
        autoSaveMaxTubePassedThresholdSpinner.setDisable(!cbAutoSaveOnMaxTubePassed.isSelected());
        autoSaveMaxTubePassedThresholdSpinner.setMaxWidth(100);
        
        autoSavePanel.getChildren().addAll(
            autoSaveTitle,
            cbAutoSaveOnGen, autoSaveGenThresholdSpinner,
            cbAutoSaveOnBLT, autoSaveBLThresholdSpinner,
            cbAutoSaveOnMaxTubePassed, autoSaveMaxTubePassedThresholdSpinner
        );
        
        // Load Brain Button
        bLoadBrain = createImportExportButton("Load Brain From File", Color.CYAN, componentsWidth);
        bLoadBrain.setOnAction(_ -> handleLoadBrain());
        
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        panel.getChildren().addAll(title, bSaveBrain, autoSavePanel, bLoadBrain, spacer);
        
        return panel;
    }
    
    private VBox createChronometerPanel(int panelWidth) {
        VBox panel = new VBox(10);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setBackground(new Background(new BackgroundFill(CHRONOMETER_BACKGROUND_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));
        panel.setPrefWidth(panelWidth);
        panel.setMinWidth(MIN_CHRONOMETER_PANEL_WIDTH);
        panel.setPrefHeight(MIN_CHRONOMETER_PANEL_HEIGHT);
        panel.setMinHeight(MIN_CHRONOMETER_PANEL_HEIGHT);
        panel.setPadding(new Insets(15, 10, 15, 10));
        
        Label title = new Label("Chronometer");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        title.setStyle("-fx-border-color: black; -fx-border-width: 1; -fx-padding: 5;");
        
        lTime = new Label("Time:");
        lTime.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        lTimeValue = new Label("00:00:00.00");
        lTimeValue.setFont(Font.font("Courier New", FontWeight.BOLD, 18));
        lTimeValue.setTextFill(Color.DARKGRAY);
        lTimeValue.setStyle("-fx-background-color: white; -fx-border-color: gray; -fx-border-width: 2; -fx-padding: 5 10 5 10;");
        
        panel.getChildren().addAll(title, lTime, lTimeValue);
        
        return panel;
    }
    
    private VBox createCentralPanel() {
        VBox centralPanel = new VBox();
        centralPanel.setPrefWidth(initialWidth - MIN_IMPORT_EXPORT_PANEL_WIDTH);
        centralPanel.setPrefHeight(initialHeight);
        
        HBox statsPanel = createStatsPanel();
        StackPane gamePanel = createGamePanel();
        VBox controlsPanel = createControlsPanel();
        
        VBox.setVgrow(gamePanel, Priority.ALWAYS);
        
        centralPanel.getChildren().addAll(statsPanel, gamePanel, controlsPanel);
        
        return centralPanel;
    }
    
    private HBox createStatsPanel() {
        HBox panel = new HBox();
        panel.setAlignment(Pos.CENTER);
        panel.setBackground(new Background(new BackgroundFill(STATS_BACKGROUND_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));
        panel.setPrefHeight(MIN_STATS_PANEL_HEIGHT);
        panel.setMinHeight(MIN_STATS_PANEL_HEIGHT);
        panel.setMinWidth(MIN_STATS_PANEL_WIDTH);
        panel.setPadding(new Insets(9, 5, 9, 5));
        panel.setSpacing(5);
        
        // Left stats
        HBox leftStats = new HBox(5);
        leftStats.setAlignment(Pos.CENTER_LEFT);
        
        lFPS = createStatsLabel("FPS: 0/" + GameClock.MAX_FPS);
        lCurrLifeTime = createStatsLabel("LT: 0,00s");
        lBestLifeTime = createStatsLabel("BLT: 0,00s");
        
        leftStats.getChildren().addAll(lFPS, lCurrLifeTime, lBestLifeTime);
        
        // Right stats
        HBox rightStats = new HBox(5);
        rightStats.setAlignment(Pos.CENTER_RIGHT);
        
        lNGen = createStatsLabel("Gen: 1");
        lNBirds = createStatsLabel("Birds: 0");
        lNTubePassed = createStatsLabel("Tubes: 0");
        lMaxTubePassed = createStatsLabel("Max Tubes: 0");
        lAutoSave = createStatsLabel("Auto-Save: ON");
        
        rightStats.getChildren().addAll(lNGen, lNBirds, lNTubePassed, lMaxTubePassed, lAutoSave);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        panel.getChildren().addAll(leftStats, spacer, rightStats);
        
        return panel;
    }
    
    private StackPane createGamePanel() {
        StackPane panel = new StackPane();
        panel.setBackground(new Background(new BackgroundFill(GAME_BACKGROUND_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));
        panel.setMinWidth(MIN_GAME_PANEL_WIDTH);
        panel.setMinHeight(MIN_GAME_PANEL_HEIGHT);
        
        gameCanvas = new Canvas(getGameWidth(), getGameHeight());
        gc = gameCanvas.getGraphicsContext2D();
        
        // Ridimensionamento canvas con il pannello
        panel.widthProperty().addListener((_, _, newVal) -> {
            gameCanvas.setWidth(newVal.doubleValue());
            repaintGame();
        });
        
        panel.heightProperty().addListener((_, _, newVal) -> {
            gameCanvas.setHeight(newVal.doubleValue());
            repaintGame();
        });
        
        panel.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                gameController.togglePause();
            }
        });
        
        panel.getChildren().add(gameCanvas);
        
        return panel;
    }
    
    private VBox createControlsPanel() {
        VBox panel = new VBox();
        panel.setAlignment(Pos.CENTER);
        panel.setBackground(new Background(new BackgroundFill(CONTROLS_BACKGROUND_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));
        panel.setPrefHeight(MIN_CONTROLS_PANEL_HEIGHT);
        panel.setMinHeight(MIN_CONTROLS_PANEL_HEIGHT);
        panel.setMinWidth(MIN_GAME_PANEL_WIDTH);
        panel.setPadding(new Insets(20));
        
        Label sliderTitle = new Label("Velocity Multiplier");
        sliderTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        sliderTitle.setTextFill(Color.WHITE);
        
        velocitySlider = new Slider(1, 15, 1);
        velocitySlider.setShowTickLabels(true);
        velocitySlider.setShowTickMarks(true);
        velocitySlider.setMajorTickUnit(1);
        velocitySlider.setMinorTickCount(0);
        velocitySlider.setBlockIncrement(1);
        velocitySlider.setSnapToTicks(true);
        velocitySlider.setPrefWidth(800);
        velocitySlider.valueProperty().addListener((_, _, newVal) -> {
            gameController.setDtMultiplier(newVal.intValue());
        });
        
        panel.getChildren().addAll(sliderTitle, velocitySlider);
        
        return panel;
    }
    
    private Button createImportExportButton(String text, Color backgroundColor, int width) {
        Button button = new Button(text);
        button.setMaxWidth(width);
        button.setPrefWidth(width);
        button.setMinHeight(30);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        button.setStyle(String.format("-fx-background-color: #%02x%02x%02x; -fx-text-fill: black;",
            (int)(backgroundColor.getRed() * 255),
            (int)(backgroundColor.getGreen() * 255),
            (int)(backgroundColor.getBlue() * 255)));
        return button;
    }
    
    private CheckBox createAutoSaveCheckBox(String text, int width) {
        CheckBox checkBox = new CheckBox(text);
        checkBox.setMaxWidth(width);
        checkBox.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        return checkBox;
    }
    
    private Spinner<Integer> createValidatedSpinner(int min, int max, int initial, Consumer<Integer> onChange) {
        Spinner<Integer> spinner = new Spinner<>(min, max, initial);
        spinner.setEditable(true);
        
        spinner.valueProperty().addListener((_, _, newVal) -> {
            if (newVal != null && onChange != null) {
                onChange.accept(newVal);
            }
        });
        
        // Converter personalizzato per gestire input non validi
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, initial));
        
        return spinner;
    }
    
    private Label createStatsLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        label.setTextFill(Color.RED);
        label.setBackground(new Background(new BackgroundFill(Color.GREEN, CornerRadii.EMPTY, Insets.EMPTY)));
        label.setStyle("-fx-border-color: red; -fx-border-width: 1; -fx-padding: 2 5 2 5;");
        return label;
    }
    
    private void drawGameObjects(GraphicsContext gc) {
        gc.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        
        // Draw background
        if (backgroundImage != null) {
            gc.drawImage(backgroundImage, 0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        }
        
        // Draw game objects usando un BufferedImage temporaneo per compatibilità
        if (currentVGameObj != null && !currentVGameObj.isEmpty()) {
            BufferedImage tempImage = new BufferedImage(
                (int)gameCanvas.getWidth(), 
                (int)gameCanvas.getHeight(), 
                BufferedImage.TYPE_INT_ARGB
            );
            Graphics2D g2d = tempImage.createGraphics();
            
            for (GameObject obj : currentVGameObj) {
                if (obj != null) {
                    obj.draw(g2d);
                }
            }
            
            g2d.dispose();
            
            // Converti BufferedImage in JavaFX Image e disegnalo
            javafx.scene.image.WritableImage fxImage = convertToFxImage(tempImage);
            gc.drawImage(fxImage, 0, 0);
        }
        
        // Draw pause overlay
        if (!gameController.isGameRunning()) {
            drawPauseOverlay(gc);
        }
    }
    
    private javafx.scene.image.WritableImage convertToFxImage(BufferedImage image) {
        javafx.scene.image.WritableImage writableImage = new javafx.scene.image.WritableImage(
            image.getWidth(), image.getHeight()
        );
        javafx.scene.image.PixelWriter pw = writableImage.getPixelWriter();
        
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                pw.setArgb(x, y, image.getRGB(x, y));
            }
        }
        
        return writableImage;
    }
    
    private void drawPauseOverlay(GraphicsContext gc) {
        double width = gameCanvas.getWidth();
        double height = gameCanvas.getHeight();
        
        // Draw dark overlay
        gc.setFill(PAUSE_OVERLAY_COLOR);
        gc.fillRect(0, 0, width, height);
        
        // Calculate pause symbol size
        double symbolSize = Math.min(width, height) / 6;
        double symbolX = (width - symbolSize) / 2;
        double symbolY = (height - symbolSize) / 2;
        
        // Draw pause bars
        double barWidth = symbolSize * BAR_WIDTH_RATIO;
        double barHeight = symbolSize * BAR_HEIGHT_RATIO;
        double barSpacing = symbolSize * BAR_GAP_RATIO;
        
        double barY = symbolY + (symbolSize - barHeight) / 2;
        double bar1X = symbolX + (symbolSize - 2 * barWidth - barSpacing) / 2;
        double bar2X = bar1X + barWidth + barSpacing;
        
        gc.setFill(PAUSE_SYMBOL_COLOR);
        gc.fillRoundRect(bar1X, barY, barWidth, barHeight, 5, 5);
        gc.fillRoundRect(bar2X, barY, barWidth, barHeight, 5, 5);
        
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1.7);
        gc.strokeRoundRect(bar1X, barY, barWidth, barHeight, 5, 5);
        gc.strokeRoundRect(bar2X, barY, barWidth, barHeight, 5, 5);
        
        // Draw pause text
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, symbolSize / 4));
        String pauseText = "PAUSED";
        double textWidth = computeTextWidth(pauseText, gc.getFont());
        double textX = symbolX + (symbolSize - textWidth) / 2;
        double textY = symbolY + symbolSize + symbolSize / 4 + 10;
        
        gc.fillText(pauseText, textX + 3, textY + 3);
        gc.setFill(Color.WHITE);
        gc.fillText(pauseText, textX, textY);
    }
    
    private double computeTextWidth(String text, Font font) {
        javafx.scene.text.Text textNode = new javafx.scene.text.Text(text);
        textNode.setFont(font);
        return textNode.getLayoutBounds().getWidth();
    }
    
    private void handleSaveBrain() {
        if (gameController.getBestBirdBrain() == null) {
            showAlert(Alert.AlertType.WARNING, "Error", "No Brain Available for Saving!");
            return;
        }
        
        FileChooser fileChooser = createJsonFileChooser();
        String defaultFileName = gameController.createManualSaveFileName();
        fileChooser.setInitialFileName(defaultFileName);
        
        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            String fileName = file.getAbsolutePath();
            if (!fileName.toLowerCase().endsWith(".json")) {
                fileName += ".json";
            }
            
            try {
                gameController.saveBestBrain(Path.of(fileName));
                showAlert(Alert.AlertType.INFORMATION, "Success", "Brain Saved Successfully!");
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Error Saving Brain: " + e.getMessage());
            }
        }
    }
    
    private void handleLoadBrain() {
        FileChooser fileChooser = createJsonFileChooser();
        File file = fileChooser.showOpenDialog(primaryStage);
        
        if (file != null) {
            Optional<ButtonType> result = showConfirmation(
                "Confirm Load",
                "Loading the Brain will Reset the Game to Generation 1.\nContinue?"
            );
            
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    gameController.loadBrain(file.getAbsolutePath());
                    showAlert(Alert.AlertType.INFORMATION, "Success", 
                        "Brain Loaded Successfully!\nGame has been Reset to Generation 1.");
                } catch (IOException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", 
                        "Error Loading Brain: " + e.getMessage() + "\nPlease Verify the File is Valid.");
                }
            }
        }
    }
    
    private FileChooser createJsonFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JSON File", "*.json")
        );
        return fileChooser;
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    private Optional<ButtonType> showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait();
    }
    
    // GameView Interface Methods
    
    @Override
    public void setController(GameController gameController) {
        this.gameController = gameController;
    }
    
    @Override
    public void updateDisplay(GameClock clock, GameStats stats, List<AbstractGameObject> vGameObj) throws NullPointerException {
        Objects.requireNonNull(clock, "Game Clock Cannot be Null");
        Objects.requireNonNull(stats, "Game Stats Cannot be Null");
        Objects.requireNonNull(vGameObj, "Game Objects List Cannot be Null");
        
        // Aggiornare UI Thread-Safe
        Platform.runLater(() -> {
            updateStatsLabels(stats);
            updateChronometerLabel(clock);
            
            currentVGameObj = vGameObj;
            drawGameObjects(gc);
        });
    }
    
    private void updateStatsLabels(GameStats stats) {
        lFPS.setText("FPS: " + stats.fps + "/" + GameClock.MAX_FPS);
        
        lCurrLifeTime.setText("LT: " + GameClock.roundAndFormatTwoDecimals(stats.currLifeTime) + "s");
        
        if (stats.bestLifeTime != lastBestLifeTime) {
            lBestLifeTime.setText("BLT: " + GameClock.roundAndFormatTwoDecimals(stats.bestLifeTime) + "s");
            lastBestLifeTime = stats.bestLifeTime;
        }
        
        if (stats.nGen != lastGen) {
            lNGen.setText("Gen: " + stats.nGen);
            lastGen = stats.nGen;
        }
        
        lNBirds.setText("Birds: " + stats.nBirds);
        
        lNTubePassed.setText("Tubes: " + stats.nTubePassed);
        
        if (stats.nTubePassed > stats.maxTubePassed) {
            lMaxTubePassed.setText("Max Tubes: " + stats.nTubePassed);
        } else {
            lMaxTubePassed.setText("Max Tubes: " + stats.maxTubePassed);
        }
        
        if (stats.isAutoSaveEnabled() != lastAutoSaveStatus) {
            lAutoSave.setText("Auto-Save: " + (stats.isAutoSaveEnabled() ? "ON" : "OFF"));
            lAutoSave.setBackground(new Background(new BackgroundFill(
                stats.isAutoSaveEnabled() ? Color.GREEN : Color.GRAY, 
                CornerRadii.EMPTY, 
                Insets.EMPTY
            )));
            lastAutoSaveStatus = stats.isAutoSaveEnabled();
        }
    }
    
    private void updateChronometerLabel(GameClock clock) {
        if (gameController.isGameRunning()) {
            lTimeValue.setText(clock.getFormattedGameTimeElapsed());
        }
    }
    
    @Override
    public void showAutoSaveMessage(String msg) {
        Platform.runLater(() -> {
            lAutoSave.setText(msg);
            
            if (autoSaveMessageTimer != null) {
                autoSaveMessageTimer.stop();
            }
            
            autoSaveMessageTimer = new PauseTransition(Duration.seconds(3));
            autoSaveMessageTimer.setOnFinished(_ -> {
                lAutoSave.setText("Auto-Save: " + (gameController.isAutoSaveEnabled() ? "ON" : "OFF"));
            });
            autoSaveMessageTimer.play();
        });
    }
    
    @Override
    public int getGameWidth() {
        if (gameCanvas != null && gameCanvas.getWidth() > 0) {
            return (int) gameCanvas.getWidth();
        }
        
        return Math.max(initialWidth - MIN_IMPORT_EXPORT_PANEL_WIDTH, MIN_GAME_PANEL_WIDTH);
    }
    
    @Override
    public int getGameHeight() {
        if (gameCanvas != null && gameCanvas.getHeight() > 0) {
            return (int) gameCanvas.getHeight();
        }
        
        return Math.max(initialHeight - MIN_STATS_PANEL_HEIGHT - MIN_CONTROLS_PANEL_HEIGHT, MIN_GAME_PANEL_HEIGHT);
    }
    
    @Override
    public void repaintGame() {
        if (gc != null && currentVGameObj != null) {
            Platform.runLater(() -> drawGameObjects(gc));
        }
    }
}