/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.model;

import flappyBirdAI.ai.BirdBrain;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;

import javax.imageio.ImageIO;

public class FlappyBird extends AbstractGameObject {
	
	//TODO qui?
	private static final int NUM_IMAGES = 4;
	private static final Image[] V_IMAGES = new Image[NUM_IMAGES];
	private static final String IMG_NAME = "/res/FB";
	
	public static final int WIDTH = 60;
	public static final int HEIGHT = 45;
	
	//TODO qui?
	public static void loadImages() {	
		for (int i = 0; i < V_IMAGES.length; ++i) {
			try {
				
				V_IMAGES[i] = ImageIO.read(FlappyBird.class.getResource(IMG_NAME + i + IMG_EXT));
				
				if (V_IMAGES[i] != null) {
					// Ridimensiona l'immagine caricata
					V_IMAGES[i] = V_IMAGES[i].getScaledInstance(WIDTH, HEIGHT, Image.SCALE_SMOOTH);
				}

			} catch(IOException e) {
            	System.err.println("Image Not Found: " + e.getMessage());
			}
		}
		
		IS_IMAGES_FOUND = (V_IMAGES.length == NUM_IMAGES);
	}

    private final int tDelayAnimation = 150;
    private final double gravity = 700, jumpForce = 300;
	private final BirdBrain brain;

	private double lifeTime = 0, vy = 0;

	public FlappyBird(int x0, int y0, BirdBrain brain) throws NullPointerException {
		this.brain = Objects.requireNonNull(brain, "Bird Brain Cannot be Null");
        x = x0;
		y = y0;
		w = FlappyBird.WIDTH;
		h = FlappyBird.HEIGHT;

		updateHitBox();
		
		if (showImage && !IS_IMAGES_FOUND) {
			showImage = false;
		}

		if (showImage) {
			startAnimation();
		}
	}
	
	public double getLifeTime() {
		return lifeTime;
	}
	
	public double getVy() {
		return vy;
	}
	
	public BirdBrain getBrain() {
		return brain;
	}

	public boolean think() {
		return brain.think();
	}

    private void startAnimation() throws RuntimeException {
		Thread animationThread = new Thread(() -> {
			while (isAlive) {
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
	public void updateXY(double dt) {
		vy += gravity * dt;
		y += (int) (vy * dt + 0.5 * gravity * Math.pow(dt, 2));

		updateHitBox();
		lifeTime += dt;
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
            g2d.draw(hitBox); 
        }
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(x, y, vy, lifeTime, isAlive, brain);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		
		FlappyBird other = (FlappyBird) obj;
		return Objects.equals(brain, other.brain)
				&& x == other.x
				&& y == other.y
				&& isAlive == other.isAlive
				&& Double.compare(vy, other.vy) == 0
				&& Double.compare(lifeTime, other.lifeTime) == 0;
	}

	@Override
	public String toString() {
		if (!isAlive) {
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