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
	void setController(GameController controller);
    void updateDisplay(GameClock clock, GameStats stats, Set<AbstractGameObject> gameObjects);
    void showAutoSaveMessage(String message);
    int getGameWidth();
    int getGameHeight();
    void repaintGame();
}