/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.model;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Objects;

public class AbstractGameObject implements GameObject {
	
	protected static final String IMG_EXT= ".png";
	protected static boolean IS_IMAGES_FOUND = false;
	
	public int x, y, w, h;
	
	protected int imageIndex = 0;
	protected boolean isAlive = true, showImage = true;
	protected Rectangle hitBox;
	
	public boolean isAlive() {
		return isAlive;
	}
	
	public void setAlive(boolean isAlive) {
		this.isAlive = isAlive;
	}

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
	public void updateImageIndex() {}
	
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