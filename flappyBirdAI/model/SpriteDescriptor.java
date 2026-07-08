/**
 * @author Federico Sabbatani
 */

package flappyBirdAI.model;

import flappyBirdAI.model.entities.FlappyBird;
import flappyBirdAI.model.entities.Tube;

public record SpriteDescriptor(String resourceKey, int nFrames) {
	public static final String IMG_PATH= "/res/";
	public static final String IMG_EXT= ".png";
	
    public static final SpriteDescriptor BIRD = new SpriteDescriptor(FlappyBird.IMG_NAME, FlappyBird.NUM_IMAGES);
    public static final SpriteDescriptor TUBE_UP = new SpriteDescriptor(Tube.IMG_NAME + "_UP", 1);
    public static final SpriteDescriptor TUBE_DOWN = new SpriteDescriptor(Tube.IMG_NAME + "_DOWN", 1);
}