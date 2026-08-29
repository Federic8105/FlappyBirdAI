/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.view;

import flappyBirdAI.controller.GameController;
import flappyBirdAI.persistence.BirdBrainFileStorage;
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
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.DirectoryChooser;
import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

//TODO: javadocs e organizzazione metodi, javaFX

public final class GameMenu extends Application {

	public static void main(String[] args) {
		// Avviare l'applicazione JavaFX
	    launch(args);
	}
	
	private static final String MENU_WINDOW_TITLE = "Flappy Bird AI - Menu";
	private static final String MENU_ICON_PATH = "/images/FB_ICON.png";
	private static final String MENU_BACKGROUND_IMAGE_PATH = "/images/MENU_BACKGROUND.png";
	private static final double MENU_BACKGROUND_OPACITY = 0.6;
	private static final Image MENU_ICON = new Image(GameMenu.class.getResourceAsStream(MENU_ICON_PATH));
	
	private static final int MIN_WIDTH = GameView.MIN_WINDOW_WIDTH;
    private static final int MIN_HEIGHT = GameView.MIN_WINDOW_HEIGHT;
    private static final int MIN_N_BIRDS = GameController.MIN_N_BIRDS_X_GEN;

    private static final int MAX_WIDTH = GameView.MAX_WINDOW_WIDTH;
    private static final int MAX_HEIGHT = GameView.MAX_WINDOW_HEIGHT;
    private static final int MAX_N_BIRDS = GameController.MAX_N_BIRDS_X_GEN;

    private static final int DEFAULT_WIDTH = Math.max(1250, MIN_WIDTH);
    private static final int DEFAULT_HEIGHT = Math.max(750, MIN_HEIGHT);
    private static final int DEFAULT_N_BIRDS = 1000;
    private static final int DEFAULT_BIRDS_REGEN_PERC = 80;
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
    private static final String BROWSE_BUTTON_STYLE = "-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 4 10 4 10; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 4, 0.15, 0, 1);";
    private static final String BROWSE_BUTTON_HOVER_STYLE = "-fx-background-color: #1b5e20; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 4 10 4 10; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 6, 0.2, 0, 2);";
    private static final String RESET_BUTTON_STYLE = "-fx-background-color: #b71c1c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 4 10 4 10; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 4, 0.15, 0, 1);";
    private static final String RESET_BUTTON_HOVER_STYLE = "-fx-background-color: #8e0000; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 4 10 4 10; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 6, 0.2, 0, 2);";
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
    private static final String THICK_SEPARATOR_STYLE = CssUtils.toDataUri(
    		".thick-separator .line {"
	        + "-fx-background-color: white;"
	        + "-fx-background-radius: 8px;"
	        + "-fx-background-insets: 0;"
	        + "-fx-border-color:  #555555;"
	        + "-fx-border-width: 1px;"
	        + "-fx-border-radius: 8px;"
	        + "-fx-border-insets: 0;"
	        + "-fx-min-height: 6.5px;"
	        + "-fx-pref-height: 6.5px;"
	        + "-fx-max-height: 6.5px;"
	        + "}"
    );
    
    private GameController gameController;

