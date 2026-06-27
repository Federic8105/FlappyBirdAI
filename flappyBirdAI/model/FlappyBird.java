/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.model;

import flappyBirdAI.ai.BirdBrain;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.Arrays;
import java.util.Objects;

public class FlappyBird extends AbstractGameObject {
	
	private static final int NUM_IMAGES = 4;
	private static final Image[] V_IMAGES = new Image[NUM_IMAGES];
	private static final String IMG_NAME = "/res/FB";
	protected static boolean ARE_IMAGES_FOUND = false;
	
	public static final int WIDTH = 60;
	public static final int HEIGHT = 45;
	
	public static void loadImages() {
		if (ARE_IMAGES_FOUND) {
			return;
		}
		
		ImageLoadResult imgLoadRes = loadImageSet(FlappyBird.class, IMG_NAME, NUM_IMAGES);
		// Ridimensiona tutte le immagini caricate (!= null) in base a WIDTH e HEIGHT e raccoglie i risultati in un nuovo array di tipo Image[]
		Image[] scaledImages = Arrays.stream(imgLoadRes.images()).map(img -> img != null ? img.getScaledInstance(WIDTH, HEIGHT, Image.SCALE_SMOOTH) : null).toArray(Image[]::new);
	    System.arraycopy(scaledImages, 0, V_IMAGES, 0, NUM_IMAGES);
	    ARE_IMAGES_FOUND = imgLoadRes.allFound();
	}

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
		
		if (showImage && !ARE_IMAGES_FOUND) {
			showImage = false;
		}

		if (showImage) {
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
					updateImageIndex();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		});
		
		animationThread.start();
    }

    @Override
	public void updateImageIndex() {
		if (imageIndex == V_IMAGES.length - 1) {
			imageIndex = 0;
		} else {
			++imageIndex;
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
	public void draw(Graphics2D g2d) {
		if (showImage) {
            g2d.drawImage(V_IMAGES[imageIndex], x, y, null);
        } else {
        	g2d.setColor(Color.red);
            g2d.draw(hitBox[0]); 
        }
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