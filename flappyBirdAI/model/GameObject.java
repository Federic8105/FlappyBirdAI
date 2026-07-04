/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.model;

import java.awt.Rectangle;
import java.util.List;

public interface GameObject {
	boolean isAlive();
	void setAlive(boolean alive);
	int getX();
	int getY();
	int getW();
	int getH();
	void updateHitBox();
	Rectangle[] getHitBox();
	void updateXY(double dt_s);
	boolean checkCollision(Rectangle[] vHitBox);
	boolean isOutOfScreen(int screenWidth, int screenHeight);
	void updateFrameIndex();
	int getFrameIndex();
	boolean isShowSprite();
	SpriteDescriptor getSpriteDescriptor();
	// Per i compositi
	default List<? extends GameObject> getRenderableComponents() {
		return List.of(this);
	}
}