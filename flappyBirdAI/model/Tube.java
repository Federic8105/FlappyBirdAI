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
	
	private static final int NUM_IMAGES = 2;
	private static final Image[] V_IMAGES = new Image[NUM_IMAGES];
	private static final String IMG_NAME = "/res/TUBE";
	protected static boolean ARE_IMAGES_FOUND = false;
	protected static boolean ARE_IMAGES_LOADED = false;
	
	public static final int DIST_X_BETWEEN_TUBES = 750;
	public static final int DIST_Y_BETWEEN_TUBES = 180;
    public static final int WIDTH = 50;
    
    // Percentuale di quanto si può spostare il buco verso l'alto o verso il basso rispetto al centro dello schermo
    private static final double HOLE_OFFSET_RATIO = 0.3;
    
    private static int lastID = 0;
    
    public static void loadImages() {
    	if (ARE_IMAGES_LOADED) {
    		return;
    	}
    	
    	for (int i = 0; i < V_IMAGES.length; ++i) {
    		try {
    			V_IMAGES[i] = ImageIO.read(Tube.class.getResource(IMG_NAME + i + IMG_EXT));

    		} catch(IOException e) {
            	System.err.println("Image Not Found: " + e.getMessage());
    		}
    	}
    	
    	ARE_IMAGES_FOUND = (V_IMAGES.length == NUM_IMAGES);
    	if (ARE_IMAGES_FOUND) {
			ARE_IMAGES_LOADED = true;
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
    
    private static int calcMaxHoleOffset(int gamePanelHeight) {
    	// Calcolare offset massimo come percentuale dell'altezza
        int maxOffsetByPercentage = (int) (gamePanelHeight * HOLE_OFFSET_RATIO);
        
        // Calcolare l'offset massimo che mantiene il buco dentro i confini dello schermo
    
        // Il buco si estende DIST_Y_BETWEEN_TUBES/2 sopra e sotto il centro
        int halfHoleSize = DIST_Y_BETWEEN_TUBES / 2;
        int screenCenter = gamePanelHeight / 2;
        
        // Offset massimo verso l'alto
        // Il centro può spostarsi fino a quando la parte superiore del buco (centro - halfHoleSize) tocca il bordo superiore ( yGamePanel = 0)
        int maxOffsetUp = screenCenter - halfHoleSize;
        
        // Offset massimo verso il basso
        // Il centro può spostarsi fino a quando la parte inferiore del buco (centro + halfHoleSize) tocca il bordo inferiore (gamePanelHeight)
        int maxOffsetDown = (gamePanelHeight - screenCenter) - halfHoleSize;
        
        // Prendere il minimo tra i due per garantire che il buco non esca dai confini
        int maxOffsetByBounds = Math.min(maxOffsetUp, maxOffsetDown);
        
        // Ritornare il minimo tra l'offset basato sulla percentuale e quello basato sui confini
        return Math.min(maxOffsetByPercentage, maxOffsetByBounds);
    }

    private final int id;
    private final double vx = 250;
    private final boolean isSuperior;
    
    private Image img;

    private Tube(int x0, int y0, int height, boolean isSuperior) {
		this.isSuperior = isSuperior;
        this.id = Tube.lastID;
        ++Tube.lastID;
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
    
    public void updateHeight(int gameHeight) {
		h += gameHeight - (y + h);
		updateHitBox();
		
		if (showImage) {
			// Ridimensiona solo immagine caricata usata dal Tube in base a w e h
			img = V_IMAGES[imageIndex].getScaledInstance(w, h, Image.SCALE_SMOOTH);
		}
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
            g2d.draw(hitBox);
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