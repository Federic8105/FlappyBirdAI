/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.view;

import flappyBirdAI.controller.GameController;
import flappyBirdAI.view.fx.FxGameView;
import flappyBirdAI.view.swing.SwingGameView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

//TODO: javaFX, javadocs e organizzazione metodi e + classi e + sotto package, reset chiamato per loadBird modifica nBirds e playOneGen vede generazione finita e fa ++
//TODO: + threads anche per gestire fine pausa senza attesa e times e per timer

public class GameMenu extends Application {

	public static void main(String[] args) {
	    launch(args);
	}
	
	private static final String MENU_WINDOW_TITLE = "Flappy Bird AI - Menù";
	public static final String MENU_ICON_PATH = "/res/FB_ICON.png";
	private static final String MENU_BACKGROUND_IMAGE_PATH = "/res/MENU_BACKGROUND.png";
	private static final double MENU_BACKGROUND_OPACITY = 0.6;
	
	private static final int MIN_WIDTH = GameView.MIN_WINDOW_WIDTH;
    private static final int MIN_HEIGHT = GameView.MIN_WINDOW_HEIGHT;
    private static final int MIN_N_BIRDS = GameController.MIN_N_BIRDS_X_GEN;

    private static final int MAX_WIDTH = GameView.MAX_WINDOW_WIDTH;
    private static final int MAX_HEIGHT = GameView.MAX_WINDOW_HEIGHT;
    private static final int MAX_N_BIRDS = GameController.MAX_N_BIRDS_X_GEN;

    private static final int DEFAULT_WIDTH = Math.max(1250, MIN_WIDTH);
    private static final int DEFAULT_HEIGHT = Math.max(750, MIN_HEIGHT);
    private static final int DEFAULT_N_BIRDS = 1000;
    private static final boolean DEFAULT_JAVAFX_CHOICE = false;

    private static final String MAIN_TITLE_STYLE = "-fx-font-size: 30px; -fx-font-weight: bold; -fx-fill: yellow; -fx-stroke: black; -fx-stroke-width: 1.2;";
    private static final String OPTIONS_TITLE_STYLE = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: green; -fx-stroke: black; -fx-stroke-width: 1.0;";
    private static final String TOGGLE_BASE_STYLE = "-fx-font-size: 16px; -fx-pref-width: 150px; -fx-pref-height: 60px;";
    private static final String TOGGLE_SELECTED_STYLE = TOGGLE_BASE_STYLE + " -fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;";
    private static final String TOGGLE_UNSELECTED_STYLE = TOGGLE_BASE_STYLE + " -fx-background-color: #cccccc; -fx-text-fill: black;";
    private static final String TRANSPARENT_TEXT_AREA_STYLE = toDataUri(
            ".error-text-area, .error-text-area .content, .error-text-area .viewport, .error-text-area .scroll-pane {"
            + "-fx-background-color: transparent;"
            + "-fx-background-insets: 0;"
            + "}"
            + ".error-text-area {"
            + "-fx-border-width: 0;"
            + "-fx-focus-color: transparent;"
            + "-fx-faint-focus-color: transparent;"
            + "}"
    );
    
    private static String toDataUri(String css) {
        return "data:text/css;base64," + Base64.getEncoder().encodeToString(css.getBytes(StandardCharsets.UTF_8));
    }
    
    private GameController gameController;

