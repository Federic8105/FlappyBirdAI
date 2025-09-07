/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.model;

import java.awt.*;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.Objects;

public class Tube extends AbstractGameObject {
	
	public static final int DIST_X_BETWEEN_TUBES = 750, DIST_Y_BETWEEN_TUBES = 180;
    public static final int WIDTH = 50;
    
    public static int lastID = 0;

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