    @Override
    public void start(Stage stage) {
    	// Pulizia dei file temporanei orfani rimasti da precedenti esecuzioni del gioco se esiste la directory di autosalvataggio
    	BirdBrainFileStorage.cleanupOrphanedTempFiles();
    	
    	configureStage(stage);
        
        // Spinner per larghezza e altezza finestra
    	Spinner<Integer> widthSpinner = createWidthSpinner();
        Spinner<Integer> heightSpinner = createHeightSpinner();
        // Contenitori verticali per larghezza e altezza finestra con label e range
        VBox widthField = rangedSpinnerField("Window Width: [px]", widthSpinner, SECONDARY_LABEL_TEXT_STYLE);
        VBox heightField = rangedSpinnerField("Window Height: [px]", heightSpinner, SECONDARY_LABEL_TEXT_STYLE);
        
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
        
        // Spinner per percentuale di uccelli rigenerati dal miglior cervello
        Spinner<Integer> birdsRegenPercSpinner = createBirdsRegenPercSpinner();
        VBox birdsRegenPercField = rangedSpinnerField("% of Regenerated Birds with Best Brain: [%]", birdsRegenPercSpinner, MAIN_LABEL_TEXT_STYLE);
        
        // Contenitore verticale per la directory di autosalvataggio
        VBox autosaveDirSection = labeledSection("Autosave Folder:", buildAutosaveDirSection(stage));

        // ToggleGroup per la scelta del motore grafico (Swing / JavaFX) con due ToggleButton, già mutualmente esclusivi
        ToggleButton swingButton = new ToggleButton("Swing");
        ToggleButton javaFXButton = new ToggleButton("JavaFX");
        ToggleGroup graphicsGroup = buildGraphicsEngineGroup(swingButton, javaFXButton);
        
        // Contenitore orizzontale per i due bottoni Swing / JavaFX 
        HBox graphicsChoiceBox = new HBox(15, swingButton, javaFXButton);
        graphicsChoiceBox.setAlignment(Pos.CENTER);
        VBox graphicsSection = labeledSection("Graphics Engine:", graphicsChoiceBox);

        // Bottone Start Game con testo e stile
        Button startButton = buildStartButton(stage, widthSpinner, heightSpinner, nBirdsSpinner, birdsRegenPercSpinner, cbFullScreen, graphicsGroup);

        // Contenitore verticale principale per tutti gli elementi del menù
        VBox root = assembleRoot(titleBox, windowModeSection, widthField, heightField, nBirdsField, birdsRegenPercField, autosaveDirSection, graphicsSection, startButton);
        
        // Contenitore a livelli per l'immagine di sfondo e il contenuto del menù sopra di essa
        StackPane container = buildContainer(root);
       
        // Creare la scena e impostarla sullo stage da mostrare
        Scene scene = new Scene(container);
        stage.setScene(scene);
        // Forzare il ridimensionamento della finestra in base al contenuto prima di mostrarla
        stage.sizeToScene();
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
        stage.getIcons().add(MENU_ICON);
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
    
    private Spinner<Integer> createBirdsRegenPercSpinner() {
        Spinner<Integer> spinner = new Spinner<>(0, 100, DEFAULT_BIRDS_REGEN_PERC, 1);
        spinner.setEditable(true);
        attachSpinnerClamping(spinner, 0, 100);
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
    
	// Costruisce la sezione per la scelta della cartella di autosalvataggio:
	// mostra il path corrente e permette di cambiarlo o ripristinare il default
	private VBox buildAutosaveDirSection(Stage stage) {
		TextField pathField = new TextField(BirdBrainFileStorage.getAutoSaveDir().toString());
		pathField.setEditable(false);
		pathField.setStyle("-fx-font-style: italic;");
		// Tooltip col path completo, utile se il campo viene troncato visivamente
		pathField.setTooltip(new Tooltip(pathField.getText()));
	
		Button browseButton = new Button("Browse");
		browseButton.setStyle(BROWSE_BUTTON_STYLE);
		
		Button resetButton = new Button("Reset");
		resetButton.setStyle(RESET_BUTTON_STYLE);
	    
	    // Cambiare lo stile dei bottoni quando il mouse passa sopra
	    browseButton.hoverProperty().addListener((_, _, isHovering) -> 
	        browseButton.setStyle(isHovering ? BROWSE_BUTTON_HOVER_STYLE : BROWSE_BUTTON_STYLE)
	    );
	    resetButton.hoverProperty().addListener((_, _, isHovering) -> 
	        resetButton.setStyle(isHovering ? RESET_BUTTON_HOVER_STYLE : RESET_BUTTON_STYLE)
	    );
		
		browseButton.setOnAction(_ -> {
		    DirectoryChooser chooser = new DirectoryChooser();
		    chooser.setTitle("Select Autosave Folder");
	
			// Se la cartella corrente esiste, usarla come punto di partenza del dialog
			File currentDir = BirdBrainFileStorage.getAutoSaveDir().toFile();
			if (currentDir.isDirectory()) {
				chooser.setInitialDirectory(currentDir);
			}
			
			File selected = chooser.showDialog(stage);
			// Se l'utente ha selezionato una cartella, aggiorna il path di autosalvataggio e il campo di testo
			if (selected != null) {
			    Path chosenDir = selected.toPath();
			    BirdBrainFileStorage.setAutoSaveDir(chosenDir);
			    pathField.setText(chosenDir.toString());
			    pathField.setTooltip(new Tooltip(chosenDir.toString()));
			}
		});
	
		resetButton.setOnAction(_ -> {
			Path defaultDir = BirdBrainFileStorage.getDefaultAutoSaveDir();
		    BirdBrainFileStorage.setAutoSaveDir(defaultDir);
		    pathField.setText(defaultDir.toString());
		    pathField.setTooltip(new Tooltip(defaultDir.toString()));
		});
		
		Region buttonsSpacer = new Region();
		// region viene espanso per spingere il bottone Reset a destra e lasciare il bottone Browse a sinistra
		HBox.setHgrow(buttonsSpacer, Priority.ALWAYS);
	
		HBox buttonsRow = new HBox(8, browseButton, buttonsSpacer, resetButton);
		
		return new VBox(4, pathField, buttonsRow);
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

    private Button buildStartButton(Stage stage, Spinner<Integer> widthSpinner, Spinner<Integer> heightSpinner, Spinner<Integer> nBirdsSpinner, Spinner<Integer> birdsRegenPercSpinner, CheckBox cbFullScreen, ToggleGroup graphicsGroup) {
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
            int birdsRegenPerc = birdsRegenPercSpinner.getValue();
            
            // Chiudere il menù prima di avviare il gioco
            stage.close();

            gameController = new GameController(useFX ? new FxGameView(w, h, isFullScreen) : new SwingGameView(w, h, isFullScreen), nBirds, birdsRegenPerc);
            // Avviare il gioco in un thread separato per evitare di bloccare l'interfaccia utente e permettere la gestione degli errori in modo asincrono
            new Thread(this::startGame, "game-thread").start();
        });

        return startButton;
    }

    private VBox assembleRoot(VBox titleBox, VBox windowModeSection, VBox widthField, VBox heightField, VBox nBirdsField, VBox birdsRegenPercField, VBox autosaveDirSection, VBox graphicsSection, Button startButton) {
    	// Linee di separazione
    	Separator[] seps = new Separator[2];
    	for (int i = 0; i < seps.length; i++) {
    		seps[i] = new Separator();
    		seps[i].setPrefWidth(CONTENT_WIDTH);
    		seps[i].getStyleClass().add("thick-separator");
    		seps[i].getStylesheets().add(THICK_SEPARATOR_STYLE);
		}

        VBox root = new VBox(14,
                titleBox,
                windowModeSection,
                widthField,
                heightField,
                seps[0],
                nBirdsField,
                birdsRegenPercField,
                autosaveDirSection,
                seps[1],
                graphicsSection,
                startButton
        );
        root.setAlignment(Pos.CENTER);
        // Impostare un padding nei lati del contenitore principale per evitare che gli elementi tocchino i bordi della finestra
        // Top, Right, Bottom, Left
        root.setPadding(new Insets(0, 50, 20, 50));
        // Sfondo trasparente per permettere di vedere l'immagine di sfondo
        root.setStyle("-fx-background-color: transparent;");
        
        // Aggiungere un margine extra sopra il bottone Start
        VBox.setMargin(startButton, new Insets(10, 0, 0, 0));

        return root;
    }

    private StackPane buildContainer(VBox root) {
    	// Caricare l'immagine di sfondo e impostare l'opacità
    	// posizione di default è già (0,0) angolo in alto a sinistra
        ImageView backgroundView = new ImageView(new Image(getClass().getResourceAsStream(MENU_BACKGROUND_IMAGE_PATH)));
        backgroundView.setOpacity(MENU_BACKGROUND_OPACITY);
        backgroundView.setPreserveRatio(false);
        // immagine di sfondo resa "unmanaged" per evitare che il layout manager la consideri nel calcolo delle dimensioni del contenitore, userà solo i componenti di root
        backgroundView.setManaged(false);

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
            ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(MENU_ICON);
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
            	gameController.exitApplication();
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

    // Metodo per attaccare il clamping dei valori a uno Spinner, limitando i valori inseriti dall'utente tra min e max
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