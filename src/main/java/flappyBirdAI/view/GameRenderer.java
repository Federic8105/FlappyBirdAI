/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.view;

import flappyBirdAI.model.AbstractGameObject;
import flappyBirdAI.model.GameObject;
import flappyBirdAI.model.SpriteDescriptor;
import java.util.Set;
import java.util.function.IntFunction;

// Interfaccia generica per il rendering dei GameObject, parametrizzata sul tipo di contesto grafico G e sul tipo di immagine I
public interface GameRenderer<G, I> {
	
	// --- Metodi Astratti ---
	
	I[] ensureLoaded(SpriteDescriptor desc);
	I loadRawImage(String path);
	void renderSingle(G gc, GameObject obj);
	
	// --- Metodi di Default ---
	
	default I[] loadFrames(SpriteDescriptor desc, IntFunction<I[]> imageArrayFactory) {
	    I[] frames = imageArrayFactory.apply(desc.nFrames()); // Non è possibile creare un array generico direttamente, quindi si utilizza una factory fornita dall'implementazione concreta

	    if (desc.nFrames() == 1) {
	        frames[0] = loadRawImage(SpriteDescriptor.IMG_PATH + desc.resourceKey() + SpriteDescriptor.IMG_EXT);
	    } else {
	        for (int i = 0; i < desc.nFrames(); ++i) {
	            frames[i] = loadRawImage(SpriteDescriptor.IMG_PATH + desc.resourceKey() + i + SpriteDescriptor.IMG_EXT);
	        }
	    }

	    return frames;
	}

	default void preloadSprites(Set<AbstractGameObject> vGameObj) {
		vGameObj.stream()
			.flatMap(obj -> obj.getRenderableComponents().stream())
			.map(GameObject::getSpriteDescriptor)
			.distinct()
			.forEach(this::ensureLoaded);
	}
	
	default void render(G gc, AbstractGameObject obj) {
		for (GameObject part : obj.getRenderableComponents()) {
			renderSingle(gc, part);
		}
	}
	
}