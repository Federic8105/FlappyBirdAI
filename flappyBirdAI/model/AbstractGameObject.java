/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.model;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Objects;

public class AbstractGameObject implements GameObject {
	
	protected static final String IMG_EXT= ".png";
	
	// Pubblici per Performance in Game Loop
	public int x, y, w, h;
	
	private boolean isAlive = true;
	
	protected int imageIndex = 0;
	protected boolean showImage = true;
	protected Rectangle[] hitBox;
	
	@Override
	public boolean isAlive() {
		return isAlive;
	}
	
	@Override
	public void setAlive(boolean alive) {
		this.isAlive = alive;
	}

	@Override
	public void updateHitBox() {
		if (hitBox == null) {
			hitBox = new Rectangle[] { new Rectangle(x, y, w, h) };
		} else {
			hitBox[0].setBounds(x, y, w, h);
		}
	}
	
	@Override
	public Rectangle[] getHitBox() {
		return hitBox;
	}
	
	@Override
	public void updateXY(double dt_ms) {}
	
	@Override
	public boolean checkCollision(Rectangle[] vHitBox) throws NullPointerException {
		Objects.requireNonNull(vHitBox, "HitBox Array Cannot be Null");
		
		for (Rectangle ownBox : hitBox) {
			for (Rectangle otherBox : vHitBox) {
				Objects.requireNonNull(otherBox, "Individual HitBox Cannot be Null");
				
				if (ownBox.intersects(otherBox)) {
					return true;
				}
			}
		}
		
		return false;
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