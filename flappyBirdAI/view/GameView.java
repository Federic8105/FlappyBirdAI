/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.view;

import flappyBirdAI.controller.GameController;
import flappyBirdAI.controller.GameStats;
import flappyBirdAI.model.AbstractGameObject;
import java.util.List;

public interface GameView {
	void setController(GameController controller);
    void updateDisplay(GameStats stats, List<AbstractGameObject> gameObjects);
    void showAutoSaveMessage(String message);
    int getGameWidth();
    int getGameHeight();
    void repaintGame();
}