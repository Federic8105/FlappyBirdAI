/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.model;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Objects;

public class AbstractGameObject implements GameObject {
	
	public static boolean IS_IMAGES_FOUND = false;
	public static final String IMG_EXT= ".png";
	
	public int x, y, w, h, iFrames = 0;
	public boolean isAlive = true;
	protected boolean showImage = true;
	protected Rectangle hitBox;

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