    @Override
    public void start(Stage stage) {
        stage.setTitle(MENU_WINDOW_TITLE);
        stage.getIcons().add(new Image(getClass().getResourceAsStream(MENU_ICON_PATH)));
        stage.setResizable(false);
        
        // Impedire che JavaFX si spenga quando questo Stage viene chiuso:
        // serve a mostrare l'Alert di errore anche quando il motore grafico scelto è Swing
        Platform.setImplicitExit(false);
        
        // Chiudere l'applicazione quando il menù viene chiuso con la 'X'
        stage.setOnCloseRequest(_ -> {
            Platform.exit();
            System.exit(0);
        });

        Spinner<Integer> widthSpinner = new Spinner<>(MIN_WIDTH, MAX_WIDTH, DEFAULT_WIDTH, 10);
        widthSpinner.setEditable(true);
        clampOnFocusLost(widthSpinner, MIN_WIDTH, MAX_WIDTH);

        Spinner<Integer> heightSpinner = new Spinner<>(MIN_HEIGHT, MAX_HEIGHT, DEFAULT_HEIGHT, 10);
        heightSpinner.setEditable(true);
        clampOnFocusLost(heightSpinner, MIN_HEIGHT, MAX_HEIGHT);

        VBox widthField = labeledField("Larghezza Finestra:", widthSpinner, MIN_WIDTH);
        VBox heightField = labeledField("Altezza Finestra:", heightSpinner, MIN_HEIGHT);

        // Titolo Menù
        Text mainTitle = new Text("Flappy Bird AI");
        mainTitle.setStyle(MAIN_TITLE_STYLE);

        // Titolo Sezione Opzioni
        Text optionsTitle = new Text("Opzioni Grafiche");
        optionsTitle.setStyle(OPTIONS_TITLE_STYLE);
        
        VBox titleBox = new VBox(4, mainTitle, optionsTitle);
        titleBox.setAlignment(Pos.CENTER);

        // Checkbox Mutuamente Esclusive: Schermo Intero / Finestra
        CheckBox cbFullScreen = new CheckBox("Schermo Intero");
        CheckBox cbWindowed = new CheckBox("Finestra");
        cbFullScreen.setSelected(true); // default: schermo intero

        VBox windowModeBox = new VBox(6, cbFullScreen, cbWindowed);
        windowModeBox.setAlignment(Pos.CENTER_LEFT);

        // Stato iniziale: campi w/h disabilitati e schiariti (schermo intero di default)
        setWindowFieldsEnabled(false, widthSpinner, heightSpinner, widthField, heightField);

        cbFullScreen.selectedProperty().addListener((_, _, isSelected) -> {
            if (isSelected) {
                cbWindowed.setSelected(false);
                setWindowFieldsEnabled(false, widthSpinner, heightSpinner, widthField, heightField);
            } else if (!cbWindowed.isSelected()) {
                cbFullScreen.setSelected(true); // impedire che nessuna delle due sia selezionata
            }
        });

        cbWindowed.selectedProperty().addListener((_, _, isSelected) -> {
            if (isSelected) {
                cbFullScreen.setSelected(false);
                setWindowFieldsEnabled(true, widthSpinner, heightSpinner, widthField, heightField);
            } else if (!cbFullScreen.isSelected()) {
                cbWindowed.setSelected(true);
            }
        });

        Separator separator = new Separator();

        Spinner<Integer> nBirdsSpinner = new Spinner<>(MIN_N_BIRDS, MAX_N_BIRDS, DEFAULT_N_BIRDS, 1);
        nBirdsSpinner.setEditable(true);
        clampOnFocusLost(nBirdsSpinner, MIN_N_BIRDS, MAX_N_BIRDS);

        // per bottoni mutuamente esclusivi
        ToggleGroup graphicsGroup = new ToggleGroup();

        ToggleButton swingButton = new ToggleButton("Swing");
        swingButton.setToggleGroup(graphicsGroup);
        swingButton.setUserData(false); // useJavaFX = false
        swingButton.setSelected(!DEFAULT_JAVAFX_CHOICE);
        swingButton.setStyle(TOGGLE_SELECTED_STYLE);

        ToggleButton javaFXButton = new ToggleButton("JavaFX");
        javaFXButton.setToggleGroup(graphicsGroup);
        javaFXButton.setUserData(true); // useJavaFX = true
        javaFXButton.setSelected(DEFAULT_JAVAFX_CHOICE);
        javaFXButton.setStyle(TOGGLE_UNSELECTED_STYLE);

        // Aggiornare lo Stile in Base alla Selezione
        graphicsGroup.selectedToggleProperty().addListener((_, _, newToggle) -> {
            swingButton.setStyle(newToggle == swingButton ? TOGGLE_SELECTED_STYLE : TOGGLE_UNSELECTED_STYLE);
            javaFXButton.setStyle(newToggle == javaFXButton ? TOGGLE_SELECTED_STYLE : TOGGLE_UNSELECTED_STYLE);
        });

        // Impedire la Deselezione di Entrambi (Almeno un Bottone Sempre Attivo)
        graphicsGroup.selectedToggleProperty().addListener((_, oldToggle, newToggle) -> {
            if (newToggle == null) {
                graphicsGroup.selectToggle(oldToggle);
            }
        });

        HBox graphicsChoiceBox = new HBox(15, swingButton, javaFXButton);
        graphicsChoiceBox.setAlignment(Pos.CENTER);

        Button startButton = new Button("Avvia Gioco");
        startButton.setOnAction(_ -> {
            boolean useJavaFX = (Boolean) graphicsGroup.getSelectedToggle().getUserData();
            boolean fullScreen = cbFullScreen.isSelected();

            int w = fullScreen ? MAX_WIDTH : widthSpinner.getValue();
            int h = fullScreen ? MAX_HEIGHT : heightSpinner.getValue();
            int nBirds = nBirdsSpinner.getValue();

            stage.close();

            gameController = new GameController(useJavaFX ? new FxGameView(w, h, fullScreen) : new SwingGameView(w, h, fullScreen), nBirds);
            new Thread(this::startGame, "game-thread").start();
        });

        VBox root = new VBox(14,
        		titleBox,
                new Label("Modalità Finestra:"),
                windowModeBox,
                widthField,
                heightField,
                separator,
                labeledField("Numero Uccelli per Generazione:", nBirdsSpinner, MIN_N_BIRDS),
                new Label("Motore Grafico:"),
                graphicsChoiceBox,
                startButton
        );
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50));
        root.setStyle("-fx-background-color: transparent;"); // lascia vedere l'immagine sotto

        ImageView backgroundView = new ImageView(new Image(getClass().getResourceAsStream(MENU_BACKGROUND_IMAGE_PATH)));
        backgroundView.setOpacity(MENU_BACKGROUND_OPACITY);
        backgroundView.setPreserveRatio(false);

        StackPane container = new StackPane(backgroundView, root);
        backgroundView.fitWidthProperty().bind(container.widthProperty());
        backgroundView.fitHeightProperty().bind(container.heightProperty());

        Scene scene = new Scene(container, 400, 480);
        stage.setScene(scene);
        stage.show();
    }
    
    private void startGame() {
    	boolean continueGame = true;
    	
    	while (continueGame) {
            try {
                gameController.playOneGen();
            } catch (Exception e) {
                continueGame = alertError(e.getMessage());
            }
        }
	}
    
    private boolean alertError(String errorMsg) throws RuntimeException {
        ButtonType restartButtonType = new ButtonType("Restart");
        ButtonType exitButtonType = new ButtonType("Exit");

        FutureTask<Boolean> task = new FutureTask<>(() -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("An error occurred while running the game:");
            alert.getDialogPane().setContent(createErrorTextArea(errorMsg));
            alert.getButtonTypes().setAll(restartButtonType, exitButtonType);
            
            // Forza il calcolo del CSS così il lookup trova già il nodo generato per l'header
            alert.getDialogPane().applyCss();
            Label headerLabel = (Label) alert.getDialogPane().lookup(".header-panel .label");
            headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            
            boolean restart = alert.showAndWait()
                    .map(bt -> bt == restartButtonType)
                    .orElse(false);

            if (restart) {
                gameController.resetGame();
            } else {
                gameController.closeGameView();
                Platform.exit();
                System.exit(0);
            }
            return restart;
        });

        Platform.runLater(task);

        try {
            return task.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (ExecutionException e) {
            throw new RuntimeException("Errore nella gestione dell'Alert", e.getCause());
        }
    }

    private TextArea createErrorTextArea(String errorMsg) {
        TextArea textArea = new TextArea(errorMsg);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefSize(480, 280);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        textArea.getStyleClass().add("error-text-area");
        textArea.getStylesheets().add(TRANSPARENT_TEXT_AREA_STYLE);
        return textArea;
    }
    
    private void setWindowFieldsEnabled(boolean enabled, Spinner<Integer> widthSpinner, Spinner<Integer> heightSpinner, VBox widthField, VBox heightField) {
        widthSpinner.setDisable(!enabled);
        heightSpinner.setDisable(!enabled);

        double opacity = enabled ? 1.0 : 0.45;
        widthField.setOpacity(opacity);
        heightField.setOpacity(opacity);
    }

    // etichetta con spinner e label
    private VBox labeledField(String labelText, Spinner<Integer> spinner, int minValue) {
        Label minLabel = new Label("(min: " + minValue + ")");
        minLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: gray;");

        HBox spinnerRow = new HBox(8, spinner, minLabel);
        spinnerRow.setAlignment(Pos.CENTER_LEFT);

        return new VBox(4, new Label(labelText), spinnerRow);
    }

    private void clampOnFocusLost(Spinner<Integer> spinner, int min, int max) {
        // Contenitore per l'Ultimo Valore Valido (inizializzato al valore corrente dello Spinner)
        int[] lastValidValue = { spinner.getValue() };

        // Aggiornare l'Ultimo Valore Valido ogni volta che lo Spinner cambia (frecce, o testo valido confermato)
        spinner.valueProperty().addListener((_, _, newValue) -> {
            if (newValue != null) {
                lastValidValue[0] = newValue;
            }
        });

        spinner.focusedProperty().addListener((_, _, isNowFocused) -> {
            if (!isNowFocused) {
                clampSpinnerValue(spinner, min, max, lastValidValue);
            }
        });

        // Stesso controllo anche alla pressione di Invio (senza perdere il focus)
        spinner.getEditor().setOnAction(_ -> clampSpinnerValue(spinner, min, max, lastValidValue));
    }

    // se il valore non è numerico, torna all'ultimo valore valido
    // se il valore è fuori dai limiti, lo riporta al limite più vicino
    private void clampSpinnerValue(Spinner<Integer> spinner, int min, int max, int[] lastValidValue) {
        try {
            int value = Integer.parseInt(spinner.getEditor().getText());
            int clamped = Math.max(min, Math.min(max, value));
            spinner.getValueFactory().setValue(clamped);
        } catch (NumberFormatException ex) {
            // Input non numerico: tornare all'ultimo valore valido invece del minimo
            spinner.getValueFactory().setValue(lastValidValue[0]);
        }
    }

}