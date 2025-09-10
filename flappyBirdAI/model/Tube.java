/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.model;

import java.awt.*;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.Objects;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;

public class Tube extends AbstractGameObject {
	
	public static final int DIST_X_BETWEEN_TUBES = 750;
	public static final int DIST_Y_BETWEEN_TUBES = 180;
    public static final int WIDTH = 50;
    // Percentuale di quanto si può spostare il buco verso l'alto o verso il basso rispetto al centro dello schermo
    public static final double HOLE_OFFSET_RATIO = 0.3;
    
    public static int lastID = 0;
    
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
    
    //TODO
    private static int calcMaxHoleOffset(int gamePanelHeight) {
        int maxOffset = (int) (gamePanelHeight * HOLE_OFFSET_RATIO);
        
        int safeZone = DIST_Y_BETWEEN_TUBES / 2 + 50;
        int maxSafeOffset = (gamePanelHeight / 2) - safeZone;
        
        return Math.min(maxOffset, maxSafeOffset);
    }

    public final boolean isSuperior;
    
    private final int id;
    private final double vx = 250;

    public Tube(int x0, int y0, int height, boolean isSuperior) {
		this.isSuperior = isSuperior;
        this.id = Tube.lastID;
        ++Tube.lastID;
        nImages = 2;
        x = x0;
        y = y0;
		w = Tube.WIDTH;
		h = height;

        updateHitBox();

        if (showImage) {
           updateIFrames();
           setImage();
        }
    }
    
    @Override
    public void setImage() {
        vFrames = new Image[1];
		try {
            vFrames[0] = ImageIO.read(getClass().getResource(AbstractGameObject.TUBE_IMG_NAME + (iFrames+1) + IMG_EXT));
            vFrames[0] = vFrames[0].getScaledInstance(w, h, Image.SCALE_SMOOTH);

            isImageFound = true;
        } catch(IOException e) {
            isImageFound = false;
            System.err.println("Tube Image Not Found for Tube ID: " + id);
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
        if (!isImageFound) {
            g2d.setColor(Color.red);
            g2d.draw(hitBox);
        } else {
            g2d.drawImage(vFrames[0], x, y, null);
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