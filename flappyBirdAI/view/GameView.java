/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.view;

import flappyBirdAI.controller.GameController;
import flappyBirdAI.controller.GameClock;
import flappyBirdAI.controller.GameStats;
import flappyBirdAI.model.AbstractGameObject;
import java.util.Set;

public interface GameView {
	
	// Window and UI Constants

	public static final String WINDOW_TITLE = "Flappy Bird AI";
	public static final String ICON_PATH = "/res/FB_ICON.png";
	public static final String GAME_BACKGROUND_IMAGE_PATH = "/res/BACKGROUND.png";
	public static final int TIMER_DELAY_MS = 2000;
    
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
    
    // Pause Symbol Ratios compared to symbol size
    public static final double BAR_WIDTH_RATIO = 1.0 / 4.5;
    public static final double BAR_HEIGHT_RATIO = 0.8;
    public static final double BAR_GAP_RATIO = 1.0 / 3.0;
	
    // Abstract Methods
    
	void setController(GameController controller);
    void updateDisplay(GameClock clock, GameStats stats, Set<AbstractGameObject> gameObjects);
    void showAutoSaveMessage(String message);
    int getGameWidth();
    int getGameHeight();
    void repaintGame();
}