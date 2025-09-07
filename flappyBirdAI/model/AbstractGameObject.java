/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.model;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.util.Objects;

public class AbstractGameObject implements GameObject {
	
	public static final String IMG_EXT= ".png";
	public static final String FB_IMG_NAME = "/res/FB";
	public static final String TUBE_IMG_NAME = "/res/TUBE";
	
	public int x, y, w, h, iFrames = 0, nImages;
	public boolean isAlive = true;
	protected boolean showImage = true, isImageFound = false;
	protected Rectangle hitBox;
	protected Image[] vFrames;

	@Override
	public void updateHitBox() {
		hitBox = new Rectangle(x, y, w, h);
	}
	
	@Override
	public Rectangle getHitBox() {
		return hitBox;
	}
	
	@Override
	public void updateXY(double dt_ms) {}
	
	@Override
	public boolean checkCollision(Rectangle[] vHitBox) throws NullPointerException {
		Objects.requireNonNull(vHitBox, "HitBox Array Cannot be Null");
		
		boolean collision = false;
        for (Rectangle box : vHitBox) {
        	Objects.requireNonNull(box, "Individual HitBox Cannot be Null");
            if (hitBox.intersects(box)) {
                collision = true;
                break;
            }
        }

		return collision;
	}
	
	@Override
	public void setImage() {}
	
	@Override
	public void updateIFrames() {}
	
	@Override
	public void draw(Graphics2D g2d) {}
	
	@Override
	public String toString() {
		if (!isAlive) {
	        return "GameObject Not Alive";
	    }
	    
	    return "GameObj --> " + String.join(" - ",
	        "W: " + w,
	        "H: " + h,
	        "X: " + x,
	        "Y: " + y
	    );
	}
}