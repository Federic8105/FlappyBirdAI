/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.model;

import java.awt.Rectangle;

public abstract class AbstractGameObject implements GameObject {
	
	// Pubblici per Performance in Game Loop
	public int x, y, w, h;
	
	protected boolean isAlive = true;
	
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
	public int getFrameIndex() {
		return frameIndex;
	}

	@Override
	public boolean isShowSprite() {
		return showSprite;
	}
	
	@Override
	public String toString() {
		if (!isAlive()) {
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