/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.view;

import flappyBirdAI.controller.GameController;
import flappyBirdAI.controller.GameStats;
import flappyBirdAI.model.AbstractGameObject;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.util.Set;

public interface GameView {
	
	// --- Costanti di Configurazione per Finestra e UI ---

	public static final String GAME_WINDOW_TITLE = "Flappy Bird AI";
	public static final String GAME_ICON_PATH = "/images/FB_ICON.png";
	public static final String GAME_BACKGROUND_IMAGE_PATH = "/images/GAME_BACKGROUND.png";
    
    // --- Costanti di Dimensioni Minime per Finestra e Pannelli ---
	
	public static final int MIN_STATS_PANEL_WIDTH = 1000;
	public static final int MIN_STATS_PANEL_HEIGHT = 40;
	public static final int MIN_CONTROLS_PANEL_HEIGHT = 150;
	public static final int MIN_IMPORT_EXPORT_PANEL_WIDTH = 250;
	public static final int MIN_CHRONOMETER_PANEL_WIDTH = MIN_IMPORT_EXPORT_PANEL_WIDTH;
	public static final int MIN_CHRONOMETER_PANEL_HEIGHT = MIN_CONTROLS_PANEL_HEIGHT;
	public static final int MIN_GAME_PANEL_WIDTH = MIN_STATS_PANEL_WIDTH;
	public static final int MIN_GAME_PANEL_HEIGHT = 500;
	public static final int MIN_IMPORT_EXPORT_PANEL_HEIGHT = MIN_GAME_PANEL_HEIGHT + MIN_STATS_PANEL_HEIGHT;
	public static final int MIN_WINDOW_WIDTH = MIN_STATS_PANEL_WIDTH + MIN_IMPORT_EXPORT_PANEL_WIDTH;
	public static final int MIN_WINDOW_HEIGHT = MIN_GAME_PANEL_HEIGHT + MIN_STATS_PANEL_HEIGHT + MIN_CONTROLS_PANEL_HEIGHT;
	
	// --- Costanti di Dimensioni Massime per Finestra ---
	
	// Ritorna dimensioni massime scalate impostate dall'OS per le finestre (escludendo barra delle applicazioni e taskbar)
	public static final Rectangle SCREEN_SIZE = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
	public static final int MAX_WINDOW_WIDTH = SCREEN_SIZE.width;
	public static final int MAX_WINDOW_HEIGHT = SCREEN_SIZE.height;
	
	// --- Costanti di Configurazione ---
    
    // Pause Symbol Ratios compared to symbol size
    public static final double BAR_WIDTH_RATIO = 1.0 / 4.5;
    public static final double BAR_HEIGHT_RATIO = 0.8;
    public static final double BAR_GAP_RATIO = 1.0 / 3.0;
    
    public static final int AUTO_SAVE_SUCCESS_DISPLAY_MS = 1250;
    public static final int CHRONOMETER_REFRESH_MS = 10;
	
    // --- Metodi Astratti per Gestione Ciclo di Vita ---
    
    void setController(GameController controller);
    void close();
    void exitGame();
    
    // --- Metodi Astratti per Aggiornamento della UI ---
    
    void updateDisplay(GameStats stats, Set<AbstractGameObject> vGameObjects);
    void repaintGame();
    void startChronometerTimer();
    
    // --- Metodi Astratti per Rendering e Animazioni ---
    
    void preloadSprites(Set<AbstractGameObject> vGameObjects);
    void updateAnimations();
    
    // --- Metodi Astratti per Gestione Pausa ---
    
    void togglePause();

	// --- Metodi Astratti per Gestione Messaggi e Notifiche ---
    
    void showBlockingWarning(String headerText, String detailText);
    void showAutoSaveMessage(boolean success, String headerText, String errorDetail);
    
    // --- Metodi Astratti per Getters Dimensioni Pannello di Gioco ---
    
    int getGameWidth();
    int getGameHeight();
    
}