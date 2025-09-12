/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.view;

import flappyBirdAI.controller.GameController;
import flappyBirdAI.controller.GameClock;
import flappyBirdAI.controller.GameStats;
import flappyBirdAI.model.AbstractGameObject;
import java.util.List;

public interface GameView {
	void setController(GameController controller);
    void updateDisplay(GameClock clock, GameStats stats, List<AbstractGameObject> gameObjects);
    void showAutoSaveMessage(String message);
    int getGameWidth();
    int getGameHeight();
    void repaintGame();
}