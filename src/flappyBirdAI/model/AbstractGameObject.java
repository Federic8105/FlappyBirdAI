/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.model;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;

public class AbstractGameObject implements GameObject {
	public static final String IMG_EXT= ".png";
	public int x, y, w, h, iFrames = 0, nImages;
	public boolean isAlive = true;
	protected boolean showImage = true, isImageFound = false;
	protected Rectangle hitBox;
	protected String imgName;
	protected Image[] vFrames;

	@Override
	public void updateHitBox() {
		hitBox = new Rectangle(x,y,w,h);
	}
	@Override
	public Rectangle getHitBox() {
		return hitBox;
	}
	@Override
	public void updateXY(double dt_ms) {}
	@Override
	public boolean checkCollision(Rectangle[] vHitBox) {
		boolean collision = false;

        for (Rectangle box : vHitBox) {
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
		if (isAlive) {
			StringBuilder sb = new StringBuilder();
			sb.append("GameObj --> W: ").append(w)
			  .append(" - H: ").append(h)
			  .append(" - X: ").append(x)
			  .append(" - Y: ").append(y);
			return sb.toString();
		} else {
			return "GameObject Not Alive";
		}
	}
}