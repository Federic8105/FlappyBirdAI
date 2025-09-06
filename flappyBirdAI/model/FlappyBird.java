/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.model;

import flappyBirdAI.ai.BirdBrain;
import java.awt.*;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.Objects;

public class FlappyBird extends AbstractGameObject {
	public static final int WIDTH = 60, HEIGHT = 45;

	public final double gravity = 700, jumpForce = 300;
    public final int tDelayAnimation = 150;
	public final BirdBrain brain;

	public double lifeTime = 0, vy = 0;

	public FlappyBird(int x0, int y0, BirdBrain brain) {
		imgName = "/res/FB";
        nImages = 4;
        x = x0;
		y = y0;
		w = FlappyBird.WIDTH;
		h = FlappyBird.HEIGHT;
		this.brain = brain;

		updateHitBox();

		if (showImage) {
			setImage();
			startAnimation();
		}
	}

	public boolean think() {
		return brain.think();
	}

    private void startAnimation() throws RuntimeException {
		Thread animationThread = new Thread(() -> {
			while (isAlive) {
				try {
					Thread.sleep(tDelayAnimation);
					updateIFrames();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		});
		
		animationThread.start();
    }
	
	@Override
	public void setImage() {
		vFrames = new Image[nImages];
		for (int i = 0; i < vFrames.length; ++i) {
			try {
				vFrames[i] = ImageIO.read(getClass().getResource(imgName + (i+1) + IMG_EXT));
				vFrames[i] = vFrames[i].getScaledInstance(w, h, Image.SCALE_SMOOTH);

				isImageFound = true;
			} catch(IOException e) {
				isImageFound = false;
            	System.err.println("FB Image Not Found");
			}
		}
	}

    @Override
	public void updateIFrames() {
		if (iFrames == vFrames.length - 1) {
			iFrames = 0;
		} else {
			++iFrames;
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
		if (!isImageFound) {
            g2d.setColor(Color.red);
            g2d.draw(hitBox);
        } else {
            g2d.drawImage(vFrames[iFrames], x, y, null);
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
		if (isAlive) {
			StringBuilder sb = new StringBuilder();
			sb.append("FlappyBird --> W: ").append(w)
			  .append(" - H: ").append(h)
			  .append(" - X: ").append(x)
			  .append(" - Y: ").append(y)
			  .append(" - LifeTime: ").append(String.format("%.2f", lifeTime))
			  .append(" - Vy: ").append(String.format("%.2f", vy))
			  .append(" - ").append(brain);
			return sb.toString();
		} else {
			return "FB Not Alive";
		}
	}
	
}