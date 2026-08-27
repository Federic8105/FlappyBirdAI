/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.model.entities;

import flappyBirdAI.model.AbstractGameObject;
import flappyBirdAI.model.SpriteDescriptor;

public class Tube extends AbstractGameObject {
	
	public static final int NUM_IMAGES = 2;
	public static final String IMG_NAME = "TUBE";

    static final int WIDTH = 50;

    private final double vx = 250;
    private final boolean isSuperior;

    // Visibilità package-private per Accesso Solo da Stesso Package (TubePair)
    Tube(int x0, int y0, int height, boolean isSuperior) {
		this.isSuperior = isSuperior;
        x = x0;
        y = y0;
		w = Tube.WIDTH;
		h = height;

        updateHitBox();
    }
    
    @Override
    public void updateXY(double dt_s) {
        x -= (int) ((int) vx * dt_s);

        updateHitBox();
    }
	
	@Override
	public SpriteDescriptor getSpriteDescriptor() {
	    return isSuperior ? SpriteDescriptor.TUBE_UP : SpriteDescriptor.TUBE_DOWN;
	}
	
	@Override
	public String toString() {
		if (!isAlive()) {
	        return "Tube Not Alive";
	    }
		
		return "Tube --> " + String.join(" - ",
	        "Sup: " + isSuperior,
	        "X: " + x,
	        "Y: " + y,
	        "W: " + w,
	        "H: " + h
	    );
	}
	
}