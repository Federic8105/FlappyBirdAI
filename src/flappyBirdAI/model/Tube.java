/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.model;

import java.awt.*;
import javax.imageio.ImageIO;
import java.io.IOException;

public class Tube extends AbstractGameObjec {
	public static final int distXBetweenTubes = 750, distYBetweenTubes = 180;
    public static final int width = 50;
    public static int lastID = 0;

    public final boolean isSuperior;
    private final int id;
    private final double vx = 250;

    public Tube(int x0, int y0, int height, boolean isSuperior) {
		this.isSuperior = isSuperior;
        this.id = Tube.lastID;
        ++Tube.lastID;
        imgName = "/res/TUBE";
        nImages = 2;
        x = x0;
        y = y0;
		w = Tube.width;
		h = height;

        updateHitBox();

        if (showImage) {
           updateIFrames();
           setImage();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Tube otherTube)) {
            return false;
        }
        return this.id == otherTube.id;
    }
    
    @Override
    public void setImage() {
        vFrames = new Image[1];
		try {
            vFrames[0] = ImageIO.read(getClass().getResource(imgName + (iFrames+1) + IMG_EXT));
            vFrames[0] = vFrames[0].getScaledInstance(w, h, Image.SCALE_SMOOTH);

            isImageFound = true;
        } catch(IOException e) {
            isImageFound = false;
            System.err.println("Tube Image Not Found");
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
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Tube --> ID: ").append(id)
		  .append(" - X: ").append(x)
		  .append(" - Y: ").append(y)
		  .append(" - W: ").append(w)
		  .append(" - H: ").append(h);
		return sb.toString();
	}
	

}