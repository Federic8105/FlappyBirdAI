/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.view.swing;

import flappyBirdAI.model.AbstractGameObject;
import flappyBirdAI.model.GameObject;
import flappyBirdAI.model.SpriteDescriptor;
import flappyBirdAI.view.GameRenderer;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public class SwingGameRenderer implements GameRenderer<Graphics2D, Image> {
	
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
	    
	    try {
	        Image img = ImageIO.read(url);
	        // ImageIO.read può restituire null oltre a lanciare eccezioni
	        if (img == null) {
	            System.err.println("Image Not Found (unreadable): " + path);
	        }
	        return img;
	    } catch (IOException e) {
	        System.err.println("Error Reading Image: " + path + " - " + e.getMessage());
	        return null;
	    }
	}
	
	// --- Rendering ---
	
	@Override
	public void renderSingle(Graphics2D gc, GameObject obj) {
        SpriteDescriptor desc = obj.getSpriteDescriptor();
        Image[] frames = ensureLoaded(desc);
        Image frame = frames.length > 0 ? frames[obj.getFrameIndex() % frames.length] : null;

        if (obj.isShowSprite() && frame != null && obj instanceof AbstractGameObject gameObj) {
        	// Disegna l'immagine scalata alle dimensioni dell'oggetto
        	gc.drawImage(frame, gameObj.x, gameObj.y, gameObj.w, gameObj.h, null);
        } else {
        	gc.setColor(Color.RED);
            for (Shape box : obj.getHitBox()) {
            	gc.draw(box);
            }
        }
    }
    
}