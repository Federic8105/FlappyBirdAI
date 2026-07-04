/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.view;

import flappyBirdAI.model.AbstractGameObject;
import flappyBirdAI.model.GameObject;
import flappyBirdAI.model.SpriteDescriptor;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public class SwingGameRenderer implements GameRenderer<Graphics2D> {
	
	private final Map<String, Image[]> spriteCache = new HashMap<>();
	
	@Override
	public Image[] ensureLoaded(SpriteDescriptor desc) {
        return spriteCache.computeIfAbsent(desc.resourceKey(), _ -> loadFrames(desc));
    }
	
	@Override
	public void renderSingle(Graphics2D g2d, GameObject obj) {
        SpriteDescriptor desc = obj.getSpriteDescriptor();
        Image[] frames = ensureLoaded(desc);
        Image frame = frames.length > 0 ? frames[obj.getFrameIndex() % frames.length] : null;

        if (obj.isShowSprite() && frame != null && obj instanceof AbstractGameObject gameObj) {
        	// Disegna l'immagine scalata alle dimensioni dell'oggetto
            g2d.drawImage(frame, gameObj.x, gameObj.y, gameObj.w, gameObj.h, null);
        } else {
            g2d.setColor(Color.red);
            for (Shape box : obj.getHitBox()) {
                g2d.draw(box);
            }
        }
    }
	
	private Image[] loadFrames(SpriteDescriptor desc) {
	    Image[] frames = new Image[desc.nFrames()];

	    if (desc.nFrames() == 1) {
	        frames[0] = loadRawImage(SpriteDescriptor.IMG_PATH + desc.resourceKey() + SpriteDescriptor.IMG_EXT);
	    } else {
	        for (int i = 0; i < desc.nFrames(); ++i) {
	            frames[i] = loadRawImage(SpriteDescriptor.IMG_PATH + desc.resourceKey() + i + SpriteDescriptor.IMG_EXT);
	        }
	    }

	    return frames;
	}
	
	private Image loadRawImage(String path) {
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
    
}