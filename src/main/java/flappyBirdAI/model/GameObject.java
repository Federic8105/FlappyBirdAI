/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.model;

import java.awt.Rectangle;
import java.util.List;
import java.util.Objects;

public interface GameObject {
	
	// --- Metodi Astratti per Getters/Setters e Query di Stato e Gestione HitBox ---
	
	boolean isAlive();
	void setAlive(boolean alive);
	int getX();
	int getY();
	int getW();
	int getH();
	Rectangle[] getHitBox();
	int getFrameIndex();
	boolean isShowSprite();
	void updateHitBox();
	
	// --- Metodi di Default per Movimento e Animazioni ---
	
	default void updateXY(double dt_s) {}
	default SpriteDescriptor getSpriteDescriptor() { return null; }
	default boolean isAnimated() { return false; }
	default void updateFrameIndex() {}
	
	// --- Metodi di Default per Utility Geometriche e Rendering ---
	
	default boolean isOutOfScreen(int screenWidth, int screenHeight) {
		return getX() + getW() < 0 || getX() > screenWidth || getY() + getH() < 0 || getY() > screenHeight;
	}
	
	default boolean checkCollision(Rectangle[] vHitBox) throws NullPointerException {
		Objects.requireNonNull(vHitBox, "HitBox Array Cannot be Null");

		for (Rectangle ownBox : getHitBox()) {
			for (Rectangle otherBox : vHitBox) {
				Objects.requireNonNull(otherBox, "Individual HitBox Cannot be Null");
				if (ownBox.intersects(otherBox)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	// Per i GameObject composti da diversi componenti
	default List<? extends GameObject> getRenderableComponents() {
		return List.of(this);
	}
	
}