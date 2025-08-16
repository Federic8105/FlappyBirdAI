/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.model;

import java.awt.Graphics2D;
import java.awt.Rectangle;

public interface GameObject {
	void updateHitBox();
	Rectangle getHitBox();
	void updateXY(double dt_ms);
	boolean checkCollision(Rectangle[] vHitBox);
	void setImage();
	void updateIFrames();
	void draw(Graphics2D g2d);
}