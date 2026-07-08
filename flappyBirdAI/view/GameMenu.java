/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.view;

import flappyBirdAI.controller.GameController;
import flappyBirdAI.utils.CssUtils;
import flappyBirdAI.view.fx.FxGameView;
import flappyBirdAI.view.swing.SwingGameView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

//TODO: javaFX, javadocs e organizzazione metodi, reset chiamato per loadBird modifica nBirds e playOneGen vede generazione finita e fa ++
//TODO: quando carico file, timer esplode
//TODO: scrittura atomica, chisuura gioco attende fine scrittura su file
//TODO: eccezzione badFileformat
//TODO: + threads anche per gestire fine pausa senza attesa e times e per timer

public final class GameMenu extends Application {

	public static void main(String[] args) {
		// Avviare l'applicazione JavaFX
	    launch(args);
	}
	
	private static final String MENU_WINDOW_TITLE = "Flappy Bird AI - Menu";
	private static final String MENU_ICON_PATH = "/res/FB_ICON.png";
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
    private static final boolean DEFAULT_FULLSCREEN_CHOICE = false;
    
    private static final double CONTENT_WIDTH = 300; // per allineare tutti i blocchi a sinistra

    private static final String MAIN_TITLE_STYLE = "-fx-font-size: 30px; -fx-font-weight: bold; -fx-fill: yellow; -fx-stroke: black; -fx-stroke-width: 1.2;";
    private static final String OPTIONS_TITLE_STYLE = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: green; -fx-stroke: black; -fx-stroke-width: 1.0;";
    private static final String MAIN_LABEL_TEXT_STYLE = "-fx-font-size: 15px; -fx-font-weight: bold; -fx-fill: white; -fx-stroke: black; -fx-stroke-width: 0.8;";
    private static final String SECONDARY_LABEL_TEXT_STYLE = "-fx-font-size: 12px; -fx-font-weight: bold; -fx-fill: white; -fx-stroke: black; -fx-stroke-width: 0.6;";
    private static final String TOGGLE_BASE_STYLE = "-fx-font-size: 16px; -fx-pref-width: 150px; -fx-pref-height: 60px;";
    private static final String TOGGLE_SELECTED_STYLE = TOGGLE_BASE_STYLE + " -fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;";
    private static final String TOGGLE_UNSELECTED_STYLE = TOGGLE_BASE_STYLE + " -fx-background-color: #cccccc; -fx-text-fill: black;";
    private static final String START_BUTTON_STYLE = "-fx-background-color: #c62828; -fx-background-radius: 8; -fx-padding: 8 16 8 16; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 6, 0.2, 0, 2);";
    private static final String START_BUTTON_HOVER_STYLE = "-fx-background-color: #a01f1f; -fx-background-radius: 8; -fx-padding: 8 16 8 16; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 8, 0.25, 0, 3);";
    // Uso di Data URI perchè CSS inline non funziona con TextArea
    private static final String TRANSPARENT_TEXT_AREA_STYLE = CssUtils.toDataUri(
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
    
    private GameController gameController;

    @Override
    public void start(Stage stage) {
    	configureStage(stage);
        
        // Spinner per larghezza e altezza finestra
    	Spinner<Integer> widthSpinner = createWidthSpinner();
        Spinner<Integer> heightSpinner = createHeightSpinner();
        // Contenitori verticali per larghezza e altezza finestra con label e range
        VBox widthField = rangedSpinnerField("Window Width:", widthSpinner, SECONDARY_LABEL_TEXT_STYLE);
        VBox heightField = rangedSpinnerField("Window Height:", heightSpinner, SECONDARY_LABEL_TEXT_STYLE);
        
        // Contenitore verticale per i titoli del menù
        VBox titleBox = buildTitleBox();
        
        // Checkbox Schermo Intero / Finestra
        CheckBox cbFullScreen = new CheckBox("Full Screen");
        CheckBox cbWindowed = new CheckBox("Windowed");
 
        // Contenitore verticale per le checkbox
        VBox windowModeBox = buildWindowModeBox(cbFullScreen, cbWindowed, widthField, heightField);
        VBox windowModeSection = labeledSection("Window Mode:", windowModeBox);
        
        // Spinner per numero di uccelli per generazione
        Spinner<Integer> nBirdsSpinner = createNBirdsSpinner();
        VBox nBirdsField = rangedSpinnerField("Birds per Generation:", nBirdsSpinner, MAIN_LABEL_TEXT_STYLE);

        // ToggleGroup per la scelta del motore grafico (Swing / JavaFX) con due ToggleButton, già mutualmente esclusivi
        ToggleButton swingButton = new ToggleButton("Swing");
        ToggleButton javaFXButton = new ToggleButton("JavaFX");
        ToggleGroup graphicsGroup = buildGraphicsEngineGroup(swingButton, javaFXButton);
        
        // Contenitore orizzontale per i due bottoni Swing / JavaFX 
        HBox graphicsChoiceBox = new HBox(15, swingButton, javaFXButton);
        graphicsChoiceBox.setAlignment(Pos.CENTER);
        VBox graphicsSection = labeledSection("Graphics Engine:", graphicsChoiceBox);

        // Bottone Start Game con testo e stile
        Button startButton = buildStartButton(stage, widthSpinner, heightSpinner, nBirdsSpinner, cbFullScreen, graphicsGroup);

        // Contenitore verticale principale per tutti gli elementi del menù
        VBox root = assembleRoot(titleBox, windowModeSection, widthField, heightField, nBirdsField, graphicsSection, startButton);
        
        // Contenitore a livelli per l'immagine di sfondo e il contenuto del menù sopra di essa
        StackPane container = buildContainer(root);
       
        // Creare la scena e impostarla sullo stage da mostrare
        Scene scene = new Scene(container, 400, 500);
        stage.setScene(scene);
        stage.show();
        
        // .runLater() per accodare il codice da eseguire necessariamente dopo lo stage.show(), che pianifica internamente delle operazioni che potrebbero non essere ancora state eseguite
        // dopo lo stage.show() il focus viene dato al primo elemento interattivo
        // se la scelta di default è finestra, dare il focus alla checkbox Windowed (seconda checkbox)
        // se la scelta di default è schermo intero, il focus rimane sulla prima checkbox FullScreen come già accade di default
        Platform.runLater(() -> {
            if (!DEFAULT_FULLSCREEN_CHOICE) {
                cbWindowed.requestFocus();
            }
        });
    }
    
    private void configureStage(Stage stage) {
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
    }
    
    private Spinner<Integer> createWidthSpinner() {
        Spinner<Integer> spinner = new Spinner<>(MIN_WIDTH, MAX_WIDTH, DEFAULT_WIDTH, 10);
        spinner.setEditable(true);
        attachSpinnerClamping(spinner, MIN_WIDTH, MAX_WIDTH);
        return spinner;
    }

    private Spinner<Integer> createHeightSpinner() {
        Spinner<Integer> spinner = new Spinner<>(MIN_HEIGHT, MAX_HEIGHT, DEFAULT_HEIGHT, 10);
        spinner.setEditable(true);
        attachSpinnerClamping(spinner, MIN_HEIGHT, MAX_HEIGHT);
        return spinner;
    }

    private Spinner<Integer> createNBirdsSpinner() {
        Spinner<Integer> spinner = new Spinner<>(MIN_N_BIRDS, MAX_N_BIRDS, DEFAULT_N_BIRDS, 1);
        spinner.setEditable(true);
        attachSpinnerClamping(spinner, MIN_N_BIRDS, MAX_N_BIRDS);
        return spinner;
    }

    private VBox buildTitleBox() {
    	// Titolo Menù
        Text mainTitle = new Text("Flappy Bird AI");
        mainTitle.setStyle(MAIN_TITLE_STYLE);

        // Titolo Sezione Opzioni
        Text optionsTitle = new Text("Graphics Options");
        optionsTitle.setStyle(OPTIONS_TITLE_STYLE);

        // Blocco contenente entrambi i titoli
        VBox titleBox = new VBox(4, mainTitle, optionsTitle);
        titleBox.setAlignment(Pos.CENTER);
        return titleBox;
    }

    // Costruisce il blocco Fullscreen/Windowed: mutua esclusione tra le due checkbox
    // e abilitazione/disabilitazione degli spinner larghezza/altezza in base alla scelta
    private VBox buildWindowModeBox(CheckBox cbFullScreen, CheckBox cbWindowed, VBox widthField, VBox heightField) {
        
    	if (DEFAULT_FULLSCREEN_CHOICE) {
			cbFullScreen.setSelected(true);
		} else {
			cbWindowed.setSelected(true);
		}

        VBox windowModeBox = new VBox(6, cbFullScreen, cbWindowed);
        windowModeBox.setAlignment(Pos.CENTER_LEFT);

        // Stato iniziale dei campi w/h
        setWindowFieldsEnabled(cbWindowed.isSelected(), widthField, heightField);

        // Listener per checkbox mutuamente esclusive
        cbFullScreen.selectedProperty().addListener((_, _, isSelected) -> {
            if (isSelected) {
                cbWindowed.setSelected(false);
                setWindowFieldsEnabled(false, widthField, heightField);
            } else if (!cbWindowed.isSelected()) {
                cbFullScreen.setSelected(true); // impedire che nessuna delle due sia selezionata
            }
        });

        cbWindowed.selectedProperty().addListener((_, _, isSelected) -> {
            if (isSelected) {
                cbFullScreen.setSelected(false);
                setWindowFieldsEnabled(true, widthField, heightField);
            } else if (!cbFullScreen.isSelected()) {
                cbWindowed.setSelected(true);
            }
        });

        return windowModeBox;
    }

    // Costruisce il ToggleGroup Swing/JavaFX: mutua esclusione e aggiornamento stile in base alla selezione
    private ToggleGroup buildGraphicsEngineGroup(ToggleButton swingButton, ToggleButton javaFXButton) {
        ToggleGroup graphicsGroup = new ToggleGroup();

        swingButton.setToggleGroup(graphicsGroup);
        swingButton.setUserData(false); // useFX = false
        swingButton.setSelected(!DEFAULT_JAVAFX_CHOICE);
        swingButton.setStyle(TOGGLE_SELECTED_STYLE);

        javaFXButton.setToggleGroup(graphicsGroup);
        javaFXButton.setUserData(true); // useFX = true
        javaFXButton.setSelected(DEFAULT_JAVAFX_CHOICE);
        javaFXButton.setStyle(TOGGLE_UNSELECTED_STYLE);

        // Aggiornare lo Stile dei Bottoni in Base alla Selezione
        // Quando un bottone viene selezionato, il suo stile diventa quello selezionato e l'altro bottone diventa quello non selezionato
        graphicsGroup.selectedToggleProperty().addListener((_, _, newToggle) -> {
            swingButton.setStyle(newToggle == swingButton ? TOGGLE_SELECTED_STYLE : TOGGLE_UNSELECTED_STYLE);
            javaFXButton.setStyle(newToggle == javaFXButton ? TOGGLE_SELECTED_STYLE : TOGGLE_UNSELECTED_STYLE);
        });

        // Impedire la Deselezione di Entrambi (Almeno un Bottone Sempre Attivo)
        // Se l'utente prova a deselezionare il bottone selezionato, lo ri-seleziona automaticamente
        graphicsGroup.selectedToggleProperty().addListener((_, oldToggle, newToggle) -> {
            if (newToggle == null) {
                graphicsGroup.selectToggle(oldToggle);
            }
        });

        return graphicsGroup;
    }

    private Button buildStartButton(Stage stage, Spinner<Integer> widthSpinner, Spinner<Integer> heightSpinner, Spinner<Integer> nBirdsSpinner, CheckBox cbFullScreen, ToggleGroup graphicsGroup) {
    	// Testo "Start Game" per applicare lo stile al testo
    	Text startButtonText = new Text("Start Game");
        startButtonText.setStyle(MAIN_LABEL_TEXT_STYLE);

        // Bottone Start Game
        Button startButton = new Button();
        startButton.setGraphic(startButtonText);
        startButton.setStyle(START_BUTTON_STYLE);

        // Cambiare lo stile del bottone quando il mouse passa sopra
        startButton.hoverProperty().addListener((_, _, isHovering) ->
                startButton.setStyle(isHovering ? START_BUTTON_HOVER_STYLE : START_BUTTON_STYLE)
        );

    	// Avvio gioco quando si preme il bottone Start Game
        startButton.setOnAction(_ -> {
            boolean useFX = (Boolean) graphicsGroup.getSelectedToggle().getUserData();
            boolean isFullScreen = cbFullScreen.isSelected();

            int w = isFullScreen ? MAX_WIDTH : widthSpinner.getValue();
            int h = isFullScreen ? MAX_HEIGHT : heightSpinner.getValue();
            int nBirds = nBirdsSpinner.getValue();
            
            // Chiudere il menù prima di avviare il gioco
            stage.close();

            gameController = new GameController(useFX ? new FxGameView(w, h, isFullScreen) : new SwingGameView(w, h, isFullScreen), nBirds);
            // Avviare il gioco in un thread separato per evitare di bloccare l'interfaccia utente e permettere la gestione degli errori in modo asincrono
            new Thread(this::startGame, "game-thread").start();
        });

        return startButton;
    }

    private VBox assembleRoot(VBox titleBox, VBox windowModeSection, VBox widthField, VBox heightField, VBox nBirdsField, VBox graphicsSection, Button startButton) {
    	// Linea di separazione
    	Separator separator = new Separator();

        VBox root = new VBox(14,
                titleBox,
                windowModeSection,
                widthField,
                heightField,
                separator, // separatore tra le opzioni di finestra e il resto
                nBirdsField,
                graphicsSection,
                startButton
        );
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50));
        // Sfondo trasparente per permettere di vedere l'immagine di sfondo
        root.setStyle("-fx-background-color: transparent;");

