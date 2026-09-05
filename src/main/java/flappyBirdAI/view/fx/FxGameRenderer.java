/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.view.fx;

import flappyBirdAI.model.AbstractGameObject;
import flappyBirdAI.model.GameObject;
import flappyBirdAI.model.SpriteDescriptor;
import flappyBirdAI.view.GameRenderer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import java.awt.Rectangle;
import java.awt.Shape;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class FxGameRenderer implements GameRenderer<GraphicsContext, Image> {
	
	// --- Campi per Caching Immagini ---
	
	private final Map<String, Image[]> spriteCache = new HashMap<>();
	
	// --- Caricamento Sprite ---

	@Override
	public Image[] ensureLoaded(SpriteDescriptor desc) {
		return spriteCache.computeIfAbsent(desc.resourceKey(), _ -> loadFrames(desc, Image[]::new));
	}
	
	@Override
	public Image loadRawImage(String path) {
	    URL url = getClass().getResource(path);
	    if (url == null) {
	        System.err.println("Image Not Found: " + path);
	        return null;
	    }
	    
	    Image img = new Image(url.toExternalForm());
	    // Se la creazione dell'immagine fallisce, il costruttore di Image non lancia un'eccezione, ma imposta lo stato di errore interno
	    // Controllare lo stato di errore dell'immagine per gestire eventuali problemi di caricamento
	    if (img.isError()) {
	        System.err.println("Error Reading Image: " + path + " - " + img.getException().getMessage());
	        return null;
	    }
	    return img;
	}
	
	// --- Rendering ---

	@Override
	public void renderSingle(GraphicsContext gc, GameObject obj) {
		SpriteDescriptor desc = obj.getSpriteDescriptor();
        Image[] frames = ensureLoaded(desc);
        Image frame = frames.length > 0 ? frames[obj.getFrameIndex() % frames.length] : null;
        
        if (obj.isShowSprite() && frame != null && obj instanceof AbstractGameObject gameObj) {
        	// Disegna l'immagine scalata alle dimensioni dell'oggetto
        	gc.drawImage(frame, gameObj.x, gameObj.y, gameObj.w, gameObj.h);
        } else {
        	gc.setStroke(Color.RED);
            for (Shape box : obj.getHitBox()) {
            	if (box instanceof Rectangle rect) {
            		gc.strokeRect(rect.x, rect.y, rect.width, rect.height);
            	}
            }
        }
	}
	
}