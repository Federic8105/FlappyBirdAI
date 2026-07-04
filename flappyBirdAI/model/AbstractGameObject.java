/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.model;

import java.awt.Rectangle;
import java.util.Objects;

public abstract class AbstractGameObject implements GameObject {
	
	// Pubblici per Performance in Game Loop
	public int x, y, w, h;
	
	private boolean isAlive = true;
	
	protected int frameIndex = 0;
	protected boolean showSprite = true;
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
	public int getX() {
		return x;
	}
	
	@Override
	public int getY() {
		return y;
	}
	
	@Override
	public int getW() {
		return w;
	}
	
	@Override
	public int getH() {
		return h;
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
	public boolean isOutOfScreen(int screenWidth, int screenHeight) {
		return x + w < 0 || x > screenWidth || y + h < 0 || y > screenHeight;
	}
	
	@Override
	public void updateFrameIndex() {}
	
	@Override
	public int getFrameIndex() {
		return frameIndex;
	}

	@Override
	public boolean isShowSprite() {
		return showSprite;
	}
	
	@Override
	public SpriteDescriptor getSpriteDescriptor() {
		return null;
	}
	
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