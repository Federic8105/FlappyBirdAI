/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.model;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;
import java.util.Random;

import javax.imageio.ImageIO;

import java.util.List;
import java.util.ArrayList;

public class Tube extends AbstractGameObject {
	
	public static final int NUM_IMAGES = 2;
	public static Image[] V_IMAGES = new Image[NUM_IMAGES];
	public static String IMG_NAME = "/res/TUBE";
	
	public static final int DIST_X_BETWEEN_TUBES = 750;
	public static final int DIST_Y_BETWEEN_TUBES = 180;
    public static final int WIDTH = 50;
    // Percentuale di quanto si può spostare il buco verso l'alto o verso il basso rispetto al centro dello schermo
    public static final double HOLE_OFFSET_RATIO = 0.3;
    
    public static int lastID = 0;
    
    public static void loadImages() {
    	for (int i = 0; i < V_IMAGES.length; ++i) {
    		try {
    			V_IMAGES[i] = ImageIO.read(Tube.class.getResource(IMG_NAME + (i + 1) + IMG_EXT));

    		} catch(IOException e) {
            	System.err.println("Image Not Found: " + e.getMessage());
    		}
    	}
    	
    	if (V_IMAGES.length == NUM_IMAGES) {
			FlappyBird.IS_IMAGES_FOUND = true;
		} else {
			FlappyBird.IS_IMAGES_FOUND = false;
		}
	}
    
    public static List<Tube> createTubePair(int gameWidth, int gameHeight, Random random) {
        int maxHoleOffset = calcMaxHoleOffset(gameHeight);
        int tubeHoleOffset = random.nextInt(-maxHoleOffset, maxHoleOffset + 1);
        
        int yTubeHoleCenter = (gameHeight / 2) + tubeHoleOffset;
        int upperTubeHeight = yTubeHoleCenter - DIST_Y_BETWEEN_TUBES / 2;
        
        List<Tube> tubePair = new ArrayList<>(2);
        
        // Tubo superiore
        tubePair.add(new Tube(gameWidth, 0, upperTubeHeight, true));
        
        // Tubo inferiore
        tubePair.add(new Tube(gameWidth, upperTubeHeight + DIST_Y_BETWEEN_TUBES, gameHeight - upperTubeHeight - DIST_Y_BETWEEN_TUBES, false));
        
        return tubePair;
    }
    
    //TODO controllo no buco fuori schermo e parametro dist minimo tra buco e bordo schermo
    private static int calcMaxHoleOffset(int gamePanelHeight) {
    	// Calcolare offset massimo come percentuale dell'altezza
        int maxOffset = (int) (gamePanelHeight * HOLE_OFFSET_RATIO);
        
        return maxOffset;
        
        //int safeZone = DIST_Y_BETWEEN_TUBES / 2 + 50;
        //int maxSafeOffset = (gamePanelHeight / 2) - safeZone;
        
        //return Math.min(maxOffset, maxSafeOffset);
    }

    public final boolean isSuperior;
    
    private final int id;
    private final double vx = 250;

    private Tube(int x0, int y0, int height, boolean isSuperior) {
		this.isSuperior = isSuperior;
        this.id = Tube.lastID;
        ++Tube.lastID;
        x = x0;
        y = y0;
		w = Tube.WIDTH;
		h = height;

        updateHitBox();

        if (showImage) {
        	updateIFrames();
        	
        	// Ridimensiona solo immagine caricata usata dal Tube in base a w e h
        	V_IMAGES[iFrames] = V_IMAGES[iFrames].getScaledInstance(w, h, Image.SCALE_SMOOTH);
        }
    }

    @Override
    public void updateIFrames() {
        if (isSuperior) {
            iFrames = 0;
        } else {
            iFrames = 1;
        }
    }
    
    @Override
    public void updateXY(double dt) {
        x -= (int) ((int) vx * dt);

        updateHitBox();
    }

	@Override
    public void draw(Graphics2D g2d) {
        if (!IS_IMAGES_FOUND) {
            g2d.setColor(Color.red);
            g2d.draw(hitBox);
        } else {
            g2d.drawImage(V_IMAGES[iFrames], x, y, null);
        }
    }
	
	 @Override
	public int hashCode() {
		return Objects.hash(id);
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
		return id == other.id;
	}
	
	@Override
	public String toString() {
		if (!isAlive) {
	        return "Tube Not Alive";
	    }
		
		return "Tube --> " + String.join(" - ",
	        "ID: " + id,
	        "X: " + x,
	        "Y: " + y,
	        "W: " + w,
	        "H: " + h
	    );
	}
	

}