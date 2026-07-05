/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.view;

import flappyBirdAI.controller.GameController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

//TODO: javaFX, javadocs e organizzazione metodi e + classi, nome brain caricato, eliminare doppio th?
//TODO: + threads anche per gestire fine pausa senza attesa e times e per timer

public class GameMenu extends Application {

	public static void main(String[] args) {
	    launch(args);
	}
	
	private static final int MIN_WIDTH = GameView.MIN_WINDOW_WIDTH;
    private static final int MIN_HEIGHT = GameView.MIN_WINDOW_HEIGHT;
    private static final int MIN_N_BIRDS = GameController.MIN_N_BIRDS_X_GEN;

    private static final int MAX_WIDTH = 3840;
    private static final int MAX_HEIGHT = 2160;
    private static final int MAX_N_BIRDS = 100_000;

    private static final int DEFAULT_WIDTH = Math.max(1250, MIN_WIDTH);
    private static final int DEFAULT_HEIGHT = Math.max(750, MIN_HEIGHT);
    private static final int DEFAULT_N_BIRDS = 1000;
    private static final boolean DEFAULT_JAVAFX_CHOICE = false;

    private static final String TOGGLE_BASE_STYLE = "-fx-font-size: 16px; -fx-pref-width: 150px; -fx-pref-height: 60px;";
    private static final String TOGGLE_SELECTED_STYLE = TOGGLE_BASE_STYLE + " -fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;";
    private static final String TOGGLE_UNSELECTED_STYLE = TOGGLE_BASE_STYLE + " -fx-background-color: #cccccc; -fx-text-fill: black;";
    
    private GameController gameController;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Flappy Bird AI - Configurazione");
        
        // Impedire che JavaFX si spenga quando questo Stage viene chiuso:
        // serve a mostrare l'Alert di errore anche quando il motore grafico scelto è Swing
        Platform.setImplicitExit(false);

        Spinner<Integer> widthSpinner = new Spinner<>(MIN_WIDTH, MAX_WIDTH, DEFAULT_WIDTH, 10);
        widthSpinner.setEditable(true);
        clampOnFocusLost(widthSpinner, MIN_WIDTH, MAX_WIDTH);

        Spinner<Integer> heightSpinner = new Spinner<>(MIN_HEIGHT, MAX_HEIGHT, DEFAULT_HEIGHT, 10);
        heightSpinner.setEditable(true);
        clampOnFocusLost(heightSpinner, MIN_HEIGHT, MAX_HEIGHT);

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
            int w = widthSpinner.getValue();
            int h = heightSpinner.getValue();
            int nBirds = nBirdsSpinner.getValue();
            boolean useJavaFX = (Boolean) graphicsGroup.getSelectedToggle().getUserData();

            stage.close();
            
            gameController = new GameController(useJavaFX ? new JavaFXGameView(w, h) : new SwingGameView(w, h), nBirds);
            // avvio del gioco in un thread separato per evitare di bloccare l'interfaccia utente
            new Thread(this::startGame, "game-thread").start();
        });

        VBox root = new VBox(14,
                labeledField("Larghezza Finestra:", widthSpinner, MIN_WIDTH),
                labeledField("Altezza Finestra:", heightSpinner, MIN_HEIGHT),
                labeledField("Numero Uccelli per Generazione:", nBirdsSpinner, MIN_N_BIRDS),
                new Label("Motore Grafico:"),
                graphicsChoiceBox,
                startButton
        );
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50));

        stage.setScene(new Scene(root, 400, 480));
        stage.show();
    }
    
    private void startGame() {
    	boolean continueGame = true;
    	
    	while (continueGame) {
            try {
                gameController.playOneGen();
            } catch (Exception e) {
                continueGame = alertError("Si è verificato un errore durante l'esecuzione del gioco: " + e.getMessage());
            }
        }
	}
    
	// Alert con errore e possibilità di riavviare il gioco o uscire.
	// Ritorna true se l'utente sceglie di riavviare, false se sceglie di uscire.
	// Nota: viene invocato dal game-thread, quindi l'alert deve essere mostrato sul thread JavaFX
	// (Platform.runLater) e il game-thread deve attendere la risposta dell'utente (CountDownLatch)
	// prima di proseguire, altrimenti si avrebbe un IllegalStateException e/o una race condition.
	private boolean alertError(String message) {
		 ButtonType restartButtonType = new ButtonType("Riavvia");
		 ButtonType exitButtonType = new ButtonType("Esci");
		
		 AtomicBoolean restart = new AtomicBoolean(false);
		 CountDownLatch latch = new CountDownLatch(1);
		
		 Platform.runLater(() -> {
		     Alert alert = new Alert(AlertType.ERROR);
		     alert.setTitle("Errore");
		 alert.setHeaderText(null);
		 alert.setContentText(message);
		 alert.getButtonTypes().setAll(restartButtonType, exitButtonType);
		
		 Optional<ButtonType> result = alert.showAndWait();
		
		 if (result.isPresent() && result.get() == restartButtonType) {
		     restart.set(true);
		     gameController.resetGame();
		     latch.countDown();
		 } else {
			    // Chiudere la finestra di gioco
			    gameController.closeGameView();

			    // Sbloccare il game-thread e fargli terminare il while loop naturalmente
			    restart.set(false);
			    latch.countDown();

			    // Fermare il toolkit JavaFX (necessario per via di setImplicitExit(false))
			    Platform.exit();
		}
		 });
		
		 try {
		     // Il game-thread attende qui finché l'utente non risponde all'alert
		     latch.await();
		 } catch (InterruptedException e) {
		     Thread.currentThread().interrupt();
		     return false;
		 }
		
		 return restart.get();
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