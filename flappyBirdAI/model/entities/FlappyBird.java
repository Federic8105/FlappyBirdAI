/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.model.entities;

import flappyBirdAI.ai.BirdBrain;
import flappyBirdAI.model.AbstractGameObject;
import flappyBirdAI.model.SpriteDescriptor;

import java.util.Objects;

public class FlappyBird extends AbstractGameObject {
	
	public static final int NUM_IMAGES = 4;
	public static final String IMG_NAME = "FB";
	
	public static final int WIDTH = 60;
	public static final int HEIGHT = 45;
	
	public double lifeTime = 0, vy = 0;
	
    private final int tDelayAnimation = 150;
    private final double gravity = 700, jumpForce = 300;
	private final BirdBrain brain;

	public FlappyBird(int x0, int y0, BirdBrain brain) throws NullPointerException {
		this.brain = Objects.requireNonNull(brain, "Bird Brain Cannot be Null");
        x = x0;
		y = y0;
		w = FlappyBird.WIDTH;
		h = FlappyBird.HEIGHT;

		updateHitBox();
		
		if (showSprite) {
			startAnimation();
		}
	}
	
	public BirdBrain getBrain() {
		return brain;
	}

	public boolean think() {
		return brain.think();
	}

    private void startAnimation() throws RuntimeException {
		Thread animationThread = new Thread(() -> {
			while (isAlive()) {
				try {
					Thread.sleep(tDelayAnimation);
					updateFrameIndex();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		});
		
		animationThread.start();
    }

    @Override
	public void updateFrameIndex() {
		if (frameIndex == NUM_IMAGES - 1) {
			frameIndex = 0;
		} else {
			++frameIndex;
		}
	}
	
	@Override
	public void updateXY(double dt_s) {
		vy += gravity * dt_s;
		y += (int) (vy * dt_s + 0.5 * gravity * Math.pow(dt_s, 2));

		updateHitBox();
		lifeTime += dt_s;
	}

	public void jump() {
		vy = -jumpForce;
	}
	
	@Override
	public SpriteDescriptor getSpriteDescriptor() {
		return SpriteDescriptor.BIRD;
	}
	
	@Override
	public int hashCode() {
		// Utilizza l'hashcode di sistema per garantire l'unicità, uguale per istanza, uguale a hashCode di default
		return System.identityHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		// Due istanze sono uguali se sono la stessa istanza, uguale a equals di default
		return this == obj;
	}

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