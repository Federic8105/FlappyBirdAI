/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.model.entities;

import flappyBirdAI.ai.BirdBrain;
import flappyBirdAI.model.AbstractGameObject;
import flappyBirdAI.model.SpriteDescriptor;
import java.util.Objects;

public class FlappyBird extends AbstractGameObject {
	
	// --- Costanti di Configurazione per Animazioni ---
	
	public static final int NUM_IMAGES = 4;
	public static final String IMG_NAME = "FB";
	
	// --- Costanti di Configurazione per Fisica ---
	
	private static final double GRAVITY = 700, JUMP_FORCE = 300;
	
	// --- Costanti di Configurazione per Dimensioni ---
	
	public static final int WIDTH = 60, HEIGHT = 45;
	
	// --- Campi di Stato ---
	
	// Pubblici per Performance in Game Loop
	public double lifeTime = 0, vy = 0;
    
	private final BirdBrain brain;
	
	// -- Costruttori ---

	public FlappyBird(int x0, int y0, BirdBrain brain) throws NullPointerException {
		this.brain = Objects.requireNonNull(brain, "Bird Brain Cannot be Null");
        x = x0;
		y = y0;
		w = FlappyBird.WIDTH;
		h = FlappyBird.HEIGHT;

		updateHitBox();
	}
	
	// --- Cervello e Decisione AI ---

	public boolean think() {
		return brain.think();
	}
	
	public BirdBrain getBrain() {
		return brain;
	}
	
	// --- Movimento e Animazioni ---
	
	public void jump() {
		vy = -JUMP_FORCE;
	}
	
	@Override
	public void updateXY(double dt_s) {
		vy += GRAVITY * dt_s;
		y += (int) (vy * dt_s + 0.5 * GRAVITY * Math.pow(dt_s, 2));

		updateHitBox();
		lifeTime += dt_s;
	}
	
	@Override
	public SpriteDescriptor getSpriteDescriptor() {
		return SpriteDescriptor.BIRD;
	}
	
	@Override
	public boolean isAnimated() {
		return true;
	}
	
	@Override
	public void updateFrameIndex() {
		if (frameIndex == NUM_IMAGES - 1) {
			frameIndex = 0;
		} else {
			++frameIndex;
		}
	}
	
	// --- Object Methods Override ---

	@Override
	public String toString() {
		if (!isAlive()) {
	        return "FlappyBird Not Alive";
	    }
	    
	    return "FlappyBird --> " + String.join(" - ",
	        "W: " + w,
	        "H: " + h,
	        "X: " + x,
	        "Y: " + y,
	        "LifeTime: " + String.format("%.2f", lifeTime),
	        "Vy: " + String.format("%.2f", vy),
	        brain.toString()
	    );
	}
	
}