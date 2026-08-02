/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.view;

import flappyBirdAI.controller.GameController;
import flappyBirdAI.controller.GameClock;
import flappyBirdAI.controller.GameStats;
import flappyBirdAI.model.AbstractGameObject;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.util.Set;

public interface GameView {
	
	// Window and UI Constants

	public static final String GAME_WINDOW_TITLE = "Flappy Bird AI";
	public static final String GAME_ICON_PATH = "/res/FB_ICON.png";
	public static final String GAME_BACKGROUND_IMAGE_PATH = "/res/GAME_BACKGROUND.png";
	public static final int AUTO_SAVE_SUCCESS_DISPLAY_MS = 1250;
    
    // Panel Minimum Dimensions Constants
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
	
	// Window Maximum Dimensions Constants
	// Ritorna dimensioni massime scalate impostate dall'OS per le finestre (escludendo barra delle applicazioni e taskbar)
	public static final Rectangle SCREEN_SIZE = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
	public static final int MAX_WINDOW_WIDTH = SCREEN_SIZE.width;
	public static final int MAX_WINDOW_HEIGHT = SCREEN_SIZE.height;
    
    // Pause Symbol Ratios compared to symbol size
    public static final double BAR_WIDTH_RATIO = 1.0 / 4.5;
    public static final double BAR_HEIGHT_RATIO = 0.8;
    public static final double BAR_GAP_RATIO = 1.0 / 3.0;
    
    // Chronometer Refresh Rate (ms)
    public static final int CHRONOMETER_REFRESH_MS = 10;
	
    // Abstract Methods
    
    void exitGame();
    void close();
	void setController(GameController controller);
	void startChronometerTimer();
    void updateDisplay(GameClock clock, GameStats stats, Set<AbstractGameObject> vGameObjects);
    void showBlockingWarning(String headerText, String detailText);
    void showAutoSaveMessage(boolean success, String headerText, String errorDetail);
    void togglePause();
    int getGameWidth();
    int getGameHeight();
    void repaintGame();
    void preloadSprites(Set<AbstractGameObject> vGameObjects);
    
}