/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.model;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import javax.imageio.ImageIO;

public class AbstractGameObject implements GameObject {
	
	// Proprietà statiche da definire in sottoclassi concrete:
	/*
		private static final int NUM_IMAGES;
		private static final Image[] V_IMAGES = new Image[NUM_IMAGES];
		private static final String IMG_NAME = "/res/";
		protected static boolean ARE_IMAGES_FOUND = false;
	*/
	protected static final String IMG_EXT= ".png";
	
	protected record ImageLoadResult(Image[] images, boolean allFound) {}
	
	protected static ImageLoadResult loadImageSet(Class<? extends AbstractGameObject> owner, String imgName, int numImages) {
	    Image[] images = new Image[numImages];
	    for (int i = 0; i < numImages; ++i) {
	        try {
	            images[i] = ImageIO.read(owner.getResource(imgName + i + IMG_EXT));
	            // ImageIO.read può restituire null oltre a lanciare eccezioni
	            if (images[i] == null) {
	                System.err.println("Image Not Found: " + imgName + i + IMG_EXT);
	            }
	        } catch (IOException e) {
	            System.err.println("Image Not Found: " + e.getMessage());
	        }
	    }
	    boolean allFound = Arrays.stream(images).allMatch(Objects::nonNull);
	    return new ImageLoadResult(images, allFound);
	}
	
	// Pubblici per Performance in Game Loop
	public int x, y, w, h;
	
	private boolean isAlive = true;
	
	protected int imageIndex = 0;
	protected boolean showImage = true;
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
	public void updateXY(double dt_ms) {}
	
	@Override
	public boolean checkCollision(Rectangle[] vHitBox) throws NullPointerException {
		Objects.requireNonNull(vHitBox, "HitBox Array Cannot be Null");
		
		for (Rectangle ownBox : hitBox) {
			for (Rectangle otherBox : vHitBox) {
				Objects.requireNonNull(otherBox, "Individual HitBox Cannot be Null");
				
				if (ownBox.intersects(otherBox)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	@Override
	public boolean isOutOfScreen(int screenWidth, int screenHeight) {
		return x + w < 0 || x > screenWidth || y + h < 0 || y > screenHeight;
	}
	
	@Override
	public void updateImageIndex() {}
	
	@Override
	public void draw(Graphics2D g2d) {}
	
	@Override
	public String toString() {
		if (!isAlive) {
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