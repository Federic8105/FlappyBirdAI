/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.view;

import java.util.Set;

import flappyBirdAI.model.AbstractGameObject;

public interface GameRenderer<G> {
	void render(G graphicsContext, AbstractGameObject obj);
	void preloadSprites(Set<AbstractGameObject> vGameObj);
}