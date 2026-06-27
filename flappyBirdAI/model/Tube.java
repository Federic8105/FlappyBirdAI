/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.Objects;

public class Tube extends AbstractGameObject {
	
	private static final int NUM_IMAGES = 2;
	private static final Image[] V_IMAGES = new Image[NUM_IMAGES];
	private static final String IMG_NAME = "/res/TUBE";
	protected static boolean ARE_IMAGES_FOUND = false;

    static final int WIDTH = 50;
    
    public static void loadImages() {
    	if (ARE_IMAGES_FOUND) {
    		return;
    	}
    	
    	ImageLoadResult imgLoadRes = loadImageSet(Tube.class, IMG_NAME, NUM_IMAGES);
        System.arraycopy(imgLoadRes.images(), 0, V_IMAGES, 0, NUM_IMAGES);
        ARE_IMAGES_FOUND = imgLoadRes.allFound();
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
	        "Sup: " + isSuperior,
	        "X: " + x,
	        "Y: " + y,
	        "W: " + w,
	        "H: " + h
	    );
	}
	
}