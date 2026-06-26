/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.model;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;
import javax.imageio.ImageIO;
import java.util.Arrays;

public class Tube extends AbstractGameObject {
	
	private static final int NUM_IMAGES = 2;
	private static final Image[] V_IMAGES = new Image[NUM_IMAGES];
	private static final String IMG_NAME = "/res/TUBE";
	protected static boolean ARE_IMAGES_FOUND = false;
	protected static boolean ARE_IMAGES_LOADED = false;

    static final int WIDTH = 50;
    
    public static void loadImages() {
    	if (ARE_IMAGES_LOADED) {
    		return;
    	}
    	
    	for (int i = 0; i < V_IMAGES.length; ++i) {
    		try {
    			V_IMAGES[i] = ImageIO.read(Tube.class.getResource(IMG_NAME + i + IMG_EXT));
    			
    			// ImageIO.read può restituire null oltre a lanciare eccezioni
    			if (V_IMAGES[i] == null) {
					System.err.println("Image Not Found: " + IMG_NAME + i + IMG_EXT);
				}
    		} catch(IOException e) {
            	System.err.println("Image Not Found: " + e.getMessage());
    		}
    	}
    	
    	ARE_IMAGES_FOUND = (V_IMAGES.length == NUM_IMAGES && Arrays.stream(V_IMAGES).allMatch(img -> img != null));
    	if (ARE_IMAGES_FOUND) {
			ARE_IMAGES_LOADED = true;
		}
	}

    private final double vx = 250;
    private final boolean isSuperior;
    private final Image img;

    // Visibilità package-private per Accesso Solo da Stesso Package (TubePair)
    Tube(int x0, int y0, int height, boolean isSuperior) {
		this.isSuperior = isSuperior;
        x = x0;
        y = y0;
		w = Tube.WIDTH;
		h = height;

        updateHitBox();
        
        if (showImage && !ARE_IMAGES_FOUND) {
			showImage = false;
		}

        if (showImage) {
        	updateImageIndex();
        	
        	// Ridimensiona solo immagine caricata usata dal Tube in base a w e h
        	img = V_IMAGES[imageIndex].getScaledInstance(w, h, Image.SCALE_SMOOTH);
        } else {
			img = null;
        }
    }
    
    public boolean isTheOppositeOf(Tube otherTube) throws NullPointerException {
    	Objects.requireNonNull(otherTube, "otherTube Cannot be Null");
    	
		// Due tubi sono opposti se hanno la stessa x ma uno è superiore e l'altro inferiore e sono vivi
		if (!isAlive() || !otherTube.isAlive()) {
			return false;
		}
		
		return x == otherTube.x && isSuperior() != otherTube.isSuperior();
    }
    
    public boolean isSuperior() {
		return isSuperior;
	}

    @Override
    public void updateImageIndex() {
        imageIndex = isSuperior ? 0 : 1;
    }
    
    @Override
    public void updateXY(double dt_s) {
        x -= (int) ((int) vx * dt_s);

        updateHitBox();
    }

	@Override
    public void draw(Graphics2D g2d) {
        if (showImage) {
        	g2d.drawImage(img, x, y, null);
        } else {
            g2d.setColor(Color.red);
            g2d.draw(hitBox[0]);
        }
    }
	
	@Override
	public int hashCode() {
		return Objects.hash(x, isSuperior, isAlive());
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		
		Tube other = (Tube) obj;
		return x == other.x
				&& isSuperior == other.isSuperior
				&& isAlive() == other.isAlive();
	}
	
	@Override
	public String toString() {
		if (!isAlive()) {
	        return "Tube Not Alive";
	    }
		
		return "Tube --> " + String.join(" - ",
	        "Sup: " + isSuperior(),
	        "X: " + x,
	        "Y: " + y,
	        "W: " + w,
	        "H: " + h
	    );
	}
	
}