        // Aggiungere margine inferiore al pulsante "Start Game"
        VBox.setMargin(startButton, new Insets(0, 0, 20, 0));

        return root;
    }

    private StackPane buildContainer(VBox root) {
    	// Caricare l'immagine di sfondo e impostare l'opacità
        ImageView backgroundView = new ImageView(new Image(getClass().getResourceAsStream(MENU_BACKGROUND_IMAGE_PATH)));
        backgroundView.setOpacity(MENU_BACKGROUND_OPACITY);
        backgroundView.setPreserveRatio(false);

        // Contenitore a livelli per l'immagine messa come sfondo e il contenuto del menù sopra di essa
        StackPane container = new StackPane(backgroundView, root);
        // Impostare le dimensioni dell'immagine di sfondo per adattarsi al contenitore se viene ridimensionato
        backgroundView.fitWidthProperty().bind(container.widthProperty());
        backgroundView.fitHeightProperty().bind(container.heightProperty());

        return container;
    }
    
    private void startGame() {
    	boolean continueGame = true;
    	
    	while (continueGame) {
            try {
                gameController.playOneGen();
            } catch (Exception e) {
                continueGame = handleGameError(e.getMessage());
            }
        }
	}
    
    private boolean handleGameError(String errorMsg) throws RuntimeException {
        ButtonType restartButtonType = new ButtonType("Restart");
        ButtonType exitButtonType = new ButtonType("Exit");

        // Operazione Async per mostrare l'Alert e attendere la risposta dell'utente senza bloccare il thread del gioco
        FutureTask<Boolean> task = new FutureTask<>(() -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("An error occurred while running the game:");
            // impostare contenuto con TextArea personalizzata
            alert.getDialogPane().setContent(createErrorTextArea(errorMsg));
            alert.getButtonTypes().setAll(restartButtonType, exitButtonType);
            
            // Forza il calcolo del CSS così il lookup trova già il nodo generato per l'header
            alert.getDialogPane().applyCss();
            // Recupero del nodo Label dell'header tramite selettore CSS e applicazione di uno stile personalizzato
            Label headerLabel = (Label) alert.getDialogPane().lookup(".header-panel .label");
            headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            
            // Mostra l'Alert e attendi la risposta dell'utente
            // Se l'utente preme "Restart", il metodo ritorna true, altrimenti false
            boolean restart = alert.showAndWait().map(bt -> bt == restartButtonType).orElse(false);

            if (restart) {
                gameController.resetGame();
            } else {
                gameController.closeGameView();
                // Chiusura pulita del thread JavaFX (menu e FxGameView se in uso)
                Platform.exit();
                // Chiusura della JVM per terminare altri thread (game-thread e SwingGameView se in uso)
                System.exit(0);
            }
            
            // eseguito solo se restart è true, altrimenti il thread del gioco si chiude
            return restart;
        });

        // Esegui il task sul thread JavaFX in modo thread-safe, inviando il codice da eseguire al thread JavaFX tramite Platform.runLater da un altro thread (game-thread) senza bloccare il thread del gioco
        // l'errore del gioco arriva da un altro thread ma per modificare l'interfaccia grafica bisogna passare al thread JavaFX
        Platform.runLater(task);

        try {
        	// Attendere il completamento del task e ottenere il risultato boolean
            return task.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (ExecutionException e) {
            throw new RuntimeException("Error while handling game error: " + e.getCause().getMessage(), e.getCause());
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
    
    // abilitare/disabilitare campi larghezza e altezza finestra in base alla scelta di schermo intero o finestra
    private void setWindowFieldsEnabled(boolean enabled, VBox widthField, VBox heightField) {
    	widthField.getChildren().forEach(node -> node.setDisable(!enabled));
    	heightField.getChildren().forEach(node -> node.setDisable(!enabled));
    	
        // campi opachi se disabilitati
        double opacity = enabled ? 1.0 : 0.45;
        widthField.setOpacity(opacity);
        heightField.setOpacity(opacity);
    }

    // creare campo contenitore verticale con label con nome, spinner e label con range min-max
    private VBox rangedSpinnerField(String labelText, Spinner<Integer> spinner, String labelStyle) {
        Text label = new Text(labelText);
        label.setStyle(labelStyle);

        // Recuperare i valori min e max dallo Spinner per mostrarli nella label
        SpinnerValueFactory.IntegerSpinnerValueFactory spinnerFactory = (SpinnerValueFactory.IntegerSpinnerValueFactory) spinner.getValueFactory();

        Label rangeLabel = new Label("[Min: " + spinnerFactory.getMin() + " - Max: " + spinnerFactory.getMax() + "]");
        rangeLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #555555;");

        // Contenitore orizzontale per spinner e label con range
        HBox spinnerRow = new HBox(8, spinner, rangeLabel);
        spinnerRow.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(4, label, spinnerRow);
        // Impostare la larghezza preferita del VBox per allineare tutti i blocchi a sinistra
        box.setPrefWidth(CONTENT_WIDTH);
        return box;
    }
    
    // creare campo contenitore verticale con label e contenuto qualsiasi
    private VBox labeledSection(String labelText, Node content) {
        Text label = new Text(labelText);
        label.setStyle(MAIN_LABEL_TEXT_STYLE);

        VBox box = new VBox(6, label, content);
        box.setPrefWidth(CONTENT_WIDTH);
        return box;
    }

    private void attachSpinnerClamping(Spinner<Integer> spinner, int min, int max) {
        // Contenitore per l'Ultimo Valore Valido (inizializzato al valore corrente dello Spinner)
    	// Uso di un array per permettere la modifica del valore all'interno del listener (altrimenti sarebbe final o effectively final)
        int[] lastValidValue = { spinner.getValue() };

        // Aggiornare l'Ultimo Valore Valido ogni volta che lo Spinner cambia (frecce, o testo valido confermato)
        spinner.valueProperty().addListener((_, _, newValue) -> {
            if (newValue != null) {
                lastValidValue[0] = newValue;
            }
        });

        // Controllo del valore quando lo Spinner perde il focus
        spinner.focusedProperty().addListener((_, _, isNowFocused) -> {
            if (!isNowFocused) {
                clampSpinnerValue(spinner, min, max, lastValidValue);
            }
        });

        // Controllo del valore quando l'utente preme Invio
        spinner.getEditor().setOnAction(_ -> clampSpinnerValue(spinner, min, max, lastValidValue));
    }

    // Controllo del valore dello Spinner e correzione se necessario
    private void clampSpinnerValue(Spinner<Integer> spinner, int min, int max, int[] lastValidValue) {
        try {
            int value = Integer.parseInt(spinner.getEditor().getText());
            // se il valore è fuori dai limiti, lo riporta al limite più vicino
            int clamped = Math.max(min, Math.min(max, value));
            spinner.getValueFactory().setValue(clamped);
        } catch (NumberFormatException ex) {
        	// se il valore non è numerico, torna all'ultimo valore valido
            spinner.getValueFactory().setValue(lastValidValue[0]);
        }
    }

}