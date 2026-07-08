/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.view;

import flappyBirdAI.model.AbstractGameObject;
import flappyBirdAI.model.GameObject;
import flappyBirdAI.model.SpriteDescriptor;
import java.awt.Image;
import java.util.Set;

public interface GameRenderer<G> {
	
	void renderSingle(G graphicsContext, GameObject obj);
	Image[] ensureLoaded(SpriteDescriptor desc);

	default void render(G graphicsContext, AbstractGameObject obj) {
		for (GameObject part : obj.getRenderableComponents()) {
			renderSingle(graphicsContext, part);
		}
	}

	default void preloadSprites(Set<AbstractGameObject> vGameObj) {
		vGameObj.stream()
			.flatMap(obj -> obj.getRenderableComponents().stream())
			.map(GameObject::getSpriteDescriptor)
			.distinct()
			.forEach(this::ensureLoaded);
	}
	
}