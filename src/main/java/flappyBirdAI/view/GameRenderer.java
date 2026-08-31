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
	
	// --- Metodi Astratti ---
	
	Image[] ensureLoaded(SpriteDescriptor desc);
	void renderSingle(G graphicsContext, GameObject obj);
	
	// --- Metodi di Default ---

	default void preloadSprites(Set<AbstractGameObject> vGameObj) {
		vGameObj.stream()
			.flatMap(obj -> obj.getRenderableComponents().stream())
			.map(GameObject::getSpriteDescriptor)
			.distinct()
			.forEach(this::ensureLoaded);
	}
	
	default void render(G graphicsContext, AbstractGameObject obj) {
		for (GameObject part : obj.getRenderableComponents()) {
			renderSingle(graphicsContext, part);
		}
	}
	
}